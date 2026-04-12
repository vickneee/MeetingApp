package com.meetup.meetingapp.ui.screens.vote_for_place_flow

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.meetup.meetingapp.data.model.DateTime
import com.meetup.meetingapp.data.model.Event
import com.meetup.meetingapp.data.model.PlaceType
import com.meetup.meetingapp.data.model.TimeSlot
import com.meetup.meetingapp.data.model.Restaurant
import com.meetup.meetingapp.data.repositories.EventRepository
import com.meetup.meetingapp.data.repositories.PlacesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.format.TextStyle
import java.util.Locale
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.update
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * ViewModel for the Place selection screen.
 *
 * @param eventRepository Repository for interacting with Event data.
 * @param placesRepository Repository for interacting with Google Places API.
 * @param savedStateHandle State handle for saving and restoring UI state.
 * @see PlaceViewModel for retrieving place list data.
 */
class PlaceViewModel(
    private val eventRepository: EventRepository,
    private val placesRepository: PlacesRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    /** Currently authenticated user's UID (empty if not logged in). */
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

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

    private var restaurantsLoaded = false

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

            debugLoad()

            // Observe event from Firestore and update Room cache
            eventRepository.observeEventById(eventId).collect { event ->
                _event.value = event
                if (event != null) {
                    buildDateLocationOptions(event.dateTimeCandidates, event.locationCandidates)

                    // ↓ REPLACE the old hasCandidates block with this
                    if (!restaurantsLoaded && eventRepository.hasRestaurantCandidates(event.id)) {
                        restaurantsLoaded = true
                        getAllRestaurant(event.id)
                        _restaurantState.value = RestaurantState.Available
                    } else if (!restaurantsLoaded) {
                        _restaurantState.value = RestaurantState.Empty
                    }
                }
            }
        }
    }

    fun debugLoad() {
        viewModelScope.launch {
            Log.d("DEBUG", "=== Starting debug load ===")
            Log.d("DEBUG", "eventId: $eventId")

            val hasCandidates = eventRepository.hasRestaurantCandidates(eventId)
            Log.d("DEBUG", "hasRestaurantCandidates: $hasCandidates")

            val syncResult = runCatching { eventRepository.syncRestaurants(eventId) }
            Log.d("DEBUG", "syncRestaurants result: $syncResult")

            eventRepository.getRestaurants(eventId)
                .take(1) // just first emission
                .collect { restaurants ->
                    Log.d("DEBUG", "getRestaurants returned ${restaurants.size} items")
                    restaurants.forEach { Log.d("DEBUG", "  → ${it.name} | ${it.placeId}") }
                }
        }
    }

    /**
     * Loads all restaurant candidates for the event from Room.
     * Ensures Firestore → Room sync before collecting.
     */
    private fun getAllRestaurant(eventId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            Log.d("PlaceViewModel", "getAllRestaurant called for $eventId")
            eventRepository.syncRestaurants(eventId)
            eventRepository.getRestaurants(eventId)
                .collect { restaurants ->
                    Log.d("PlaceViewModel", "Loaded ${restaurants.size} restaurants")
                    _allRestaurants.update { it.copy(allRestaurants = restaurants) }
                    if (restaurants.isNotEmpty()) {
                        _restaurantState.value = RestaurantState.Available
                    } else {
                        _restaurantState.value = RestaurantState.Empty
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
            Log.d("PlaceViewModel", "Filtering ${all.size} restaurants by $location and $timing")
            val filtered = all.filter { restaurant ->
                // Robust location matching
                val query = location?.trim() ?: ""
                val locationMatch = query.isEmpty() ||
                        (restaurant.address?.contains(query, ignoreCase = true) == true) ||
                        (restaurant.name.contains(query, ignoreCase = true))

                val timingMatch =
                    timing == null || isRestaurantOpenForTiming(restaurant, timing)

                locationMatch && timingMatch
            }
            Log.d("PlaceViewModel", "Filtered result size: ${filtered.size}")
            filtered
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    /**
     * Converts a DateTime into a 3-letter weekday abbreviation (Mon, Tue…)
     */
    fun DateTime.toDayAbbrev(): String {
        val localDate = this.toLocalDate()
        return localDate.dayOfWeek.name.take(3).lowercase().replaceFirstChar { it.uppercase() }
    }

    /**
     * Extracts weekday from openingHours string, handling ranges like "Mon-Fri".
     */
    fun parseDays(hours: String): List<String> {
        val daysOfWeek = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        val normalized = hours.takeWhile { it != ':' }

        // Handle ranges like "Monday-Friday"
        if (normalized.contains("-") || normalized.contains("–")) {
            val parts = normalized.split(Regex("[-–]")).map {
                it.trim().take(3).lowercase().replaceFirstChar { c -> c.uppercase() }
            }
            if (parts.size == 2) {
                val startIdx = daysOfWeek.indexOf(parts[0])
                val endIdx = daysOfWeek.indexOf(parts[1])
                if (startIdx != -1 && endIdx != -1) {
                    return if (startIdx <= endIdx) {
                        daysOfWeek.subList(startIdx, endIdx + 1)
                    } else {
                        // Wraps around weekend
                        daysOfWeek.subList(startIdx, 7) + daysOfWeek.subList(0, endIdx + 1)
                    }
                }
            }
        }

        // Fallback to literal matches
        val found = daysOfWeek.filter { normalized.contains(it, ignoreCase = true) }
        if (found.isNotEmpty()) return found

        val dayRegex = Regex("([A-Za-z]+):")
        val match = dayRegex.find(hours) ?: return emptyList()
        return listOf(match.groupValues[1].take(3).lowercase().replaceFirstChar { it.uppercase() })
    }

    /**
     * Normalizes Google Places openingHours strings.
     */
    fun normalizeHours(raw: String): String {
        return raw
            .replace("–", "-")
            .replace(Regex("[\\u202F\\u2009\\u200A\\u200B\\uFEFF\\u00A0]"), "")
            .replace("AM", " AM")
            .replace("PM", " PM")
            .trim()
    }

    /**
     * Extracts start/end time from openingHours string.
     */
    fun extractTimeRange(hours: String): Pair<String, String>? {
        val normalized = normalizeHours(hours)
        val regex = Regex("(\\d{1,2}:\\d{2}\\s?[AP]M)\\s?-\\s?(\\d{1,2}:\\d{2}\\s?[AP]M)")
        val match = regex.find(normalized) ?: return null
        val (start, end) = match.destructured
        return convertTo24(start) to convertTo24(end)
    }

    /**
     * Converts "9:00 AM" → "09:00" (24-hour format)
     */
    fun convertTo24(time: String): String {
        val formatter12 = DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH)
        val formatter24 = DateTimeFormatter.ofPattern("HH:mm")
        val localTime = LocalTime.parse(time.uppercase(), formatter12)
        return localTime.format(formatter24)
    }

    /**
     * Checks if restaurant is open during the target time slot (lenient overlap).
     */
    fun hasOverlap(
        openStart: String,
        openEnd: String,
        targetStart: String,
        targetEnd: String
    ): Boolean {
        fun toMinutes(t: String): Int {
            val parts = t.split(":")
            if (parts.size < 2) return 0
            return (parts[0].toIntOrNull() ?: 0) * 60 + (parts[1].toIntOrNull() ?: 0)
        }

        val oStart = toMinutes(openStart)
        val oEnd = toMinutes(openEnd)
        val tStart = toMinutes(targetStart)
        val tEnd = toMinutes(targetEnd)

        // Handle cross-midnight (e.g., 18:00 - 02:00)
        val actualOEnd = if (oEnd <= oStart) oEnd + 1440 else oEnd

        val overlapStart = maxOf(oStart, tStart)
        val overlapEnd = minOf(actualOEnd, tEnd)

        return overlapEnd > overlapStart
    }

    /**
     * Determines whether a restaurant is open during the selected timing.
     */
    fun isRestaurantOpenForTiming(restaurant: Restaurant, timing: DateTime): Boolean {
        val targetDay = timing.toDayAbbrev()
        val targetStart = timing.timeSlot.start
        val targetEnd = timing.timeSlot.end
        val hoursList = restaurant.openingHours

        // If no opening hours are available, assume it's open to be safe
        if (hoursList.isNullOrEmpty()) return true

        return hoursList.any { hours ->
            val days = parseDays(hours)
            if (!days.contains(targetDay)) return@any false
            val range = extractTimeRange(hours) ?: return@any false
            val (openStart, openEnd) = range
            hasOverlap(openStart, openEnd, targetStart, targetEnd)
        }
    }

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
    fun buildDateLocationOptions(
        dateTimes: List<DateTime>,
        locations: List<String>
    ) {
        val options = dateTimes.flatMap { dt ->
            locations.map { loc ->
                DateLocationOption(timing = dt, location = loc)
            }
        }
        _dateAndAreaState.value = DateAndAreaState(
            dateLocationOptions = options
        )

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
                val result = eventRepository.submitVote(
                    eventId = eventId,
                    placeId = placeId,
                    userId = userId,
                    dateTime = timing
                )

                result.onSuccess {
                    _voteState.update { it.copy(isVoted = true) }
                    _voteResultState.value = VoteResultState.VoteSuccess
                }.onFailure { e ->
                    Log.e("PlaceViewModel", "Vote submission failed", e)
                    when (e) {
                        is FirebaseNetworkException ->
                            _voteResultState.value =
                                VoteResultState.VoteError("Network error, please check your connection")

                        else ->
                            _voteResultState.value =
                                VoteResultState.VoteError("Failed to submit vote, please retry")
                    }
                }
            }
        }
    }

    /**
     * Loads whether the user has already voted for this restaurant at the selected time.
     *
     * Updates voteState.isVoted based on Firestore result.
     */
    fun fetchUserVote(placeId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            selectedTiming.value?.let { timing ->
                val result = eventRepository.getUserVote(
                    eventId = eventId,
                    placeId = placeId,
                    userId = userId,
                    dateTime = timing
                )

                result.onSuccess { exists ->
                    _voteState.update { it.copy(isVoted = exists) }
                }.onFailure { e ->
                    Log.e("PlaceViewModel", "Failed to fetch user vote", e)
                    _voteResultState.value =
                        VoteResultState.VoteError("Failed to load vote status")
                }
            }
        }
    }
}

