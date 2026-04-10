package com.meetup.meetingapp.network

/**
 * Represents the top‑level response object returned by the
 * Google Places **Text Search API**.
 *
 * A successful response contains:
 *  - `results`: a list of candidate places matching the text query
 *  - `status`: the API status string (e.g., "OK", "ZERO_RESULTS", "OVER_QUERY_LIMIT")
 *
 * This model mirrors the JSON structure documented here:
 * https://developers.google.com/maps/documentation/places/web-service/search-text
 *
 * @property results List of place candidates returned by the query, or null if none.
 * @property status API status string indicating success or error type.
 */
data class PlacesTextSearchResponse(
    val results: List<PlaceItem>?,
    val status: String?
)

/**
 * Represents a single place item returned by the Google Places
 * **Text Search API**.
 *
 * This object contains basic information about a place, including:
 *  - Name
 *  - Address
 *  - Coordinates
 *  - Rating and review count
 *  - Price level
 *  - Place types
 *  - Photo metadata
 *
 * Text Search results are lightweight and often incomplete; the app
 * typically fetches full details using the Place Details API afterward.
 *
 * Example JSON:
 * {
 *   "place_id": "abc123",
 *   "name": "Example Restaurant",
 *   "formatted_address": "Helsinki, Finland",
 *   "rating": 4.5,
 *   "user_ratings_total": 120,
 *   "types": ["restaurant", "food"],
 *   "geometry": { "location": { "lat": 60.17, "lng": 24.94 } }
 * }
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
 */
data class PlaceItem(
    val place_id: String?,
    val name: String?,
    val formatted_address: String?,
    val geometry: Geometry?,
    val rating: Double?,
    val user_ratings_total: Int?,
    val price_level: Int?,
    val types: List<String>?,
    val photos: List<PlacePhoto>?
)



