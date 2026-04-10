package com.meetup.meetingapp.ui.screens.vote_for_place_flow

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import com.meetup.meetingapp.data.model.DateTime
import com.meetup.meetingapp.data.model.Event
import com.meetup.meetingapp.data.model.PlaceType
import com.meetup.meetingapp.data.model.TimeSlot
import com.meetup.meetingapp.data.model.Restaurant
import com.meetup.meetingapp.data.repositories.EventRepository
import com.meetup.meetingapp.data.repositories.PlacesRepository
import com.meetup.meetingapp.ui.screens.vote_for_restaurant_flow.ChooseDateAndAreaDestination

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.format.TextStyle
import java.util.Locale
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import java.time.LocalTime
import java.time.format.DateTimeFormatter


/**
 * ViewModel for the Place selection screen.
 *
 * @param eventRepository Repository providing access to event and submission data.
 * @param savedStateHandle Used to retrieve the navigation argument `eventId`.
 * @property eventId The ID of the event to load.
 * @property _event Mutable state flow containing the event data.
 * @property event State flow exposing the event data.
 * @property _uiState Mutable state flow containing the UI state.
 * @property uiState State flow exposing the UI state.
 * @property _dateAndAreaState Mutable state flow containing the date and area state.
 * @property dateAndAreaState State flow exposing the date and area state.
 * @property viewModelScope Coroutine scope associated with the ViewModel.
 * @constructor Creates a new instance of the PlaceViewModel.
 */
