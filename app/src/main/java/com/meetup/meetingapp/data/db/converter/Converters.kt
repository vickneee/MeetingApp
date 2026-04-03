package com.meetup.meetingapp.data.db.converter

import androidx.room.TypeConverter
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.meetup.meetingapp.data.model.DateTime
import com.meetup.meetingapp.data.model.FoodCategory
import com.meetup.meetingapp.data.model.LocationOption
import com.meetup.meetingapp.data.model.PlaceType
import com.meetup.meetingapp.data.model.TimeSlot
import java.time.LocalDate

class Converters {

    private val gson = GsonBuilder()
        .registerTypeAdapter(LocalDate::class.java, LocalDateAdapter)
        .create()

    @TypeConverter
    fun fromTimeSlotList(list: List<TimeSlot>): String = gson.toJson(list)

    @TypeConverter
    fun toTimeSlotList(json: String): List<TimeSlot> =
        gson.fromJson(json, object : TypeToken<List<TimeSlot>>() {}.type)

    @TypeConverter
    fun fromDateTimeList(list: List<DateTime>): String = gson.toJson(list)

    @TypeConverter
    fun toDateTimeList(json: String): List<DateTime> =
        gson.fromJson(json, object : TypeToken<List<DateTime>>() {}.type)

    @TypeConverter
    fun fromPlaceTypeList(list: List<PlaceType>): String = gson.toJson(list)

    @TypeConverter
    fun toPlaceTypeList(json: String): List<PlaceType> =
        gson.fromJson(json, object : TypeToken<List<PlaceType>>() {}.type)

    @TypeConverter
    fun fromFoodCategoryList(list: List<FoodCategory>): String = gson.toJson(list)

    @TypeConverter
    fun toFoodCategoryList(json: String): List<FoodCategory> =
        gson.fromJson(json, object : TypeToken<List<FoodCategory>>() {}.type)

    @TypeConverter
    fun fromLocationOption(option: LocationOption): String = gson.toJson(option)

    @TypeConverter
    fun toLocationOption(json: String): LocationOption =
        gson.fromJson(json, LocationOption::class.java)

    @TypeConverter
    fun fromDateTime(dateTime: DateTime?): String? =
        dateTime?.let { gson.toJson(it) }

    @TypeConverter
    fun toDateTime(json: String?): DateTime? =
        json?.let { gson.fromJson(it, DateTime::class.java) }

    @TypeConverter
    fun fromList(value: List<String>): String {
        return value.joinToString(",")
    }

    @TypeConverter
    fun toList(value: String): List<String> {
        if (value.isEmpty()) return emptyList()
        return value.split(",")
    }
}