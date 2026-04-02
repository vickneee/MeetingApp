package com.meetup.meetingapp.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meetup.meetingapp.data.model.DateRange
import com.meetup.meetingapp.data.model.Event
import com.meetup.meetingapp.data.model.LocationOption
import com.meetup.meetingapp.data.model.PlaceType
import com.meetup.meetingapp.data.model.TimeSlot
import com.meetup.meetingapp.data.repositories.EventRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Represents all possible states of the event creation process.
 *
 * This sealed interface ensures that all states are known at compile time,
 */
sealed interface EventState {

    /**
     * Represents a successful event creation.
     *
     * @property eventCode The unique code generated for the event.
     * @property eventKey The secret key associated with the event.
     * @property eventId The unique identifier of the created event.
     */
    data class Success(val eventCode: String, val eventKey: String, val eventId: String): EventState

    /**
     * Represents a failure during event creation.
     *
     * @property error The exception that caused the failure.
     */
    data class Error(val error: Throwable): EventState

    /**
     * Represents the initial idle state before any event creation attempt.
     */
    object Loading: EventState
}

/**
 * ViewModel responsible for managing the event creation flow.
 *
 * This ViewModel exposes:
 * - [uiState]: The current user input for the event creation form.
 * - [eventState]: The result of the event creation attempt (success, error, idle).
 *
 * Responsibilities:
 * - Maintain and update the event creation form data.
 * - Provide functions to update individual fields (title, host name, date range, etc.).
 * - Call [EventRepository] to create the event and emit the result.
 *
 * @property eventRepository The repository used to create and persist events.
 */
class EventViewModel(private val eventRepository: EventRepository):  ViewModel(){

    private val _uiState = MutableStateFlow(EventUiState())

    val uiState = _uiState.asStateFlow()

    private val _eventState = MutableStateFlow<EventState>(EventState.Loading)

    val eventState = _eventState.asStateFlow()

    /**
     * Synchronizes events from the repository.
     */
    private val _events = MutableStateFlow<List<Event>>(emptyList())
    val events = _events.asStateFlow()

    init {
        observeEvents()
        syncEvents()
    }

    /**
     * Synchronizes events from the repository.
     */
    private fun observeEvents() {
        viewModelScope.launch {
            eventRepository.getEvents()
                .collect { _events.value = it }
        }
    }

    /**
     * Synchronizes events from the repository.
     */
    private fun syncEvents() {
        viewModelScope.launch(Dispatchers.IO) {
            eventRepository.syncEvents()
        }
    }

    /**
     * Attempts to create a new event using the current UI state.
     *
     * Steps:
     * 1. Retrieve the latest [EventUiState] value.
     * 2. Pass it to the repository to create the event.
     * 3. If successful, emit [EventState.Success].
     * 4. If an exception occurs, emit [EventState.Error].
     *
     * This function runs inside [viewModelScope] to ensure proper lifecycle handling.
     */
    fun createEvent(){
        viewModelScope.launch(Dispatchers.IO)  {
            try {
                val(eventCode, eventKey, eventId) = eventRepository.createEvent(uiState.value).getOrThrow()

                withContext(Dispatchers.Main) {
                    _eventState.value = EventState.Success(eventCode, eventKey, eventId)
                }
            } catch (e: Throwable){
                _eventState.value = EventState.Error(e)
            }
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
    val dateRange: DateRange = DateRange(),
    val timeSlots: List<TimeSlot> = listOf(),
    val locations: LocationOption = LocationOption(),
    val placeTypes: List<PlaceType> = listOf()
    )
