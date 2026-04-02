package com.meetup.meetingapp.ui.screens.create_creating_event_page

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

class CreateCreatingEventPageViewModel : ViewModel() {

    var eventTitle by mutableStateOf("")
    var hostName by mutableStateOf("")
    var date by mutableStateOf("")

    fun createEventName(newEventName: String) {
        eventTitle = newEventName
    }

    fun createHostName(newHostName: String) {
        hostName = newHostName
    }

    fun createDate(newDate: String) {
        date = newDate
    }
}