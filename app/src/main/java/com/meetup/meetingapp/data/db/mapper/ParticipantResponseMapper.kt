package com.meetup.meetingapp.data.db.mapper

import com.google.gson.Gson
import com.meetup.meetingapp.data.db.entities.ParticipantResponseEntity
import com.meetup.meetingapp.data.model.ParticipantResponse

/**
 * Mapper object for converting ParticipantResponseEntity objects to ParticipantResponse objects.
 * @constructor Creates a new instance of the ParticipantResponseMapper object.
 *
 * @see ParticipantResponseEntity for the mapping logic.
 * @see ParticipantResponse for the mapping logic.
 */
object ParticipantResponseMapper {
    private val gson = Gson()

    /**
     * Converts a ParticipantResponse object to a ParticipantResponseEntity object.
     * @return The converted ParticipantResponseEntity object.
     *
     * @param eventId The ID of the event associated with the participant response.
     * @see ParticipantResponseEntity for the mapping logic.
     */
    fun ParticipantResponse.toEntity(eventId: String): ParticipantResponseEntity =
        ParticipantResponseEntity(
            id = "${eventId}_$name",
            eventId = eventId,
            name = name,
            dateTimes = gson.toJson(dateTimes),
            locations = gson.toJson(locations),
            placeTypes = gson.toJson(placeTypes),
            foodCategories = gson.toJson(foodCategories),
        )
}
