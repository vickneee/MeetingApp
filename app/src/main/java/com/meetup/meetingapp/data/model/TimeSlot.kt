package com.meetup.meetingapp.data.model

import java.time.LocalTime

// Available time slot for the event.
data class TimeSlot(
    val start: LocalTime,
    val end: LocalTime
)