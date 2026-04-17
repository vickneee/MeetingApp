package com.meetup.meetingapp.ui.screens.participant_dashboard

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.meetup.meetingapp.data.model.Event
import com.meetup.meetingapp.data.model.EventStatus
import com.meetup.meetingapp.data.repositories.EventRepository
import kotlinx.coroutines.Dispatchers
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

    /**
     * The ID of the event to load.
     */
    private val eventId: String = savedStateHandle[ParticipantDashboardDestination.eventIdArg] ?: ""

    /**
     * Mutable state flow containing the event data.
     */
    private val _event = MutableStateFlow<Event?>(null)

    /**
     * State flow exposing the event data.
     */
    val event: StateFlow<Event?> = _event.asStateFlow()

    /**
     * Mutable state flow containing the UI state.
     */
    private val _uiState = MutableStateFlow(ParticipantDashboardUiState())

    /**
     * State flow exposing the UI state.
     */
    val uiState: StateFlow<ParticipantDashboardUiState> = _uiState.asStateFlow()

    /**
     * The ID of the current user.
     */
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    /**
     * Initializes the ViewModel by:
     * 1. Observing the event from Firestore and updating the Room cache.
     * 2. Observing submissions from Firestore and updating the Room cache.
     */
    init {
        viewModelScope.launch {
            // Observe event from Firestore and update Room cache
            eventRepository.observeEventById(eventId).collect { event ->
                _event.value = event
                event?.let {
                    _uiState.value = _uiState.value.copy(
                        status = it.status
                    )
                    // If event is created, set status to COLLECTING_AVAILABILITY
                    if (it.status == EventStatus.CREATED) {
                        eventRepository.updateEventStatus(
                            it.id,
                            EventStatus.COLLECTING_AVAILABILITY
                        )
                    }
                    // Only check vote when restaurants exist and voting is active
                    if (it.status == EventStatus.COLLECTING_RESTAURANT_VOTES ||
                        it.status == EventStatus.FINALIZED
                    ) {
                        fetchUserVote()
                    }
                }
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

    /**
     * Fetches the user's vote status for the current event.
     */
    fun fetchUserVote() {
        viewModelScope.launch(Dispatchers.IO) {
            val event = _event.value ?: return@launch

            val hasVoted = eventRepository.hasUserVotedInEvent(
                eventId = eventId,
                userId = userId,
                timings = event.dateTimeCandidates
            )

            _uiState.value = _uiState.value.copy(hasVoted = hasVoted)
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
    val attendees: List<String> = emptyList(),
    val status: EventStatus = EventStatus.UNKNOWN,
    val hasVoted: Boolean = false
)