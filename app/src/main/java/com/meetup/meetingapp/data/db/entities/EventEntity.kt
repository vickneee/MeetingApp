package com.meetup.meetingapp.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents an event entity in the Room database.
 *
 * Room acts as the single source of truth for event data in the app.
 * @property id The unique identifier of the event.
 * @property eventCode The unique identifier of the event.
 * @property eventKey The unique identifier of the event.
 * @property hostId The unique identifier of the event.
 * @property status The unique identifier of the event.
 * @property eventTitle The unique identifier of the event.
 * @property hostName The unique identifier of the event.
 * @property dateRangeStartString The unique identifier of the event.
 * @property dateRangeEndString The unique identifier of the event.
 * @property timeSlotsJson The unique identifier of the event.
 * @property locationOptionsJson The unique identifier of the event.
 * @property placeTypeOptionsJson The unique identifier of the event.
 * @property dateTimeCandidatesJson The unique identifier of the event.
 * @property locationCandidatesJson The unique identifier of the event.
 * @property foodCategoryCandidatesJson The unique identifier of the event.
 * @property restaurantCandidatesJson The unique identifier of the event.
 * @property placeTypeCandidatesJson The unique identifier of the event.
 * @property finalTimeJson The unique identifier of the event.
 * @property finalPlace The unique identifier of the event.
 * @property createdAt The unique identifier of the event.
 * @property createdAtString The unique identifier of the event.
 */
@Entity(tableName = "events")
data class EventEntity(
    @PrimaryKey val id: String,
    val eventCode: String,
    val eventKey: String,
    val hostId: String,
    val status: String, // EventStatus.name
    val eventTitle: String,
    val hostName: String,
    val dateRangeStartString: String, // ← for display in Room
    val dateRangeEndString: String, // ← for display in Room
    // Complex lists → JSON strings (via Converter)
    val timeSlotsJson: String,
    val locationOptionsJson: String,
    val placeTypeOptionsJson: String,
    val dateTimeCandidatesJson: String,
    val locationCandidatesJson: String,
    val foodCategoryCandidatesJson: String,
    val restaurantCandidatesJson: String,
    val placeTypeCandidatesJson: String,
    // Final selections → JSON strings
    val finalTimeJson: String?,
    val finalPlace: String?,
    val createdAt: Long, // Timestamp → Long (epoch millis)
    val createdAtString: String? = null, // optional formatted string
)
