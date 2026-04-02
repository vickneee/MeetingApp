package com.meetup.meetingapp.data.model

import java.time.LocalDate

/**
 * Available date range for the event.
 *
 * @property start The starting date of the range.
 * @property end The ending date of the range.
 */
data class DateRange(
    val start: String = LocalDate.now().toString(),
    val end: String = LocalDate.now().plusDays(7).toString()
) {
    fun startDate(): LocalDate = LocalDate.parse(start)
    fun endDate(): LocalDate = LocalDate.parse(end)
}
