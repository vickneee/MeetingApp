package com.meetup.meetingapp.data.model

/**
 * Represents the current progress stage of the event.
 *
 * This enum defines the lifecycle of an event, from its initial creation
 * through different stages of participant availability collection,
 * restaurant candidate generation, and finalization.
 *
 * @property displayName A human-readable label for the status to be shown in the UI.
 */
enum class EventStatus(val displayName: String) {
    CREATED("Created"),
    COLLECTING_AVAILABILITY("Collecting Availability"),
    FIRST_VOTING_CLOSED("First Voting Closed"),
    RESTAURANT_CANDIDATES_GENERATED("First Voting Closed"),
    COLLECTING_RESTAURANT_VOTES("Collecting Place Votes"),
    FINALIZED("Finalized"),
    UNKNOWN("Unknown")
}
