package com.meetup.meetingapp.data.model

import com.google.gson.annotations.SerializedName

/**
 * Represents a single address component returned by the Google Places API.
 *
 * Each component contains:
 * - A long name (e.g., "San Francisco")
 * - A short name (e.g., "SF")
 * - A list of type identifiers describing what the component represents
 *   (e.g., ["locality"], ["country"], ["postal_code"])
 *
 * This model is used when parsing the `address_components` array from
 * Google Place Details responses.
 */
data class AddressComponent(
    @SerializedName("long_name")
    val longName: String?,
    @SerializedName("short_name")
    val shortName: String?,
    val types: List<String>?,
)
