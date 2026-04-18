package com.meetup.meetingapp.ui.screens.host_dashboard

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.meetup.meetingapp.data.model.Event
import com.meetup.meetingapp.data.model.EventStatus
import com.meetup.meetingapp.data.repositories.EventRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
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
 * @property eventId The ID of the event to load.
 * @property _event Mutable state flow containing the event data.
 * @property event State flow exposing the event data.
 * @property _uiState Mutable state flow containing the UI state.
 * @property uiState State flow exposing the UI state.
 * @property _closeVotingState Mutable state flow indicating the state of the "Close Voting" action.
 * @property closeVotingState State flow exposing the state of the "Close Voting" action.
 * @property viewModelScope Coroutine scope associated with the ViewModel.
 */
class HostDashboardViewModel(
    private val eventRepository: EventRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    /**
     * The ID of the event to load.
     */
    private val eventId: String = savedStateHandle["eventId"] ?: ""

    /**
     * Mutable state flow containing the event data.
     */
    private val _event = MutableStateFlow<Event?>(null)

    /**
     * State flow exposing the event data.
     */
    val event = _event.asStateFlow()

    /**
     * Mutable state flow indicating the state of the "Close Voting" action.
     */
    private val _closeVotingState = MutableStateFlow<CloseVotingState>(CloseVotingState.Idle)

    /**
     * State flow exposing the state of the "Close Voting" action.
     */
    val closeVotingState = _closeVotingState.asStateFlow()

    /**
     * Mutable state flow containing the UI state.
     */
    private val _uiState = MutableStateFlow(HostDashboardUiState())

    /**
     * State flow exposing the UI state.
     */
    val uiState = _uiState.asStateFlow()

    /**
     * The ID of the current user.
     */
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    /**
     *
     * Initializes the ViewModel by:
     * 1. Observing the event from Firestore and updating the Room cache.
     * 2. Observing submissions from Firestore and updating the Room cache.
     * 3. Setting the initial UI state based on the event's status.
     * 4. If the event is created, sets the status to COLLECTING_AVAILABILITY.
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
                        fetchRestaurantVotesStatus()
                    }
                }
            }
        }
        viewModelScope.launch {
            // Observe submissions from Firestore and update Room cache
            eventRepository.observeSubmissions(eventId).collect { submissions ->
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
                // aggregateParticipantResponses sets status to FIRST_VOTING_CLOSED
                eventRepository.aggregateParticipantResponses(eventId).getOrThrow()

                eventRepository.syncEventById(eventId)

                val updatedEvent = eventRepository.getEventById(eventId)
                    .first()

                updatedEvent?.let {
                    // fetchAndSaveRestaurants sets status to RESTAURANT_CANDIDATES_GENERATED
                    eventRepository.fetchAndSaveRestaurants(it)
                }

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

    /**
     * Starts the restaurant voting process for the current event.
     */
    fun startRestaurantVoting() {
        viewModelScope.launch {
            eventRepository.updateEventStatus(
                eventId,
                EventStatus.COLLECTING_RESTAURANT_VOTES
            )
            // Force local refresh
            eventRepository.syncEventById(eventId)
        }
    }

    /**
     * Updates the status of the event.
     * @param status The new status to set.
     */
    fun updateEventStatus(status: EventStatus) {
        viewModelScope.launch {
            if (status == EventStatus.FINALIZED) {
                _closeVotingState.value = CloseVotingState.Loading
                viewModelScope.launch(Dispatchers.IO) {
                    eventRepository.aggregateRestaurantVotes(eventId)
                        .onSuccess {
                            withContext(Dispatchers.Main) {
                                _closeVotingState.value = CloseVotingState.Success
                            }
                        }
                        .onFailure { e ->
                            withContext(Dispatchers.Main) {
                                _closeVotingState.value = CloseVotingState.Error(e)
                            }
                        }
                }
            } else {
                eventRepository.updateEventStatus(eventId, status)
                eventRepository.syncEventById(eventId)
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

    /**
     * Fetches the restaurant votes status for the current event.
     */
    fun fetchRestaurantVotesStatus() {
        viewModelScope.launch(Dispatchers.IO) {
            val hasVotes = eventRepository.hasAnyRestaurantVotes(eventId)
            _uiState.value = _uiState.value.copy(hasAnyRestaurantVotes = hasVotes)
        }
    }
}

/**
 * Represents the UI state of the Host Dashboard screen.
 * Contains submission count, attendee names, and event status.
 * @property submissionsCount Number of participant submissions.
 * @property attendees List of participant names who submitted availability.
 * @property status Current status of the event.
 * @property hasVoted Whether the current user has voted in the event.
 * @property hasAnyRestaurantVotes Whether any user has voted for a restaurant in the event.
 */
data class HostDashboardUiState(
    val submissionsCount: Int = 0,
    val attendees: List<String> = emptyList(),
    val status: EventStatus = EventStatus.UNKNOWN,
    val hasVoted: Boolean = false,
    val hasAnyRestaurantVotes: Boolean = false
)

/**
 * Represents the UI state of the "Close Voting" action.
 *
 * This sealed interface defines the possible states of the "Close Voting" action:
 * - Idle: The action is not in progress.
 * - Loading: The action is in progress.
 * - Success: The action completed successfully.
 * - Error: An error occurred during the action.
 */
sealed interface CloseVotingState {
    object Idle : CloseVotingState
    object Loading : CloseVotingState
    object Success : CloseVotingState
    data class Error(val error: Throwable) : CloseVotingState
}
