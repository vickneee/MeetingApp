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
import com.meetup.meetingapp.data.model.ParticipantResponse
import com.meetup.meetingapp.data.model.PlaceType
import com.meetup.meetingapp.data.model.Restaurant
import com.meetup.meetingapp.data.model.Vote
import com.meetup.meetingapp.ui.screens.create_event_flow.EventUiState
import com.meetup.meetingapp.ui.screens.participant_input_flow.ParticipantInputState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
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
    private val restaurantDao : RestaurantDao,
    private val placesRepository: PlacesRepository
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
     * Retrieves a list of restaurants for a given location.
     *
     * @param location The location for which to retrieve restaurants.
     * @return A [Flow] emitting a list of [Restaurant] objects.
     */
    override fun getRestaurantsByLocation(location: String): Flow<List<Restaurant>> {
        return flowOf(emptyList())
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
                .filter { it.id.lowercase().endsWith("_cities") }
                .flatMap { doc ->
                    val country = doc.id
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
        val snapshot = db.collection("events")
            .document(eventId)
            .collection("restaurants")
            .limit(1)
            .get()
            .await()

        return !snapshot.isEmpty
    }

    /**
     * Saves a list of restaurant candidates to Firestore under:
     *     events/{eventId}/restaurants/{placeId}
     *
     * Workflow:
     *  1. Writes each restaurant document to Firestore.
     *  2. Calls [syncRestaurants] to pull the saved data into Room.
     *  3. Wraps the entire operation in a Result for safe error handling.
     *
     * Notes:
     *  - Each restaurant is stored using its placeId as the document ID.
     *  - If Firestore write succeeds but Room sync fails, the failure is propagated.
     *
     * @param eventId The ID of the event to which restaurants belong.
     * @param restaurants The list of restaurants to save.
     * @return Result.success(Unit) on success, or Result.failure(e) on any error.
     */
    override suspend fun saveAllRestaurants(
        eventId: String,
        restaurants: List<Restaurant>
    ): Result<Unit> {

        return try {
            val ref = db.collection("events")
                .document(eventId)
                .collection("restaurants")

            restaurants.forEach { restaurant ->
                ref.document(restaurant.placeId)
                    .set(restaurant)
                    .await()
            }

            val syncResult = syncRestaurants(eventId)

            syncResult.fold(
                onSuccess = {
                    Result.success(Unit)
                },
                onFailure = { e ->
                    Result.failure(e)
                }
            )
        } catch (e: Exception) {
            Log.e("EventRepository", "API error", e)
            Result.failure(e)
        }
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
    override suspend fun syncRestaurants(eventId: String):Result<Unit>{
        if (!hasRestaurantCandidates(eventId)){
            return Result.failure(Exception("No Restaurant information is available"))
        } else{
            try{
                val restaurants =  db.collection("events")
                    .document(eventId)
                    .collection("restaurants")
                    .get()
                    .await()
                    .mapNotNull { it.toObject(Restaurant::class.java) }
                    .map { it.toEntity(eventId) }

                restaurantDao.upsertRestaurants(restaurants)

                return Result.success(Unit)
            }catch (e: Exception) {
                return Result.failure(e)
            }
        }
    }

    /**
     * Returns a Flow of restaurant candidates for the given event from Room.
     *
     * Behavior:
     *  - Room is the single source of truth.
     *  - Emits updates automatically whenever the underlying database changes.
     *  - Converts Room entities → domain models using RestaurantMapper.
     *
     * Usage:
     *  - Collected by ViewModel to provide reactive UI updates.
     *
     * @param eventId The ID of the event whose restaurants should be observed.
     * @return A Flow emitting the list of restaurants for the event.
     */
    override fun getRestaurants(eventId: String): Flow<List<Restaurant>>{
        return restaurantDao.getRestaurants(eventId)
            .map { list ->
                list.map { entity ->
                    with(RestaurantMapper) { entity.toDomain() }
                }
            }
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
        dateTime: DateTime
    ): Result<Unit> {
        val userName = try {
            val response = db.collection("events")
                .document(eventId)
                .collection("participantResponses")
                .document(userId)
                .get()
                .await()
                .toObject(ParticipantResponse::class.java)
            
            if (response != null) {
                response.name
            } else {
                val eventDoc = db.collection("events").document(eventId).get().await().toObject(Event::class.java)
                if (eventDoc?.hostId == userId) {
                    eventDoc.hostName
                } else {
                    auth.currentUser?.displayName ?: "Unknown"
                }
            }
        } catch (e: Exception) {
            "Unknown"
        }

        val vote = Vote(dateTime = dateTime, userId = userId, userName = userName)
        return try {
            db.collection("events")
                .document(eventId)
                .collection("restaurants")
                .document(placeId)
                .collection("votes")
                .document("${userId}_${dateTime}")
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
        dateTime: DateTime
    ): Result<Boolean> {
        return try {
            val snapshot = db.collection("events")
                .document(eventId)
                .collection("restaurants")
                .document(placeId)
                .collection("votes")
                .document("${userId}_${dateTime}")
                .get()
                .await()

            Result.success(snapshot.exists())

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Retrieves a list of restaurants for a given location.
     *
     * @param event The event for which to retrieve restaurants.
     * @return A [Result] containing a list of [Restaurant] objects on success,
     */
    override suspend fun fetchAndSaveRestaurants(event: Event): Result<Unit> {
        val seen = mutableSetOf<String>()
        val allRestaurants = mutableListOf<Restaurant>()

        event.locationCandidates.forEach { city ->
            val combinations = event.placeTypeCandidates.flatMap { placeType ->
                when (placeType) {
                    PlaceType.RESTAURANT -> {
                        if (event.foodCategoryCandidates.isEmpty()) {
                            listOf(Triple(city, placeType, null))
                        } else {
                            event.foodCategoryCandidates.map { Triple(city, placeType, it) }
                        }
                    }
                    PlaceType.CAFE -> listOf(Triple(city, placeType, null))
                    PlaceType.BAR -> listOf(Triple(city, placeType, null))
                }
            }.shuffled().take(10)

            combinations.forEach { (city, placeType, foodCategory) ->
                val query = when {
                    placeType == PlaceType.RESTAURANT && foodCategory != null ->
                        "${foodCategory.queryName} restaurant in $city"
                    placeType == PlaceType.CAFE -> "cafe in $city"
                    placeType == PlaceType.BAR -> "bar in $city"
                    else -> "${placeType.queryName} in $city"
                }
                placesRepository.fetchRestaurants(query).onSuccess { restaurants ->
                    restaurants.firstOrNull()?.let { restaurant ->
                        if (seen.add(restaurant.placeId)) {
                            // TAG with the city name
                            allRestaurants.add(restaurant.copy(searchLocation = city))
                        }
                    }
                }
            }
        }

        return if (allRestaurants.isNotEmpty()) {
            saveAllRestaurants(event.id, allRestaurants).also {
                if (it.isSuccess) {
                    updateEventStatus(event.id, EventStatus.COLLECTING_RESTAURANT_VOTES)
                }
            }
        } else {
            Result.failure(Exception("No restaurants found"))
        }
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
        timings: List<DateTime>
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
            val restaurantsSnapshot = db.collection("events")
                .document(eventId)
                .collection("restaurants")
                .get()
                .await()

            for (restaurantDoc in restaurantsSnapshot.documents) {
                val votesSnapshot = db.collection("events")
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
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Aggregates all participant responses for the given event and updates the event document
     * with the majority-voted candidates (date/time, location, place type, and food category).
     * @param eventId The ID of the event whose participant responses should be aggregated.
     * @return [Result.success] if aggregation and update succeed, or [Result.failure]
     *         if no responses are found or any Firestore/processing error occurs.
     */
    override suspend fun aggregateRestaurantVotes(eventId: String): Result<Unit> {
        return try {
            val restaurantsSnapshot = db.collection("events")
                .document(eventId)
                .collection("restaurants")
                .get()
                .await()

            // Count votes per placeId
            val voteCounts = mutableMapOf<String, Int>()
            val votesByPlace = mutableMapOf<String, List<Vote>>()

            restaurantsSnapshot.documents.forEach { restaurantDoc ->
                val placeId = restaurantDoc.id
                val votesSnapshot = db.collection("events")
                    .document(eventId)
                    .collection("restaurants")
                    .document(placeId)
                    .collection("votes")
                    .get()
                    .await()

                val votes = votesSnapshot.documents.mapNotNull {
                    it.toObject(Vote::class.java)
                }
                voteCounts[placeId] = votes.size
                votesByPlace[placeId] = votes
            }

            if (voteCounts.isEmpty()) return Result.failure(Exception("No votes found"))

            // Find winning placeId — random tiebreaker if tied
            val maxVotes = voteCounts.maxOfOrNull { it.value }
                ?: return Result.failure(Exception("No votes found"))
            val winnerPlaceId = voteCounts
                .filter { it.value == maxVotes }
                .keys.random()

            // Find most common DateTime from winner's votes — random tiebreaker if tied
            val winnerVotes = votesByPlace[winnerPlaceId] ?: emptyList()
            val groupedTimings = winnerVotes
                .mapNotNull { it.dateTime }
                .groupBy { it }
            val maxTimingVotes = groupedTimings.maxOfOrNull { it.value.size }
                ?: return Result.failure(Exception("No timings found in winner votes"))
            val winnerTime = groupedTimings
                .filter { it.value.size == maxTimingVotes }
                .keys.random()

            // Save to events document
            db.collection("events")
                .document(eventId)
                .update(
                    mapOf(
                        "finalPlace" to winnerPlaceId,
                        "finalTime" to winnerTime,
                        "status" to EventStatus.FINALIZED.name
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
     * Observes restaurant votes for the given event and returns a list of [Vote] objects.
     * @param eventId The ID of the event to observe votes for.
     * @return A [Flow] emitting a list of [Vote] objects.
     */
    override fun observeRestaurantVotes(eventId: String): Flow<List<Vote>> = callbackFlow {
        val listeners = mutableListOf<ListenerRegistration>()

        val mainListener = db.collection("events")
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
                    val listener = restaurantDoc.reference.collection("votes")
                        .addSnapshotListener { voteSnapshot, voteError ->
                            if (voteError != null) return@addSnapshotListener

                            val votes = voteSnapshot?.documents?.mapNotNull {
                                it.toObject(Vote::class.java)
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

    override suspend fun getParticipantResponse(eventId: String, userId: String): ParticipantResponse? {
        return try {
            db.collection("events")
                .document(eventId)
                .collection("participantResponses")
                .document(userId)
                .get()
                .await()
                .toObject(ParticipantResponse::class.java)
        } catch (e: Exception) {
            null
        }
    }
}
