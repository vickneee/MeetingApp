package com.meetup.meetingapp

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.rememberNavController
import androidx.test.platform.app.InstrumentationRegistry
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.meetup.meetingapp.data.AppContainer
import com.meetup.meetingapp.data.model.DateTime
import com.meetup.meetingapp.data.model.Event
import com.meetup.meetingapp.data.model.EventStatus
import com.meetup.meetingapp.data.model.ParticipantResponse
import com.meetup.meetingapp.data.model.Restaurant
import com.meetup.meetingapp.data.model.TimeSlot
import com.meetup.meetingapp.data.repositories.EventRepository
import com.meetup.meetingapp.data.repositories.PlacesRepository
import com.meetup.meetingapp.data.repositories.SubmissionRepository
import com.meetup.meetingapp.data.repositories.UserRepository
import com.meetup.meetingapp.ui.navigation.MeetingAppNavHost
import com.meetup.meetingapp.ui.screens.participantinput.ParticipantInputState
import com.meetup.meetingapp.ui.theme.MeetingAppTheme
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import kotlinx.coroutines.flow.flowOf
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class PlaceVotingFlowTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var mockEventRepository: EventRepository
    private lateinit var mockUserRepository: UserRepository
    private lateinit var mockFirebaseAuth: FirebaseAuth
    private lateinit var mockFirebaseUser: FirebaseUser

    private val testEventId = "test_event_id"
    private val testUserId = "test_user_id"
    private val testRestaurantId = "test_restaurant_id"

    @Before
    fun setUp() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val app = instrumentation.targetContext.applicationContext as MeetingApplication

        mockUserRepository = mockk(relaxed = true)
        mockFirebaseAuth = mockk(relaxed = true)
        mockFirebaseUser = mockk(relaxed = true)

        val baseMockRepository = mockk<EventRepository>(relaxed = true)

        // Workaround for MockK issues with suspend functions returning Result value class.
        // Implementing the methods in an anonymous object avoids dynamic proxy issues with value classes.
        mockEventRepository = object : EventRepository by baseMockRepository {
            override suspend fun getUserVote(
                eventId: String,
                placeId: String,
                userId: String,
                dateTime: DateTime
            ): Result<Boolean> = Result.success(false)

            override suspend fun submitVote(
                eventId: String,
                placeId: String,
                userId: String,
                dateTime: DateTime
            ): Result<Unit> = Result.success(Unit)

            override suspend fun createParticipantAvailability(
                participantInput: ParticipantInputState
            ): Result<Unit> = Result.success(Unit)

            override suspend fun aggregateParticipantResponses(eventId: String): Result<Unit> =
                Result.success(Unit)

            override suspend fun aggregateRestaurantVotes(eventId: String): Result<Unit> =
                Result.success(Unit)

            override suspend fun saveAllRestaurants(
                eventId: String,
                restaurants: List<Restaurant>
            ): Result<Unit> = Result.success(Unit)

            override suspend fun syncRestaurants(eventId: String): Result<Unit> =
                Result.success(Unit)

            override suspend fun fetchAndSaveRestaurants(event: Event): Result<Unit> =
                Result.success(Unit)
        }

        mockkStatic(FirebaseAuth::class)
        every { FirebaseAuth.getInstance() } returns mockFirebaseAuth
        every { mockFirebaseAuth.currentUser } returns mockFirebaseUser
        every { mockFirebaseUser.uid } returns testUserId

        val testTiming = DateTime(
            date = "2024-05-28",
            timeSlot = TimeSlot("11:00", "13:00")
        )

        val testEvent = Event(
            id = testEventId,
            eventCode = "VOTE12",
            eventKey = "key123",
            eventTitle = "Voting Dinner",
            hostName = "Alice",
            hostId = "host_id",
            status = EventStatus.COLLECTING_RESTAURANT_VOTES,
            dateTimeCandidates = listOf(testTiming),
            locationCandidates = listOf("Helsinki")
        )

        val testRestaurant = Restaurant(
            placeId = testRestaurantId,
            name = "Test Restaurant",
            rating = 4.8,
            userRatingCount = 100,
            address = "Mannerheimintie 1, Helsinki",
            types = listOf("restaurant"),
            priceLevel = 2,
            latitude = 60.1699,
            longitude = 24.9384,
            openingHours = listOf("Tuesday: 00:00–23:59")
        )

        val testParticipant = ParticipantResponse(
            userId = testUserId,
            name = "Bobby"
        )

        // Mock behaviors on the base delegate
        every { baseMockRepository.observeEventById(testEventId) } returns flowOf(testEvent)
        every { baseMockRepository.getEventById(testEventId) } returns flowOf(testEvent)
        coEvery { baseMockRepository.hasRestaurantCandidates(testEventId) } returns true
        coEvery { baseMockRepository.getRestaurantsOnce(testEventId, any(), any()) } returns listOf(testRestaurant)
        every { baseMockRepository.observeSubmissions(testEventId) } returns flowOf(listOf(testParticipant))
        every { baseMockRepository.observeRestaurantVotes(testEventId) } returns flowOf(emptyList())
        coEvery { baseMockRepository.hasUserSubmittedAvailability(testEventId, testUserId) } returns true
        coEvery { baseMockRepository.getParticipantResponse(testEventId, testUserId) } returns testParticipant
        coEvery { baseMockRepository.hasUserVotedInEvent(any(), any(), any()) } returns false

        // Replace app container
        app.container = object : AppContainer {
            override val userRepository = mockUserRepository
            override val eventRepository = mockEventRepository
            override val placesRepository = mockk<PlacesRepository>(relaxed = true)
            override val submissionRepository = mockk<SubmissionRepository>(relaxed = true)
            override val db = mockk<FirebaseFirestore>(relaxed = true)
            override val placesApiKey = "test_api_key"
        }
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun voteForPlaceFlow_stepsInOrder() {
        composeTestRule.setContent {
            MeetingAppTheme {
                val navController = rememberNavController()
                MeetingAppNavHost(navController = navController)

                // Directly navigate to Participant Dashboard for the test event
                LaunchedEffect(Unit) {
                    navController.navigate("participant_dashboard_waiting/$testEventId")
                }
            }
        }

        // 1. Participant Dashboard
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithText("VOTE12", substring = true).fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("Event Code: VOTE12").assertIsDisplayed()
        composeTestRule.onNodeWithText("Vote Time & Place").performClick()

        // 2. DateAndAreaPage (Step 1)
        composeTestRule.waitUntil(timeoutMillis = 10000) {
            composeTestRule.onAllNodesWithText("Choose a date, time & area").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("Choose a date, time & area").assertIsDisplayed()

        // Use a more flexible matcher for the option
        composeTestRule.onNodeWithText("Helsinki", substring = true).performClick()

        // 3. PlaceListPage (Step 2)
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithText("Test Restaurant").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("Test Restaurant").assertIsDisplayed()
        composeTestRule.onNodeWithText("Test Restaurant").performClick()

        // 4. PlaceDetailsPage (Step 3)
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithText("Vote for this restaurant").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("Test Restaurant").assertIsDisplayed()

        // Perform Vote
        composeTestRule.onNodeWithText("Vote for this restaurant").performClick()

        // 5. Back to Dashboard (Success)
        // Wait for the navigation to complete and the dashboard to show the welcome message
        composeTestRule.waitUntil(timeoutMillis = 20000) {
            composeTestRule.onAllNodesWithText("Hi, Bobby!", substring = true).fetchSemanticsNodes().isNotEmpty()
        }

        // Verify we are back and the correct user is welcomed
        composeTestRule.onAllNodesWithText("Hi, Bobby!", substring = true).onFirst().assertIsDisplayed()
        composeTestRule.onAllNodesWithText("Event Code: VOTE12", substring = true).onFirst().assertIsDisplayed()
    }
}
