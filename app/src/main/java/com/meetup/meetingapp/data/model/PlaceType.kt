package com.meetup.meetingapp.data.model

/**
 * Represents the types of places that can be selected for an event.
 *
 * Each enum entry includes:
 * - A display label for the place type
 * - A `queryName` string used directly as a keyword when querying
 *   the Google Places API
 *
 * These values help filter search results to match the desired venue type.
 */
enum class PlaceType(
    val queryName: String,
) {
    CAFE("cafe"),
    RESTAURANT("restaurant"),
    BAR("bar"),
}
