package com.meetup.meetingapp.data.db.converter

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import java.lang.reflect.Type
import java.time.LocalDate

/**
 * Gson adapter for serializing and deserializing [LocalDate].
 *
 * This adapter converts:
 * - A [LocalDate] into its ISO-8601 string representation (e.g., "2024-05-01") when serializing.
 * - A JSON string containing a date into a [LocalDate] when deserializing.
 *
 * Used when Gson needs to handle LocalDate fields in API responses or database objects.
 */
object LocalDateAdapter : JsonSerializer<LocalDate>, JsonDeserializer<LocalDate> {
    override fun serialize(
        src: LocalDate,
        type: Type,
        context: JsonSerializationContext,
    ): JsonElement = JsonPrimitive(src.toString())

    override fun deserialize(
        json: JsonElement,
        type: Type,
        context: JsonDeserializationContext,
    ): LocalDate = LocalDate.parse(json.asString)
}
