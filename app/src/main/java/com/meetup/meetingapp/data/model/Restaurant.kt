package com.meetup.meetingapp.data.model


data class Restaurant(
    val placeId: String,
    val name: String,
    val address: String?,
    val rating: Double?,
    val userRatingCount: Int?,
    val priceLevel: Int?,
    val types: List<String>?,
    val latitude: Double?,
    val longitude: Double?,
    val openingHours: List<String>?,
    val photoReference: String?
)


