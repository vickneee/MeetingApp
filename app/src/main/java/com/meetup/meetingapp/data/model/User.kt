package com.meetup.meetingapp.data.model

data class User(
    // Unique identifier for the user (Firebase Auth UID).
    val uid: String = "",

    // IDs of events this user created as a host.
    val createdEventIds: List<String> = listOf(),

    // IDs of events this user joined as a participant.
    val joinedEventIds: List<String> = listOf()
)