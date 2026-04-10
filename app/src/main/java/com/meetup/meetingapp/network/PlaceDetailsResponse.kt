package com.meetup.meetingapp.network


data class PlaceDetailsResponse(
    val result: PlaceDetailsResult?,
    val status: String?
)

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
