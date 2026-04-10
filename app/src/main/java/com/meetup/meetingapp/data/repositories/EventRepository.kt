package com.meetup.meetingapp.data.repositories

import com.meetup.meetingapp.data.model.CountryOption
import com.meetup.meetingapp.data.model.Event
import com.meetup.meetingapp.data.model.EventStatus
import com.meetup.meetingapp.data.model.ParticipantResponse
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

    /**
     * Creates a new event in Firestore and returns the generated event code and event key.
     *
     * @return A [Result] containing a triple of (eventCode, eventKey, eventId) on success,
     * or an error on failure.
     */
    suspend fun createEvent(
        eventValues: EventUiState
    ): Result<Triple<String, String, String>>

    /**
     * Stores the participant's availability and preferences in Firestore.
     *
     * @param participantInput The participant's selected availability, locations,
     * place types, and food categories.
     */
    suspend fun createParticipantAvailability(participantInput: ParticipantInputState): Result<Unit>

    /**
     * Aggregates all participant responses for the given event and updates the event document
     * with the majority-voted candidates (date/time, location, place type, and food category).
     *
     * @param eventId The ID of the event whose participant responses should be aggregated.
     * @return [Result.success] if aggregation and update succeed, or [Result.failure]
     *         if no responses are found or any Firestore/processing error occurs.
     */
    suspend fun aggregateParticipantResponses(eventId: String): Result<Unit>

    /**
     * Retrieves a list of events from the local Room database.
     *
     * @return A [Flow] emitting a list of [Event] objects.
     */
    fun getEvents(): Flow<List<Event>>

    /**
     * Synchronizes events from Firestore with the local Room database.
     *
     * @param eventCode The event code to search for.
     * @param eventKey The event key to search for.
     */
    suspend fun syncEventByEventCodeAndKey(eventCode: String, eventKey: String)

    /**
     * Synchronizes an event by its ID from Firestore with the local Room database.
     *
     * @param eventId The ID of the event to retrieve.
     */
    suspend fun syncEventById(eventId: String)

    /**
     * Retrieves an event by its ID from the local Room database.
     *
     * @param id The ID of the event to retrieve.
     * @return A [Flow] emitting the [Event] object with the specified ID.
     */
    fun getEventById(id: String): Flow<Event?>

    /**
     * Retrieves an event by its event code from the local Room database.
     *
     * @param eventCode The event code to search for.
     * @return A [Flow] emitting the [Event] object with the specified event code
     */
    fun getEventByEventCode(eventCode: String): Flow<Event?>

    /**
     * Updates the status of an event in Firestore and local Room database.
     *
     * @param eventId The ID of the event to update.
     * @param newStatus The new status to set for the event.
     */
    suspend fun updateEventStatus(eventId: String, newStatus: EventStatus)

    /**
     * Retrieves a list of cities from the local Room database.
     *
     * @param country The country for which to retrieve cities.
     * @return A [Flow] emitting a list of [String] representing city names.
     */
    fun getCitiesByCountry(country: CountryOption): Flow<List<String>>

    /**
     * Retrieves a list of cities from the local Room database.
     *
     * @return A [Flow] emitting a list of [String] representing city names.
     */
    suspend fun syncCities()

    /**
     * Retrieves participant responses from Firestore and updates the local Room database.
     *
     * @param eventId The ID of the event to retrieve responses for.
     * @return A [Flow] emitting a list of [ParticipantResponse] objects.
     */
    // Firestore real-time listener
    fun observeEventById(eventId: String): Flow<Event?>

    /**
     * Retrieves participant responses from Firestore and updates the local Room database.
     *
     * @param eventId The ID of the event to retrieve responses for.
     * @return A [Flow] emitting a list of [ParticipantResponse] objects
     */
    // Firestore real-time listener
    fun observeSubmissions(eventId: String): Flow<List<ParticipantResponse>>
}
