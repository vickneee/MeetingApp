package com.meetup.meetingapp.data.model

// Represents the current progress stage of the event.
enum class EventStatus {
    CREATED,
    COLLECTING_AVAILABILITY,
    FIRST_VOTING_CLOSED,
    RESTAURANT_CANDIDATES_GENERATED,
    COLLECTING_RESTAURANT_VOTES,
    FINALIZED
}
