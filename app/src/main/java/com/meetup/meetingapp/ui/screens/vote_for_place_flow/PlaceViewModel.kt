package com.meetup.meetingapp.ui.screens.vote_for_place_flow

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.meetup.meetingapp.data.model.DateTime
import com.meetup.meetingapp.data.model.Event
import com.meetup.meetingapp.data.model.TimeSlot
import com.meetup.meetingapp.data.model.Restaurant
import com.meetup.meetingapp.data.repositories.EventRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.format.TextStyle
import java.util.Locale
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import android.annotation.SuppressLint
import android.location.Location
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.meetup.meetingapp.data.model.EventStatus
import kotlinx.coroutines.tasks.await
import com.meetup.meetingapp.utils.isRestaurantOpenForTiming


/**
 * ViewModel responsible for managing restaurant details, voting state,
 * and Google Places–related data for the Place Details screen.
 *
 * This ViewModel exposes:
 * - A flow of restaurant details resolved from the global restaurant list
 * - The user's current vote state for the selected restaurant
 * - The result of vote submission attempts (success or error)
 * - Utility functions for formatting opening hours, price levels, and photo URLs
 *
 * Responsibilities:
 * - Fetch and observe the user's vote for a given place
 * - Submit votes to the backend via [EventRepository]
 * - Build Google Places Photo API URLs using the provided API key
 * - Provide derived UI state such as open/closed labels and formatted price levels
 *
 * @param eventRepository Repository used for reading and writing event‑related data,
 *                        including user votes.
 * @param apiKey Google Places API key used for building photo URLs.
 * @param savedStateHandle State handle for restoring and persisting UI‑related state.
 */
