package com.meetup.meetingapp.data.db.mapper

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.meetup.meetingapp.data.db.entities.RestaurantEntity
import com.meetup.meetingapp.data.model.Restaurant

object RestaurantMapper {

    private val gson = GsonBuilder().create()

    // Safe JSON parse helper — prevents crashes on malformed/empty JSON
    private inline fun <reified T> safeFromJson(json: String?, default: T): T {
        if (json.isNullOrBlank()) return default
        return runCatching {
            gson.fromJson<T>(json, object : TypeToken<T>() {}.type)
        }.getOrDefault(default)
    }


    fun Restaurant.toEntity(eventId: String): RestaurantEntity = RestaurantEntity(
        placeId = placeId,
        eventId = eventId,

        name = name,
        address = address ?: "",
        latitude = latitude ?: 0.0,
        longitude = longitude ?: 0.0,

        openingHoursJson = gson.toJson(openingHours),
        typesJson = gson.toJson(types),

        photoReference = photoReference,
        priceLevel = priceLevel ?: 0,
        rating = rating ?: 0.0,
        userRatingCount = userRatingCount ?: 0
    )



    fun RestaurantEntity.toDomain(): Restaurant = Restaurant(
        placeId = placeId,
        name = name,
        address = address,
        latitude = latitude,
        longitude = longitude,

        // Safe JSON decode
        openingHours = safeFromJson(openingHoursJson, emptyList()),
        types = safeFromJson(typesJson, emptyList()),

        photoReference = photoReference,
        priceLevel = priceLevel,
        rating = rating,
        userRatingCount = userRatingCount
    )
}