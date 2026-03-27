package com.meetup.meetingapp.data.model

import java.time.LocalDate

// Available date range for the event.
data class DateRange(
    val start: LocalDate,
    val end: LocalDate
)