package com.meetup.meetingapp.data.model

// Location selected for the event.
data class LocationOption(
    val country: String = "",
    val region: String = "",
    val cities: List<String> = listOf()
)