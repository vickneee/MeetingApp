package com.meetup.meetingapp.data.model

data class Vote(
    val placeId: String = "",
    val dateTime: DateTime? = null,
    val userId: String = "",
    val userName: String = "",
)
