package com.meetup.meetingapp.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "restaurants")
data class RestaurantEntity(

    @PrimaryKey
    val placeId: String,

    val eventId: String,

    val name: String,
    val address: String,

    val latitude: Double,
    val longitude: Double,


    val openingHoursJson: String,
    val typesJson: String?,

    val photoReference: String?,
    val priceLevel: Int,
    val rating: Double,
    val userRatingCount: Int
)
