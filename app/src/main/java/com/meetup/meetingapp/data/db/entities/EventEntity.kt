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

    // DateRange flattened
    val dateRangeStart: Long,        // LocalDate → epoch day
    val dateRangeEnd: Long,

    // Complex lists → JSON strings (via Converter)
    val timeSlotsJson: String,
    val locationOptionsJson: String,
    val placeTypeOptionsJson: String,
    val dateTimeCandidatesJson: String,
    val locationCandidatesJson: String,
    val foodCategoryCandidatesJson: String,
    val restaurantCandidatesJson: String,

    // Final selections → JSON strings
    val finalTimeJson: String?,
    val finalPlace: String?,

    val createdAt: Long              // Timestamp → Long (epoch millis)
)