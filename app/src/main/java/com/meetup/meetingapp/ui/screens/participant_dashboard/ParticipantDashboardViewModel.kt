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
import kotlinx.coroutines.flow.combine
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
            val eventFlow = eventRepository.observeEventById(eventId)
            val submissionsFlow = eventRepository.observeSubmissions(eventId)
            val votesFlow = eventRepository.observeRestaurantVotes(eventId)

            combine(eventFlow, submissionsFlow, votesFlow) { eventData, submissions, votes ->
                _event.value = eventData
                eventData?.let { e ->
                    val isSecondRound = e.status == EventStatus.COLLECTING_RESTAURANT_VOTES ||
                            e.status == EventStatus.FINALIZED

                    val count = if (isSecondRound) {
                        votes.distinctBy { it.userId }.size
                    } else {
                        submissions.size
                    }

                    val names = if (isSecondRound) {
                        votes.distinctBy { it.userId }.map { it.userName }
                    } else {
                        submissions.map { it.name }
                    }

                    _uiState.value = _uiState.value.copy(
                        status = e.status,
                        submissionsCount = count,
                        attendees = names
                    )

                    // Side effects
                    if (e.status == EventStatus.CREATED) {
                        viewModelScope.launch {
                            eventRepository.updateEventStatus(e.id, EventStatus.COLLECTING_AVAILABILITY)
                        }
                    }
                    if (isSecondRound) {
                        fetchUserVote()
                    }
                }
            }.collect {}
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
 * @property attendees a list of participant names.
 * @property status Current status of the event.
 * @property hasVoted Whether the current user has voted in the event.
 */
data class ParticipantDashboardUiState(
    val submissionsCount: Int = 0,
    val attendees: List<String> = emptyList(),
    val status: EventStatus = EventStatus.UNKNOWN,
    val hasVoted: Boolean = false
)
