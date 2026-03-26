package com.meetup.meetingapp.data.model

/**
 * Represents an application user stored in Firestore.
 *
 * This model contains the user's unique identifier and references to events
 * they have created or joined. It does not store detailed event information,
 * only the Firestore document IDs for lightweight referencing.
 *
 * @property uid Unique identifier for the user (Firebase Auth UID).
 * @property createdEventIds List of event IDs created by this user as a host.
 * @property joinedEventIds List of event IDs this user has joined as a participant.
 */
data class User(
    // Unique identifier for the user (Firebase Auth UID).
    val uid: String,

    // IDs of events this user created as a host.
    val createdEventIds: List<String> = listOf(),

    // IDs of events this user joined as a participant.
    val joinedEventIds: List<String> = listOf()
)