package com.meetup.meetingapp.data.model

/**
 * Represents a single participant in an event.
 *
 * @property uid  Unique identifier of the user participating in the event.
 * @property name Display name chosen by the participant.
 */

data class Participant(
    val uid: String = "",
    val name: String = "",
)
