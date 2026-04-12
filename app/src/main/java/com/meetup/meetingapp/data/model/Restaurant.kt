package com.meetup.meetingapp.data.model

/**
 * Domain model representing a restaurant candidate used in the event voting flow.
 *
 * This model is:
 *  - Loaded from Google Places API
 *  - Saved to Firestore under: events/{eventId}/restaurants/{placeId}
 *  - Synced into Room as RestaurantEntity
 *  - Used by the UI for filtering, display, and voting
 *
 * All fields include safe default values to ensure:
 *  - Firestore deserialization (toObject) never fails
 *  - Room → Domain mapping is stable
 *  - Missing API fields do not crash the app
 *
 * @property placeId Unique Google Places ID for the restaurant.
 * @property name Display name of the restaurant.
 * @property address Full formatted address (maybe null if API omits it).
 * @property rating Average Google rating (0.0–5.0).
 * @property userRatingCount Number of user reviews contributing to the rating.
 * @property priceLevel Google price level (0–4), where higher means more expensive.
 * @property types List of Google Places types (e.g., "cafe", "restaurant").
 * @property latitude Latitude coordinate for map display.
 * @property longitude Longitude coordinate for map display.
 * @property openingHours List of daily opening hours strings as returned by Places API.
 *                       Example: "Monday: 9:00 AM – 8:00 PM"
 *                       Parsed later for filtering by time slot.
 * @property photoReference Google Places photo reference token for loading images.
 */
data class Restaurant(
    val placeId: String = "",
    val name: String = "",
    val address: String? = "",
    val rating: Double? = 0.0,
    val userRatingCount: Int? = 0,
    val priceLevel: Int? = 0,
    val types: List<String>? = emptyList(),
    val latitude: Double? = 0.0,
    val longitude: Double? = 0.0,
    val openingHours: List<String>? = emptyList(),
    val photoReference: String? = ""
)


