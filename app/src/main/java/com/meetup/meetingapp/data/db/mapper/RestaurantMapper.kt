package com.meetup.meetingapp.data.db.mapper

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.meetup.meetingapp.data.db.entities.RestaurantEntity
import com.meetup.meetingapp.data.model.Restaurant

/**
 * Mapper responsible for converting between:
 *
 *  • Domain model: [Restaurant]
 *  • Room entity: [RestaurantEntity]
 *
 * This ensures consistent data flow across:
 *  - Google Places API → Domain
 *  - Firestore → Domain
 *  - Domain → Room (local cache)
 *
 * It also safely handles JSON-encoded fields such as openingHours and types.
 */
object RestaurantMapper {
    private val gson = GsonBuilder().create()

    /**
     * Safely parses a JSON string into the specified type.
     *
     * This helper prevents crashes caused by:
     *  - Null or blank JSON strings
     *  - Malformed JSON returned from Firestore
     *  - Unexpected type mismatches
     *
     * If parsing fails, the provided [default] value is returned.
     *
     * @param json The JSON string to decode.
     * @param default The fallback value if parsing fails.
     */
    private inline fun <reified T> safeFromJson(
        json: String?,
        default: T,
    ): T {
        if (json.isNullOrBlank()) return default
        return runCatching {
            gson.fromJson<T>(json, object : TypeToken<T>() {}.type)
        }.getOrDefault(default)
    }

    /**
     * Converts a domain [Restaurant] into a Room [RestaurantEntity].
     *
     * Responsibilities:
     *  - Adds the eventId (not present in the domain model)
     *  - Serializes list fields (openingHours, types) into JSON strings
     *  - Applies safe defaults for nullable fields
     *
     * This is used when saving restaurant candidates into Room after
     * fetching them from Firestore or the Places API.
     *
     * @param eventId The ID of the event this restaurant belongs to.
     * @return A Room entity ready for database insertion.
     */
    fun Restaurant.toEntity(eventId: String): RestaurantEntity =
        RestaurantEntity(
            placeId = placeId,
            eventId = eventId,
            name = name,
            address = address ?: "",
            searchLocation = searchLocation ?: "",
            latitude = latitude ?: 0.0,
            longitude = longitude ?: 0.0,
            openingHoursJson = gson.toJson(openingHours),
            typesJson = gson.toJson(types),
            photoReference = photoReference,
            priceLevel = priceLevel ?: 0,
            rating = rating ?: 0.0,
            userRatingCount = userRatingCount ?: 0,
        )

    /**
     * Converts a Room [RestaurantEntity] back into a domain [Restaurant].
     *
     * Responsibilities:
     *  - Decodes JSON fields using [safeFromJson]
     *  - Restores the domain model used by ViewModels and UI
     *  - Ensures null-safe values for all fields
     *
     * This is used when reading restaurant data from Room.
     *
     * @return A fully constructed domain [Restaurant].
     */
    fun RestaurantEntity.toDomain(): Restaurant =
        Restaurant(
            placeId = placeId,
            name = name,
            address = address,
            searchLocation = searchLocation,
            latitude = latitude,
            longitude = longitude,
            // Safe JSON decode
            openingHours = safeFromJson(openingHoursJson, emptyList()),
            types = safeFromJson(typesJson, emptyList()),
            photoReference = photoReference,
            priceLevel = priceLevel,
            rating = rating,
            userRatingCount = userRatingCount,
        )
}
