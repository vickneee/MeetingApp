package com.meetup.meetingapp.data.db.mapper

import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.meetup.meetingapp.data.db.entities.ParticipantResponseEntity
import com.meetup.meetingapp.data.model.*

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
     * Converts a ParticipantResponseEntity object to a ParticipantResponse object.
     * @return The converted ParticipantResponse object.
     */
    fun ParticipantResponseEntity.toDomain(): ParticipantResponse =
        ParticipantResponse(
            name = name,
            dateTimes = dateTimes.safeFromJson(),
            locations = locations.toStringList(),
            placeTypes = placeTypes.safeFromJson(),
            foodCategories = foodCategories.safeFromJson(),
        )

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

    private fun String?.toStringList(): List<String> =
        try {
            if (this.isNullOrBlank()) emptyList()
            else gson.fromJson(this, object : TypeToken<List<String>>() {}.type)
        } catch (_: Exception) {
            emptyList()
        }

    private inline fun <reified T> String?.safeFromJson(): T {
        if (this.isNullOrBlank()) {
            return when (T::class) {
                List::class -> emptyList<Any>() as T
                else -> null as T
            }
        }
        return try {
            gson.fromJson(this, object : TypeToken<T>() {}.type)
        } catch (e: Exception) {
            Log.w("ParticipantResponseMapper", "Failed to parse JSON: $this", e)
            when (T::class) {
                List::class -> emptyList<Any>() as T
                else -> null as T
            }
        }
    }
}
