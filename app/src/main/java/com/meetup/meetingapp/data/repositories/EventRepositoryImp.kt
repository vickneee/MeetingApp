package com.meetup.meetingapp.data.repositories

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.meetup.meetingapp.data.db.daos.CityDao
import com.meetup.meetingapp.data.db.daos.EventDao
import com.meetup.meetingapp.data.db.daos.ParticipantResponseDao
import com.meetup.meetingapp.data.db.daos.RestaurantDao
import com.meetup.meetingapp.data.db.mapper.CityMapper
import com.meetup.meetingapp.data.db.mapper.EventMapper
import com.meetup.meetingapp.data.db.mapper.FirestoreCityList
import com.meetup.meetingapp.data.db.mapper.ParticipantResponseMapper
import com.meetup.meetingapp.data.db.mapper.RestaurantMapper
import com.meetup.meetingapp.data.db.mapper.RestaurantMapper.toEntity
import com.meetup.meetingapp.data.model.CountryOption
import com.meetup.meetingapp.data.model.DateTime
import com.meetup.meetingapp.data.model.Event
import com.meetup.meetingapp.data.model.EventStatus
import com.meetup.meetingapp.data.model.FoodCategory
import com.meetup.meetingapp.data.model.ParticipantResponse
import com.meetup.meetingapp.data.model.PlaceType
import com.meetup.meetingapp.data.model.Restaurant
import com.meetup.meetingapp.data.model.Vote
import com.meetup.meetingapp.ui.screens.eventcreation.EventUiState
import com.meetup.meetingapp.ui.screens.participantinput.ParticipantInputState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.tasks.await
import java.util.Locale
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
    private val participantResponseDao: ParticipantResponseDao,
    private val restaurantDao: RestaurantDao,
    private val placesRepository: PlacesRepository,
    private val auth: FirebaseAuth,
) : EventRepository {
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
    override suspend fun createEvent(eventValues: EventUiState): Result<Triple<String, String, String>> {
        // Create a new document reference with an auto-generated ID.
        val docRef = db.collection("events").document()
        val eventId = docRef.id

        // Characters used for generating the event code.
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"

        // Generate a 6-character event code (e.g., "A1B2C3").
        val eventCode =
            (1..6)
                .map { chars.random() }
                .joinToString("")

        // Generate a 5-digit numeric event key (e.g., "48392").
        val eventKey =
            (1..5)
                .map { (0..9).random() }
                .joinToString("")

        // Ensure the user is logged in before creating an event.
        val uid = uid ?: return Result.failure(Exception("User is not logged in"))

        // Build the Event object to be stored in Firestore.
        val event =
            Event(
                eventCode = eventCode,
                eventKey = eventKey,
                hostId = uid,
                id = eventId,
                eventTitle = eventValues.eventTitle,
                hostName = eventValues.hostName,
                dateRange = eventValues.dateRange,
                timeSlots = eventValues.timeSlots,
                locationOptions = eventValues.locations,
                placeTypeOptions = eventValues.placeTypes,
            )

        try {
            // Store the event document in Firestore.
            docRef.set(event).await()

            //  Save to Room
            with(EventMapper) { eventDao.upsertEvent(event.toEntity()) }

            // After successful creation, update the host's created event list.
            userRepository.addCreatedEvent(eventId = eventId, uid = uid)

            // Return the generated eventCode, eventKey and eventId.
            return Result.success(Triple(eventCode, eventKey, eventId))
        } catch (e: Exception) {
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
    override suspend fun createParticipantAvailability(participantInput: ParticipantInputState): Result<Unit> {
        val eventId = participantInput.eventId

        val participantResponse =
            ParticipantResponse(
                userId = uid ?: "",
                name = participantInput.participantName,
                dateTimes = participantInput.selectedDateTimes,
                locations = participantInput.selectedLocations,
                placeTypes = participantInput.selectedPlaceTypes,
                foodCategories = participantInput.selectedFoodCategories,
            )

        // Ensure the user is logged in before creating an event.
        val uid = uid ?: return Result.failure(Exception("User is not logged in"))

        return try {
            db
                .collection("events")
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
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    data class AggregatedCandidates(
        val dateTimeCandidates: List<DateTime>,
        val locationCandidates: List<String>,
        val placeTypeCandidates: List<PlaceType>,
        val foodCategoryCandidates: List<FoodCategory>,
    )

    /**
     * Computes the top‑voted candidates for each category from a list of participant responses.
     *
     * This function performs pure aggregation logic only. It does not access Firestore or
     * perform any I/O. Each category (date/time, location, place type, food category) is
     * flattened across all responses and passed to `findTopCandidates`, which returns the
     * most frequently selected values.
     *
     * @param responses The list of participant responses to aggregate.
     * @return An [AggregatedCandidates] object containing the top candidates for each category.
     */
    fun aggregateCandidatesFromResponses(responses: List<ParticipantResponse>): AggregatedCandidates {
        val dateTimeCandidates =
            findTopCandidates(responses.flatMap { it.dateTimes })

        val locationCandidates =
            findTopCandidates(responses.flatMap { it.locations })

        val placeTypeCandidates =
            findTopCandidates(responses.flatMap { it.placeTypes })

        val foodCategoryCandidates =
            findTopCandidates(responses.flatMap { it.foodCategories })

        return AggregatedCandidates(
            dateTimeCandidates,
            locationCandidates,
            placeTypeCandidates,
            foodCategoryCandidates,
        )
    }

    /**
     * Fetches all participant responses for the given event, aggregates their selections,
     * and updates the event document with the majority‑voted candidates.
     *
     * Workflow:
     * 1. Reads all `participantResponses` from Firestore.
     * 2. Converts them into model objects.
     * 3. Aggregates the top candidates using `aggregateCandidatesFromResponses`.
     * 4. Updates the event document with the computed candidates and sets the status to
     *    [EventStatus.FIRST_VOTING_CLOSED].
     * 5. Calls `syncEventById(eventId)` to refresh the local cache.
     *
     * @param eventId The ID of the event whose responses should be aggregated.
     * @return [Result.success] if aggregation and update succeed, or [Result.failure] if
     *         no responses exist or a Firestore error occurs.
     */
    override suspend fun aggregateParticipantResponses(eventId: String): Result<Unit> {
        return try {
            val snapshot =
                db
                    .collection("events")
                    .document(eventId)
                    .collection("participantResponses")
                    .get()
                    .await()

            val participantResponses =
                snapshot.documents.mapNotNull {
                    it.toObject(ParticipantResponse::class.java)
                }

            if (participantResponses.isEmpty()) {
                return Result.failure(Exception("No participant responses found"))
            }

            val aggregated = aggregateCandidatesFromResponses(participantResponses)

            db
                .collection("events")
                .document(eventId)
                .update(
                    mapOf(
                        "status" to EventStatus.FIRST_VOTING_CLOSED,
                        "dateTimeCandidates" to aggregated.dateTimeCandidates,
                        "locationCandidates" to aggregated.locationCandidates,
                        "placeTypeCandidates" to aggregated.placeTypeCandidates,
                        "foodCategoryCandidates" to aggregated.foodCategoryCandidates,
                    ),
                ).await()

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

        val grouped =
            items
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
        eventDao
            .getAllEvents()
            .map { entities -> entities.map { with(EventMapper) { it.toDomain() } } }

    /**
     * Retrieves an event by its event code and key from Firestore and updates the local Room database.
     *
     * @param eventCode The event code to search for.
     * @param eventKey The event key to search for.
     */
    override suspend fun syncEventByEventCodeAndKey(
        eventCode: String,
        eventKey: String,
    ) {
        try {
            val event =
                db
                    .collection("events")
                    .whereEqualTo("eventCode", eventCode)
                    .whereEqualTo("eventKey", eventKey)
                    .get()
                    .await()
                    .documents
                    .firstNotNullOfOrNull { it.toObject(Event::class.java) }

            if (event != null) {
                val entity = with(EventMapper) { event.toEntity() }
                eventDao.upsertEvent(entity)
            }
        } catch (_: Exception) {
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
            val event =
                db
                    .collection("events")
                    .document(eventId)
                    .get()
                    .await()
                    .toObject(Event::class.java)

            if (event != null) {
                val entity = with(EventMapper) { event.toEntity() }
                eventDao.upsertEvent(entity)
            }
        } catch (_: Exception) {
            // sync failure won't crash the app
        }
    }

    /**
     * Retrieves a list of restaurants for a given location.
     *
     * @param location The location for which to retrieve restaurants.
     * @return A [Flow] emitting a list of [Restaurant] objects.
     */
    override fun getRestaurantsByLocation(location: String): Flow<List<Restaurant>> = flowOf(emptyList())

    /**
     * Retrieves an event by its ID from the local Room database.
     *
     * @param id The ID of the event to retrieve.
     * @return A [Flow] emitting the [Event] object with the specified ID.
     */
    override fun getEventById(id: String): Flow<Event?> =
        eventDao
            .getEventById(id)
            .map { it?.let { with(EventMapper) { it.toDomain() } } }

    /**
     * Retrieves an event by its event code from the local Room database.
     *
     * @param eventCode The event code to search for.
     * @return A [Flow] emitting the [Event] object with the specified event code
     */
    override fun getEventByEventCode(eventCode: String): Flow<Event?> =
        eventDao
            .getEventByCode(eventCode)
            .map { it?.let { with(EventMapper) { it.toDomain() } } }

    /**
     * Updates the status of an event in the database.
     *
     * @param eventId The ID of the event to update.
     * @param newStatus The new status to set for the event.
     * @throws Exception if the update operation fails.
     */
    override suspend fun updateEventStatus(
        eventId: String,
        newStatus: EventStatus,
    ) {
        try {
            db
                .collection("events")
                .document(eventId)
                .update("status", newStatus.name)
                .await()

            // Also update local Room cache
            eventDao.updateEventStatus(eventId, newStatus.name)
        } catch (_: Exception) {
            // sync failure won't crash the app
        }
    }

    /**
     * Retrieves a list of cities from the local Room database.
     *
     * @param country The country for which to retrieve cities.
     * @return A [Flow] emitting a list of [String] representing city names.
     */
    override fun getCitiesByCountry(country: CountryOption): Flow<List<String>> =
        cityDao
            .getCitiesByCountry(country.name)
            .map { entityCity ->
                entityCity.map { it.name }
            }

    /**
     * Retrieves a list of cities from the local Room database.
     *
     * @return A [Flow] emitting a list of [String] representing city names.
     * @throws Exception if the synchronization operation fails.
     */
    override suspend fun syncCities() {
        try {
            val docs =
                db
                    .collection("static_data")
                    .get()
                    .await()
                    .documents

            val allCities =
                docs
                    .filter { it.id.lowercase().endsWith("_cities") }
                    .flatMap { doc ->
                        val country =
                            doc.id
                                .split("_")[0]
                                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }

                        val data = doc.toObject(FirestoreCityList::class.java)

                        data?.items?.map { city ->
                            with(CityMapper) { city.toEntity(country) }
                        } ?: emptyList()
                    }

            if (allCities.isNotEmpty()) {
                cityDao.upsertCities(allCities)
            }
        } catch (e: Exception) {
            Log.e("CitySync", "Sync failed", e)
        }
    }

    /**
     * Retrieves an event by its ID from Firestore and updates the local Room database.
     *
     * @param eventId The ID of the event to retrieve.
     * @return A [Flow] emitting the [Event] object with the specified ID.
     * @throws Exception if the synchronization operation fails.
     */
    override fun observeEventById(eventId: String): Flow<Event?> =
        callbackFlow {
            val listener =
                db
                    .collection("events")
                    .document(eventId)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            close(error)
                            return@addSnapshotListener
                        }
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
    override fun observeSubmissions(eventId: String): Flow<List<ParticipantResponse>> =
        callbackFlow {
            val listener =
                db
                    .collection("events")
                    .document(eventId)
                    .collection("participantResponses")
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            close(error)
                            return@addSnapshotListener
                        }
                        val responses =
                            snapshot
                                ?.documents
                                ?.mapNotNull { doc ->
                                    doc
                                        .toObject(ParticipantResponse::class.java)
                                        ?.copy(userId = doc.id)
                                }
                                ?: emptyList()
                        // Also update Room cache
                        CoroutineScope(Dispatchers.IO).launch {
                            val entities =
                                responses.map {
                                    with(ParticipantResponseMapper) {
                                        it.toEntity(eventId)
                                    }
                                }
                            participantResponseDao.upsertResponses(entities)
                        }
                        trySend(responses)
                    }
            awaitClose { listener.remove() }
        }

    /**
     * Checks whether the event already has at least one restaurant candidate stored in Firestore.
     *
     * This is used to determine whether the app should:
     *  - Load existing restaurant candidates from Firestore → Room, or
     *  - Trigger a new Places API fetch to generate candidates.
     *
     * Implementation details:
     *  - Reads the "events/{eventId}/restaurants" subcollection.
     *  - Uses `.limit(1)` for efficiency (we only need to know if *any* exist).
     *  - Returns true if at least one document exists.
     *
     * @param eventId The ID of the event whose restaurant candidates should be checked.
     * @return True if at least one restaurant document exists, false otherwise.
     */
    override suspend fun hasRestaurantCandidates(eventId: String): Boolean {
        // Check Room first (instant)
        val roomCount = restaurantDao.getRestaurantCount(eventId)
        if (roomCount > 0) {
            Log.d("hasRestaurantCandidates", "Found $roomCount in Room")
            return true
        }
        // Fall back to Firestore
        val snapshot =
            db
                .collection("events")
                .document(eventId)
                .collection("restaurants")
                .limit(1)
                .get()
                .await()
        Log.d("hasRestaurantCandidates", "Firestore isEmpty: ${snapshot.isEmpty}")
        return !snapshot.isEmpty
    }

    /**
     * Persists discovered restaurants as individual documents within a sub-collection.
     *
     * Using a sub-collection (events/{eventId}/restaurants/{placeId}) instead of a
     * primitive list of Strings in the parent document provides:
     * 1. **Atomicity**: We can update a single restaurant's vote count without re-writing the whole list.
     * 2. **Scalability**: Avoids the 1MB Firestore document size limit if the group discovers many places.
     * 3. **Structure**: Allows storing complex [Restaurant] objects (URLs, Ratings, Coordinates).
     *
     * @param eventId The Firestore Document ID of the parent event.
     * @param restaurants The list of [Restaurant] objects to be stored.
     */
    override suspend fun saveAllRestaurants(
        eventId: String,
        restaurants: List<Restaurant>,
    ): Result<Unit> =
        try {
            val batch = db.batch()
            val restaurantsRef =
                db
                    .collection("events")
                    .document(eventId)
                    .collection("restaurants")

            restaurants.forEach { restaurant ->
                // Use Google's unique Place ID as the Doc ID to prevent duplicate entries
                val docRef = restaurantsRef.document(restaurant.placeId)
                batch.set(docRef, restaurant)
            }

            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }

    /**
     * Synchronizes restaurant candidates from Firestore into the local Room database.
     *
     * Workflow:
     *  1. Checks if Firestore contains any restaurant candidates.
     *     - If none exist, returns failure.
     *  2. Fetches all restaurant documents from:
     *        events/{eventId}/restaurants
     *  3. Converts Firestore models → Room entities.
     *  4. Upserts them into the local Room database.
     *
     * Purpose:
     *  - Ensures Room is always the single source of truth for restaurant data.
     *  - Used after saving new candidates or when loading existing ones.
     *
     * @param eventId The ID of the event whose restaurants should be synced.
     * @return Result.success(Unit) on success, or Result.failure(e) on error.
     */
    override suspend fun syncRestaurants(eventId: String): Result<Unit> {
        if (!hasRestaurantCandidates(eventId)) {
            Log.e("syncRestaurants", "No candidates in Firestore for eventId: $eventId")
            return Result.failure(Exception("No Restaurant information is available"))
        } else {
            try {
                val restaurants =
                    db
                        .collection("events")
                        .document(eventId)
                        .collection("restaurants")
                        .get()
                        .await()
                        .mapNotNull { it.toObject(Restaurant::class.java) }
                        .map { it.toEntity(eventId) }

                Log.d(
                    "syncRestaurants",
                    "Fetched ${restaurants.size} from Firestore, upserting to Room",
                )
                restaurantDao.upsertRestaurants(restaurants)
                Log.d("syncRestaurants", "Upsert complete")
                return Result.success(Unit)
            } catch (e: Exception) {
                Log.e("syncRestaurants", "Error", e)
                return Result.failure(e)
            }
        }
    }

    /**
     * Retrieves a reactive stream of restaurants associated with a specific event.
     *
     * This implementation serves as a bridge between the local database and the UI.
     * interface, it currently returns the raw results from the local Room database,
     * allowing the calling ViewModel to perform fine-grained filtering (e.g.,
     * checking complex opening hours) using its local utility functions.
     *
     * @param eventId The unique identifier of the event.
     * @param targetTime Optional [DateTime] used by the caller for availability filtering.
     * @param lat Latitude for search biasing (consumed by the API sync layer).
     * @param lng Longitude for search biasing (consumed by the API sync layer).
     * @return A [Flow] emitting the list of [Restaurant] domain models.
     */
    override fun getRestaurants(
        eventId: String,
        targetTime: DateTime?,
        lat: Double,
        lng: Double,
    ): Flow<List<Restaurant>> =
        restaurantDao
            .getRestaurants(eventId)
            .map { list ->
                list.map { entity ->
                    with(RestaurantMapper) { entity.toDomain() }
                }
            }

    /**
     * Resolves the appropriate display name for a user when submitting a vote.
     *
     * This function is a pure, side‑effect‑free helper used by [submitVote].
     * It determines the most suitable userName based on the following priority:
     *
     * 1. Participant-provided name (participantResponse.name), if non-null and non-blank.
     * 2. Host name (event.hostName), if the user is the host of the event.
     * 3. FirebaseAuth displayName (currentUserName), if available.
     * 4. "Unknown" as a final fallback.
     *
     * @param participantResponse The participant's response document, if it exists.
     * @param event The event document, used to determine host identity.
     * @param currentUserName The FirebaseAuth displayName of the current user.
     * @param userId The ID of the user whose name is being resolved.
     * @return The resolved display name for the user.
     */
    fun resolveUserName(
        participantResponse: ParticipantResponse?,
        event: Event?,
        currentUserName: String?,
        userId: String,
    ): String =
        when {
            !participantResponse?.name.isNullOrBlank() -> participantResponse.name
            event?.hostId == userId -> event.hostName
            !currentUserName.isNullOrBlank() -> currentUserName
            else -> "Unknown"
        }

    /**
     * Submits a vote for a specific restaurant and date-time within an event.
     *
     * This method writes a Vote document to Firestore at the following path:
     *
     * events/{eventId}/restaurants/{placeId}/votes/{userId}_{dateTime}
     *
     * The document ID is constructed using the userId and dateTime to ensure that:
     * - A user cannot vote more than once for the same restaurant at the same date-time
     * - Subsequent submissions overwrite the existing vote for that combination
     *
     * The Vote data is stored only in Firestore. It is not cached in Room because
     * vote data is highly dynamic and should rely on Firestore as the single source
     * of truth. The caller should re-query Firestore (e.g., via getUserVote) to
     * reflect the updated voting state in the UI.
     *
     * @param eventId The ID of the event
     * @param placeId The ID of the restaurant being voted for
     * @param userId The ID of the user submitting the vote
     * @param dateTime The selected date-time slot for the vote
     * @return A Result indicating success or failure of the Firestore write operation
     */
    override suspend fun submitVote(
        eventId: String,
        placeId: String,
        userId: String,
        dateTime: DateTime,
    ): Result<Unit> {
        val participantResponse =
            try {
                db
                    .collection("events")
                    .document(eventId)
                    .collection("participantResponses")
                    .document(userId)
                    .get()
                    .await()
                    .toObject(ParticipantResponse::class.java)
            } catch (_: Exception) {
                null
            }

        val eventDoc =
            try {
                db
                    .collection("events")
                    .document(eventId)
                    .get()
                    .await()
                    .toObject(Event::class.java)
            } catch (_: Exception) {
                null
            }

        val currentUserName = auth.currentUser?.displayName

        val userName =
            resolveUserName(
                participantResponse = participantResponse,
                event = eventDoc,
                currentUserName = currentUserName,
                userId = userId,
            )

        val vote =
            Vote(placeId = placeId, dateTime = dateTime, userId = userId, userName = userName)
        return try {
            db
                .collection("events")
                .document(eventId)
                .collection("restaurants")
                .document(placeId)
                .collection("votes")
                .document("${userId}_$dateTime")
                .set(vote)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Checks whether the user has already submitted a vote for the given restaurant and time slot.
     *
     * This queries Firestore at:
     * events/{eventId}/restaurants/{placeId}/votes/{userId}_{dateTime}
     *
     * @return Result<Boolean> — true if the vote document exists, false if not.
     *         Returns Result.failure(e) if the Firestore request fails.
     */
    override suspend fun getUserVote(
        eventId: String,
        placeId: String,
        userId: String,
        dateTime: DateTime,
    ): Result<Boolean> =
        try {
            val snapshot =
                db
                    .collection("events")
                    .document(eventId)
                    .collection("restaurants")
                    .document(placeId)
                    .collection("votes")
                    .document("${userId}_$dateTime")
                    .get()
                    .await()

            Result.success(snapshot.exists())
        } catch (e: Exception) {
            Result.failure(e)
        }

    /**
     * Orchestrates the discovery of restaurant candidates based on participant preferences.
     *
     * This process is "lossy" by design—it takes the most popular voted categories
     * and finds the best matching physical locations to present to the group.
     *
     * ### Data Flow:
     * 1. **Query Generation**: Maps [PlaceType] and food categories into human-readable
     * strings (e.g., "Vegan restaurant in Helsinki").
     * 2. **Availability Check**: Uses [Event.dateTimeCandidates] to filter out
     * marked as "Closed" during the planned event window.
     * 3. **Deduplication**: Uses a [MutableSet] of Place IDs to ensure that if a
     * popular venue matches multiple queries, it only appears once.
     * 4. **Persistence**: Saves results into a **Firestore sub-collection** (`/restaurants`)
     * rather than a flat list, allowing for more detailed venue metadata.
     * 5. **State Transition**: Once saved, moves the event to [EventStatus.COLLECTING_RESTAURANT_VOTES].
     *
     * @param event The current [Event] containing aggregated preferences.
     * @return A [Result] indicating success or failure of the discovery process.
     */
    override suspend fun fetchAndSaveRestaurants(event: Event): Result<Unit> {
        val seen = mutableSetOf<String>()
        val allRestaurants = mutableListOf<Restaurant>()
        val mutex = Mutex()

        // We use the first date-time candidate to validate opening hours.
        // If the group hasn't picked a time yet, validation is skipped (returns true).
        val targetTime = event.dateTimeCandidates.firstOrNull()

        // Geometric biasing: Ensures Google prioritizes results near the chosen area.
        val lat = event.selectedLocationLat ?: 0.0
        val lng = event.selectedLocationLng ?: 0.0

        // Pass components to fetchRestaurants when multi-country support is needed
        // Currently PlacesRepositoryImp hardcodes "country:fi"
        val components =
            event.locationOptions.countries
                .mapNotNull { countryName ->
                    CountryOption.entries.find { it.name == countryName }?.code
                }.distinct()
                .joinToString("|") { "country:$it" }

        coroutineScope {
            event.locationCandidates
                .map { city ->
                    async {
                        // Generate a matrix of search combinations (City + Type + Category)
                        val combinations =
                            event.placeTypeCandidates
                                .flatMap { placeType ->
                                    when (placeType) {
                                        PlaceType.RESTAURANT -> {
                                            if (event.foodCategoryCandidates.isEmpty()) {
                                                listOf(Triple(city, placeType, null))
                                            } else {
                                                event.foodCategoryCandidates.map {
                                                    Triple(
                                                        city,
                                                        placeType,
                                                        it,
                                                    )
                                                }
                                            }
                                        }

                                        PlaceType.CAFE -> listOf(Triple(city, placeType, null))
                                        PlaceType.BAR -> listOf(Triple(city, placeType, null))
                                    }
                                }.shuffled()
                                .take(10) // Limit API usage and keep the list manageable

                        combinations
                            .map { (city, placeType, foodCategory) ->
                                async {
                                    val query = buildSearchQuery(city, placeType, foodCategory)

                                    placesRepository
                                        .fetchRestaurants(
                                            query = query,
                                            targetTime = targetTime,
                                            lat = lat,
                                            lng = lng,
                                            components = components.ifEmpty { null },
                                        ).onSuccess { restaurants ->
                                            // Grab the top result for each query combination to maximize variety
                                            restaurants.firstOrNull()?.let { restaurant ->
                                                mutex.withLock {
                                                    if (seen.add(restaurant.placeId)) {
                                                        allRestaurants.add(restaurant.copy(searchLocation = city))
                                                    }
                                                }
                                            }
                                        }
                                }
                            }.awaitAll()
                    }
                }.awaitAll()
        }

        return if (allRestaurants.isNotEmpty()) {
            // Persist to sub-collection and update the main event status
            saveAllRestaurants(event.id, allRestaurants).fold(
                onSuccess = {
                    updateEventStatus(event.id, EventStatus.COLLECTING_RESTAURANT_VOTES)
                    Result.success(Unit)
                },
                onFailure = { Result.failure(it) },
            )
        } else {
            Result.failure(Exception("No places found matching the criteria and timing."))
        }
    }

    /**
     * Internal helper to format strings for the Google Places TextSearch API.
     */
    private fun buildSearchQuery(
        city: String,
        type: PlaceType,
        category: FoodCategory?,
    ): String =
        when (type) {
            PlaceType.RESTAURANT if category != null -> "${category.queryName} restaurant in $city"
            PlaceType.CAFE -> "cafe in $city"
            PlaceType.BAR -> "bar in $city"
            else -> "${type.queryName} in $city"
        }

    /**
     * Retrieves a list of restaurants for a given location.
     * @param eventId The event for which to retrieve restaurants.
     * @param userId The user for which to retrieve restaurants.
     * @param timings The list of date-times for which to retrieve restaurants.
     */
    override suspend fun hasUserVotedInEvent(
        eventId: String,
        userId: String,
        timings: List<DateTime>,
    ): Boolean {
        syncRestaurants(eventId)

        val restaurants = getRestaurants(eventId).first()

        if (restaurants.isEmpty()) return false

        return restaurants.any { restaurant ->
            timings.any { timing ->
                getUserVote(eventId, restaurant.placeId, userId, timing)
                    .getOrDefault(false)
            }
        }
    }

    /**
     * Checks if there are any restaurant votes for the given event.
     * @param eventId The ID of the event to check.
     * @return `true` if any votes exist, `false` otherwise.
     */
    override suspend fun hasAnyRestaurantVotes(eventId: String): Boolean {
        return try {
            val restaurantsSnapshot =
                db
                    .collection("events")
                    .document(eventId)
                    .collection("restaurants")
                    .get()
                    .await()

            for (restaurantDoc in restaurantsSnapshot.documents) {
                val votesSnapshot =
                    db
                        .collection("events")
                        .document(eventId)
                        .collection("restaurants")
                        .document(restaurantDoc.id)
                        .collection("votes")
                        .limit(1)
                        .get()
                        .await()

                if (!votesSnapshot.isEmpty) {
                    return true
                }
            }
            false
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Determines the winning placeId based on vote counts.
     *
     * This is a pure function with no side effects. It selects the placeId with the
     * highest number of votes. If multiple placeIds are tied for the highest count,
     * one of them is chosen at random (tie‑breaker).
     *
     * @param voteCounts A map of placeId → vote count.
     * @return The winning placeId.
     * @throws IllegalArgumentException If the map is empty.
     */
    fun pickWinningPlace(voteCounts: Map<String, Int>): String {
        require(voteCounts.isNotEmpty()) { "No votes found" }

        val maxVotes = voteCounts.maxOf { it.value }
        val topPlaces = voteCounts.filter { it.value == maxVotes }.keys

        return topPlaces.random() // tie-breaker
    }

    /**
     * Determines the winning DateTime from a list of votes.
     *
     * This is a pure function with no side effects. It extracts all non-null
     * dateTime values from the provided votes and selects the one with the highest
     * frequency. If multiple DateTime values are tied for the highest count,
     * one of them is chosen at random (tie‑breaker).
     *
     * @param votes The list of votes containing optional dateTime selections.
     * @return The winning DateTime.
     * @throws IllegalArgumentException If no valid dateTime values exist.
     */
    fun pickWinningTime(votes: List<Vote>): DateTime {
        val timings = votes.mapNotNull { it.dateTime }
        require(timings.isNotEmpty()) { "No timings found" }

        val grouped = timings.groupBy { it }
        val maxCount = grouped.maxOf { it.value.size }
        val topTimes = grouped.filter { it.value.size == maxCount }.keys

        return topTimes.random() // tie-breaker
    }

    /**
     * Aggregates restaurant votes for the given event and updates the event document
     * with the final selected place and time.
     *
     * Workflow:
     * 1. Fetches all restaurants under `events/{eventId}/restaurants`.
     * 2. For each restaurant, fetches its `votes` subcollection and counts votes.
     * 3. Uses `pickWinningPlace` to determine the winning placeId.
     * 4. Uses `pickWinningTime` to determine the winning DateTime for that place.
     * 5. Updates the event document with:
     *    - finalPlace (winning placeId)
     *    - finalTime (winning DateTime)
     *    - status = FINALIZED
     * 6. Calls `syncEventById(eventId)` to refresh local cache.
     *
     * This function handles Firestore I/O and error wrapping. The winner selection
     * logic is delegated to pure helper functions for easier testing.
     *
     * @param eventId The ID of the event whose restaurant votes should be aggregated.
     * @return [Result.success] if aggregation and update succeed, or [Result.failure]
     *         if no votes exist or a Firestore error occurs.
     */
    override suspend fun aggregateRestaurantVotes(eventId: String): Result<Unit> {
        return try {
            val restaurantsSnapshot =
                db
                    .collection("events")
                    .document(eventId)
                    .collection("restaurants")
                    .get()
                    .await()

            // Count votes per placeId
            val voteCounts = mutableMapOf<String, Int>()
            val votesByPlace = mutableMapOf<String, List<Vote>>()

            restaurantsSnapshot.documents.forEach { restaurantDoc ->
                val placeId = restaurantDoc.id
                val votesSnapshot =
                    db
                        .collection("events")
                        .document(eventId)
                        .collection("restaurants")
                        .document(placeId)
                        .collection("votes")
                        .get()
                        .await()

                val votes =
                    votesSnapshot.documents.mapNotNull {
                        it.toObject(Vote::class.java)
                    }
                voteCounts[placeId] = votes.size
                votesByPlace[placeId] = votes
            }

            if (voteCounts.isEmpty()) return Result.failure(Exception("No votes found"))

            val winnerPlaceId = pickWinningPlace(voteCounts)
            val winnerTime = pickWinningTime(votesByPlace[winnerPlaceId] ?: emptyList())

            // Save to events document
            db
                .collection("events")
                .document(eventId)
                .update(
                    mapOf(
                        "finalPlace" to winnerPlaceId,
                        "finalTime" to winnerTime,
                        "status" to EventStatus.FINALIZED.name,
                    ),
                ).await()

            syncEventById(eventId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Observes restaurant votes for the given event and returns a list of [Vote] objects.
     * @param eventId The ID of the event to observe votes for.
     * @return A [Flow] emitting a list of [Vote] objects.
     */
    override fun observeRestaurantVotes(eventId: String): Flow<List<Vote>> =
        callbackFlow {
            val listeners = mutableListOf<ListenerRegistration>()

            val mainListener =
                db
                    .collection("events")
                    .document(eventId)
                    .collection("restaurants")
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            close(error)
                            return@addSnapshotListener
                        }

                        // Remove old listeners for votes
                        listeners.forEach { it.remove() }
                        listeners.clear()

                        val restaurantDocs = snapshot?.documents ?: emptyList()
                        if (restaurantDocs.isEmpty()) {
                            trySend(emptyList())
                            return@addSnapshotListener
                        }

                        val allVotesMap = mutableMapOf<String, List<Vote>>()

                        restaurantDocs.forEach { restaurantDoc ->
                            val listener =
                                restaurantDoc.reference
                                    .collection("votes")
                                    .addSnapshotListener { voteSnapshot, voteError ->
                                        if (voteError != null) return@addSnapshotListener

                                        val votes =
                                            voteSnapshot?.documents?.mapNotNull { doc ->
                                                doc
                                                    .toObject(Vote::class.java)
                                                    ?.copy(placeId = restaurantDoc.id)
                                            } ?: emptyList()

                                        allVotesMap[restaurantDoc.id] = votes

                                        // Emit the union of all votes collected so far
                                        trySend(allVotesMap.values.flatten())
                                    }
                            listeners.add(listener)
                        }
                    }

            awaitClose {
                mainListener.remove()
                listeners.forEach { it.remove() }
            }
        }

    override suspend fun getParticipantResponse(
        eventId: String,
        userId: String,
    ): ParticipantResponse? =
        try {
            db
                .collection("events")
                .document(eventId)
                .collection("participantResponses")
                .document(userId)
                .get()
                .await()
                .toObject(ParticipantResponse::class.java)
        } catch (_: Exception) {
            null
        }

    /**
     * Observes the participant response for a specific user and event in real-time.
     * @param eventId The ID of the event.
     * @param userId The ID of the user.
     * @return A [Flow] emitting the [ParticipantResponse] object for the user.
     */
    override fun observeParticipantResponse(
        eventId: String,
        userId: String,
    ): Flow<ParticipantResponse?> =
        callbackFlow {
            val listener =
                db
                    .collection("events")
                    .document(eventId)
                    .collection("participantResponses")
                    .document(userId)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            close(error)
                            return@addSnapshotListener
                        }
                        val response = snapshot?.toObject(ParticipantResponse::class.java)
                        if (response != null) {
                            // Also update Room cache
                            CoroutineScope(Dispatchers.IO).launch {
                                val entity =
                                    with(ParticipantResponseMapper) { response.toEntity(eventId) }
                                participantResponseDao.upsertResponses(listOf(entity))
                            }
                        }
                        trySend(response)
                    }
            awaitClose { listener.remove() }
        }

    /**
     * Checks if the user has submitted availability for the given event.
     * @param eventId The ID of the event.
     * @param userId The ID of the user.
     * @return `true` if the user has submitted availability, `false` otherwise.
     */
    override suspend fun hasUserSubmittedAvailability(
        eventId: String,
        userId: String,
    ): Boolean =
        try {
            val snapshot =
                db
                    .collection("events")
                    .document(eventId)
                    .collection("participantResponses")
                    .document(userId)
                    .get()
                    .await()
            snapshot.exists()
        } catch (_: Exception) {
            false
        }

    /**
     * Retrieves a list of restaurants for a given location.
     *
     * @param eventId The event for which to retrieve restaurants.
     * @param targetTime The time for which to retrieve restaurants.
     * @param forceRefresh Whether to force a refresh of the data.
     * @return A [Flow] emitting a list of [Restaurant] objects.
     */
    override suspend fun getRestaurantsOnce(
        eventId: String,
        targetTime: DateTime?,
        forceRefresh: Boolean,
    ): List<Restaurant> {
        Log.d("getRestaurantsOnce", "Syncing restaurants for eventId: $eventId")

        if (forceRefresh) {
            syncRestaurants(eventId)
        }

        val result =
            restaurantDao
                .getRestaurantsOnce(eventId)
                .map { with(RestaurantMapper) { it.toDomain() } }
        Log.d("getRestaurantsOnce", "Got ${result.size} restaurants from Room")
        return result
    }
}
