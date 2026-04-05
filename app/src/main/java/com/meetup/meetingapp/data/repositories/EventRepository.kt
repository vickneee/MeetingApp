package com.meetup.meetingapp.data.repositories

import com.meetup.meetingapp.data.db.entities.CityEntity
import com.meetup.meetingapp.data.model.CountryOption
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
    // Cloud Firestore operations
    suspend fun createEvent(
        eventValues: EventUiState
    ): Result<Triple<String, String, String>>

    // Cloud Firestore operations
    suspend fun getEventByCode(eventCode: String): Event?

    // Cloud Firestore operations
    suspend fun createParticipantAvailability(participantInput: ParticipantInputState): Result<Unit>

    // Room database operations
    fun getEvents(): Flow<List<Event>>

    // Room database operations SyncEvents
    suspend fun syncEvents()

    // Room database operations
    suspend fun syncEventByEventCodeAndKey(eventCode: String, eventKey: String)

    // Room database operations
    suspend fun syncCities()

    // Room database operations
    fun getEventById(id: String): Flow<Event?>

    // Room database operations
    fun getEventByEventCode(eventCode: String): Flow<Event?>

    // Room database operations
    fun getCitiesByCountry(country: CountryOption): Flow<List<String>>

}
