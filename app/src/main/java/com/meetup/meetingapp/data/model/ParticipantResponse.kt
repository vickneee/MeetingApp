package com.meetup.meetingapp.data.model

/**
 * Represents a single participant's submitted response for an event.
 *
 * This model stores all answers a participant provides during the
 * event‑joining flow. Each field corresponds to one step of the
 * participant input process.
 *
 * @property userId The unique identifier of the user who submitted this response.
 * @property name Display name chosen by the participant.
 * @property dateTimes List of date/time options the participant is available for.
 * @property locations List of preferred locations (e.g., cities) selected by the participant.
 * @property placeTypes List of preferred place types (e.g., café, restaurant).
 * @property foodCategories List of preferred food categories chosen by the participant.
 */
data class ParticipantResponse(
    val userId: String = "",
    val name: String = "",
    val dateTimes: List<DateTime> = listOf(),
    val locations: List<String> = listOf(),
    val placeTypes: List<PlaceType> = listOf(),
    val foodCategories: List<FoodCategory> = listOf()
)
