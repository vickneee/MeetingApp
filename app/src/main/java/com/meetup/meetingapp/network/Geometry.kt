package com.meetup.meetingapp.network

/**
 * Represents the geometry section of a Google Places API response.
 *
 * The Geometry object typically contains the geographic coordinates
 * of a place (latitude and longitude). Only the `location` field is
 * used in this app, as viewport and bounds are not required.
 *
 * This model mirrors the structure returned by:
 *   - Places Text Search API
 *   - Places Details API
 *
 * @property location A [LatLngLiteral] containing the latitude and longitude
 *                    of the place. May be null if the API omits it.
 */
data class Geometry(
    val location: LatLngLiteral?
)

/**
 * Represents a latitude/longitude pair returned by the Google Places API.
 *
 * This is the standard coordinate format used by Google Maps Platform.
 * Both values are nullable because some API responses may omit them.
 *
 * Example JSON:
 *   "location": { "lat": 60.1699, "lng": 24.9384 }
 *
 * @property lat The latitude of the place, or null if missing.
 * @property lng The longitude of the place, or null if missing.
 */
data class LatLngLiteral(
    val lat: Double?,
    val lng: Double?
)
