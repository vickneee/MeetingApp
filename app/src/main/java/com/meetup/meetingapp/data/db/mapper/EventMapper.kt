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
import com.meetup.meetingapp.data.model.FoodCategory
import com.meetup.meetingapp.data.model.LocationOption
import com.meetup.meetingapp.data.model.PlaceType
import com.meetup.meetingapp.data.model.TimeSlot
import java.time.LocalDate
import java.util.Date
import java.text.SimpleDateFormat
import java.util.Locale

object EventMapper {

    private val gson = GsonBuilder()
        .registerTypeAdapter(LocalDate::class.java, LocalDateAdapter)
        .create()

    private val displayDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

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

        // Timestamps
        createdAt = createdAt.toDate().time,
        createdAtString = displayDateFormat.format(createdAt.toDate())
    )

    fun EventEntity.toDomain(): Event = Event(
        id = id,
        eventCode = eventCode,
        eventKey = eventKey,
        hostId = hostId,
        status = EventStatus.valueOf(status),
        eventTitle = eventTitle,
        hostName = hostName,
        dateRange = DateRange(
            start = dateRangeStartString, // use string for easier display
            end = dateRangeEndString
        ),
        timeSlots = gson.fromJson(timeSlotsJson, object : TypeToken<List<TimeSlot>>() {}.type),
        locationOptions = gson.fromJson(locationOptionsJson, LocationOption::class.java),
        placeTypeOptions = gson.fromJson(placeTypeOptionsJson, object : TypeToken<List<PlaceType>>() {}.type),
        dateTimeCandidates = gson.fromJson(dateTimeCandidatesJson, object : TypeToken<List<DateTime>>() {}.type),
        locationCandidates = gson.fromJson(locationCandidatesJson, object : TypeToken<List<String>>() {}.type),
        foodCategoryCandidates = gson.fromJson(foodCategoryCandidatesJson, object : TypeToken<List<FoodCategory>>() {}.type),
        restaurantCandidates = gson.fromJson(restaurantCandidatesJson, object : TypeToken<List<String>>() {}.type),
        finalTime = finalTimeJson?.let { gson.fromJson(it, DateTime::class.java) },
        finalPlace = finalPlace,
        createdAt = Timestamp(Date(createdAt.takeIf { it > 0 } ?: System.currentTimeMillis()))
    )
}
