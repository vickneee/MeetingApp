package com.meetup.meetingapp.data.db.converter

import androidx.room.TypeConverter

/**
 * A utility class for converting between lists of strings and a comma-separated string.
 * This class is used for Room database type conversion.
 * @constructor Creates a new instance of the Converters class.
 */
class Converters {

    /**
     * Converts a list of strings to a comma-separated string.
     * @param value The list of strings to be converted.
     * @return A comma-separated string representation of the input list.
     */
    @TypeConverter
    fun fromList(value: List<String>): String {
        return value.joinToString(",")
    }

    /**
     * Converts a comma-separated string to a list of strings.
     * @param value The comma-separated string to be converted.
     * @return A list of strings parsed from the input string.
     */
    @TypeConverter
    fun toList(value: String): List<String> {
        if (value.isEmpty()) return emptyList()
        return value.split(",")
    }
}