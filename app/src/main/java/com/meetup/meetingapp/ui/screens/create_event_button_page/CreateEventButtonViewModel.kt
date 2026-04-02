package com.meetup.meetingapp.ui.screens.create_event_button_page

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class CreateEventButtonViewModel : ViewModel() {

    // State for the checkboxes
    var restaurant by mutableStateOf(false)
    var cafe by mutableStateOf(false)
    var bar by mutableStateOf(false)

    fun updateRestaurant(selected: Boolean) {
        restaurant = selected
    }

    fun updateCafe(selected: Boolean) {
        cafe = selected
    }

    fun updateBar(selected: Boolean) {
        bar = selected
    }

    fun createEvent() {
        println("Event created with Restaurant: $restaurant, Cafe: $cafe, Bar: $bar")
    }
}