package com.meetup.meetingapp.ui.screens.participantdashboard

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.meetup.meetingapp.NOTIFICATION_TITLE_PARTICIPANTS
import com.meetup.meetingapp.R
import com.meetup.meetingapp.data.model.Event
import com.meetup.meetingapp.data.model.EventStatus
import com.meetup.meetingapp.data.repositories.EventRepository
import com.meetup.meetingapp.utils.makeEventFinalizedNotification
import com.meetup.meetingapp.utils.makeSubmissionReminderNotification
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for the Participant Dashboard screen.
 *
 * This ViewModel is responsible for:
 * - Loading the event data for the given eventId.
 * - Observing real-time updates to the event from Firestore (via the repository).
 * - Syncing participant submissions from Firestore into the local Room database.
 * - Exposing UI state such as submission count, attendee names, and event status.
 * - Firing a local notification when the host starts the Place Voting round.
 *
 * @param application The application context for accessing resources and permissions.
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
    application: Application,
    private val eventRepository: EventRepository,
    savedStateHandle: SavedStateHandle,
) : AndroidViewModel(application) {
    /**
     * The ID of the event to load.
     */
    private val eventId: String = savedStateHandle[ParticipantDashboardDestination.EVENT_ID_ARG] ?: ""

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

    /** Tracks the previous event status to detect transitions. */
    private var lastStatus: EventStatus? = null

    /**
     * Initializes the ViewModel by:
     * 1. Observing the event from Firestore and updating the Room cache.
     * 2. Observing submissions from Firestore and updating the Room cache.
     * 3. Observing restaurant votes for the second round.
     */
    init {
        viewModelScope.launch {
            val eventFlow = eventRepository.observeEventById(eventId)
            val submissionsFlow = eventRepository.observeSubmissions(eventId)
            val votesFlow = eventRepository.observeRestaurantVotes(eventId)

            combine(eventFlow, submissionsFlow, votesFlow) { eventData, submissions, votes ->
                _event.value = eventData

                eventData?.let { e ->
                    // Notification Logic: Detect transition to Place Voting
                    if (lastStatus != null &&
                        lastStatus != EventStatus.COLLECTING_RESTAURANT_VOTES &&
                        e.status == EventStatus.COLLECTING_RESTAURANT_VOTES
                    ) {
                        triggerPlaceVotingNotification()
                    }

                    // Notification Logic: Detect transition to Finalized
                    if (lastStatus != null &&
                        lastStatus != EventStatus.FINALIZED &&
                        e.status == EventStatus.FINALIZED
                    ) {
                        triggerEventFinalizedNotification()
                    }

                    lastStatus = e.status

                    val currentName =
                        if (e.hostId == userId) {
                            e.hostName
                        } else {
                            submissions.find { it.userId == userId }?.name ?: ""
                        }

                    val isSecondRound =
                        e.status == EventStatus.COLLECTING_RESTAURANT_VOTES ||
                            e.status == EventStatus.FINALIZED

                    val availabilityCount = submissions.size
                    val votesCount = votes.distinctBy { it.userId }.size

                    val count = if (isSecondRound) votesCount else availabilityCount
                    val total = if (isSecondRound) availabilityCount else 0

                    val hasSubmittedAvailability = submissions.any { it.userId == userId }
                    val hasVoted = votes.any { it.userId == userId }

                    val names =
                        if (isSecondRound) {
                            votes.distinctBy { it.userId }.map { it.userName }
                        } else {
                            submissions.map { it.name }
                        }

                    _uiState.update {
                        it.copy(
                            status = e.status,
                            submissionsCount = count,
                            attendees = names,
                            currentUserName = currentName.ifEmpty { it.currentUserName },
                            totalParticipants = total,
                            hasSubmittedAvailability = hasSubmittedAvailability,
                            hasVoted = hasVoted,
                        )
                    }

                    // Side effects
                    if (e.status == EventStatus.CREATED) {
                        viewModelScope.launch {
                            eventRepository.updateEventStatus(e.id, EventStatus.COLLECTING_AVAILABILITY)
                        }
                    }
                }
            }.collect {}
        }

        // Background fetch for user name to ensure it's available for "You can now vote"
        fetchCurrentUserName()
    }

    /**
     * Fires a local notification notifying the participant that voting has started.
     */
    private fun triggerPlaceVotingNotification() {
        val context = getApplication<Application>().applicationContext
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        ) {
            makeSubmissionReminderNotification(
                title = NOTIFICATION_TITLE_PARTICIPANTS,
                message = context.getString(R.string.place_voting_started),
                context,
            )
        }
    }

    /**
     * Fires a local notification notifying the host that the event has been finalized.
     */
    private fun triggerEventFinalizedNotification() {
        val context = getApplication<Application>().applicationContext
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        ) {
            makeEventFinalizedNotification(
                message = context.getString(R.string.event_finalized_message),
                context = context
            )
        }
    }

    /**
     * Fetches the current user's name from their initial submission or event host data.
     */
    private fun fetchCurrentUserName() {
        viewModelScope.launch {
            val response = eventRepository.getParticipantResponse(eventId, userId)
            if (response != null) {
                _uiState.update { it.copy(currentUserName = response.name) }
            } else {
                val eventDoc = eventRepository.getEventById(eventId).first()
                if (eventDoc?.hostId == userId) {
                    _uiState.update { it.copy(currentUserName = eventDoc.hostName) }
                }
            }
        }
    }

    /**
     * Fetches the user's vote status for the current event.
     */
    fun fetchUserVote() {
        viewModelScope.launch(Dispatchers.IO) {
            val currentEvent = _event.value ?: return@launch
            val hasVoted =
                eventRepository.hasUserVotedInEvent(
                    eventId = eventId,
                    userId = userId,
                    timings = currentEvent.dateTimeCandidates,
                )
            _uiState.update { it.copy(hasVoted = hasVoted) }
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
 * @property currentUserName The name of the current user.
 * @property totalParticipants The total number of participants expected.
 * @property hasSubmittedAvailability Whether the current user has submitted availability.
 */
data class ParticipantDashboardUiState(
    val submissionsCount: Int = 0,
    val attendees: List<String> = emptyList(),
    val status: EventStatus = EventStatus.UNKNOWN,
    val hasVoted: Boolean = false,
    val currentUserName: String = "",
    val totalParticipants: Int = 0,
    val hasSubmittedAvailability: Boolean = false,
)
