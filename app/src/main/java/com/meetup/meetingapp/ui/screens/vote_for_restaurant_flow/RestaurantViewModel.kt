package com.meetup.meetingapp.ui.screens.vote_for_restaurant_flow

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meetup.meetingapp.R
import com.meetup.meetingapp.data.model.DateTime
import com.meetup.meetingapp.data.model.Event
import com.meetup.meetingapp.data.model.PlaceType
import com.meetup.meetingapp.data.model.TimeSlot
import com.meetup.meetingapp.data.model.Restaurant
import com.meetup.meetingapp.data.repositories.EventRepository
import com.meetup.meetingapp.data.repositories.PlacesRepository
import com.meetup.meetingapp.ui.screens.participant_input_flow.SubmitState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale
import kotlinx.coroutines.flow.combine


class RestaurantViewModel(
    private val eventRepository: EventRepository,
    private val placesRepository: PlacesRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {


    private val eventId: String =
        savedStateHandle[ParticipantDashChooseDateAndAreaDestination.eventIdArg] ?: ""

    private val _event = MutableStateFlow<Event?>(null)
    val event: StateFlow<Event?> = _event.asStateFlow()

    private val _uiState = MutableStateFlow(RestaurantUiState())
    val uiState: StateFlow<RestaurantUiState> = _uiState.asStateFlow()

    private val _dateAndAreaState = MutableStateFlow(DateAndAreaState())
    val dateAndAreaState = _dateAndAreaState.asStateFlow()

    private val _restaurantState = MutableStateFlow<RestaurantState>(RestaurantState.Loading)
    val restaurantState = _restaurantState.asStateFlow()

    val allRestaurants: StateFlow<List<Restaurant>> =
        eventRepository.getRestaurants(eventId)
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                emptyList()
            )

    private val selectedTiming = MutableStateFlow<DateTime?>(null)
    private val selectedLocation = MutableStateFlow<String?>(null)

    // --- DateTime → "Mon" などの曜日に変換 ---
    fun DateTime.toDayAbbrev(): String {
        val localDate = this.toLocalDate()
        return localDate.dayOfWeek.name.take(3).lowercase().replaceFirstChar { it.uppercase() }
    }

    // --- "Mon-Fri 09:00-18:00" から曜日範囲を抽出 ---
    fun parseDays(hours: String): List<String> {
        val dayRegex = Regex("([A-Z][a-z]{2})(?:-([A-Z][a-z]{2}))?")
        val match = dayRegex.find(hours) ?: return emptyList()

        val start = match.groupValues[1]
        val end = match.groupValues.getOrNull(2)

        val days = listOf("Mon","Tue","Wed","Thu","Fri","Sat","Sun")

        return if (end == null) {
            listOf(start)
        } else {
            val startIndex = days.indexOf(start)
            val endIndex = days.indexOf(end)
            if (startIndex <= endIndex) days.subList(startIndex, endIndex + 1) else emptyList()
        }
    }

    // --- "09:00-18:00" を抽出 ---
    fun extractTimeRange(hours: String): Pair<String, String>? {
        val regex = Regex("(\\d{2}:\\d{2})-(\\d{2}:\\d{2})")
        val match = regex.find(hours) ?: return null
        val (start, end) = match.destructured
        return start to end
    }


    fun hasAtLeastTwoHoursOverlap(
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

        return overlapMinutes >= 120
    }


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

            hasAtLeastTwoHoursOverlap(openStart, openEnd, targetStart, targetEnd)
        }
    }


    fun setFilter(timing: DateTime, location: String) {
        selectedTiming.value = timing
        selectedLocation.value = location
    }


    val filteredRestaurants: StateFlow<List<Restaurant>> =
        combine(allRestaurants, selectedTiming, selectedLocation) { all, timing, location ->
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



    init {
        viewModelScope.launch {
            eventRepository.syncEventById(eventId) // Sync from Firestore to Room first
            eventRepository.getEventById(eventId).collect { event ->
                _event.value = event

                if(event != null){
                    buildDateLocationOptions(event.dateTimeCandidates, event.locationCandidates)

                    val hasCandidates = eventRepository.hasRestaurantCandidates(event.id)

                    if (hasCandidates) {
                        eventRepository.syncRestaurants(event.id)

                        _restaurantState.value = RestaurantState.Available
                        return@collect
                    }

                    fetchAllCombinations(event)

                }
            }
        }


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
                    eventRepository.syncRestaurants(event.id)

                    _restaurantState.value = RestaurantState.Available

                }

                saveResult.onFailure { e ->
                    _restaurantState.value = RestaurantState.Error(e)
                }
            }
        }
    }








    fun submitVote(restaurantId: String) {
        // handle vote submission
    }
}

data class RestaurantUiState(
    val restaurants: List<Restaurant> = emptyList(),
    val selectedRestaurantId: String? = null,
    val isSubmitting: Boolean = false,
    val isSubmitted: Boolean = false
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
){
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

sealed interface RestaurantState {

    /** Submission is in progress or has not started yet. */
    object Loading : RestaurantState

    /** Submission completed successfully. */
    object Available : RestaurantState

    object Empty : RestaurantState

    /** Submission failed due to an error. */
    data class Error(val error: Throwable) : RestaurantState
}

