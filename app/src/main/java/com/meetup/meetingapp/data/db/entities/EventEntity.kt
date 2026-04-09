package com.meetup.meetingapp.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "events")
data class EventEntity(
    @PrimaryKey val id: String,
    val eventCode: String,
    val eventKey: String,
    val hostId: String,
    val status: String,              // EventStatus.name
    val eventTitle: String,
    val hostName: String,

    val dateRangeStartString: String, // ← for display in Room
    val dateRangeEndString: String,   // ← for display in Room

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

    val createdAt: Long,              // Timestamp → Long (epoch millis)
    val createdAtString: String? = null // optional formatted string
)