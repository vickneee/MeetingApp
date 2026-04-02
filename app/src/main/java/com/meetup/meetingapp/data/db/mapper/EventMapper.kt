package com.meetup.meetingapp.data.db.mapper

import com.google.firebase.Timestamp
import com.google.gson.Gson
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

object EventMapper {

    private val gson = GsonBuilder()
        .registerTypeAdapter(LocalDate::class.java, LocalDateAdapter)
        .create()

    fun Event.toEntity(): EventEntity = EventEntity(
        id = id,
        eventCode = eventCode,
        eventKey = eventKey,
        hostId = hostId,
        status = status.name,
        eventTitle = eventTitle,
        hostName = hostName,
        dateRangeStart = dateRange.start.toEpochDay(),
        dateRangeEnd = dateRange.end.toEpochDay(),
        timeSlotsJson = gson.toJson(timeSlots),
        locationOptionsJson = gson.toJson(locationOptions),
        placeTypeOptionsJson = gson.toJson(placeTypeOptions),
        dateTimeCandidatesJson = gson.toJson(dateTimeCandidates),
        locationCandidatesJson = gson.toJson(locationCandidates),
        foodCategoryCandidatesJson = gson.toJson(foodCategoryCandidates),
        restaurantCandidatesJson = gson.toJson(restaurantCandidates),
        finalTimeJson = finalTime?.let { gson.toJson(it) },
        finalPlace = finalPlace,
        createdAt = createdAt.toDate().time
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
            start = LocalDate.ofEpochDay(dateRangeStart),
            end = LocalDate.ofEpochDay(dateRangeEnd)
        ),
        timeSlots = Gson().fromJson(timeSlotsJson, object : TypeToken<List<TimeSlot>>() {}.type),
        locationOptions = Gson().fromJson(locationOptionsJson, LocationOption::class.java),
        placeTypeOptions = Gson().fromJson(placeTypeOptionsJson, object : TypeToken<List<PlaceType>>() {}.type),
        dateTimeCandidates = Gson().fromJson(dateTimeCandidatesJson, object : TypeToken<List<DateTime>>() {}.type),
        locationCandidates = Gson().fromJson(locationCandidatesJson, object : TypeToken<List<String>>() {}.type),
        foodCategoryCandidates = Gson().fromJson(foodCategoryCandidatesJson, object : TypeToken<List<FoodCategory>>() {}.type),
        restaurantCandidates = Gson().fromJson(restaurantCandidatesJson, object : TypeToken<List<String>>() {}.type),
        finalTime = finalTimeJson?.let { Gson().fromJson(it, DateTime::class.java) },
        finalPlace = finalPlace,
        createdAt = Timestamp(Date(createdAt))
    )
}