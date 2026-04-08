package com.meetup.meetingapp.data.repositories

/**
 * Repository interface for managing user-related operations.
 *
 * Implementations of this interface handle communication with the underlying
 * data source (e.g., Cloud Firestore) to create user profiles and update
 * user-specific event lists such as created or joined events.
 */
interface UserRepository {

    /**
     * Creates a new user document in the database.
     *
     * This method is typically called when a user signs in for the first time.
     * The implementation should initialize the user's profile with default fields.
     *
     * @param uid The unique identifier of the authenticated user.
     */
    suspend fun createUser(uid: String)

    /**
     * Adds an event ID to the list of events created by the user.
     *
     * This method updates the user's document by appending the event ID
     * to the "createdEventIds" array field. If the event ID already exists,
     * it will not be duplicated.
     *
     * @param eventId The ID of the event created by the user.
     * @param uid The unique identifier of the user.
     */
    suspend fun addCreatedEvent(eventId: String, uid: String)

    /**
     * Adds an event ID to the list of events the user has joined.
     *
     * This method updates the user's document by appending the event ID
     * to the "joinedEventIds" array field. If the event ID already exists,
     * it will not be duplicated.
     *
     * @param eventId The ID of the event the user joined.
     * @param uid The unique identifier of the user.
     */
    suspend fun addJoinedEvent(eventId: String, uid: String)

    /**
     * Retrieves the list of event IDs the user has created.
     *
     * This method queries the user's document in the database and returns
     * the list of event IDs associated with the user's creation.
     *
     * @param uid The unique identifier of the user.
     */
    suspend fun getJoinedEventIds(uid: String): List<String>
}