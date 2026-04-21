package com.meetup.meetingapp.ui.screens.participant_input_flow

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.meetup.meetingapp.data.model.DateTime
import com.meetup.meetingapp.data.model.Event
import com.meetup.meetingapp.data.model.FoodCategory
import com.meetup.meetingapp.data.model.PlaceType
import com.meetup.meetingapp.data.repositories.EventRepository
import com.meetup.meetingapp.utils.buildAllAvailableDateTimes
import com.meetup.meetingapp.utils.buildDateAvailability
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * ViewModel for the participant input flow.
 *
 * @param eventRepository Repository providing access to event and submission data.
 * @param savedStateHandle Used to retrieve the navigation argument `eventCode`.
 * @property eventCode The event code to load.
 * @property eventKey The event key to load.
 * @property _event Mutable state flow containing the event data.
 * @property event State flow exposing the event data.
 * @property _participantState Mutable state flow containing the participant input state.
 * @property participantState State flow exposing the participant input state.
 * @property _fetchState Mutable state flow containing the fetch state.
 * @property fetchState State flow exposing the fetch state.
 * @property _submitState Mutable state flow containing the submit state.
 * @property submitState State flow exposing the submit state.
 * @property _isLoading Mutable state flow indicating whether data is loading.
 * @property isLoading State flow exposing the loading state.
 * @property currentUserId User ID of the current user.
 * @property dates State flow exposing the list of available dates and time slots.
 * @constructor Creates a new instance of the ParticipantViewModel.
 */
