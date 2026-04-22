package com.meetup.meetingapp.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity representing a user stored locally.
 *
 * This entity is the local persistence layer version of the model [User].
 *
 * Room acts as the single source of truth for user data in the app.
 * @property uid Unique user ID (primary key).
 * @property createdEventIds List of event IDs the user has created.
 * @property joinedEventIds List of event IDs the user has joined.
 */
@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val uid: String,
    val createdEventIds: List<String>,
    val joinedEventIds: List<String>,
)
