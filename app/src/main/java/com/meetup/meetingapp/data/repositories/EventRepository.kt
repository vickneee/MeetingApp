package com.meetup.meetingapp.data.repositories

import com.meetup.meetingapp.data.model.Event
import com.meetup.meetingapp.ui.screens.EventUiState
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for managing event-related operations.
 *
 * Creates a new event and returns the generated event code and event key.
 * Implementations of this interface handle communication with the underlying
 * data source (e.g., Cloud Firestore).
 */
interface EventRepository {
    suspend fun createEvent(
        eventValues: EventUiState
    ): Result<Triple<String, String, String>>

    // Room database operations
    fun getEvents(): Flow<List<Event>>
    suspend fun syncEvents()

    // Room database operations
    suspend fun getEventById(id: String): Flow<Event?>
}