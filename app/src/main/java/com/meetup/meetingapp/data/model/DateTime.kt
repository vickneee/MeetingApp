package com.meetup.meetingapp.data.model

import java.time.LocalDate

/**
 * Represents a combination of a specific date and a selected time slot.
 *
 * @property date The calendar date selected by the user.
 * @property timeSlot The specific time slot chosen for that date.
 */
data class DateTime(
    val date: String = LocalDate.now().toString(), // Firestore friendly
    val timeSlot: TimeSlot = TimeSlot()
) {
    fun toLocalDate(): LocalDate = LocalDate.parse(date)
}