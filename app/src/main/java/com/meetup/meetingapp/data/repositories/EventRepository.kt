package com.meetup.meetingapp.data.repositories

import com.meetup.meetingapp.data.model.Event
import com.meetup.meetingapp.ui.screens.EventUiState
import com.meetup.meetingapp.ui.screens.participant_input.ParticipantInputState
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for managing event and participant-related operations.
 *
 * Provides methods for creating and retrieving events, synchronizing local data,
 * and storing participant availability and preferences. Implementations of this
 * interface handle communication with the underlying data sources such as
 * Room and Cloud Firestore.
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

    // Cloud Firestore operations
    suspend fun getEventByCode(eventCode: String): Event?

    // Cloud Firestore operations
    suspend fun createParticipantAvailability(participantInput: ParticipantInputState): Result<Unit>
}
