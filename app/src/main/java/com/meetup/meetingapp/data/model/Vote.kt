package com.meetup.meetingapp.data.model

/**
 * Represents a user's vote for a specific restaurant at a selected date and time.
 *
 * This model is stored in Firestore under:
 * `events/{eventId}/restaurants/{placeId}/votes/{userId}_{dateTime}`
 *
 * Each vote records:
 * - The restaurant being voted for
 * - The selected date/time slot
 * - The user who submitted the vote
 * - The display name of the voter
 *
 * @property placeId The ID of the restaurant the user voted for.
 * @property dateTime The selected date and time slot for the vote.
 * @property userId The ID of the user who submitted the vote.
 * @property userName The display name of the voter.
 */
data class Vote(
    val placeId: String = "",
    val dateTime: DateTime? = null,
    val userId: String = "",
    val userName: String = "",
)
