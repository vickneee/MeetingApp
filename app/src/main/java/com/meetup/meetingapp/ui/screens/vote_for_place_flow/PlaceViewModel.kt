package com.meetup.meetingapp.ui.screens.vote_for_place_flow

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meetup.meetingapp.data.model.DateTime
import com.meetup.meetingapp.data.model.Event
import com.meetup.meetingapp.data.model.Restaurant
import com.meetup.meetingapp.data.repositories.EventRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.time.format.TextStyle
import java.util.Locale

class PlaceViewModel(
    private val eventRepository: EventRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val eventId: String =
        savedStateHandle[ChooseDateAndAreaDestination.eventIdArg] ?: ""

    private val _event = MutableStateFlow<Event?>(null)
    val event: StateFlow<Event?> = _event.asStateFlow()

    private val _uiState = MutableStateFlow(PlaceUiState())
    val uiState: StateFlow<PlaceUiState> = _uiState.asStateFlow()

    private val _dateAndAreaState = MutableStateFlow(DateAndAreaState())
    val dateAndAreaState = _dateAndAreaState.asStateFlow()

    private val _placeListState = MutableStateFlow<List<Restaurant>>(emptyList())
    val placeListState = _placeListState.asStateFlow()

    init {
        viewModelScope.launch {
            eventRepository.syncEventById(eventId) // Sync from Firestore to Room first
            eventRepository.getEventById(eventId).collect { event ->
                _event.value = event

                if(event != null){
                    buildDateLocationOptions(event.dateTimeCandidates, event.locationCandidates)
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
}

/**
 * UI state for the date & area selection screen.
 *
 * @param dateLocationOptions List of all selectable date–location combinations.
 */
data class DateAndAreaState(
    val dateLocationOptions: List<DateLocationOption> = listOf<DateLocationOption>()
)

