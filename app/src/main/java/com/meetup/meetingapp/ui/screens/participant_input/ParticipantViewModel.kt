package com.meetup.meetingapp.ui.screens.participant_input

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meetup.meetingapp.data.model.DateTime
import com.meetup.meetingapp.data.model.Event
import com.meetup.meetingapp.data.model.FoodCategory
import com.meetup.meetingapp.data.model.PlaceType
import com.meetup.meetingapp.data.repositories.EventRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ParticipantViewModel(
    private val eventRepository: EventRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // Event code entered by participant to join
    private val eventCode: String = savedStateHandle["eventCode"] ?: ""
    private val eventKey: String = savedStateHandle["eventKey"] ?: ""

    // The fetched event from Firestore
    private val _event = MutableStateFlow<Event?>(null)
    val event = _event.asStateFlow()

    // Participant input state
    private val _participantState = MutableStateFlow(ParticipantInputState())
    val participantState = _participantState.asStateFlow()

    // Fetch state
    private val _fetchState = MutableStateFlow<FetchState>(FetchState.Loading)
    val fetchState = _fetchState.asStateFlow()

    // State representing the submission process (sending participant responses)
    private val _submitState = MutableStateFlow<SubmitState>(SubmitState.Loading)

    val submitState = _submitState.asStateFlow()

    init {
        syncEvent()
        observeEventByEventCode(eventCode)
    }

    /**
     *  Synchronizes events from the repository.
     */
    private fun syncEvent(){
        viewModelScope.launch(Dispatchers.IO){
            eventRepository.syncEventByEventCodeAndKey(eventCode, eventKey)
        }
    }

    /**
     * Observes the event with the given ID from the repository.
     * Room database is the single source of truth for event data.
     *
     * @param eventCode The event code of the event to observe.
     */
    private fun observeEventByEventCode(eventCode: String) {
        viewModelScope.launch {
            eventRepository.getEventByEventCode(eventCode)
                .collect { event ->
                    if (event != null) {
                        _event.value = event
                        _participantState.update { it.copy(eventId = event.id) }
                        _fetchState.value = FetchState.Success
                    } else {
                        _fetchState.value = FetchState.Error("Event not found")
                    }
                }
        }
    }

    /**
     * Submits the participant's input (availability and preferences).
     *
     * This function triggers the submission flow and updates [submitState]
     * to reflect the result. The actual Firestore write operation will be
     * added here once integrated with the repository.
     */
    fun submitParticipantInput() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                eventRepository.createParticipantAvailability(_participantState.value).getOrThrow()
                withContext(Dispatchers.Main) {
                    _submitState.value = SubmitState.Success
                }
            } catch (e: Throwable) {
                withContext(Dispatchers.Main) {
                    _submitState.value = SubmitState.Error(e)
                }
            }
        }
    }

    fun toggleDateTime(dateTime: DateTime) {
        _participantState.update { current ->
            val updated = if (current.selectedDateTimes.contains(dateTime))
                current.selectedDateTimes - dateTime
            else
                current.selectedDateTimes + dateTime
            current.copy(selectedDateTimes = updated)
        }
    }

    fun togglePlaceType(placeType: PlaceType) {
        _participantState.update { current ->
            val updated = if (current.selectedPlaceTypes.contains(placeType))
                current.selectedPlaceTypes - placeType
            else
                current.selectedPlaceTypes + placeType
            current.copy(selectedPlaceTypes = updated)
        }
    }

    fun toggleFoodCategory(category: FoodCategory) {
        _participantState.update { current ->
            val updated = if (current.selectedFoodCategories.contains(category))
                current.selectedFoodCategories - category
            else
                current.selectedFoodCategories + category
            current.copy(selectedFoodCategories = updated)
        }
    }

    fun updateName(name: String) {
        _participantState.update { it.copy(participantName = name) }
    }

}

// Participant input UI state
data class ParticipantInputState(
    val eventId: String = "",
    val participantName: String = "",
    val selectedDateTimes: List<DateTime> = emptyList(),
    val selectedLocations: List<String> = emptyList(),
    val selectedPlaceTypes: List<PlaceType> = emptyList(),
    val selectedFoodCategories: List<FoodCategory> = emptyList()
)

// Fetch state
sealed interface FetchState {
    object Loading : FetchState
    object Success : FetchState
    data class Error(val message: String) : FetchState
}

/**
 * Represents the state of submitting participant input to the backend.
 *
 * Used to track the progress and result of the submission process,
 * allowing the UI to react accordingly (e.g., showing loading indicators,
 * navigating on success, or displaying error messages).
 */
sealed interface SubmitState {

    /** Submission is in progress or has not started yet. */
    object Loading : SubmitState

    /** Submission completed successfully. */
    object Success : SubmitState

    /** Submission failed due to an error. */
    data class Error(val error: Throwable) : SubmitState
}
