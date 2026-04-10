package com.meetup.meetingapp.network





data class PlacesTextSearchResponse(
    val results: List<PlaceItem>?,
    val status: String?
)


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



