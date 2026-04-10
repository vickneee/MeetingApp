package com.meetup.meetingapp.data.model

import java.time.LocalDate
import java.time.format.DateTimeParseException

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
    fun startDate(): LocalDate = try {
        if (start.isBlank()) LocalDate.now() else LocalDate.parse(start)
    } catch (e: DateTimeParseException) {
        LocalDate.now()
    }

    fun endDate(): LocalDate = try {
        if (end.isBlank()) LocalDate.now().plusDays(7) else LocalDate.parse(end)
    } catch (e: DateTimeParseException) {
        LocalDate.now().plusDays(7)
    }
}
