package com.meetup.meetingapp.data.repositories

import com.meetup.meetingapp.data.db.daos.EventDao
import com.meetup.meetingapp.data.db.mapper.FirestoreCity
import com.meetup.meetingapp.data.db.mapper.FirestoreCityList
import com.meetup.meetingapp.data.model.DateTime
import com.meetup.meetingapp.data.model.Event
import com.meetup.meetingapp.data.model.FoodCategory
import com.meetup.meetingapp.data.model.ParticipantResponse
import com.meetup.meetingapp.data.model.PlaceType
import com.meetup.meetingapp.data.model.TimeSlot
import com.meetup.meetingapp.data.model.Vote
import com.meetup.meetingapp.ui.screens.eventcreation.EventUiState
import com.meetup.meetingapp.ui.screens.participantinput.ParticipantInputState
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.verify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class EventRepositoryImpTest {
    val mockDb = mockk<com.google.firebase.firestore.FirebaseFirestore>(relaxed = true)
    private val mockEventDao = mockk<EventDao>(relaxed = true)
    private val mockUserRepo = mockk<UserRepository>(relaxed = true)
    private val mockCityDao = mockk<com.meetup.meetingapp.data.db.daos.CityDao>(relaxed = true)
    private val mockAuth = mockk<com.google.firebase.auth.FirebaseAuth>(relaxed = true)

    private val repo =
        EventRepositoryImp(
            db = mockDb,
            userRepository = mockUserRepo,
            eventDao = mockEventDao,
            cityDao = mockCityDao,
            participantResponseDao = mockk(relaxed = true),
            restaurantDao = mockk(relaxed = true),
            placesRepository = mockk(relaxed = true),
            auth = mockAuth,
        )

    @Test
    fun `returns majority candidates for all categories`() {
        val responses =
            listOf(
                ParticipantResponse(
                    dateTimes =
                        listOf(
                            DateTime("2026-04-23", TimeSlot("11:00", "13:00")),
                            DateTime("2026-04-24", TimeSlot("13:00", "16:00")),
                        ),
                    locations = listOf("Helsinki", "Vantaa"),
                    placeTypes = listOf(PlaceType.RESTAURANT),
                    foodCategories = listOf(FoodCategory.SUSHI, FoodCategory.STEAK),
                ),
                ParticipantResponse(
                    dateTimes =
                        listOf(
                            DateTime("2026-04-23", TimeSlot("11:00", "13:00")),
                        ),
                    locations = listOf("Helsinki", "Espoo"),
                    placeTypes = listOf(PlaceType.RESTAURANT, PlaceType.CAFE),
                    foodCategories = listOf(FoodCategory.STEAK),
                ),
            )

        val result = repo.aggregateCandidatesFromResponses(responses)

        assertEquals(
            listOf(DateTime("2026-04-23", TimeSlot("11:00", "13:00"))),
            result.dateTimeCandidates,
        )
        assertEquals(listOf("Helsinki"), result.locationCandidates)
        assertEquals(listOf(PlaceType.RESTAURANT), result.placeTypeCandidates)
        assertEquals(listOf(FoodCategory.STEAK), result.foodCategoryCandidates)
    }

    @Test
    fun `returns all top candidates when counts are tied`() {
        val responses =
            listOf(
                ParticipantResponse(
                    dateTimes = listOf(DateTime("2026-04-23", TimeSlot("11:00", "13:00"))),
                    locations = listOf("Helsinki"),
                    placeTypes = listOf(PlaceType.RESTAURANT),
                    foodCategories = listOf(FoodCategory.SUSHI),
                ),
                ParticipantResponse(
                    dateTimes = listOf(DateTime("2026-04-24", TimeSlot("13:00", "16:00"))),
                    locations = listOf("Espoo"),
                    placeTypes = listOf(PlaceType.CAFE),
                    foodCategories = listOf(FoodCategory.STEAK),
                ),
            )

        val result = repo.aggregateCandidatesFromResponses(responses)

        assertEquals(
            listOf(
                DateTime("2026-04-23", TimeSlot("11:00", "13:00")),
                DateTime("2026-04-24", TimeSlot("13:00", "16:00")),
            ),
            result.dateTimeCandidates,
        )
        assertEquals(listOf("Helsinki", "Espoo"), result.locationCandidates)
        assertEquals(listOf(PlaceType.RESTAURANT, PlaceType.CAFE), result.placeTypeCandidates)
        assertEquals(listOf(FoodCategory.SUSHI, FoodCategory.STEAK), result.foodCategoryCandidates)
    }

    @Test
    fun `returns candidates from single participant`() {
        val responses =
            listOf(
                ParticipantResponse(
                    dateTimes = listOf(DateTime("2026-04-23", TimeSlot("11:00", "13:00"))),
                    locations = listOf("Helsinki"),
                    placeTypes = listOf(PlaceType.RESTAURANT),
                    foodCategories = listOf(FoodCategory.SUSHI),
                ),
            )

        val result = repo.aggregateCandidatesFromResponses(responses)

        assertEquals(
            listOf(DateTime("2026-04-23", TimeSlot("11:00", "13:00"))),
            result.dateTimeCandidates,
        )
        assertEquals(listOf("Helsinki"), result.locationCandidates)
        assertEquals(listOf(PlaceType.RESTAURANT), result.placeTypeCandidates)
        assertEquals(listOf(FoodCategory.SUSHI), result.foodCategoryCandidates)
    }

    @Test
    fun `pickWinningPlace returns the only winner`() {
        val result = repo.pickWinningPlace(mapOf("A" to 5, "B" to 2))
        assertEquals("A", result)
    }

    @Test
    fun `pickWinningPlace returns one of tied winners`() {
        val winners = mutableSetOf<String>()
        repeat(20) {
            winners += repo.pickWinningPlace(mapOf("A" to 3, "B" to 3))
        }
        assertTrue(winners.all { it == "A" || it == "B" })
    }

    @Test(expected = IllegalArgumentException::class)
    fun `pickWinningPlace throws when no votes exist`() {
        repo.pickWinningPlace(emptyMap())
    }

    @Test
    fun `pickWinningTime returns the clear winner`() {
        val t1 = DateTime("2026-04-23", TimeSlot("11:00", "13:00"))
        val t2 = DateTime("2026-04-24", TimeSlot("13:00", "16:00"))

        val votes =
            listOf(
                Vote(dateTime = t1),
                Vote(dateTime = t1),
                Vote(dateTime = t1),
                Vote(dateTime = t2),
            )

        repeat(10) {
            val result = repo.pickWinningTime(votes)
            assertEquals(t1, result)
        }
    }

    @Test
    fun `pickWinningTime returns one of tied times`() {
        val t1 = DateTime("2026-04-23", TimeSlot("11:00", "13:00"))
        val t2 = DateTime("2026-04-24", TimeSlot("13:00", "16:00"))

        val votes =
            listOf(
                Vote(dateTime = t1),
                Vote(dateTime = t1),
                Vote(dateTime = t2),
                Vote(dateTime = t2),
            )

        val results = (1..20).map { repo.pickWinningTime(votes) }
        assertTrue(results.all { it == t1 || it == t2 })
    }

    @Test(expected = IllegalArgumentException::class)
    fun `pickWinningTime throws when no timings exist`() {
        repo.pickWinningTime(emptyList())
    }

    @Test
    fun `resolveUserName returns participant name when available`() {
        val response = ParticipantResponse(name = "Alice", userId = "user1")
        val event = Event(hostId = "host1", hostName = "HostUser")

        val result =
            repo.resolveUserName(
                participantResponse = response,
                event = event,
                currentUserName = null,
                userId = "user1",
            )

        assertEquals("Alice", result)
    }

    @Test
    fun `resolveUserName returns hostName when user is host`() {
        val response = ParticipantResponse(name = "", userId = "host1")
        val event = Event(hostId = "host1", hostName = "HostUser")

        val result =
            repo.resolveUserName(
                participantResponse = response,
                event = event,
                currentUserName = null,
                userId = "host1",
            )

        assertEquals("HostUser", result)
    }

    @Test
    fun `resolveUserName returns currentUserName when participant and host names unavailable`() {
        val event = Event(hostId = "host1", hostName = "HostUser")

        val result =
            repo.resolveUserName(
                participantResponse = null,
                event = event,
                currentUserName = "FirebaseUser",
                userId = "user1",
            )

        assertEquals("FirebaseUser", result)
    }

    @Test
    fun `resolveUserName returns Unknown when no name sources available`() {
        val event = Event(hostId = "host1", hostName = "HostUser")

        val result =
            repo.resolveUserName(
                participantResponse = null,
                event = event,
                currentUserName = null,
                userId = "user1",
            )

        assertEquals("Unknown", result)
    }

// --- REPOSITORY ACTION TESTS ---

    @Test
    fun `createEvent success stores to firestore and local database`() =
        runTest {
            // 1. Setup nested Firestore mocks
            val mockCollection = mockk<com.google.firebase.firestore.CollectionReference>(relaxed = true)
            val mockDoc = mockk<com.google.firebase.firestore.DocumentReference>(relaxed = true)
            val mockTask = mockk<com.google.android.gms.tasks.Task<Void>>()

            every { mockDb.collection("events") } returns mockCollection
            every { mockCollection.document() } returns mockDoc
            every { mockDoc.id } returns "generated_event_id"

            // 2. Mock Coroutine suspension (await)
            mockkStatic("kotlinx.coroutines.tasks.TasksKt")
            every { mockDoc.set(any()) } returns mockTask
            coEvery { mockTask.await() } returns mockk()

            val uiState = EventUiState(eventTitle = "New Meeting", hostName = "Alice")

            // 3. Act
            val result = repo.createEvent(uiState)

            // 4. Assert
            assertEquals("generated_event_id", result.third)

            coVerify { mockDoc.set(any()) }
            coVerify { mockEventDao.upsertEvent(any()) }

            unmockkStatic("kotlinx.coroutines.tasks.TasksKt")
        }

    @Test
    fun `createEvent failure throws exception`() =
        runTest {
            val mockCollection = mockk<com.google.firebase.firestore.CollectionReference>(relaxed = true)
            val mockDoc = mockk<com.google.firebase.firestore.DocumentReference>(relaxed = true)

            every { mockDb.collection("events") } returns mockCollection
            every { mockCollection.document() } returns mockDoc
            every { mockDoc.set(any()) } throws Exception("Network Error")

            val uiState = EventUiState(eventTitle = "Broken Event")

            try {
                repo.createEvent(uiState)
            } catch (e: Exception) {
                assertEquals("Network Error", e.message)
            }

            coVerify(exactly = 0) { mockEventDao.upsertEvent(any()) }
        }

    @Test
    fun `syncCities parses firestore documents and stores cities`() =
        runTest {
            // --- Firestore mocks ---
            val mockCollection = mockk<com.google.firebase.firestore.CollectionReference>()
            val mockQuerySnapshot = mockk<com.google.firebase.firestore.QuerySnapshot>()
            val mockTask = mockk<com.google.android.gms.tasks.Task<com.google.firebase.firestore.QuerySnapshot>>()

            val doc1 = mockk<com.google.firebase.firestore.DocumentSnapshot>()
            val ignoredDoc = mockk<com.google.firebase.firestore.DocumentSnapshot>()

            every { mockDb.collection("static_data") } returns mockCollection
            every { mockCollection.get() } returns mockTask

            mockkStatic("kotlinx.coroutines.tasks.TasksKt")
            coEvery { mockTask.await() } returns mockQuerySnapshot

            every { mockQuerySnapshot.documents } returns listOf(doc1, ignoredDoc)

            // --- IDs ---
            every { doc1.id } returns "finland_cities"
            every { ignoredDoc.id } returns "random_data"

            // --- Firestore data ---
            val finlandData =
                FirestoreCityList(
                    items =
                        listOf(
                            FirestoreCity("Helsinki", "helsinki finland"),
                            FirestoreCity("Espoo", "espoo finland"),
                        ),
                )

            every { doc1.toObject(FirestoreCityList::class.java) } returns finlandData
            every { ignoredDoc.toObject(FirestoreCityList::class.java) } returns null

            // --- Act ---
            repo.syncCities()

            // --- Assert ---
            coVerify {
                mockCityDao.upsertCities(
                    match { cities ->
                        cities.size == 2 &&
                            cities.any { it.name == "Helsinki" && it.country == "Finland" } &&
                            cities.any { it.name == "Espoo" && it.country == "Finland" }
                    },
                )
            }

            unmockkStatic("kotlinx.coroutines.tasks.TasksKt")
        }

    @Test
    fun `createParticipantAvailability success stores response to firestore`() = runTest {
        // 1. Setup Auth
        val mockUser = mockk<com.google.firebase.auth.FirebaseUser>()
        every { mockAuth.currentUser } returns mockUser
        every { mockUser.uid } returns "test_user_456"

        // 2. Setup Task Mock
        val mockTask = mockk<com.google.android.gms.tasks.Task<Void>>(relaxed = true)
        mockkStatic("kotlinx.coroutines.tasks.TasksKt")

        // Force the task to appear completed immediately
        every { mockTask.isComplete } returns true
        every { mockTask.isSuccessful } returns true
        coEvery { mockTask.await() } returns mockk<Void>()

        // 3. THE "DEEP MOCK": Catch the chain regardless of exact path calls
        // This mocks db.collection(...).document(...).collection(...).document(...).set(...)
        every {
            mockDb.collection(any())
                .document(any())
                .collection(any())
                .document(any())
                .set(any())
        } returns mockTask

        val input = ParticipantInputState(
            eventId = "event_abc",
            participantName = "Bob",
            selectedLocations = listOf("Helsinki")
        )

        // 4. Act
        val result = repo.createParticipantAvailability(input)

        // 5. Assert
        assertTrue("Error message: ${result.exceptionOrNull()?.message}", result.isSuccess)

        unmockkStatic("kotlinx.coroutines.tasks.TasksKt")
    }

    @Test
    fun `createParticipantAvailability returns failure when user not logged in`() = runTest {
        // Mock current user as null
        every { mockAuth.currentUser } returns null

        val input = ParticipantInputState(
            eventId = "any_id",
            participantName = "Bob"
        )

        // Act
        val result = repo.createParticipantAvailability(input)

        // Assert
        assertTrue("Result should be failure", result.isFailure)
        assertEquals("User is not logged in", result.exceptionOrNull()?.message)

        // Verify Firestore was never called
        verify(exactly = 0) { mockDb.collection(any()) }
    }

    @Test
    fun `createParticipantAvailability returns failure when firestore fails`() = runTest {
        // Setup Auth
        val mockUser = mockk<com.google.firebase.auth.FirebaseUser>()
        every { mockAuth.currentUser } returns mockUser
        every { mockUser.uid } returns "test_user_456"

        // Mock firestore to throw exception
        every { mockDb.collection(any()) } throws Exception("Firestore Error")

        val input = ParticipantInputState(eventId = "id", participantName = "Bob")

        // Act
        val result = repo.createParticipantAvailability(input)

        // Assert
        assertTrue(result.isFailure)
        assertEquals("Firestore Error", result.exceptionOrNull()?.message)
    }
}
