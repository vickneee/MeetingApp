package com.meetup.meetingapp.ui.screens.host_dashboard

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meetup.meetingapp.data.model.Event
import com.meetup.meetingapp.data.model.EventStatus
import com.meetup.meetingapp.data.repositories.EventRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * ViewModel for the Host Dashboard screen.
 *
 * This ViewModel is responsible for:
 * - Loading the event data for the given eventId.
 * - Observing real-time updates to the event from Firestore (via the repository).
 * - Syncing participant submissions from Firestore into the local Room database.
 * - Exposing UI state such as submission count, attendee names, and event status.
 * - Handling the "Close Voting" action and updating the event's aggregated results.
 *
 * @param eventRepository Repository providing access to event and submission data.
 * @param savedStateHandle Used to retrieve the navigation argument `eventId`.
 */

class HostDashboardViewModel(
    private val eventRepository: EventRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // Pass eventId via navigation
    private val eventId: String = savedStateHandle["eventId"] ?: ""

    private val _event = MutableStateFlow<Event?>(null)
    val event = _event.asStateFlow()

    private val _closeVotingState = MutableStateFlow<CloseVotingState>(CloseVotingState.Idle)

    val closeVotingState = _closeVotingState.asStateFlow()

    private val _uiState = MutableStateFlow(HostDashboardUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            eventRepository.observeEventById(eventId).collect { event ->
                _event.value = event
                event?.let {
                    _uiState.value = _uiState.value.copy(
                        status = it.status
                    )
                    if (it.status == EventStatus.CREATED) {
                        eventRepository.updateEventStatus(
                            it.id,
                            EventStatus.COLLECTING_AVAILABILITY
                        )
                    }
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

    /**
     * Triggers the "Close Voting" operation for the current event.
     *
     * This function:
     * 1. Sets the UI state to Loading.
     * 2. Calls `aggregateParticipantResponses(eventId)` on a background thread to compute
     *    the majority-voted candidates and update the event status in Firestore.
     * 3. Updates the UI state to Success if the operation completes successfully.
     * 4. Updates the UI state to Error if any exception occurs.
     *
     * The result is exposed through [closeVotingState], which the UI observes to
     * enable/disable the button and display error messages.
     */
    fun closeVoting() {
        _closeVotingState.value = CloseVotingState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            try {
                eventRepository.aggregateParticipantResponses(eventId).getOrThrow()

                withContext(Dispatchers.Main) {
                    _closeVotingState.value = CloseVotingState.Success
                }
            } catch (e: Throwable) {
                withContext(Dispatchers.Main) {
                    _closeVotingState.value = CloseVotingState.Error(e)
                }
            }
        }
    }
}

data class HostDashboardUiState(
    val submissionsCount: Int = 0,
    val attendees: List<String> = emptyList(),
    val status: EventStatus = EventStatus.UNKNOWN
)

/**
 * Represents the UI state of the "Close Voting" action.
 *
 * - Idle: No action has been taken yet.
 * - Loading: The aggregation process is running.
 * - Success: Voting has been successfully closed and results saved.
 * - Error: An exception occurred during the process.
 */
sealed interface CloseVotingState {

    object Idle : CloseVotingState

    object Loading : CloseVotingState
    object Success : CloseVotingState

    data class Error(val error: Throwable) : CloseVotingState
}
