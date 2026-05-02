package com.meetup.meetingapp

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
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
import com.meetup.meetingapp.ui.theme.MeetingAppTheme
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.flow.flowOf
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

        mockEventRepository = mockk(relaxed = true)
        mockUserRepository = mockk(relaxed = true)
        mockFirebaseAuth = mockk(relaxed = true)
        mockFirebaseUser = mockk(relaxed = true)

        mockkStatic(FirebaseAuth::class)
        every { FirebaseAuth.getInstance() } returns mockFirebaseAuth
        every { mockFirebaseAuth.currentUser } returns mockFirebaseUser
        every { mockFirebaseUser.uid } returns testUserId

        val testTiming = DateTime(
            date = "2024-05-20",
            timeSlot = TimeSlot("18:00", "21:00")
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
            longitude = 24.9384
        )

        val testParticipant = ParticipantResponse(
            userId = testUserId,
            name = "Bobby"
        )

        // Mock EventRepository behaviors
        every { mockEventRepository.observeEventById(testEventId) } returns flowOf(testEvent)
        every { mockEventRepository.getEventById(testEventId) } returns flowOf(testEvent)
        coEvery { mockEventRepository.hasRestaurantCandidates(testEventId) } returns true
        coEvery { mockEventRepository.getRestaurantsOnce(testEventId, any(), any()) } returns listOf(testRestaurant)
        every { mockEventRepository.observeSubmissions(testEventId) } returns flowOf(listOf(testParticipant))
        every { mockEventRepository.observeRestaurantVotes(testEventId) } returns flowOf(emptyList())
        coEvery { mockEventRepository.getUserVote(testEventId, testRestaurantId, testUserId, any()) } returns Result.success(false)
        coEvery { mockEventRepository.submitVote(testEventId, testRestaurantId, testUserId, any()) } returns Result.success(Unit)
        coEvery { mockEventRepository.hasUserSubmittedAvailability(testEventId, testUserId) } returns true
        coEvery { mockEventRepository.getParticipantResponse(testEventId, testUserId) } returns testParticipant
        coEvery { mockEventRepository.hasUserVotedInEvent(any(), any(), any()) } returns false

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

    @Test
    fun voteForPlaceFlow_stepsInOrder() {
        composeTestRule.setContent {
            MeetingAppTheme {
                val navController = rememberNavController()
                MeetingAppNavHost(navController = navController)
                
                // Directly navigate to Participant Dashboard for the test event
                navController.navigate("participant_dashboard_waiting/$testEventId")
            }
        }

        // 1. Participant Dashboard
        composeTestRule.onNodeWithText("Event Code: VOTE12").assertIsDisplayed()
        // Wait for potential loading if any, though we mocked it fast
        composeTestRule.onNodeWithText("Vote Time & Place").performClick()

        // 2. DateAndAreaPage (Step 1)
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithText("Choose a date, time & area").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("Choose a date, time & area").assertIsDisplayed()
        
        // Match the label: "May 20 (18:00–21:00) — Helsinki"
        // Note: dash between times is en-dash (–), dash before Helsinki is em-dash (—)
        composeTestRule.onNodeWithText("May 20 (18:00–21:00) — Helsinki").performClick()

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
        composeTestRule.onNodeWithText("Mannerheimintie 1, Helsinki").assertIsDisplayed()
        
        // Perform Vote
        composeTestRule.onNodeWithText("Vote for this restaurant").performClick()

        // 5. Back to Dashboard (Success)
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithText("Event Code: VOTE12").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("Event Code: VOTE12").assertIsDisplayed()
    }
}