class PlaceViewModel(
    private val eventRepository: EventRepository,
    private val apiKey: String,
    private val fusedLocationClient: FusedLocationProviderClient,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    /** Currently authenticated user's UID (empty if not logged in). */
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    /** Private property that holds users current distance from the restaurant */
    private val _restaurantDistance = MutableStateFlow<String?>(null)

    /** Publicly observable read-only version of the calculated distance  as a String */
    val restaurantDistance: StateFlow<String?> = _restaurantDistance.asStateFlow()

    /** Event ID passed from navigation arguments. */
    private val eventId: String =
        savedStateHandle[ChooseDateAndAreaDestination.eventIdArg] ?: ""

    /** The event data observed from Firestore. */
    private val _event = MutableStateFlow<Event?>(null)
    val event: StateFlow<Event?> = _event.asStateFlow()

    /** Whether the user has voted for the selected restaurant. */
    private val _voteState = MutableStateFlow(VoteState())
    val voteState = _voteState.asStateFlow()

    /** Result of a vote submission (success or error message). */
    private val _voteResultState = MutableStateFlow<VoteResultState?>(null)
    val voteResultState = _voteResultState.asStateFlow()

    /** Available date × location combinations for filtering */
    private val _dateAndAreaState = MutableStateFlow(DateAndAreaState())
    val dateAndAreaState = _dateAndAreaState.asStateFlow()

    /** Loading state for restaurant candidates */
    private val _restaurantState = MutableStateFlow<RestaurantState>(RestaurantState.Loading)
    val restaurantState = _restaurantState.asStateFlow()

    /** User-selected timing filter */
    val selectedTiming = MutableStateFlow<DateTime?>(null)

    /** User-selected location filter */
    val selectedLocation = MutableStateFlow<String?>(null)

    /** All restaurants loaded from Room */
    private val _allRestaurants = MutableStateFlow(AllRestaurantState())
    val allRestaurants = _allRestaurants.asStateFlow()

    /** Whether all restaurants have been loaded from Room. */
    private var restaurantsLoaded = false

    /** UI state for the Place Details screen. */
    private val _uiState = MutableStateFlow(PlaceUiState())
    val uiState: StateFlow<PlaceUiState> = _uiState.asStateFlow()

    /**
     * Initialization:
     * - Sync event from Firestore → Room
     * - Observe event changes
     * - Load restaurant candidates (from Room or Places API)
     */
    init {
        viewModelScope.launch {
            if (eventId.isEmpty()) {
                Log.e("PlaceViewModel", "Event ID is missing!")
                _restaurantState.value = RestaurantState.Error(Exception("Event ID is missing"))
                return@launch
            }

            // Observe event from Firestore and update Room cache
            eventRepository.observeEventById(eventId).collect { event ->
                _event.value = event
                if (event == null) {
                    _restaurantState.value = RestaurantState.Loading
                    return@collect
                }
                buildDateLocationOptions(event.dateTimeCandidates, event.locationCandidates)

                // HasCandidates block
                if (!restaurantsLoaded && eventRepository.hasRestaurantCandidates(event.id)) {
                    restaurantsLoaded = true
                    getAllRestaurant(event.id)
                    _restaurantState.value = RestaurantState.Available

                    // Update event status to COLLECTING_RESTAURANT_VOTES
                    viewModelScope.launch {
                        eventRepository.updateEventStatus(
                            event.id,
                            EventStatus.COLLECTING_RESTAURANT_VOTES
                        )
                    }

                } else if (!restaurantsLoaded) {
                    _restaurantState.value = RestaurantState.Empty
                }
            }
        }

        // Separate launch — runs concurrently, not blocked by the collect above
        viewModelScope.launch {
            eventRepository.observeSubmissions(eventId).collect { submissions ->
                _uiState.update { it.copy(submissionsCount = submissions.size) }
            }
        }
    }

    /**
     * Orchestrates the loading of place-specific data.
     * @param placeId The unique identifier for the restaurant from Google Places.
     * @param lat The latitude of the restaurant; used for distance calculation.
     * @param lng The longitude of the restaurant; used for distance calculation.
     */
    fun loadPlaceData(placeId: String, lat: Double?, lng: Double?) {
        fetchUserVote(placeId)
        updateDistanceToRestaurant(lat, lng)
    }

    /**
     * Performs a one-time GPS request and calculates distance between the user
     * and the restaurant coordinates.
     */
    @SuppressLint("MissingPermission")
    fun updateDistanceToRestaurant(destLat: Double?, destLng: Double?) {
        if (destLat == null || destLng == null) {
            _restaurantDistance.value = "Unknown distance"
            return
        }

        // 2. Launch in a background thread so the UI doesn't freeze while waiting for GPS
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // 3. Request current location from the hardware
                val location: Location? = fusedLocationClient.getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    CancellationTokenSource().token
                ).await()

                location?.let { userLoc ->
                    val results = FloatArray(1)
                    // 4. Calculate the distance in meters
                    Location.distanceBetween(
                        userLoc.latitude, userLoc.longitude,
                        destLat, destLng,
                        results
                    )

                    val distanceInMeters = results[0]

                    // 5. Format the result into a human-readable string
                    _restaurantDistance.value = if (distanceInMeters < 1000) {
                        "${distanceInMeters.toInt()} m"
                    } else {
                        "%.1f km".format(distanceInMeters / 1000)
                    }
                } ?: run {
                    _restaurantDistance.value = "GPS unavailable"
                }
            } catch (e: Exception) {
                Log.e("PlaceViewModel", "Distance calculation failed", e)
                _restaurantDistance.value = "Error getting distance"
            }
        }
    }

    /**
     * Loads all restaurant candidates for the event from Room.
     * Ensures Firestore → Room sync before collecting.
     */
    private fun getAllRestaurant(eventId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            eventRepository.syncRestaurants(eventId)
            eventRepository.getRestaurants(eventId)
                .collect { restaurants ->
                    _allRestaurants.update { it.copy(allRestaurants = restaurants) }
                    _restaurantState.value = if (restaurants.isNotEmpty()) {
                        RestaurantState.Available
                    } else {
                        RestaurantState.Empty
                    }
                }
        }
    }

    /**
     * Filtered list of restaurants based on:
     * - Selected location
     * - Selected timing (opening hours overlap)
     */
    val filteredRestaurants: StateFlow<List<Restaurant>> =
        combine(
            allRestaurants,
            selectedTiming,
            selectedLocation
        ) { allState, timing, location ->
            val all = allState.allRestaurants
            all.filter { restaurant ->
                val query = location?.trim() ?: ""
                val locationMatch = query.isEmpty() ||
                        (restaurant.address?.contains(query, ignoreCase = true) == true) ||
                        (restaurant.name.contains(query, ignoreCase = true))

                val timingMatch = timing == null || isRestaurantOpenForTiming(restaurant, timing)
                locationMatch && timingMatch
            }
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )



    /**
     * Updates user-selected filters.
     */
    fun setFilter(timing: DateTime, location: String) {
        selectedTiming.value = timing
        selectedLocation.value = location
    }

    /**
     * Builds all combinations of date/time and location options.
     */
    fun buildDateLocationOptions(dateTimes: List<DateTime>, locations: List<String>) {
        val options = dateTimes.flatMap { dt ->
            locations.map { loc -> DateLocationOption(timing = dt, location = loc) }
        }
        _dateAndAreaState.value = DateAndAreaState(dateLocationOptions = options)

        // Auto-select first option so the list isn't empty before the user picks
        if (selectedTiming.value == null && options.isNotEmpty()) {
            selectedTiming.value = options.first().timing
            selectedLocation.value = options.first().location
        }
    }

    /**
     * Submits the user's vote for the specified restaurant and time slot.
     *
     * @return Result.success on success, or Result. Failure on Firestore error.
     */
    fun submitVote(placeId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            selectedTiming.value?.let { timing ->
                eventRepository.submitVote(eventId, placeId, userId, timing)
                    .onSuccess {
                        _voteState.update { it.copy(isVoted = true) }
                        _voteResultState.value = VoteResultState.VoteSuccess
                    }.onFailure { e ->
                        _voteResultState.value = VoteResultState.VoteError("Vote failed")
                    }
            }
        }
    }

    /**
     * Returns a flow that emits the restaurant matching the given Place ID.
     *
     * This simply maps the current list of all restaurants and finds the one
     * whose `placeId` matches the provided value. Emits `null` if not found.
     *
     * @param placeId Google Places ID of the restaurant to retrieve.
     * @return A [Flow] emitting the matching [Restaurant] or null.
     */
    fun fetchRestaurantDetail(placeId: String): Flow<Restaurant?> {
        return allRestaurants.map { state -> state.allRestaurants.find { it.placeId == placeId } }
    }


    /**
     * Converts a Google Places price level (0–4) into a Euro‑sign representation.
     *
     * Example:
     * - 0 → "€"
     * - 1 → "€€"
     * - 2 → "€€€"
     *
     * @param level The price level integer from Google Places.
     * @return A repeated Euro‑sign string, or empty string if invalid.
     */
    fun formatPriceLevel(level: Int?): String =
        if (level == null || level < 0) "" else "€".repeat(level + 1)

    fun buildPhotoUrl(photoReference: String?): String? {
        if (photoReference.isNullOrEmpty()) return null
        return "https://maps.googleapis.com/maps/api/place/photo?maxwidth=800&photo_reference=$photoReference&key=$apiKey"
    }

    /**
     * Loads whether the user has already voted for this restaurant at the selected time.
     *
     * Updates voteState.isVoted based on Firestore result.
     */
    fun fetchUserVote(placeId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            selectedTiming.value?.let { timing ->
                eventRepository.getUserVote(eventId, placeId, userId, timing)
                    .onSuccess { exists -> _voteState.update { it.copy(isVoted = exists) } }
            }
        }
    }
}

