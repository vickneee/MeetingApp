package com.meetup.meetingapp.ui.screens.host_dashboard

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meetup.meetingapp.data.model.Event
import com.meetup.meetingapp.data.repositories.EventRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

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

    init {
        viewModelScope.launch {
            eventRepository.getEventById(eventId)
                .collect { _event.value = it }
        }
    }

    /**
     * Placeholder for the "Close Voting" button logic
     */
    fun closeVoting() {
        // Implementation
    }
}

