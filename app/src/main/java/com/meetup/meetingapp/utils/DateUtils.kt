package com.meetup.meetingapp.utils

import com.meetup.meetingapp.data.model.DateTime
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale


/**
 * Converts a date string (yyyy-MM-dd) to European format (dd.MM.yyyy).
 */
fun String.toEuroDate(): String {
    return try {
        val date = LocalDate.parse(this)
        val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
        date.format(formatter)
    } catch (_: Exception) {
        this
    }
}

/**
 * Converts a DateTime to a human-readable display label.
 * Example: "Nov 1 (12:00–13:00)"
 */
fun DateTime.toDisplayLabel(): String {
    return try {
        val localDate = LocalDate.parse(this.date)
        val month = localDate.month.getDisplayName(TextStyle.SHORT, Locale.ENGLISH)
        "$month ${localDate.dayOfMonth} (${timeSlot.start}–${timeSlot.end})"
    } catch (_: Exception) {
        "${this.date} (${timeSlot.start}–${timeSlot.end})"
    }
}
