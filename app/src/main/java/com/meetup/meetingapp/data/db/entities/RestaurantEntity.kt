package com.meetup.meetingapp.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity representing a restaurant candidate stored locally for an event.
 *
 * This entity is the local persistence layer version of the domain model [Restaurant].
 * It is populated by:
 *   • Firestore → Repository → Room (syncRestaurants)
 *   • Places API → Repository → Firestore → Room
 *
 * Room acts as the single source of truth for restaurant data in the app.
 *
 * Notes:
 *  - Complex fields such as openingHours and types are stored as JSON strings
 *    (openingHoursJson, typesJson) and decoded in the mapper.
 *  - eventId is required because each event has its own set of restaurant candidates.
 *
 * @property placeId Unique Google Places ID (primary key).
 * @property eventId ID of the event this restaurant belongs to.
 * @property name Restaurant display name.
 * @property address Full formatted address.
 * @property latitude Latitude coordinate for map display.
 * @property longitude Longitude coordinate for map display.
 * @property openingHoursJson JSON-encoded list of opening hours strings.
 * @property typesJson JSON-encoded list of Google Places types.
 * @property photoReference Google Places photo reference token.
 * @property priceLevel Google price level (0–4).
 * @property rating Average Google rating (0.0–5.0).
 * @property userRatingCount Number of user reviews.
 */
@Entity(tableName = "restaurants")
data class RestaurantEntity(

    @PrimaryKey
    val placeId: String,

    val eventId: String,

    val name: String,
    val address: String,

    val latitude: Double,
    val longitude: Double,

    val openingHoursJson: String,
    val typesJson: String?,

    val photoReference: String?,
    val priceLevel: Int,
    val rating: Double,
    val userRatingCount: Int
)
