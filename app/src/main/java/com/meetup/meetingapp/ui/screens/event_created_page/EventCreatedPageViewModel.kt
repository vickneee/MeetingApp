package com.meetup.meetingapp.ui.screens.event_created_page

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class EventCreatedViewModel : ViewModel() {

    var eventCode by mutableStateOf("A7F9K2")
    var eventKey by mutableStateOf("1234")

    fun updateEventDetails(code: String, key: String) {
        eventCode = code
        eventKey = key
    }
}