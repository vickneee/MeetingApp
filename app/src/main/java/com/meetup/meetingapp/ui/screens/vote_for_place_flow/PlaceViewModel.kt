package com.meetup.meetingapp.ui.screens.vote_for_place_flow

import android.annotation.SuppressLint
import android.location.Location
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.firebase.auth.FirebaseAuth
import com.meetup.meetingapp.data.model.DateTime
import com.meetup.meetingapp.data.model.Event
import com.meetup.meetingapp.data.model.EventStatus
import com.meetup.meetingapp.data.model.Restaurant
import com.meetup.meetingapp.data.repositories.EventRepository
import com.meetup.meetingapp.utils.calculateDistanceMeters
import com.meetup.meetingapp.utils.filterRestaurants
import com.meetup.meetingapp.utils.formatDistance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

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
    val apiKey: String,
    private val fusedLocationClient: FusedLocationProviderClient,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    /** Currently authenticated user's UID (empty if not logged in). */
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    /** Private property that holds users current distance from the restaurant */
    private val _restaurantDistance = MutableStateFlow<String?>(null)

    /** Publicly observable read-only version of the calculated distance  as a String */
    val restaurantDistance: StateFlow<String?> = _restaurantDistance.asStateFlow()

    /** Event ID passed from navigation arguments. */
    private val eventId: String =
        savedStateHandle[PlaceDetailsDestination.eventIdArg]
            ?: savedStateHandle[DateAndAreaPageDestination.EVENTIDARG] ?: ""

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
    private var isInitialFetchComplete = false

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

                // HasCandidates block
                if (!restaurantsLoaded && eventRepository.hasRestaurantCandidates(event.id)) {
                    restaurantsLoaded = true
                    getAllRestaurant(event.id)

                    // Update event status to COLLECTING_RESTAURANT_VOTES ONLY if it's currently generated
                    // This prevents finalized events from reverting to "Collecting" status.
                    if (event.status == EventStatus.RESTAURANT_CANDIDATES_GENERATED) {
                        viewModelScope.launch {
                            eventRepository.updateEventStatus(
                                event.id,
                                EventStatus.COLLECTING_RESTAURANT_VOTES,
                            )
                        }
                    }
                } else if (!restaurantsLoaded) {
                    _restaurantState.value = RestaurantState.Empty
                }
            }
        }

        // Re-build options whenever either event or all restaurants change
        viewModelScope.launch {
            combine(_event, _allRestaurants) { event, restaurants ->
                if (event != null && restaurants.allRestaurants.isNotEmpty()) {
                    buildDateLocationOptions(
                        event.dateTimeCandidates,
                        event.locationCandidates,
                        restaurants.allRestaurants,
                    )
                }
            }.collect {}
        }

        // Observe availability submissions and restaurant votes to update counts
        viewModelScope.launch {
            if (eventId.isNotEmpty()) {
                val submissionsFlow = eventRepository.observeSubmissions(eventId)
                val votesFlow = eventRepository.observeRestaurantVotes(eventId)

                combine(submissionsFlow, votesFlow) { submissions, votes ->
                    val availabilityCount = submissions.size
                    val votesCount = votes.distinctBy { it.userId }.size

                    val currentStatus = _event.value?.status ?: EventStatus.UNKNOWN
                    val isSecondRound = currentStatus == EventStatus.COLLECTING_RESTAURANT_VOTES || 
                                       currentStatus == EventStatus.FINALIZED

                    _uiState.update { state ->
                        state.copy(
                            submissionsCount = if (isSecondRound) votesCount else availabilityCount,
                            totalAvailabilityCount = availabilityCount,
                            attendees = if (isSecondRound) {
                                votes.distinctBy { it.userId }.map { it.userName }
                            } else {
                                submissions.map { it.name }
                            }
                        )
                    }
                }.collect {}
            }
        }
    }

    /**
     * Orchestrates the loading of place-specific data.
     * @param placeId The unique identifier for the restaurant from Google Places.
     * @param lat The latitude of the restaurant; used for distance calculation.
     * @param lng The longitude of the restaurant; used for distance calculation.
     */
    fun loadPlaceData(
        placeId: String,
        lat: Double?,
        lng: Double?,
    ) {
        fetchUserVote(placeId)
        updateDistanceToRestaurant(lat, lng)
    }

    /**
     * Performs a one-time GPS request and calculates distance between the user
     * and the restaurant coordinates.
     */
    @SuppressLint("MissingPermission")
    fun updateDistanceToRestaurant(
        destLat: Double?,
        destLng: Double?,
    ) {
        if (destLat == null || destLng == null) {
            _restaurantDistance.value = "Unknown distance"
            return
        }

        // 2. Launch in a background thread so the UI doesn't freeze while waiting for GPS
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // 3. Request current location from the hardware
                val location: Location? =
                    fusedLocationClient
                        .getCurrentLocation(
                            Priority.PRIORITY_HIGH_ACCURACY,
                            CancellationTokenSource().token,
                        ).await()

                location?.let { userLoc ->
                    // 4. Calculate distance and format it for display
                    val distance =
                        calculateDistanceMeters(
                            userLoc.latitude,
                            userLoc.longitude,
                            destLat,
                            destLng,
                        )

                    _restaurantDistance.value = formatDistance(distance)
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
     * Loads all restaurant candidates for the event from the Repository.
     * It uses the event's selected location to bias the search and fetches
     * details based on the target meeting time.
     */
    private fun getAllRestaurant(eventId: String) {
        viewModelScope.launch(Dispatchers.IO) {

            try {
                val event = _event.value ?: return@launch
                val lat = event.selectedLocationLat ?: return@launch
                val lng = event.selectedLocationLng ?: return@launch
                val timing = selectedTiming.value ?: return@launch
                val restaurants = eventRepository.getRestaurantsOnce(
                    eventId,
                    timing,
                    lat,
                    lng

                )
                isInitialFetchComplete = true
                _allRestaurants.value = AllRestaurantState(restaurants)
                _restaurantState.value =
                    if (restaurants.isEmpty()) {
                        RestaurantState.Empty
                    } else {
                        RestaurantState.Available(restaurants)
                    }
            } catch (e: Exception) {
                _restaurantState.value = RestaurantState.Error(e)
            }
        }
    }

    /**
     * A reactive list of restaurants that combines the raw data from Room/API
     * with the user's current UI filters (Timing and Area).
     */
    @OptIn(FlowPreview::class)
    val filteredRestaurants: StateFlow<List<Restaurant>> =
        combine(
            allRestaurants,
            selectedTiming,
            selectedLocation,
        ) { allState, timing, location ->
            filterRestaurants(
                restaurants = allState.allRestaurants,
                timing = timing,
                location = location,
            )
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList(),
        )

    /**
     * Updates user-selected filters.
     */
    fun setFilter(
        timing: DateTime,
        location: String,
    ) {
        selectedTiming.value = timing
        selectedLocation.value = location
    }

    /**
     * Builds and filters combinations of date/time and location options
     * that have at least one restaurant available.
     */
    fun buildDateLocationOptions(
        dateTimes: List<DateTime>,
        locations: List<String>,
        allRestaurants: List<Restaurant>,
    ) {
        val options =
            dateTimes.flatMap { dt ->
                locations.mapNotNull { loc ->
                    val filtered =
                        filterRestaurants(
                            restaurants = allRestaurants,
                            timing = dt,
                            location = loc,
                        )
                    if (filtered.isNotEmpty()) {
                        DateLocationOption(timing = dt, location = loc)
                    } else {
                        null
                    }
                }
            }
        _dateAndAreaState.value = DateAndAreaState(dateLocationOptions = options)



        // Restaurant state handling
        _restaurantState.value =
            when {
                !isInitialFetchComplete -> RestaurantState.Loading
                allRestaurants.isEmpty() -> RestaurantState.Empty
                else -> RestaurantState.Available(allRestaurants)
            }
        // Auto-select first option if none selected
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
                eventRepository
                    .submitVote(eventId, placeId, userId, timing)
                    .onSuccess {
                        _voteState.update { it.copy(isVoted = true) }
                        _voteResultState.value = VoteResultState.VoteSuccess
                    }.onFailure { e ->
                        Log.e("VoteError", "Failed to vote", e)
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
    fun fetchRestaurantDetail(placeId: String): Flow<Restaurant?> =
        allRestaurants.map { state -> state.allRestaurants.find { it.placeId == placeId } }

    /**
     * Loads whether the user has already voted for this restaurant at the selected time.
     *
     * Updates voteState.isVoted based on Firestore result.
     */
    fun fetchUserVote(placeId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            selectedTiming.value?.let { timing ->
                eventRepository
                    .getUserVote(eventId, placeId, userId, timing)
                    .onSuccess { exists -> _voteState.update { it.copy(isVoted = exists) } }
            }
        }
    }
}

/**
 * UI state for all restaurants.
 * @property allRestaurants List of all restaurants.
 */
data class AllRestaurantState(
    val allRestaurants: List<Restaurant> = listOf(),
)

/**
 * Represents a date and location combination.
 *
 * @property timing The date and time for the event.
 * @property location The location for the event.
 * @property label A human-readable label for the combination.
 * @property timingArg A string representation of the timing for navigation.
 */
data class DateLocationOption(
    val timing: DateTime,
    val location: String,
) {
    val label: String get() = "${timing.toDisplayLabel()} — $location"
    val timingArg: String get() = timing.toSerializableString()
}

/**
 * UI state for the Date & Area selection screen.
 *
 * @property dateLocationOptions List of available date and location combinations.
 * @see DateAndAreaPageDestination for navigation destination.
 */
data class DateAndAreaState(
    val dateLocationOptions: List<DateLocationOption> = listOf(),
)

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
    data class Available(
        val restaurants: List<Restaurant>
    ) : RestaurantState

    object Empty : RestaurantState

    data class Error(
        val error: Throwable,
    ) : RestaurantState
}

/**
 * Holds the user's current voting status for the selected restaurant.
 *
 * @property isVoted True if the user has already voted.
 */
data class VoteState(
    val isVoted: Boolean = false,
)

sealed class VoteResultState {
    object VoteSuccess : VoteResultState()

    data class VoteError(
        val message: String,
    ) : VoteResultState()
}

/**
 * UI state for the Place Details screen.
 *
 * @property submissionsCount The number of submissions for this event.
 * @property totalAvailabilityCount The number of people who shared availability in Round 1.
 * @property attendees A list of participant names.
 */
data class PlaceUiState(
    val submissionsCount: Int = 0,
    val totalAvailabilityCount: Int = 0,
    val attendees: List<String> = emptyList(),
)
