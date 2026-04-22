package com.meetup.meetingapp.data.model

import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

/**
 * Represents a combination of a specific date and a selected time slot.
 *
 * @property date The calendar date selected by the user.
 * @property timeSlot The specific time slot chosen for that date.
 */
data class DateTime(
    val date: String = LocalDate.now().toString(), // Firestore friendly
    val timeSlot: TimeSlot = TimeSlot(),
) {
    fun toLocalDate(): LocalDate = LocalDate.parse(date)

    /**
     * Converts a DateTime to a human-readable display label.
     * Example: "Nov 1 (12:00–13:00)"
     */
    fun toDisplayLabel(): String =
        try {
            val localDate = LocalDate.parse(this.date)
            val month = localDate.month.getDisplayName(TextStyle.SHORT, Locale.ENGLISH)
            "$month ${localDate.dayOfMonth} (${timeSlot.start}–${timeSlot.end})"
        } catch (_: Exception) {
            "${this.date} (${timeSlot.start}–${timeSlot.end})"
        }

    /**
     * Converts a DateTime to a string for storage in Firestore.
     * Format: "yyyy-MM-dd|start-end"
     * @return String representation of the DateTime.
     */
    fun toSerializableString(): String = "$date|${timeSlot.start}-${timeSlot.end}"
}
