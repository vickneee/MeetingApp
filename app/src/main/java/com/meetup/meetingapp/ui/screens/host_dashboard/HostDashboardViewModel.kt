package com.meetup.meetingapp.ui.screens.host_dashboard

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meetup.meetingapp.data.model.Event
import com.meetup.meetingapp.data.model.EventStatus
import com.meetup.meetingapp.data.repositories.EventRepository
import com.meetup.meetingapp.ui.screens.participant_input.SubmitState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * ViewModel for the Host Dashboard screen.
 * @param eventRepository The repository to access event data.
 * @param savedStateHandle The saved state handle to retrieve navigation arguments.
 * @constructor Creates a new instance of the HostDashboardViewModel.
 */
class HostDashboardViewModel(private val eventRepository: EventRepository,
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
            eventRepository.getEventById(eventId)
                .collect { event ->
                    _event.value = event
                    event?.let {
                        _uiState.value = _uiState.value.copy(
                            status = it.status
                        )
                        // Auto-advance from CREATED to COLLECTING_AVAILABILITY
                        if (it.status == EventStatus.CREATED) {
                            eventRepository.updateEventStatus(it.id, EventStatus.COLLECTING_AVAILABILITY)
                        }
                    }
                }
        }
        viewModelScope.launch {
            eventRepository.getSubmissionsByEventId(eventId)
                .collect { submissions ->
                    _uiState.value = _uiState.value.copy(
                        submissionsCount = submissions.size,
                        attendees = submissions.map { it.name }
                    )
                }
        }
    }

    /**
     * Placeholder for the "Close Voting" button logic
     */
    fun closeVoting() {
        _closeVotingState.value = CloseVotingState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            try{
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

sealed interface CloseVotingState {

    object Idle : CloseVotingState

    object Loading : CloseVotingState
    object Success : CloseVotingState

    data class Error(val error: Throwable) : CloseVotingState
}
