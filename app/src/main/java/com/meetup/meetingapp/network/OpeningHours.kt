package com.meetup.meetingapp.network

import com.google.gson.annotations.SerializedName

/**
 * Represents the opening hours section returned by the Google Places API.
 *
 * The `weekday_text` field contains human‑readable strings describing
 * the opening hours for each day of the week, typically in the format:
 *
 *   "Monday: 9:00 AM – 8:00 PM"
 *
 * This raw text is later parsed in the ViewModel to determine whether a
 * restaurant is open during a selected time slot.
 *
 * @property weekdayText A list of formatted opening‑hours strings, or null if
 *                        the API does not provide opening hours for the place.
 */
data class OpeningHours(
    @SerializedName("weekday_text")
    val weekdayText: List<String>?,
)