class ParticipantViewModel(
    private val eventRepository: EventRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    /**
     * The event code to load.
     */
    private val eventCode: String = savedStateHandle["eventCode"] ?: ""

    /**
     * The event key to load.
     */
    private val eventKey: String = savedStateHandle["eventKey"] ?: ""

    /**
     * Mutable state flow containing the event data.
     */
    private val _event = MutableStateFlow<Event?>(null)

    /**
     * State flow exposing the event data.
     */
    val event = _event.asStateFlow()

    /**
     * Mutable state flow containing the participant input state.
     */
    private val _participantState = MutableStateFlow(ParticipantInputState())

    /**
     * State flow exposing the participant input state.
     */
    val participantState = _participantState.asStateFlow()

    /**
     * Mutable state flow containing the fetch state.
     */
    private val _fetchState = MutableStateFlow<FetchState>(FetchState.Loading)

    /**
     * State flow exposing the fetch state.
     */
    val fetchState = _fetchState.asStateFlow()

    /**
     * Mutable state flow containing the submit state.
     */
    private val _submitState = MutableStateFlow<SubmitState>(SubmitState.Idle)

    /**
     * State flow exposing the submit state.
     */
    val submitState = _submitState.asStateFlow()

    /**
     * Mutable state flow indicating whether data is loading.
     */
    private val _isLoading = MutableStateFlow(true)

    /**
     * State flow exposing the loading state.
     */
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    /**
     * User ID of the current user.
     */
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    /**
     * Mutable state flow containing the participant dashboard UI state.
     */
    private val _uiState = MutableStateFlow(SubmitState.ParticipantDashboardUiState())

    /**
     * State flow exposing the participant dashboard UI state.
     */
    val uiState = _uiState.asStateFlow()

    /**
     * State flow indicating whether the current user is the host of the event.
     */
    val isHost: StateFlow<Boolean> = _event
        .map { event ->
            event != null && event.hostId == currentUserId  // compare hostId to actual user ID
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    /**
     * Represents the availability of time slots for each date.
     *
     * @see UiTimeSlot for more information about time slots.
     */
    val dates: StateFlow<List<DateAvailability>> = combine(_event, _participantState) { event, state ->
        if (event == null) {
            emptyList()
        } else {
            buildDateAvailability(event, state.selectedDateTimes)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * Flattened list of all available date and time slot combinations for the event.
     */
    val allAvailableDateTimes: StateFlow<List<DateTime>> = _event.map { event ->
        if (event == null) emptyList()
        else buildAllAvailableDateTimes(event)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * Initializes the view model by syncing the event and observing it.
     */
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
                        _participantState.update { 
                            it.copy(
                                eventId = event.id,
                                participantName = if (it.participantName.isEmpty() && event.hostId == currentUserId) event.hostName else it.participantName
                            ) 
                        }
                        checkIfUserAlreadySubmitted(event.id)
                        observeSubmissions(event.id)
                        _fetchState.value = FetchState.Success
                        _isLoading.value = false
                    } else {
                        _fetchState.value = FetchState.Error("Event not found")
                        _isLoading.value = false
                    }
                }
        }
    }

    /**
     * Checks if the current user has already submitted a response for the event.
     */
    private fun checkIfUserAlreadySubmitted(eventId: String) {
        val uid = currentUserId ?: return
        viewModelScope.launch {
            val response = eventRepository.getParticipantResponse(eventId, uid)
            if (response != null) {
                _uiState.update { it.copy(
                    isAlreadySubmitted = true,
                    submittedName = response.name
                ) }
                // Also update the input state name if it's empty
                if (_participantState.value.participantName.isEmpty()) {
                    _participantState.update { it.copy(participantName = response.name) }
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

    /**
     * Toggles the selection of a date and time slot.
     *
     * @param dateTime The date and time slot to toggle.
     */
    fun toggleDateTime(dateTime: DateTime) {
        _participantState.update { current ->
            val updated = if (current.selectedDateTimes.contains(dateTime))
                current.selectedDateTimes - dateTime
            else
                current.selectedDateTimes + dateTime
            current.copy(selectedDateTimes = updated)
        }
    }

    /**
     * Toggles the selection of a date and time slot.
     *
     * @param date The date of the slot.
     * @param slotIndex The index of the slot in the list.
     * @see UiTimeSlot for more information about time slots.
     */
    fun toggleDateTime(date: String, slotIndex: Int) {
        val currentEvent = _event.value ?: return
        if (slotIndex !in currentEvent.timeSlots.indices) return
        
        val slot = currentEvent.timeSlots[slotIndex]
        val dateTime = DateTime(date = date, timeSlot = slot)
        toggleDateTime(dateTime)
    }

    /**
     * Toggles the selection of a place type.
     *
     * @param placeType The place type to toggle.
     * @see PlaceType for more information about place types.
     */
    fun togglePlaceType(placeType: PlaceType) {
        _participantState.update { current ->
            val updated = if (current.selectedPlaceTypes.contains(placeType))
                current.selectedPlaceTypes - placeType
            else
                current.selectedPlaceTypes + placeType
            current.copy(selectedPlaceTypes = updated)
        }
    }

    /**
     * Toggles the selection of a location.
     *
     * @param city The city to toggle.
     */
    fun toggleLocation(city: String) {
        _participantState.update { current ->
            val updated = if (current.selectedLocations.contains(city))
                current.selectedLocations - city
            else
                current.selectedLocations + city
            current.copy(selectedLocations = updated)
        }
    }

    /**
     * Toggles the selection of a food category.
     *
     * @param category The food category to toggle.
     * @see FoodCategory for more information about food categories.
     */
    fun toggleFoodCategory(category: FoodCategory) {
        _participantState.update { current ->
            val updated = if (current.selectedFoodCategories.contains(category))
                current.selectedFoodCategories - category
            else
                current.selectedFoodCategories + category
            current.copy(selectedFoodCategories = updated)
        }
    }

    /**
     * Updates the participant's name.
     *
     * @param name The new name of the participant.
     */
    fun updateName(name: String) {
        _participantState.update { it.copy(participantName = name) }
    }

    /**
     * Observes submissions for the given event ID.
     *
     * @param eventId The ID of the event to observe submissions for.
     */
    private fun observeSubmissions(eventId: String) {
        viewModelScope.launch {
            eventRepository.observeSubmissions(eventId).collect { submissions ->
                _uiState.update { it.copy(submissionsCount = submissions.size) }
            }
        }
    }

}

/**
 * Represents input state for the participant.
 *
 * @property eventId The ID of the event.
 * @property participantName The name of the participant.
 * @property selectedDateTimes The list of selected date and time slots.
 * @property selectedLocations The list of selected locations.
 * @property selectedPlaceTypes The list of selected place types.
 * @property selectedFoodCategories The list of selected food categories.
 */
data class ParticipantInputState(
    val eventId: String = "",
    val participantName: String = "",
    val selectedDateTimes: List<DateTime> = emptyList(),
    val selectedLocations: List<String> = emptyList(),
    val selectedPlaceTypes: List<PlaceType> = emptyList(),
    val selectedFoodCategories: List<FoodCategory> = emptyList()
)

/**
 * Represents the state of fetching an event.
 *
 * Used to track the progress and result of the fetch process
 * (e.g., loading, success, or error).
 * @see FetchState.Loading for loading state.
 * @see FetchState.Success for success state.
 * @see FetchState.Error for error state.
 */
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
 *
 * @see SubmitState.Loading for loading state.
 * @see SubmitState.Success for success state.
 * @see SubmitState.Error for error state.
 */
sealed interface SubmitState {
    /**
     * Initial state of the submission process.
     */
    object Idle : SubmitState
    /** Submission is in progress or has not started yet. */
    object Loading : SubmitState

    /** Submission completed successfully. */
    object Success : SubmitState

    /** Submission failed due to an error. */
    data class Error(val error: Throwable) : SubmitState

    /**
     * Represents the state of the participant dashboard.
     */
    data class ParticipantDashboardUiState(
        val submissionsCount: Int = 0,
        val isAlreadySubmitted: Boolean = false,
        val submittedName: String = ""
    )
}
