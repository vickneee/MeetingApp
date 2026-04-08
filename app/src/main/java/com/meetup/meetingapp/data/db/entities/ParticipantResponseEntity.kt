package com.meetup.meetingapp.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents an event entity in the Room database.
 * @property id The unique identifier of the event.
 * @property eventId The unique identifier of the event.
 * @property name The name of the event.
 * @property dateTimes The date and time range of the event.
 * @property locations The locations where the event takes place.
 * @property placeTypes The types of places where the event takes place.
 * @property foodCategories The food categories that can be consumed at the event.
 */
@Entity(tableName = "participant_responses")
data class ParticipantResponseEntity(
    @PrimaryKey
    val id: String,
    val eventId: String,
    val name: String,
    val dateTimes: String,
    val locations: String,
    val placeTypes: String,
    val foodCategories: String
)