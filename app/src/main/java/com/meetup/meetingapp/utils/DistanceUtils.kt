package com.meetup.meetingapp.utils

import android.location.Location
import java.util.Locale

/**
 * Calculates the straight-line distance (in meters) between two geographic coordinates.
 *
 * This function is a thin wrapper around [Location.distanceBetween], making it easier
 * to test and reuse without depending on Android framework components in the caller.
 *
 * @param userLat Latitude of the user's current location.
 * @param userLng Longitude of the user's current location.
 * @param destLat Latitude of the destination (e.g., restaurant).
 * @param destLng Longitude of the destination.
 *
 * @return The distance in meters as a [Float].
 */
fun calculateDistanceMeters(
    userLat: Double,
    userLng: Double,
    destLat: Double,
    destLng: Double
): Float {
    val results = FloatArray(1)
    Location.distanceBetween(userLat, userLng, destLat, destLng, results)
    return results[0]
}

/**
 * Formats a distance value (in meters) into a human-readable string.
 *
 * Rules:
 * - Distances under 1000 meters are shown in meters (e.g., "850 m")
 * - Distances of 1000 meters or more are shown in kilometers with one decimal place
 *   (e.g., "1.2 km")
 *
 * This function uses Locale.US to ensure a consistent decimal separator (dot) in tests.
 *
 * @param distanceMeters The distance in meters.
 * @return A formatted string such as `"850 m"` or `"1.2 km"`.
 */
fun formatDistance(distanceMeters: Float): String {
    return if (distanceMeters < 1000) {
        "${distanceMeters.toInt()} m"
    } else {
        String.format(Locale.US, "%.1f km", distanceMeters / 1000)
    }
}
