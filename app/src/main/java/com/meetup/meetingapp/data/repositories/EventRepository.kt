package com.meetup.meetingapp.data.repositories

import com.meetup.meetingapp.data.model.CountryOption
import com.meetup.meetingapp.data.model.DateTime
import com.meetup.meetingapp.data.model.Event
import com.meetup.meetingapp.data.model.EventStatus
import com.meetup.meetingapp.data.model.ParticipantResponse
import com.meetup.meetingapp.data.model.Restaurant
import com.meetup.meetingapp.data.model.Vote
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
     * Retrieves a list of restaurants for a given location.
     *
     * @param location The location for which to retrieve restaurants.
     * @return A [Flow] emitting a list of [Restaurant] objects.
     * @throws Exception if the synchronization operation fails.
     */
    fun getRestaurantsByLocation(location: String): Flow<List<Restaurant>>

    /**
     * Updates the status of an event in the database.
     *
     * @param eventId The ID of the event to update.
     * @param newStatus The newStatus to set for the event.
     * @throws Exception if the update operation fails.
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
     * Checks if restaurant candidates exist for a given event.
     *
     * @param eventId The ID of the event to check.
     * @return `true` if restaurant candidates exist, `false` otherwise
     */
    suspend fun hasRestaurantCandidates(eventId: String): Boolean

    /**
     * Saves a list of restaurants to the local Room database.
     *
     * @param eventId The ID of the event to associate with the restaurants.
     * @param restaurants The list of [Restaurant] objects to save.
     * @return A [Result] indicating the success or failure of the operation.
     */
    suspend fun saveAllRestaurants(
        eventId: String,
        restaurants: List<Restaurant>
    ): Result<Unit>

    /**
     * Retrieves a list of restaurants from the local Room database.
     *
     * @param eventId The ID of the event to retrieve restaurants for.
     * @return A [Flow] emitting a list of [Restaurant] objects.
     */
    suspend fun syncRestaurants(
        eventId: String
    ): Result<Unit>

    /**
     * Retrieves a list of restaurants from the local Room database.
     *
     * @param eventId The ID of the event to retrieve restaurants for.
     * @return A [Flow] emitting a list of [Restaurant] objects.
     * @throws Exception if the synchronization operation fails.
     */
    fun getRestaurants(eventId: String): Flow<List<Restaurant>>

    /**
     * Retrieves an event by its ID from Firestore and updates the local Room database.
     *
     * @param eventId The ID of the event to retrieve.
     * @return A [Flow] emitting the [Event] object with the specified ID.
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

    /**
     * Writes a vote to Firestore for the given event, restaurant, user, and date-time.
     *
     * The vote is stored under:
     * events/{eventId}/restaurants/{placeId}/votes/{userId}_{dateTime}
     *
     * @return Result.success(Unit) on success, or Result.failure(e) on error.
     */
    suspend fun submitVote(eventId: String, placeId: String, userId: String, dateTime: DateTime): Result<Unit>

    /**
     * Returns whether the user has already voted for the given restaurant and time slot.
     *
     * @return Result<Boolean> — true if the vote document exists in Firestore.
     */
    suspend fun getUserVote(eventId: String, placeId: String, userId: String, dateTime: DateTime): Result<Boolean>

    /**
     * Fetches and saves restaurants for the given event.
     *
     * @param event The event for which to fetch and save restaurants.
     * @return Result.success(Unit) on success, or Result.failure(e) on error.
     */
    suspend fun fetchAndSaveRestaurants(event: Event): Result<Unit>

    /**
     * Checks if restaurants exist for the given event.
     * @param eventId The ID of the event to check.
     * @param userId The ID of the user to check.
     * @param timings The list of date-times to check.
     * @return `true` if restaurants exist, `false` otherwise.
     * @throws Exception if the synchronization operation fails.
     */
    suspend fun hasUserVotedInEvent(
        eventId: String,
        userId: String,
        timings: List<DateTime>
    ): Boolean

    /**
     * Checks if there are any restaurant votes for the given event.
     * @param eventId The ID of the event to check.
     * @return `true` if any votes exist, `false` otherwise.
     */
    suspend fun hasAnyRestaurantVotes(eventId: String): Boolean

    /**
     * Aggregates restaurant votes for the given event.
     * @param eventId The ID of the event to aggregate votes for.
     * @return Result.success(Unit) on success, or Result.failure(e) on error.
     */
    suspend fun aggregateRestaurantVotes(eventId: String): Result<Unit>

    /**
     * Observes restaurant votes for the given event and returns a list of [Vote] objects.
     * @param eventId The ID of the event to observe votes for.
     * @return A [Flow] emitting a list of [Vote] objects.
     */
    fun observeRestaurantVotes(eventId: String): Flow<List<Vote>>

    /**
     * Retrieves the participant response for a specific user and event.
     * @param eventId The ID of the event.
     * @param userId The ID of the user.
     * @return The [ParticipantResponse] if found, null otherwise.
     */
    suspend fun getParticipantResponse(eventId: String, userId: String): ParticipantResponse?

    /**
     * Observes the participant response for a specific user and event in real-time.
     * @param eventId The ID of the event.
     * @param userId The ID of the user.
     * @return A [Flow] emitting the [ParticipantResponse] object for the user.
     */
    fun observeParticipantResponse(eventId: String, userId: String): Flow<ParticipantResponse?>
}
