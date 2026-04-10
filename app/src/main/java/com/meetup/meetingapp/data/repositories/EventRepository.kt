package com.meetup.meetingapp.data.repositories

import com.meetup.meetingapp.data.model.CountryOption
import com.meetup.meetingapp.data.model.Event
import com.meetup.meetingapp.data.model.EventStatus
import com.meetup.meetingapp.data.model.ParticipantResponse
import com.meetup.meetingapp.data.model.Restaurant
import com.meetup.meetingapp.data.model.TimeSlot
import com.meetup.meetingapp.ui.screens.create_event_flow.EventUiState
import com.meetup.meetingapp.ui.screens.participant_input_flow.ParticipantInputState
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

    // Cloud Firestore operations
    suspend fun aggregateParticipantResponses(eventId: String): Result<Unit>

    // Cloud Firestore operations
    fun getSubmissionsByEventId(eventId: String): Flow<List<ParticipantResponse>>

    // Cloud Firestore → Room sync
    suspend fun syncSubmissions(eventId: String)

    // Room database operations
    fun getEvents(): Flow<List<Event>>

    // Room database operations SyncEvents
    suspend fun syncEvents()

    // Room database operations
    suspend fun syncEventByEventCodeAndKey(eventCode: String, eventKey: String)

    // Cloud Firestore → Room sync for a single event
    suspend fun syncEventById(eventId: String)

    // Room database operations
    suspend fun syncCities()

    // Room database operations
    fun getEventById(id: String): Flow<Event?>

    // Room database operations
    fun getEventByEventCode(eventCode: String): Flow<Event?>

    // Room database operations
    fun getCitiesByCountry(country: CountryOption): Flow<List<String>>

    // Room database operations
    fun getAvailabilityByEventCode(eventCode: String): Flow<Pair<List<String>, List<TimeSlot>>>

    // Firebase operations
    suspend fun updateEventStatus(eventId: String, newStatus: EventStatus)

    /**
     * Synchronizes the list of events the user has joined.
     *
     * This method retrieves the user's document from the database and
     * updates the local Room database with the event IDs associated with
     * the user's joining.
     */
    suspend fun syncJoinedEvents()

    // Cloud Firestore
    suspend fun hasRestaurantCandidates(eventId: String): Boolean

    // Cloud Firestore
    suspend fun saveAllRestaurants(
        eventId: String,
        restaurants: List<Restaurant>
    ): Result<Unit>

    // Cloud Firestore → Room sync
    suspend fun syncRestaurants(
        eventId: String
    ): Result<Unit>

    // Room database operations
    fun getRestaurants(eventId: String): Flow<List<Restaurant>>
}
