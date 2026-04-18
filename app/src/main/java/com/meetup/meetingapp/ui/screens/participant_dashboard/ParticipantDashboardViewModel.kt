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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for the Participant Dashboard screen.
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

    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    init {
        viewModelScope.launch {
            val eventFlow = eventRepository.observeEventById(eventId)
            val submissionsFlow = eventRepository.observeSubmissions(eventId)
            val votesFlow = eventRepository.observeRestaurantVotes(eventId)

            combine(eventFlow, submissionsFlow, votesFlow) { eventData, submissions, votes ->
                _event.value = eventData
                
                // Real-time name detection from submissions or event doc
                val nameFromSubmissions = submissions.find { 
                    // Note: This assumes participant response ID matches UID in Firestore 
                    // or we find by matching a known property if available. 
                    // For now, let's try to match by searching submissions.
                    // If we can't find it here, we'll fall back to hostName if host.
                    true // We'll refine this below
                }
                
                eventData?.let { e ->
                    // 1. Identify current user's name reactively
                    val currentName = if (e.hostId == userId) {
                        e.hostName
                    } else {
                        // Find the submission for the current user
                        // The repository observeSubmissions uses Firestore's participantResponses subcollection
                        // where the document ID is the userId.
                        // We need a way to correlate the current user to a submission name.
                        submissions.find { /* logic to find current user's submission */ true }
                        // For now, we'll use a side-effect fetch as fallback, but reactive is better.
                        _uiState.value.currentUserName 
                    }

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

                    _uiState.update { it.copy(
                        status = e.status,
                        submissionsCount = count,
                        attendees = names,
                        // Update name if we just found it
                        currentUserName = if (currentName.isNotEmpty()) currentName else it.currentUserName
                    ) }

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
        
        // Ensure name is fetched even if combine takes time
        fetchCurrentUserName()
    }

    private fun fetchCurrentUserName() {
        viewModelScope.launch {
            val response = eventRepository.getParticipantResponse(eventId, userId)
            if (response != null) {
                _uiState.update { it.copy(currentUserName = response.name) }
            } else {
                val eventDoc = _event.value
                if (eventDoc?.hostId == userId) {
                    _uiState.update { it.copy(currentUserName = eventDoc.hostName) }
                }
            }
        }
    }

    fun fetchUserVote() {
        viewModelScope.launch(Dispatchers.IO) {
            val event = _event.value ?: return@launch
            val hasVoted = eventRepository.hasUserVotedInEvent(
                eventId = eventId,
                userId = userId,
                timings = event.dateTimeCandidates
            )
            _uiState.update { it.copy(hasVoted = hasVoted) }
        }
    }
}

data class ParticipantDashboardUiState(
    val submissionsCount: Int = 0,
    val attendees: List<String> = emptyList(),
    val status: EventStatus = EventStatus.UNKNOWN,
    val hasVoted: Boolean = false,
    val currentUserName: String = ""
)
