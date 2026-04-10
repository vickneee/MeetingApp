package com.meetup.meetingapp.data.repositories

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.meetup.meetingapp.data.db.daos.CityDao
import com.meetup.meetingapp.data.db.daos.EventDao
import com.meetup.meetingapp.data.db.daos.ParticipantResponseDao
import com.meetup.meetingapp.data.db.mapper.CityMapper
import com.meetup.meetingapp.data.db.mapper.EventMapper
import com.meetup.meetingapp.data.db.mapper.FirestoreCityList
import com.meetup.meetingapp.data.db.mapper.ParticipantResponseMapper
import com.meetup.meetingapp.data.model.CountryOption
import com.meetup.meetingapp.data.model.Event
import com.meetup.meetingapp.data.model.EventStatus
import com.meetup.meetingapp.data.model.ParticipantResponse
import com.meetup.meetingapp.ui.screens.create_event_flow.EventUiState
import com.meetup.meetingapp.ui.screens.participant_input_flow.ParticipantInputState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlin.collections.flatMap

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
    private val eventDao: EventDao,
    private val cityDao: CityDao,
    private val participantResponseDao: ParticipantResponseDao
): EventRepository {

    /**
     * Firebase Firestore instance.
     */
    private val auth = FirebaseAuth.getInstance()

    /**
     * Retrieves the UID of the currently authenticated user.
     */
    private val uid get() = auth.currentUser?.uid

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

            // Track joined event on user document
            userRepository.addJoinedEvent(eventId = eventId, uid = uid)

            // Also save to Room immediately
            val entity = with(ParticipantResponseMapper) { participantResponse.toEntity(eventId) }
            participantResponseDao.upsertResponses(listOf(entity))

            Result.success(Unit)
        } catch (e: Exception){
            Result.failure(e)
        }
    }

    /**
     * Aggregates all participant responses for the given event and updates the event document
     * with the majority-voted candidates (date/time, location, place type, and food category).
     *
     * This function:
     * 1. Fetches all participantResponses under the event.
     * 2. Computes the top candidates for each category using `findTopCandidates`.
     * 3. Updates the event document with the aggregated results and sets the event status
     *    to `FIRST_VOTING_CLOSED`.
     * 4. Calls `syncEventById(eventId)` to refresh the local cache after the update.
     *
     * @param eventId The ID of the event whose participant responses should be aggregated.
     *
     * @return [Result.success] if aggregation and update succeed, or [Result.failure]
     *         if no responses are found or any Firestore/processing error occurs.
     *
     * @throws Exception Wrapped inside [Result.failure] if Firestore operations fail.
     */
    override suspend fun aggregateParticipantResponses(eventId: String): Result<Unit>{
        return try {
            val snapshot = db.collection("events")
                .document(eventId)
                .collection("participantResponses")
                .get()
                .await()

            val participantResponses = snapshot.documents.mapNotNull {
                it.toObject(ParticipantResponse::class.java)
            }

            if (participantResponses.isEmpty()) {
                return Result.failure(Exception("No participant responses found"))
            }

            val dateTimeCandidates = findTopCandidates(
                participantResponses.flatMap { it.dateTimes }
            )

            val locationCandidates = findTopCandidates(
                participantResponses.flatMap { it.locations }
            )

            val placeTypeCandidates = findTopCandidates(
                participantResponses.flatMap { it.placeTypes }
            )

            val foodCategoryCandidates = findTopCandidates(
                participantResponses.flatMap { it.foodCategories }
            )

            db.collection("events")
                .document(eventId)
                .update(
                    mapOf(
                        "status" to EventStatus.FIRST_VOTING_CLOSED,
                        "dateTimeCandidates" to dateTimeCandidates,
                        "locationCandidates" to locationCandidates,
                        "placeTypeCandidates" to placeTypeCandidates,
                        "foodCategoryCandidates" to foodCategoryCandidates
                    )
                )
                .await()

            syncEventById(eventId)

            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Returns the list of items that appear most frequently in the given collection.
     *
     * This function groups all items by equality, counts their occurrences, and
     * identifies the maximum frequency. All items whose count matches the maximum
     * frequency are returned. If the input list is empty, an empty list is returned.
     *
     * @param items The list of items to evaluate.
     * @return A list of the most frequent item(s). Multiple items are returned if
     *         there is a tie for the highest frequency.
     */
    private fun <T> findTopCandidates(items: List<T>): List<T> {
        if (items.isEmpty()) return emptyList()

        val grouped = items
            .groupBy { it }
            .mapValues { (_, list) -> list.size }
            .entries
            .sortedByDescending { it.value }

        val maxCount = grouped.first().value

        return grouped
            .filter { it.value == maxCount }
            .map { it.key }
    }

    /**
     * Retrieves a list of events from the local Room database.
     *
     * @return A [Flow] emitting a list of [Event] objects.
     */
    override fun getEvents(): Flow<List<Event>> =
        eventDao.getAllEvents()
            .map { entities -> entities.map { with(EventMapper) { it.toDomain() } } }

    /**
     * Retrieves an event by its event code and key from Firestore and updates the local Room database.
     *
     * @param eventCode The event code to search for.
     * @param eventKey The event key to search for.
     */
    override suspend fun syncEventByEventCodeAndKey(eventCode: String, eventKey: String){
        try {
            val event = db.collection("events")
                .whereEqualTo("eventCode", eventCode)
                .whereEqualTo("eventKey", eventKey)
                .get()
                .await()
                .documents.firstNotNullOfOrNull { it.toObject(Event::class.java) }

            if (event != null) {
                val entity = with(EventMapper) { event.toEntity() }
                eventDao.upsertEvent(entity)
            }

        } catch (e: Exception) {
            // sync failure won't crash the app, Room still serves cached data
        }
    }

    /**
     * Retrieves an event by its ID from the local Room database.
     *
     * @param eventId The ID of the event to retrieve.
     * @return A [Flow] emitting the [Event] object with the specified ID.
     */
    override suspend fun syncEventById(eventId: String) {
        try {
            val event = db.collection("events")
                .document(eventId)
                .get()
                .await()
                .toObject(Event::class.java)

            if (event != null) {
                val entity = with(EventMapper) { event.toEntity() }
                eventDao.upsertEvent(entity)
            }
        } catch (e: Exception) {
            // sync failure won't crash the app
        }
    }

    /**
     * Retrieves an event by its ID from the local Room database.
     *
     * @param id The ID of the event to retrieve.
     * @return A [Flow] emitting the [Event] object with the specified ID.
     */
    override fun getEventById(id: String): Flow<Event?> {
        return eventDao.getEventById(id)
            .map { it?.let { with(EventMapper) { it.toDomain() } } }
    }

    /**
     * Retrieves an event by its event code from the local Room database.
     *
     * @param eventCode The event code to search for.
     * @return A [Flow] emitting the [Event] object with the specified event code
     */
    override fun getEventByEventCode(eventCode: String): Flow<Event?>{
        return eventDao.getEventByCode(eventCode)
            .map { it?.let { with(EventMapper) { it.toDomain() } } }
    }

    /**
     * Updates the status of an event in the database.
     *
     * @param eventId The ID of the event to update.
     * @param newStatus The new status to set for the event.
     * @throws Exception if the update operation fails.
     */
    override suspend fun updateEventStatus(eventId: String, newStatus: EventStatus) {
        try {
            db.collection("events")
                .document(eventId)
                .update("status", newStatus.name)
                .await()

            // Also update local Room cache
            eventDao.updateEventStatus(eventId, newStatus.name)
        } catch (e: Exception) {
            // sync failure won't crash the app
        }
    }

    /**
     * Retrieves a list of cities from the local Room database.
     *
     * @param country The country for which to retrieve cities.
     * @return A [Flow] emitting a list of [String] representing city names.
     */
    override fun getCitiesByCountry(country: CountryOption): Flow<List<String>>{
        return cityDao.getCitiesByCountry(country.name)
            .map { entityCity ->
                entityCity.map{it.name}
            }
    }

    /**
     * Retrieves a list of cities from the local Room database.
     *
     * @return A [Flow] emitting a list of [String] representing city names.
     * @throws Exception if the synchronization operation fails.
     */
    override suspend fun syncCities() {
        try {
            val docs = db.collection("static_data")
                .get()
                .await()
                .documents

            val allCities = docs
                .filter { it.id.endsWith("_cities") }
                .flatMap { doc ->
                    val country = doc.id
                        .removeSuffix("_cities")
                        .replaceFirstChar { it.uppercase() }

                    val data = doc.toObject(FirestoreCityList::class.java)

                    data?.items?.map { city ->
                        with(CityMapper) { city.toEntity(country) }
                    } ?: emptyList()
                }

            cityDao.upsertCities(allCities)

        } catch (e: Exception) {
            // sync failure won't crash the app, Room still serves cached data
        }
    }

    /**
     * Retrieves an event by its ID from Firestore and updates the local Room database.
     *
     * @param eventId The ID of the event to retrieve.
     * @return A [Flow] emitting the [Event] object with the specified ID.
     * @throws Exception if the synchronization operation fails.
     */
    override fun observeEventById(eventId: String): Flow<Event?> = callbackFlow {
        val listener = db.collection("events")
            .document(eventId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                val event = snapshot?.toObject(Event::class.java)
                if (event != null) {
                    val entity = with(EventMapper) { event.toEntity() }
                    CoroutineScope(Dispatchers.IO).launch { eventDao.upsertEvent(entity) }
                }
                trySend(event)
            }
        awaitClose { listener.remove() }
    }

    /**
     * Retrieves participant responses from Firestore and updates the local Room database.
     * @param eventId The ID of the event to retrieve responses for.
     * @return A [Flow] emitting a list of [ParticipantResponse] objects
     */
    override fun observeSubmissions(eventId: String): Flow<List<ParticipantResponse>> = callbackFlow {
        val listener = db.collection("events")
            .document(eventId)
            .collection("participantResponses")
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                val responses = snapshot?.documents
                    ?.mapNotNull { it.toObject(ParticipantResponse::class.java) }
                    ?: emptyList()
                // Also update Room cache
                CoroutineScope(Dispatchers.IO).launch {
                    val entities = responses.map { with(ParticipantResponseMapper) { it.toEntity(eventId) } }
                    participantResponseDao.upsertResponses(entities)
                }
                trySend(responses)
            }
        awaitClose { listener.remove() }
    }
}