class PlaceViewModel(
    private val eventRepository: EventRepository,
    private val placesRepository: PlacesRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {


    private val eventId: String =
        savedStateHandle[ChooseDateAndAreaDestination.eventIdArg] ?: ""

    private val _event = MutableStateFlow<Event?>(null)
    val event: StateFlow<Event?> = _event.asStateFlow()

    private val _uiState = MutableStateFlow(PlaceUiState())
    val uiState: StateFlow<PlaceUiState> = _uiState.asStateFlow()

    /** Available date × location combinations for filtering */
    private val _dateAndAreaState = MutableStateFlow(DateAndAreaState())
    val dateAndAreaState = _dateAndAreaState.asStateFlow()

    private val _placeListState = MutableStateFlow<List<Restaurant>>(emptyList())
    val placeListState = _placeListState.asStateFlow()

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

    /**
     * Initialization:
     * - Sync event from Firestore → Room
     * - Observe event changes
     * - Load restaurant candidates (from Room or Places API)
     */
    init {
        viewModelScope.launch {
            // Observe event from Firestore and update Room cache
            eventRepository.observeEventById(eventId).collect { event ->
                _event.value = event

                if(event != null){
                    buildDateLocationOptions(event.dateTimeCandidates, event.locationCandidates)

                    val hasCandidates = eventRepository.hasRestaurantCandidates(event.id)

                    if (hasCandidates) {
                        getAllRestaurant(event.id)

                        _restaurantState.value = RestaurantState.Available
                        return@collect
                    }
                    fetchAllCombinations(event)
                }
            }
        }
    }

    /**
     * Loads all restaurant candidates for the event from Room.
     * Ensures Firestore → Room sync before collecting.
     */
    private fun getAllRestaurant(eventId: String){
        viewModelScope.launch(Dispatchers.IO) {
            eventRepository.syncRestaurants(eventId)
            eventRepository.getRestaurants(eventId)
                .collect { restaurants ->
                    _allRestaurants.update {
                        it.copy(allRestaurants = restaurants)
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
            allRestaurants.map { it.allRestaurants },   // ← AllRestaurantState から取り出す
            selectedTiming,
            selectedLocation
        ) { all, timing, location ->

            all.forEach { restaurant ->
                Log.d("OpeningHoursDebug", "Name: ${restaurant.name}")
                Log.d("OpeningHoursDebug", "Hours: ${restaurant.openingHours}")
            }

            all.filter { restaurant ->
                val locationMatch =
                    location == null ||
                            (restaurant.address?.contains(location, ignoreCase = true) == true)

                val timingMatch =
                    timing == null || isRestaurantOpenForTiming(restaurant, timing)

                locationMatch && timingMatch
            }
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
     * Extracts weekday from openingHours string.
     * Example: "Monday: 9:00 AM - 8:00 PM" → ["Mon"]
     */
    fun parseDays(hours: String): List<String> {
        val dayRegex = Regex("([A-Za-z]+):")
        val match = dayRegex.find(hours) ?: return emptyList()

        val full = match.groupValues[1] // Monday
        val abbrev = full.take(3)       // Mon

        return listOf(abbrev)
    }

    /**
     * Normalizes Google Places openingHours strings by:
     * - Replacing EN DASH with ASCII hyphen
     * - Removing invisible Unicode spaces
     * - Ensuring AM/PM has a leading space
     */
    fun normalizeHours(raw: String): String {
        return raw
            .replace("–", "-") // EN DASH → ASCII
            .replace(Regex("[\\u202F\\u2009\\u200A\\u200B\\uFEFF\\u00A0]"), "") // 全不可視スペース削除
            .replace("AM", " AM")
            .replace("PM", " PM")
            .trim()
    }

    /**
     * Extracts start/end time from openingHours string.
     * Example: "Monday: 9:00 AM - 8:00 PM" → ("09:00", "20:00")
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
     * Checks if restaurant is open for at least 1.5 hours (90 minutes)
     * during the selected time slot.
     */
    fun hasAtLeastOneAndHalfHoursOverlap(
        openStart: String,
        openEnd: String,
        targetStart: String,
        targetEnd: String
    ): Boolean {

        fun toMinutes(t: String): Int {
            val (h, m) = t.split(":").map { it.toInt() }
            return h * 60 + m
        }

        val oStart = toMinutes(openStart)
        val oEnd = toMinutes(openEnd)
        val tStart = toMinutes(targetStart)
        val tEnd = toMinutes(targetEnd)

        val overlapStart = maxOf(oStart, tStart)
        val overlapEnd = minOf(oEnd, tEnd)

        val overlapMinutes = overlapEnd - overlapStart

        return overlapMinutes >= 90
    }


    /**
     * Determines whether a restaurant is open during the selected timing.
     */
    fun isRestaurantOpenForTiming(restaurant: Restaurant, timing: DateTime): Boolean {
        val targetDay = timing.toDayAbbrev()
        val targetStart = timing.timeSlot.start
        val targetEnd = timing.timeSlot.end

        val hoursList = restaurant.openingHours ?: return false

        return hoursList.any { hours ->
            val days = parseDays(hours)
            if (!days.contains(targetDay)) return@any false

            val range = extractTimeRange(hours) ?: return@any false
            val (openStart, openEnd) = range

            hasAtLeastOneAndHalfHoursOverlap(openStart, openEnd, targetStart, targetEnd)
        }
    }

    /**
     * Updates user-selected filters.
     */
    fun setFilter(timing: DateTime, location: String) {
        selectedTiming.value = timing
        selectedLocation.value = location
        Log.d("setFilter", "${selectedTiming.value}")
        Log.d("setFilter", "${selectedLocation.value}")
    }







    /**
     * Builds all combinations of date/time and location options.
     *
     * Example:
     * dateTimes = [Apr 12 (09–12), Apr 12 (12–15)]
     * locations = ["Helsinki", "Espoo"]
     *
     * Result:
     * - Apr 12 (09–12) — Helsinki
     * - Apr 12 (09–12) — Espoo
     * - Apr 12 (12–15) — Helsinki
     * - Apr 12 (12–15) — Espoo
     *
     * @param dateTimes List of available date/time candidates.
     * @param locations List of available location candidates.
     */
    fun buildDateLocationOptions(
        dateTimes: List<DateTime>,
        locations: List<String>
    ){
        val options = dateTimes.flatMap { dt ->
            locations.map { loc ->
                DateLocationOption(timing = dt, location = loc)
            }
        }

        _dateAndAreaState.value = DateAndAreaState(
            dateLocationOptions = options
        )
    }



    /**
     * Fetches fallback restaurant candidates using Places API
     * when no Firestore candidates exist.
     */
    fun fetchAllCombinations(event: Event) {

        viewModelScope.launch {


            val maxCallsPerCity = 5

            val allRestaurants = mutableListOf<Restaurant>()
            val seen = mutableSetOf<String>()

            event.locationCandidates.forEach { city ->

                val combinations = event.placeTypeCandidates.flatMap { placeType ->

                    when (placeType) {


                        PlaceType.RESTAURANT -> {
                            event.foodCategoryCandidates.map { foodCategory ->
                                Triple(city, placeType, foodCategory)
                            }
                        }


                        PlaceType.CAFE -> {
                            listOf(Triple(city, placeType, null))
                        }


                        PlaceType.BAR -> {
                            listOf(Triple(city, placeType, null))
                        }

                    }
                }

                val limited = combinations.shuffled().take(maxCallsPerCity)

                limited.forEach { (city, placeType, foodCategory) ->

                    val query = when {
                        placeType == PlaceType.RESTAURANT && foodCategory != null ->
                            "${foodCategory.queryName} restaurant in $city"

                        placeType == PlaceType.CAFE ->
                            "cafe in $city"

                        placeType == PlaceType.BAR ->
                            "bar in $city"

                        else ->
                            "${placeType.queryName} in $city"
                    }


                    val result = placesRepository.fetchRestaurants(query)

                    result.onSuccess { restaurants ->
                        val first = restaurants.firstOrNull()
                        if (first != null && seen.add(first.placeId)) {
                            allRestaurants.add(first)
                        }
                    }

                }
            }
            if (allRestaurants.isEmpty()) {
                _restaurantState.value = RestaurantState.Empty
                return@launch
            } else {
                Log.d("fetchAllCombinations", "$allRestaurants")

                val saveResult = eventRepository.saveAllRestaurants(event.id, allRestaurants)

                saveResult.onSuccess {
                    getAllRestaurant(event.id)

                    _restaurantState.value = RestaurantState.Available

                }

                saveResult.onFailure { e ->
                    _restaurantState.value = RestaurantState.Error(e)
                }
            }
        }
    }


    fun getRestaurants(timing: DateTime, location: String) {
         val restaurants = eventRepository.getRestaurantsByLocation(location)
    }

    fun submitVote(restaurantId: String) {
        // handle vote submission
    }
}

data class PlaceUiState(
    val restaurants: List<Restaurant> = emptyList(),
    val selectedRestaurantId: String? = null,
    val isSubmitting: Boolean = false,
    val isSubmitted: Boolean = false
)

data class AllRestaurantState(
    val allRestaurants: List<Restaurant> = listOf()
)

/**
 * Converts a [DateTime] into a human-readable label.
 *
 * Example output:
 * "Apr 12 (09:00–12:00)"
 *
 * @return A formatted display string.
 */
fun DateTime.toDisplayLabel(): String {
    val localDate = this.toLocalDate()
    val month = localDate.month.getDisplayName(TextStyle.SHORT, Locale.ENGLISH)
    val day = localDate.dayOfMonth

    return "$month $day (${timeSlot.start}–${timeSlot.end})"
}

/**
 * Represents a single selectable combination of date/time and location.
 *
 * @param timing The date and time slot.
 * @param location The location name.
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
 * UI state for the date & area selection screen.
 *
 * @param dateLocationOptions List of all selectable date–location combinations.
 */
data class DateAndAreaState(
    val dateLocationOptions: List<DateLocationOption> = listOf<DateLocationOption>()
)

fun DateTime.toSerializableString(): String {
    return "$date|${timeSlot.start}-${timeSlot.end}"
}

/**
 * Converts a serialized DateTime string into a DateTime object.
 *
 * This function is used when navigating between screens, where DateTime
 * cannot be passed directly and must be encoded as a string.
 *
 * Expected input format:
 *     "YYYY-MM-DD|HH:mm-HH:mm"
 *
 * Example:
 *     "2026-04-19|12:00-15:00"
 *
 * Parsing steps:
 *  - Split by "|" to separate date and time range
 *  - Split time range by "-" to extract start/end times
 *  - Construct a DateTime with a TimeSlot(start, end)
 *
 * @receiver A serialized DateTime string.
 * @return A reconstructed DateTime object.
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
 * Represents the loading state of restaurant candidates in the voting flow.
 *
 * This sealed interface allows the UI to react appropriately depending on
 * whether restaurants are being loaded, successfully available, missing,
 * or failed due to an error.
 *
 * States:
 *  - Loading:    Data is being fetched (Firestore → Room or Places API)
 *  - Available:  Restaurant candidates are successfully loaded
 *  - Empty:      No candidates were found for the event
 *  - Error:      An exception occurred during loading or saving
 */
sealed interface RestaurantState {

    /** Submission is in progress or has not started yet. */
    object Loading : RestaurantState

    /** Submission completed successfully. */
    object Available : RestaurantState

    object Empty : RestaurantState

    /** Submission failed due to an error. */
    data class Error(val error: Throwable) : RestaurantState
}