/**
 * UI state for all restaurants.
 * @property allRestaurants List of all restaurants.
 */
data class AllRestaurantState(
    val allRestaurants: List<Restaurant> = listOf()
)

/**
 * Converts a DateTime to a human-readable string.
 * @return Formatted string representing the date and time.
 * @see toDisplayLabel for a more specific label.
 */
fun DateTime.toDisplayLabel(): String {
    val localDate = this.toLocalDate()
    val month = localDate.month.getDisplayName(TextStyle.SHORT, Locale.ENGLISH)
    val day = localDate.dayOfMonth
    return "$month $day (${timeSlot.start}–${timeSlot.end})"
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
data class DateLocationOption(
    val timing: DateTime,
    val location: String
) {
    val label: String
        get() = "${timing.toDisplayLabel()} — $location"

    val timingArg: String
        get() = timing.toSerializableString()
}

/**
 * UI state for the Date & Area selection screen.
 *
 * @property dateLocationOptions List of available date and location combinations.
 * @see DateAndAreaPageDestination for navigation destination.
 */
data class DateAndAreaState(
    val dateLocationOptions: List<DateLocationOption> = listOf<DateLocationOption>()
)

/**
 * Converts a DateTime to a string for storage in Firestore.
 * @return String representation of the DateTime.
 */
fun DateTime.toSerializableString(): String {
    return "$date|${timeSlot.start}-${timeSlot.end}"
}

/**
 * Converts a string representation of a DateTime to a DateTime object.
 * @return DateTime object parsed from the string.
 *
 * @see toSerializableString for the string format.
 */
fun String.toDateTime(): DateTime {
    val parts = split("|")
    val date = parts[0]
    val timeParts = parts[1].split("-")
    val start = timeParts[0]
    val end = timeParts[1]
    return DateTime(
        date = date,
        timeSlot = TimeSlot(start, end)
    )
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
data class VoteState(
    val isVoted: Boolean = false
)

/**
 * Represents the result of a vote submission attempt.
 */
sealed class VoteResultState {
    object VoteSuccess : VoteResultState()
    data class VoteError(val message: String) : VoteResultState()
}

