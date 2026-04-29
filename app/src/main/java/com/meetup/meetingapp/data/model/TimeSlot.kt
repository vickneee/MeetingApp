package com.meetup.meetingapp.data.model

import java.time.LocalTime

/**
 * Represents an available time slot for an event.
 *
 * The time values are stored as ISO‑8601 formatted strings (e.g., "12:00").
 * By default,
 * - [start] is set to the current local time.
 * - [end] is set to one hour after the current local time.
 *
 * This model is used when defining selectable time ranges for event scheduling.
 *
 * @property start The starting time of the slot, as a string.
 * @property end The ending time of the slot, as a string.
 */
data class TimeSlot(
    val start: String = LocalTime.now().toString(),
    val end: String = LocalTime.now().plusHours(1).toString(),
)
