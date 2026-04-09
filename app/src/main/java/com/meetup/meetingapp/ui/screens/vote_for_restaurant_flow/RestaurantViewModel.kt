package com.meetup.meetingapp.ui.screens.vote_for_restaurant_flow

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meetup.meetingapp.data.model.DateTime
import com.meetup.meetingapp.data.model.Event
import com.meetup.meetingapp.data.model.TimeSlot
import com.meetup.meetingapp.data.model.Restaurant
import com.meetup.meetingapp.data.repositories.EventRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.format.TextStyle
import java.util.Locale

class RestaurantViewModel(
    private val eventRepository: EventRepository,
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

