package com.meetup.meetingapp.network

/**
 * Represents the top‑level response object returned by the
 * Google Places **Place Details API**.
 *
 * A successful response contains:
 *  - `result`: the detailed information for the requested place
 *  - `status`: the API status string (e.g., "OK", "ZERO_RESULTS", "INVALID_REQUEST")
 *
 * This model mirrors the JSON structure documented here:
 * https://developers.google.com/maps/documentation/places/web-service/details
 *
 * @property result The detailed place information, or null if the request failed.
 * @property status The API status string indicating success or error type.
 */
data class PlaceDetailsResponse(
    val result: PlaceDetailsResult?,
    val status: String?
)

/**
 * Represents the detailed information for a single place returned by
 * the Google Places **Place Details API**.
 *
 * This object contains richer data than Text Search results, including:
 *  - Full address
 *  - Coordinates
 *  - Opening hours
 *  - Price level
 *  - Ratings and review count
 *  - Place types
 *  - Photo metadata
 *
 * All fields are nullable because the Places API may omit fields depending
 * on availability, permissions, or the requested `fields` parameter.
 *
 * Example fields returned:
 *   "name": "Ravintola Example",
 *   "formatted_address": "Helsinki, Finland",
 *   "geometry": { "location": { "lat": 60.17, "lng": 24.94 } },
 *   "opening_hours": { "weekday_text": [...] }
 *
 * @property place_id Unique Google Places ID for the place.
 * @property name Display name of the place.
 * @property formatted_address Full formatted address string.
 * @property geometry Geographic coordinates (lat/lng).
 * @property rating Average user rating (0.0–5.0).
 * @property user_ratings_total Total number of user reviews.
 * @property price_level Google price level (0–4).
 * @property types List of Google Places types describing the place.
 * @property photos List of photo metadata objects (not actual images).
 * @property opening_hours Human‑readable opening hours for each weekday.
 */
data class PlaceDetailsResult(
    val place_id: String?,
    val name: String?,
    val formatted_address: String?,
    val geometry: Geometry?,
    val rating: Double?,
    val user_ratings_total: Int?,
    val price_level: Int?,
    val types: List<String>?,
    val photos: List<PlacePhoto>?,
    val opening_hours: OpeningHours?
)
