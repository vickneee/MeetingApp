package com.meetup.meetingapp.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a city entity in the Room database.
 *
 * Room acts as the single source of truth for city data in the app.
 * @property name The unique identifier of the city.
 * @property searchQuery The unique identifier of the city.
 * @property country The unique identifier of the city.
 */
@Entity(tableName = "cities")
data class CityEntity(
    @PrimaryKey
    val name: String,
    val searchQuery: String,
    val country: String
)