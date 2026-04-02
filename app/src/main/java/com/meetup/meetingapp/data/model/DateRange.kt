package com.meetup.meetingapp.data.model

import java.time.LocalDate

/**
 * Available date range for the event.
 *
 * @property start The starting date of the range.
 * @property end The ending date of the range.
 */
data class DateRange(
    val start: LocalDate = LocalDate.now(),
    val end: LocalDate = LocalDate.now().plusDays(7)
)
