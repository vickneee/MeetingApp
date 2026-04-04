package com.meetup.meetingapp.data.repositories

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.meetup.meetingapp.data.db.daos.EventDao
import com.meetup.meetingapp.data.db.mapper.EventMapper
import com.meetup.meetingapp.data.model.Event
import com.meetup.meetingapp.data.model.ParticipantResponse
import com.meetup.meetingapp.ui.screens.EventUiState
import com.meetup.meetingapp.ui.screens.participant_input.ParticipantInputState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
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
    private val userRepository: UserRepository,
    private val eventDao: EventDao
): EventRepository {

    // Firebase Authentication instance to retrieve the current user.
    private val auth = FirebaseAuth.getInstance()

    // UID of the currently authenticated user (event host).
    private val uid get() = auth.currentUser?.uid

    /**
     * Retrieves a list of events from the local Room database.
     *
     * @return A [Flow] emitting a list of [Event] objects.
     */
    // UI reads from Room (single source of truth)
    override fun getEvents(): Flow<List<Event>> =
        eventDao.getAllEvents()
            .map { entities -> entities.map { with(EventMapper) { it.toDomain() } } }

    /**
     * Synchronizes events from Firestore with the local Room database.
     *
     */
    // Sync events from Firestore to Room
    override suspend fun syncEvents() {
        val uid = uid ?: return
        try {
            db.collection("events")
                .whereEqualTo("hostId", uid)
                .get()
                .await()
                .documents
                .mapNotNull { it.toObject(Event::class.java) }
                .map { with(EventMapper) { it.toEntity() } }
                .also { eventDao.upsertEvents(it) }
        } catch (e: Exception) {
            // sync failure won't crash the app, Room still serves cached data
        }
    }

    override suspend fun syncEventByEventCodeAndKey(eventCode: String, eventKey: String){
        try {
            val event = db.collection("events")
                .whereEqualTo("eventCode", eventCode)
                .whereEqualTo("eventKey", eventKey)
                .get()
                .await()
                .documents
                .mapNotNull { it.toObject(Event::class.java) }
                .firstOrNull()

            if (event != null) {
                val entity = with(EventMapper) { event.toEntity() }
                eventDao.upsertEvent(entity)
            }

        } catch (e: Exception) {
            // sync failure won't crash the app, Room still serves cached data
        }
    }

    override fun getEventById(id: String): Flow<Event?> {
        return eventDao.getEventById(id)
            .map { it?.let { with(EventMapper) { it.toDomain() } } }
    }

    override fun getEventByEventCode(eventCode: String): Flow<Event?>{
        return eventDao.getEventByCode(eventCode)
            .map { it?.let { with(EventMapper) { it.toDomain() } } }
    }

    /**
     * Creates a new event in Firestore and returns the generated event code and event key.
     *
     * The event is stored under the "events" collection with an auto-generated document ID.
     * After successful creation, the event ID is added to the host's "createdEventIds"
     * via [UserRepository].
     *
     * @return A [Result] containing a triple of (eventCode, eventKey, eventId) on success,
     * or an error on failure.
     */
    override suspend fun createEvent(
        eventValues: EventUiState
    ): Result<Triple<String, String, String>>{

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

            //  Save to Room
            with(EventMapper) { eventDao.upsertEvent(event.toEntity()) }

            // After successful creation, update the host's created event list.
            userRepository.addCreatedEvent(eventId= eventId, uid = uid)

            // Return the generated eventCode, eventKey and eventId.
            return Result.success(Triple(eventCode, eventKey, eventId))
        } catch(e: Exception){
            // Return the error if any Firestore operation fails.
            return Result.failure(e)
        }
    }

    override suspend fun getEventByCode(eventCode: String): Event? {
        return db.collection("events")
            .whereEqualTo("eventCode", eventCode)
            .get()
            .await()
            .documents
            .firstOrNull()
            ?.toObject(Event::class.java)
    }

    /**
     * Stores the participant's availability and preferences in Firestore.
     *
     * The data is written under:
     *   /events/{eventId}/participantResponses/{uid}
     *
     * The document ID corresponds to the currently authenticated user's UID.
     * On success, the function returns [Result.success] with [Unit].
     * If the user is not logged in or the write operation fails, a failure result is returned.
     *
     * @param participantInput The participant's selected availability, locations,
     * place types, and food categories.
     */
    override suspend fun createParticipantAvailability(participantInput: ParticipantInputState): Result<Unit>{
        val eventId = participantInput.eventId

        val participantResponse = ParticipantResponse(
            name = participantInput.participantName,
            dateTimes = participantInput.selectedDateTimes,
            locations = participantInput.selectedLocations,
            placeTypes = participantInput.selectedPlaceTypes,
            foodCategories = participantInput.selectedFoodCategories
        )

        // Ensure the user is logged in before creating an event.
        val uid = uid ?: return Result.failure(Exception("User is not logged in"))

        return try {
            db.collection("events")
                .document(eventId)
                .collection("participantResponses")
                .document(uid)
                .set(participantResponse)
                .await()

            Result.success(Unit)
        } catch (e: Exception){
            Result.failure(e)
        }
    }
}