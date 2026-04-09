package com.meetup.meetingapp.ui.screens.vote_for_restaurant_flow

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meetup.meetingapp.data.model.DateTime
import com.meetup.meetingapp.data.model.Event
import com.meetup.meetingapp.data.model.TimeSlot
import com.meetup.meetingapp.data.repositories.EventRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.format.TextStyle
import java.util.Locale

/**
 * ViewModel for the Restaurant screen.
 *
 * @param eventRepository The repository to access event data.
 * @param savedStateHandle The saved state handle to retrieve navigation arguments.
 * @constructor Creates a new instance of the RestaurantViewModel.
 */
class RestaurantViewModel(
    private val eventRepository: EventRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val eventId: String = savedStateHandle[ParticipantDashboardWaitingDestination.eventIdArg] ?: ""

    private val _event = MutableStateFlow<Event?>(null)
    val event: StateFlow<Event?> = _event.asStateFlow()

    private val _uiState = MutableStateFlow(RestaurantUiState())
    val uiState: StateFlow<RestaurantUiState> = _uiState.asStateFlow()

    private val _dateAndAreaState = MutableStateFlow(DateAndAreaState())
    val dateAndAreaState = _dateAndAreaState.asStateFlow()

    init {
        viewModelScope.launch {
            eventRepository.getEventById(eventId).collect { event ->
                _event.value = event // From Room

                if(event != null){
                    buildDateLocationOptions(event.dateTimeCandidates, event.locationCandidates)
                }

            }
        }
        viewModelScope.launch {
            // Sync from Firestore to Room first
            eventRepository.syncSubmissions(eventId)
            // Then collect from Room
            eventRepository.getSubmissionsByEventId(eventId).collect { submissions ->
                _uiState.value = _uiState.value.copy(
                    submissionsCount = submissions.size,
                    attendees = submissions.map { it.name } // From Room
                )
            }
        }
    }

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




    fun getRestaurants(timing: DateTime, location: String){

    }
}

/**
 * UI state for the Restaurant screen.
 *
 * @property submissionsCount The number of submissions made.
 * @property attendees The list of attendees' names.
 */
data class RestaurantUiState(
    val submissionsCount: Int = 0,
    val attendees: List<String> = emptyList()
)

fun DateTime.toDisplayLabel(): String {
    val localDate = this.toLocalDate()
    val month = localDate.month.getDisplayName(TextStyle.SHORT, Locale.ENGLISH)
    val day = localDate.dayOfMonth

    return "$month $day (${timeSlot.start}–${timeSlot.end})"
}

data class DateLocationOption(
    val timing: DateTime,
    val location: String
){
    val label: String
        get() = "${timing.toDisplayLabel()} — $location"
}

data class DateAndAreaState(
    val dateLocationOptions: List<DateLocationOption> = listOf<DateLocationOption>()
)
