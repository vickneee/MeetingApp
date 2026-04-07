package com.meetup.meetingapp.data.model

// Represents the current progress stage of the event.
enum class EventStatus(val displayName: String) {
    CREATED("Created"),
    COLLECTING_AVAILABILITY("Collecting Availability"),
    FIRST_VOTING_CLOSED("First Voting Closed"),
    RESTAURANT_CANDIDATES_GENERATED("Restaurant Candidates Generated"),
    COLLECTING_RESTAURANT_VOTES("Collecting Restaurant Votes"),
    FINALIZED("Finalized"),
    UNKNOWN("Unknown")
}
