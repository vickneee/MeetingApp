package com.meetup.meetingapp.data.model

// Types of places available for the event.
enum class PlaceType(
    val queryName: String,
) {
    CAFE("cafe"),
    RESTAURANT("restaurant"),
    BAR("bar"),
}
