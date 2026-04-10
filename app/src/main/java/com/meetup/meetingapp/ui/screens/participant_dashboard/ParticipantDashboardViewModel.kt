package com.meetup.meetingapp.ui.screens.participant_dashboard

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meetup.meetingapp.data.model.Event
import com.meetup.meetingapp.data.repositories.EventRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the Participant Dashboard screen.
 *
 * This ViewModel is responsible for:
 * - Loading the event data for the given eventId.
 * - Observing real-time updates to the event from Firestore (via the repository).
 *
 * @param eventRepository Repository providing access to event and submission data.
 * @param savedStateHandle Used to retrieve the navigation argument `eventId`.
 * @property eventId The ID of the event to load.
 * @property _event Mutable state flow containing the event data.
 * @property event State flow exposing the event data.
 * @property _uiState Mutable state flow containing the UI state.
 * @property uiState State flow exposing the UI state.
 * @property viewModelScope Coroutine scope associated with the ViewModel.
 */
class ParticipantDashboardViewModel(
    private val eventRepository: EventRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val eventId: String = savedStateHandle[ParticipantDashboardDestination.eventIdArg] ?: ""

    private val _event = MutableStateFlow<Event?>(null)
    val event: StateFlow<Event?> = _event.asStateFlow()

    private val _uiState = MutableStateFlow(ParticipantDashboardUiState())
    val uiState: StateFlow<ParticipantDashboardUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            // Observe event from Firestore and update Room cache
            eventRepository.observeEventById(eventId).collect { event ->
                _event.value = event
            }
        }
        viewModelScope.launch {
            // Observe submissions from Firestore and update Room cache
            eventRepository.observeSubmissions(eventId).collect { submissions ->
                _uiState.value = _uiState.value.copy(
                    submissionsCount = submissions.size,
                    attendees = submissions.map { it.name }
                )
            }
        }
    }
}

/**
 * Represents the UI state of the Participant Dashboard screen.
 *
 * @property submissionsCount The number of submissions made by participants.
 * @property attendees A list of participant names.
 */
data class ParticipantDashboardUiState(
    val submissionsCount: Int = 0,
    val attendees: List<String> = emptyList()
)