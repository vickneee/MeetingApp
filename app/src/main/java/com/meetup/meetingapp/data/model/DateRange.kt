package com.meetup.meetingapp.data.model

import java.time.LocalDate
import java.time.format.DateTimeParseException

/**
 * Represents the available date range for an event.
 *
 * This model stores the start and end dates as ISO‑8601 strings (e.g., "2024‑05‑01").
 * It also provides safe parsing helpers that convert these strings into [LocalDate]
 * while gracefully handling invalid or blank values.
 *
 * Default values:
 * - [start] defaults to today's date.
 * - [end] defaults to 7 days after today.
 *
 * @property start The starting date of the range, stored as a string.
 * @property end The ending date of the range, stored as a string.
 */
data class DateRange(
    val start: String = LocalDate.now().toString(),
    val end: String = LocalDate.now().plusDays(7).toString(),
) {
    /**
     * Parses [start] into a [LocalDate].
     *
     * Returns today's date if:
     * - the string is blank, or
     * - parsing fails due to an invalid format.
     */
    fun startDate(): LocalDate =
        try {
            if (start.isBlank()) LocalDate.now() else LocalDate.parse(start)
        } catch (_: DateTimeParseException) {
            LocalDate.now()
        }

    /**
     * Parses [end] into a [LocalDate].
     *
     * Returns 7 days from today if:
     * - the string is blank, or
     * - parsing fails due to an invalid format.
     */
    fun endDate(): LocalDate =
        try {
            if (end.isBlank()) LocalDate.now().plusDays(7) else LocalDate.parse(end)
        } catch (_: DateTimeParseException) {
            LocalDate.now().plusDays(7)
        }
}
