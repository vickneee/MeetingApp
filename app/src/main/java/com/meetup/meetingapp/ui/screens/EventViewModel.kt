package com.meetup.meetingapp.ui.screens

import androidx.compose.runtime.mutableFloatStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meetup.meetingapp.data.model.DateRange
import com.meetup.meetingapp.data.model.LocationOption
import com.meetup.meetingapp.data.model.PlaceType
import com.meetup.meetingapp.data.model.TimeSlot
import com.meetup.meetingapp.data.repositories.EventRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.sql.Date
import java.time.LocalDate

/**
 * ViewModel responsible for managing the state of the event creation flow.
 *
 * This ViewModel exposes a StateFlow<EventUiState> that represents the current
 * user input for creating an event. Each update function modifies a specific
 * part of the UI state using immutable copy operations.
 *
 * Responsibilities:
 * - Hold and update event creation form data
 * - Provide functions to modify individual fields (title, host name, date range, etc.)
 * - Interact with EventRepository to persist the final event
 */
class EventViewModel(private val eventRepository: EventRepository):  ViewModel(){

    private val _uiState = MutableStateFlow(EventUiState())

    val uiState = _uiState.asStateFlow()

    /**
     * Creates a new event using the current UI state.
     *
     * This function collects the latest EventUiState value and passes it to the
     * EventRepository. The repository handles validation and persistence.
     */
    fun createEvent(){
        viewModelScope.launch {
            eventRepository.createEvent(uiState.value)
        }
    }

    /**
     * Updates the event title field in the UI state.
     *
     * @param title The new event title entered by the user.
     */
    fun updateTitle(title: String){
        _uiState.update {current ->
            current.copy(
                eventTitle = title
            )

        }
    }

    /**
     * Updates the host name field in the UI state.
     *
     * @param name The name of the event host.
     */
    fun updateHostName(name: String){
        _uiState.update {current ->
            current.copy(
                hostName = name
            )
        }
    }

    /**
     * Updates the selected date range for the event.
     *
     * @param dateRange The new date range chosen by the user.
     */
    fun updateDateRange(dateRange: DateRange){
        _uiState.update {current ->
            current.copy(
                dateRange = dateRange
            )
        }
    }

    /**
     * Adds a new time slot to the event.
     *
     * @param slot The time slot to add.
     */
    fun addTimeSlot(slot: TimeSlot){
        _uiState.update {current ->
            current.copy(
                timeSlots = current.timeSlots + slot
            )
        }
    }

    /**
     * Removes a time slot from the event.
     *
     * @param slot The time slot to remove.
     */
    fun removeTimeSlot(slot: TimeSlot){
        _uiState.update {current ->
            current.copy(
                timeSlots = current.timeSlots - slot
            )
        }
    }

    /**
     * Adds a city to the list of selected cities in the location options.
     *
     * @param city The city name to add.
     */
    fun addCity(city: String) {
        _uiState.update { current ->
            current.copy(
                locations = current.locations.copy(
                    cities = current.locations.cities + city
                )
            )
        }
    }

    /**
     * Removes a city from the list of selected cities.
     *
     * @param city The city name to remove.
     */
    fun removeCity(city: String) {
        _uiState.update { current ->
            current.copy(
                locations = current.locations.copy(
                    cities = current.locations.cities - city
                )
            )
        }
    }

    /**
     * Adds a place type (e.g., RESTAURANT, CAFE) to the selected list.
     *
     * @param placeType The place type to add.
     */
    fun addPlaceType(placeType: PlaceType){
        _uiState.update {current ->
            current.copy(
                placeTypes = current.placeTypes + placeType
            )
        }
    }

    /**
     * Removes a place type from the selected list.
     *
     * @param placeType The place type to remove.
     */
    fun removePlaceType(placeType: PlaceType){
        _uiState.update {current ->
            current.copy(
                placeTypes = current.placeTypes - placeType
            )
        }
    }

}

/**
 * Represents the full UI state for the event creation screen.
 *
 * This state is immutable and updated through copy() operations inside the ViewModel.
 * Each field corresponds to a user input required to create an event.
 *
 * @property eventTitle The title of the event.
 * @property hostName The name of the event host.
 * @property dateRange The selected date range for the event.
 * @property timeSlots A list of selected time slots.
 * @property locations The selected location information (country, region, cities).
 * @property placeTypes A list of selected place types (e.g., RESTAURANT, CAFE).
 */
data class EventUiState(
    val eventTitle: String = "",
    val hostName:String = "",
    val dateRange: DateRange = DateRange(LocalDate.now(), LocalDate.now().plusDays(7)),
    val timeSlots: List<TimeSlot> = listOf(),
    val locations: LocationOption = LocationOption(),
    val placeTypes: List<PlaceType> = listOf()
    )