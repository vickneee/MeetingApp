package com.meetup.meetingapp.utils

/**
 * Converts a Google Places price level (0–4) into a Euro‑sign representation.
 *
 * Example:
 * - 0 → "€"
 * - 1 → "€€"
 * - 2 → "€€€"
 *
 * @param level The price level integer from Google Places.
 * @return A repeated Euro‑sign string, or empty string if invalid.
 */
fun formatPriceLevel(level: Int?): String =
    if (level == null || level < 0) "" else "€".repeat(level + 1)

/**
 * Builds a Google Places Photo API URL from a given photo reference.
 *
 * This function returns a complete URL that can be used to fetch a place photo
 * from the Google Places API. If the provided [photoReference] is null or empty,
 * the function returns null instead of generating an invalid URL.
 *
 * @param photoReference The photo reference string returned by the Places API.
 * @return A full photo URL, or null if [photoReference] is null or empty.
 */
fun buildPhotoUrl(photoReference: String?, apiKey: String): String? {
    if (photoReference.isNullOrEmpty()) return null
    return "https://maps.googleapis.com/maps/api/place/photo?maxwidth=800&photo_reference=$photoReference&key=$apiKey"
}