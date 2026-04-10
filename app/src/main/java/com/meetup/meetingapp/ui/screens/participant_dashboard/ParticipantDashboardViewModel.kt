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
            eventRepository.observeEventById(eventId).collect { event ->
                _event.value = event
            }
        }
        viewModelScope.launch {
            eventRepository.observeSubmissions(eventId).collect { submissions ->
                _uiState.value = _uiState.value.copy(
                    submissionsCount = submissions.size,
                    attendees = submissions.map { it.name }
                )
            }
        }
    }
}

data class ParticipantDashboardUiState(
    val submissionsCount: Int = 0,
    val attendees: List<String> = emptyList()
)