/**
 * UI state for all restaurants.
 * @property allRestaurants List of all restaurants.
 */
data class AllRestaurantState(val allRestaurants: List<Restaurant> = listOf())

/**
 * Converts a DateTime to a human-readable string.
 * @return Formatted string representing the date and time.
 * @see toDisplayLabel for a more specific label.
 */

fun DateTime.toDisplayLabel(): String {
    val localDate = this.toLocalDate()
    return "${
        localDate.month.getDisplayName(
            TextStyle.SHORT,
            Locale.ENGLISH
        )
    } ${localDate.dayOfMonth} (${timeSlot.start}–${timeSlot.end})"
}

/**
 * Represents a date and location combination.
 *
 * @property timing The date and time for the event.
 * @property location The location for the event.
 * @property label A human-readable label for the combination.
 * @property timingArg A string representation of the timing for navigation.
 * @see toDisplayLabel for a more specific label.
 */
data class DateLocationOption(val timing: DateTime, val location: String) {
    val label: String get() = "${timing.toDisplayLabel()} — $location"
    val timingArg: String get() = timing.toSerializableString()
}

/**
 * UI state for the Date & Area selection screen.
 *
 * @property dateLocationOptions List of available date and location combinations.
 * @see DateAndAreaPageDestination for navigation destination.
 */
data class DateAndAreaState(val dateLocationOptions: List<DateLocationOption> = listOf())

/**
 * Converts a DateTime to a string for storage in Firestore.
 * @return String representation of the DateTime.
 */
fun DateTime.toSerializableString(): String = "$date|${timeSlot.start}-${timeSlot.end}"

/**
 * Converts a string representation of a DateTime to a DateTime object.
 * @return DateTime object parsed from the string.
 *
 * @see toSerializableString for the string format.
 */
fun String.toDateTime(): DateTime {
    val parts = split("|")
    val times = parts[1].split("-")
    return DateTime(date = parts[0], timeSlot = TimeSlot(times[0], times[1]))
}

/**
 * Represents the state of restaurant candidates.
 *
 * @property Loading Indicates that loading is in progress.
 * @property Available Indicates that restaurant candidates are available.
 * @property Empty Indicates that no restaurant candidates are available.
 * @property Error Indicates that an error occurred while loading restaurant candidates.
 */
sealed interface RestaurantState {
    object Loading : RestaurantState
    object Available : RestaurantState
    object Empty : RestaurantState
    data class Error(val error: Throwable) : RestaurantState
}

/**
 * Holds the user's current voting status for the selected restaurant.
 *
 * @property isVoted True if the user has already voted.
 */
data class VoteState(val isVoted: Boolean = false)
sealed class VoteResultState {
    object VoteSuccess : VoteResultState()
    data class VoteError(val message: String) : VoteResultState()
}

/**
 * UI state for the Place Details screen.
 *
 * @property submissionsCount The number of submissions for this event.
 */
data class PlaceUiState(
    val submissionsCount: Int = 0
)