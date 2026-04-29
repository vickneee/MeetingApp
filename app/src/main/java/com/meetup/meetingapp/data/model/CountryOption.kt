package com.meetup.meetingapp.data.model

/**
 * Represents the list of supported countries in the Meeting App.
 *
 * Each entry contains:
 * - A human‑readable enum name (e.g., Finland)
 * - A two‑letter ISO country code used for API requests or filtering
 *
 * This enum is used when restricting search results, Places API queries,
 * or location‑based options to specific countries.
 */
enum class CountryOption(
    val code: String,
) {
    /** Finland, represented by the ISO country code "FI". */
    Finland("FI"),
}
