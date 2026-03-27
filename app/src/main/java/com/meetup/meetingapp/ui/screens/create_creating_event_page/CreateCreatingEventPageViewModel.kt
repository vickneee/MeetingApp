package com.meetup.meetingapp.ui.screens.create_creating_event_page

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.meetup.meetingapp.data.repositories.ExampleRepository
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

class CreateCreatingEventPageViewModel(
    private val exampleRepository: ExampleRepository
) : ViewModel() {

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