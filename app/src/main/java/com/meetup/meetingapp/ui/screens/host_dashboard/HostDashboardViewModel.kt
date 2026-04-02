package com.meetup.meetingapp.ui.screens.host_dashboard

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meetup.meetingapp.data.model.Event
import com.meetup.meetingapp.data.repositories.EventRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

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

//    // State for the event details as seen in your screenshot
//    var eventCode by mutableStateOf("A7F9K2")
//    var eventTitle by mutableStateOf("Meet & Chat")
//    var hostName by mutableStateOf("Julia")
//
//    var attendees by mutableStateOf(listOf("Alice", "Bob", "Charlie", "Diana"))
//
//    val submissionsCount: Int
//        get() = attendees.size

    /**
     * Placeholder for the "Close Voting" button logic
     */
    fun closeVoting() {
        // Implementation
    }

//    /**
//     * Logic to update data if needed
//     */
//    fun updateEventDetails(code: String, title: String, host: String) {
//        eventCode = code
//        eventTitle = title
//        hostName = host
//    }
}