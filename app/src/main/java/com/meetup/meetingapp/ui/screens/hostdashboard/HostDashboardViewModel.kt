package com.meetup.meetingapp.ui.screens.hostdashboard

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.meetup.meetingapp.data.model.Event
import com.meetup.meetingapp.data.model.EventStatus
import com.meetup.meetingapp.data.repositories.EventRepository
import com.meetup.meetingapp.data.repositories.SubmissionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * ViewModel for the Host Dashboard screen.
 *
 * This ViewModel is responsible for managing the state of the Host Dashboard screen,
 *
 * @property eventRepository The repository for managing events.
 * @property submissionRepository The repository for managing submissions.
 * @property savedStateHandle A handle to saved state, used for passing arguments.
 * @property eventId The ID of the event to display.
 * @property uiState The current state of the UI.
 * @property currentUserId The ID of the current user.
 *
 * @property closeVotingState The current state of the close voting operation.
 * @property _closeVotingState Mutable state flow for the close voting operation.
 */
class HostDashboardViewModel(
    private val eventRepository: EventRepository,
    private val submissionRepository: SubmissionRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val eventId: String = savedStateHandle["eventId"] ?: ""

    private val _uiState =
        MutableStateFlow(
            HostDashboardUiState(
                event = null,
                status = EventStatus.UNKNOWN,
                hasHostSubmittedAvailability = false,
                hasAnyRestaurantVotes = false,
                isInitialLoading = true,
            ),
        )
    val uiState = _uiState.asStateFlow()

    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    init {
        viewModelScope.launch {
            val eventFlow = eventRepository.observeEventById(eventId)
            val submissionsFlow = eventRepository.observeSubmissions(eventId)
            val votesFlow = eventRepository.observeRestaurantVotes(eventId)

            // Track previous vote count to only schedule when a new vote arrives
            var lastVoteCount = -1

            combine(eventFlow, submissionsFlow, votesFlow) { eventData, submissions, votes ->
                eventData?.let { e ->
                    val isSecondRound =
                        e.status == EventStatus.COLLECTING_RESTAURANT_VOTES ||
                            e.status == EventStatus.FINALIZED

                    val availabilityCount = submissions.size
                    val votesCount = votes.distinctBy { it.userId }.size

                    // Logic:
                    // In second round (voting): Num is current votes, Denom is total Round 1 submmissions.
                    // In first round (availability): Num is current submissions, Denom is 0 (or we just hide it).
                    val count = if (isSecondRound) votesCount else availabilityCount
                    val total = if (isSecondRound) availabilityCount else 0

                    val names =
                        if (isSecondRound) {
                            votes.distinctBy { it.userId }.map { it.userName }
                        } else {
                            submissions.map { it.name }
                        }

                    val currentName =
                        if (e.hostId == currentUserId) {
                            e.hostName
                        } else {
                            submissions.find { it.userId == currentUserId }?.name ?: ""
                        }

                    // Check if host has submitted availability in the first round
                    val hasAvailability = submissions.any { it.userId == currentUserId || it.name == e.hostName }

                    _uiState.update { currentState ->
                        currentState.copy(
                            event = eventData,
                            status = e.status,
                            submissionsCount = count,
                            totalParticipants = total,
                            attendees = names,
                            hasHostSubmittedAvailability = hasAvailability,
                            hasAnyRestaurantVotes = votes.isNotEmpty(),
                            isInitialLoading = false, // Data from all flows has arrived
                            currentUserName = currentName,
                            noPlacesFound = if (e.status != EventStatus.FIRST_VOTING_CLOSED) false else currentState.noPlacesFound,
                        )
                    }

                    if (e.status == EventStatus.FIRST_VOTING_CLOSED) {
                        viewModelScope.launch(Dispatchers.IO) {
                            val hasRestaurants = eventRepository.hasRestaurantCandidates(e.id)
                            if (!hasRestaurants) {
                                _uiState.update { it.copy(noPlacesFound = true) }
                            }
                        }
                    }

                    // Side effects
                    if (e.status == EventStatus.CREATED) {
                        viewModelScope.launch {
                            eventRepository.updateEventStatus(e.id, EventStatus.COLLECTING_AVAILABILITY)
                        }
                    }
                    if (isSecondRound) {
                        fetchUserVote()
                    }

                    if (e.status == EventStatus.COLLECTING_RESTAURANT_VOTES && votesCount != lastVoteCount)  {
                        lastVoteCount = votesCount
                        submissionRepository.scheduleSubmissionCheck(e.id)
                    }
                }
            }.collect {}
        }
    }

    /**
     * Initiates the close voting process.
     *
     * This method triggers the close voting operation, which includes:
     * - Aggregating participant responses.
     * - Syncing the event data.
     * - Fetching and handling restaurant data.
     */
    fun closeVoting() {
        _closeVotingState.value = CloseVotingState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // 1. Aggregate responses
                eventRepository.aggregateParticipantResponses(eventId).getOrThrow()

                // 2. Sync to ensure local cache is fresh
                eventRepository.syncEventById(eventId)

                // 3. Get the latest event state
                val updatedEvent = eventRepository.getEventById(eventId).first()

                if (updatedEvent != null) {
                    // 4. Fetch restaurants and handle the result explicitly
                    val result = eventRepository.fetchAndSaveRestaurants(updatedEvent)

                    result
                        .onSuccess {
                            withContext(Dispatchers.Main) {
                                _closeVotingState.value = CloseVotingState.Success
                            }
                        }.onFailure { error ->
                            withContext(Dispatchers.Main) {
                                _closeVotingState.value = CloseVotingState.Error(error)
                                if (error.message?.contains("No places found") == true) {
                                    _uiState.update { it.copy(noPlacesFound = true) }
                                }
                            }
                        }
                } else {
                    throw Exception("Event not found after sync")
                }
            } catch (e: Throwable) {
                withContext(Dispatchers.Main) {
                    _closeVotingState.value = CloseVotingState.Error(e)
                }
            }
        }
    }

    private val _closeVotingState = MutableStateFlow<CloseVotingState>(CloseVotingState.Idle)
    val closeVotingState = _closeVotingState.asStateFlow()

    /**
     * Updates the event status.
     *
     * This method is used to update the status of the event.
     */
    fun updateEventStatus(status: EventStatus) {
        viewModelScope.launch {
            if (status == EventStatus.FINALIZED) {
                _closeVotingState.value = CloseVotingState.Loading
                viewModelScope.launch(Dispatchers.IO) {
                    eventRepository
                        .aggregateRestaurantVotes(eventId)
                        .onSuccess {
                            withContext(Dispatchers.Main) {
                                _closeVotingState.value = CloseVotingState.Success
                            }
                        }.onFailure { e ->
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
     * Fetches the user's vote status.
     *
     * This method is used to check if the current user has voted in the event.
     */
    fun fetchUserVote() {
        viewModelScope.launch(Dispatchers.IO) {
            val currentEvent = uiState.value.event ?: return@launch
            val hasVoted =
                eventRepository.hasUserVotedInEvent(
                    eventId = eventId,
                    userId = currentUserId,
                    timings = currentEvent.dateTimeCandidates,
                )
            _uiState.update { it.copy(hasVoted = hasVoted) }
        }
    }

    /**
     * Checks if the host has submitted availability.
     *
     * This method is used to check if the host has submitted availability.
     */
    fun checkHostAvailability() {
        viewModelScope.launch {
            val eventValue = uiState.value.event ?: return@launch
            val result =
                eventRepository.hasUserSubmittedAvailability(
                    eventId = eventValue.id,
                    userId = eventValue.hostId,
                )
            _uiState.update {
                it.copy(hasHostSubmittedAvailability = result)
            }
        }
    }
}

/**
 * Represents the UI state of the Host Dashboard screen.
 * @property event The event data.
 * @property submissionsCount The number of submissions.
 * @property totalParticipants The total number of participants.
 * @property attendees A list of attendee names.
 * @property status The current event status.
 * @property hasVoted Whether the current user has voted.
 * @property hasAnyRestaurantVotes Whether any restaurant votes have been submitted.
 * @property hasHostSubmittedAvailability Whether the host has submitted availability.
 * @property isInitialLoading Whether data is still loading.
 * @property currentUserName The name of the current user.
 * @property noPlacesFound Whether no places were found.
 */
data class HostDashboardUiState(
    val event: Event? = null,
    val submissionsCount: Int = 0,
    val totalParticipants: Int = 0,
    val attendees: List<String> = emptyList(),
    val status: EventStatus = EventStatus.UNKNOWN,
    val hasVoted: Boolean = false,
    val hasAnyRestaurantVotes: Boolean = false,
    val hasHostSubmittedAvailability: Boolean = false,
    val isInitialLoading: Boolean = true,
    val currentUserName: String = "",
    val noPlacesFound: Boolean = false,
)

/**
 * Represents the state of the close voting operation.
 * This sealed interface defines four possible states:
 * - Idle: No operation is in progress.
 * - Loading: A close voting operation is in progress.
 * - Success: The close voting operation was successful.
 * - Error: An error occurred during the close voting operation.
 * @property error The error that occurred, if any.
 */
sealed interface CloseVotingState {
    object Idle : CloseVotingState

    object Loading : CloseVotingState

    object Success : CloseVotingState

    data class Error(
        val error: Throwable,
    ) : CloseVotingState
}
