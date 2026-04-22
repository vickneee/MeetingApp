package com.meetup.meetingapp.data.model

import java.time.LocalTime

// Available time slot for the event.
data class TimeSlot(
    val start: String = LocalTime.now().toString(),
    val end: String = LocalTime.now().plusHours(1).toString(),
)
