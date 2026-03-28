package com.meetup.meetingapp.ui.screens.host_dashboard

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.meetup.meetingapp.data.repositories.ExampleRepository

class HostDashboardViewModel(private val exampleRepository: ExampleRepository) : ViewModel() {

    // State for the event details as seen in your screenshot
    var eventCode by mutableStateOf("A7F9K2")
    var eventTitle by mutableStateOf("Meet & Chat")
    var hostName by mutableStateOf("Julia")

    var attendees by mutableStateOf(listOf("Alice", "Bob", "Charlie", "Diana"))

    val submissionsCount: Int
        get() = attendees.size

    /**
     * Placeholder for the "Close Voting" button logic
     */
    fun closeVoting() {
        // Implementation
    }

    /**
     * Logic to update data if needed
     */
    fun updateEventDetails(code: String, title: String, host: String) {
        eventCode = code
        eventTitle = title
        hostName = host
    }
}