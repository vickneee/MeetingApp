package com.meetup.meetingapp.data.repositories

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.meetup.meetingapp.data.model.Event
import com.meetup.meetingapp.ui.screens.EventUiState
import kotlinx.coroutines.tasks.await

/**
 * Implementation of [EventRepository] responsible for creating events
 * and storing them in Cloud Firestore.
 *
 * This class also coordinates with [UserRepository] to update the host's
 * created event list after a successful event creation.
 *
 * @property db The Firestore instance used for database operations.
 * @property userRepository Repository used to update user-related data,
 * such as adding the created event ID to the host's profile.
 */
class EventRepositoryImp(
    private val db: FirebaseFirestore,
    private val userRepository: UserRepository
): EventRepository {

    // Firebase Authentication instance to retrieve the current user.
    private val auth = FirebaseAuth.getInstance()

    // UID of the currently authenticated user (event host).
    private val uid get() = auth.currentUser?.uid


    /**
     * Creates a new event in Firestore and returns the generated event code and event key.
     *
     * The event is stored under the "events" collection with an auto-generated document ID.
     * After successful creation, the event ID is added to the host's "createdEventIds"
     * via [UserRepository].
     *
     * @return A [Result] containing a pair of (eventCode, eventKey) on success,
     * or an error on failure.
     */
    override suspend fun createEvent(
        eventValues: EventUiState
    ): Result<Pair<String, String>>{

        // Create a new document reference with an auto-generated ID.
        val docRef = db.collection("events").document()
        val eventId = docRef.id

        // Characters used for generating the event code.
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"

        // Generate a 6-character event code (e.g., "A1B2C3").
        val eventCode =
            (1..6)
            .map{chars.random()}
            .joinToString("")

        // Generate a 5-digit numeric event key (e.g., "48392").
        val eventKey =
            (1..5)
                .map{(0..9).random()}
                .joinToString("")

        // Ensure the user is logged in before creating an event.
        val uid = uid ?: return Result.failure(Exception("User is not logged in"))

        // Build the Event object to be stored in Firestore.
        val event = Event(
            eventCode = eventCode,
            eventKey= eventKey,
            hostId = uid,
            id = eventId,
            eventTitle = eventValues.eventTitle,
            hostName = eventValues.hostName,
            dateRange = eventValues.dateRange,
            timeSlots = eventValues.timeSlots,
            locationOptions = eventValues.locations,
            placeTypeOptions = eventValues.placeTypes
        )

        try {
            // Store the event document in Firestore.
            docRef.set(event).await()

            // After successful creation, update the host's created event list.
            userRepository.addCreatedEvent(eventId= eventId, uid = uid)

            // Return the generated eventCode and eventKey.
            return Result.success(Pair(eventCode, eventKey))
        } catch(e: Exception){
            // Return the error if any Firestore operation fails.
            return Result.failure(e)
        }


    }

}