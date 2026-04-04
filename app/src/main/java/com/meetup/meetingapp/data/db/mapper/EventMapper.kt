package com.meetup.meetingapp.data.db.mapper

import com.google.firebase.Timestamp
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.meetup.meetingapp.data.db.converter.LocalDateAdapter
import com.meetup.meetingapp.data.db.entities.EventEntity
import com.meetup.meetingapp.data.model.DateRange
import com.meetup.meetingapp.data.model.DateTime
import com.meetup.meetingapp.data.model.Event
import com.meetup.meetingapp.data.model.EventStatus
import com.meetup.meetingapp.data.model.LocationOption
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.Date
import java.util.Locale

object EventMapper {

    private val gson = GsonBuilder()
        .registerTypeAdapter(LocalDate::class.java, LocalDateAdapter)
        .create()

    private val displayDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    // Safe JSON parse helper — prevents crashes on malformed/empty JSON
    private inline fun <reified T> safeFromJson(json: String?, default: T): T {
        if (json.isNullOrBlank()) return default
        return runCatching {
            gson.fromJson<T>(json, object : TypeToken<T>() {}.type)
        }.getOrDefault(default)
    }

    fun Event.toEntity(): EventEntity = EventEntity(
        id = id,
        eventCode = eventCode,
        eventKey = eventKey,
        hostId = hostId,
        status = status.name,
        eventTitle = eventTitle,
        hostName = hostName,

        // Store string for Room
        dateRangeStartString = dateRange.start,
        dateRangeEndString = dateRange.end,

        // Lists → JSON
        timeSlotsJson = gson.toJson(timeSlots),
        locationOptionsJson = gson.toJson(locationOptions),
        placeTypeOptionsJson = gson.toJson(placeTypeOptions),
        dateTimeCandidatesJson = gson.toJson(dateTimeCandidates),
        locationCandidatesJson = gson.toJson(locationCandidates),
        foodCategoryCandidatesJson = gson.toJson(foodCategoryCandidates),
        restaurantCandidatesJson = gson.toJson(restaurantCandidates),

        // Final selections
        finalTimeJson = finalTime?.let { gson.toJson(it) },
        finalPlace = finalPlace,

        // Timestamps — store both millis and display string
        createdAt = createdAt.toDate().time,
        createdAtString = displayDateFormat.format(createdAt.toDate())
    )

    fun EventEntity.toDomain(): Event = Event(
        id = id,
        eventCode = eventCode,
        eventKey = eventKey,
        hostId = hostId,
        // Safe enum parse — falls back to UNKNOWN if value is corrupted
        status = runCatching { EventStatus.valueOf(status) }.getOrDefault(EventStatus.UNKNOWN),
        eventTitle = eventTitle,
        hostName = hostName,
        dateRange = DateRange(
            start = dateRangeStartString,
            end = dateRangeEndString
        ),
        // All JSON fields use safeFromJson — no silent crashes
        timeSlots = safeFromJson(timeSlotsJson, emptyList()),
        locationOptions = safeFromJson(locationOptionsJson, LocationOption()),
        placeTypeOptions = safeFromJson(placeTypeOptionsJson, emptyList()),
        dateTimeCandidates = safeFromJson(dateTimeCandidatesJson, emptyList()),
        locationCandidates = safeFromJson(locationCandidatesJson, emptyList()),
        foodCategoryCandidates = safeFromJson(foodCategoryCandidatesJson, emptyList()),
        restaurantCandidates = safeFromJson(restaurantCandidatesJson, emptyList()),
        // finalTime is nullable — safe to keep ?.let pattern
        finalTime = finalTimeJson?.let {
            runCatching { gson.fromJson(it, DateTime::class.java) }.getOrNull()
        },
        finalPlace = finalPlace,
        // Fallback to current time if stored millis is invalid
        createdAt = Timestamp(Date(createdAt.takeIf { it > 0 } ?: System.currentTimeMillis()))
    )
}