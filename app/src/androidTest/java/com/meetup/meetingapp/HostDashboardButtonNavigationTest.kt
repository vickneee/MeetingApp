package com.meetup.meetingapp

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.navigation.compose.rememberNavController
import androidx.test.platform.app.InstrumentationRegistry
import com.google.firebase.firestore.FirebaseFirestore
import com.meetup.meetingapp.MeetingApplication
import com.meetup.meetingapp.data.AppContainer
import com.meetup.meetingapp.data.model.DateTime
import com.meetup.meetingapp.data.model.Event
import com.meetup.meetingapp.data.model.EventStatus
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
import kotlinx.coroutines.flow.flowOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * UI Test for navigation buttons on the Host Dashboard.
 */
class HostDashboardButtonNavigationTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var mockEventRepository: EventRepository
    private lateinit var mockSubmissionRepository: SubmissionRepository
    private lateinit var app: MeetingApplication

    @Before
    fun setUp() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        app = instrumentation.targetContext.applicationContext as MeetingApplication

        mockEventRepository = mockk(relaxed = true)
        mockSubmissionRepository = mockk(relaxed = true)

        // Default mock setup for repositories
        every { mockEventRepository.getEvents() } returns flowOf(emptyList())
        every { mockEventRepository.observeEventById(any()) } returns flowOf(null)
        every { mockEventRepository.observeSubmissions(any()) } returns flowOf(emptyList())
        every { mockEventRepository.observeRestaurantVotes(any()) } returns flowOf(emptyList())
        coEvery { mockEventRepository.hasUserSubmittedAvailability(any(), any()) } returns true
        coEvery { mockEventRepository.hasUserVotedInEvent(any(), any(), any()) } returns false
        every { mockEventRepository.getEventByEventCode(any()) } returns flowOf(null)

        app.container = object : AppContainer {
            override val userRepository = mockk<UserRepository>(relaxed = true)
            override val eventRepository = mockEventRepository
            override val placesRepository = mockk<PlacesRepository>(relaxed = true)
            override val submissionRepository = mockSubmissionRepository
            override val db = mockk<FirebaseFirestore>(relaxed = true)
            override val placesApiKey = "test_api_key"
        }
    }

    private fun setupDashboard(event: Event, hasSubmittedAvailability: Boolean = true) {
        every { mockEventRepository.observeEventById(event.id) } returns flowOf(event)
        every { mockEventRepository.getEventByEventCode(event.eventCode) } returns flowOf(event)
        coEvery { mockEventRepository.hasUserSubmittedAvailability(event.id, any()) } returns hasSubmittedAvailability

        composeTestRule.setContent {
            MeetingAppTheme {
                val navController = rememberNavController()
                MeetingAppNavHost(navController = navController)
                navController.navigate("host_dashboard/${event.id}")
            }
        }
    }

    @Test
    fun hostDashboard_clickVote_navigatesToDateAndArea() {
        val testTiming =
            DateTime(
                date = "2024-05-28",
                timeSlot = TimeSlot("11:00", "13:00"),
            )

        val testEvent = Event(
            id = "test_event_vote",
            eventCode = "VOTE12",
            eventKey = "key123",
            hostName = "Bobby",
            hostId = "test_user_id",
            status = EventStatus.COLLECTING_RESTAURANT_VOTES,
            eventTitle = "Vote Test",
            dateTimeCandidates = listOf(testTiming),
            locationCandidates = listOf("Helsinki")
        )

        val testRestaurant = Restaurant(
            placeId = "res123",
            name = "Test Restaurant",
            address = "Helsinki Center"
        )

        coEvery { mockEventRepository.hasRestaurantCandidates(testEvent.id) } returns true
        coEvery { mockEventRepository.getRestaurantsOnce(testEvent.id, any(), any()) } returns listOf(testRestaurant)

        setupDashboard(testEvent)

        // Click the "Vote Time & Place" button
        composeTestRule.onNodeWithText("Vote Time & Place").performClick()

        // Assert that the Date & Area selection page is displayed
        composeTestRule.waitUntil(timeoutMillis = 10000) {
            composeTestRule
                .onAllNodesWithText("Choose a date, time & area")
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        composeTestRule.onNodeWithText("Choose a date, time & area").assertIsDisplayed()
    }

    @Test
    fun hostDashboard_clickFinalPlan_navigatesToPlaceDetails() {
        val testEvent = Event(
            id = "test_event_final",
            eventCode = "FINAL1",
            eventKey = "key456",
            hostName = "Bobby",
            hostId = "test_user_id",
            status = EventStatus.FINALIZED,
            eventTitle = "Final Test",
            finalPlace = "some_place_id"
        )

        val testRestaurant = Restaurant(
            placeId = "some_place_id",
            name = "Test Restaurant",
            address = "123 Test St",
            rating = 4.5,
            userRatingCount = 100
        )

        coEvery { mockEventRepository.hasRestaurantCandidates(testEvent.id) } returns true
        coEvery { mockEventRepository.getRestaurantsOnce(testEvent.id, any(), any()) } returns listOf(testRestaurant)
        coEvery { mockEventRepository.getUserVote(any(), any(), any(), any()) } returns Result.success(false)

        setupDashboard(testEvent)

        // Click the "View Final Plan" button
        composeTestRule.onNodeWithText("View Final Plan").performClick()

        // Assert that the Place Details page is displayed
        composeTestRule.onNodeWithText("Place Details").assertIsDisplayed()
    }

    @Test
    fun hostDashboard_clickFillAvailability_navigatesToParticipantInput() {
        val testEvent = Event(
            id = "test_event_avail",
            eventCode = "AVAIL1",
            eventKey = "key789",
            hostName = "Bobby",
            hostId = "test_user_id",
            status = EventStatus.COLLECTING_AVAILABILITY,
            eventTitle = "Avail Test"
        )
        // Host has NOT submitted availability
        setupDashboard(testEvent, hasSubmittedAvailability = false)

        // Click the "Fill My Availability" button
        composeTestRule.onNodeWithText("Fill My Availability").performClick()

        // Assert that the MeetUp Details page (first step of participant input) is displayed
        composeTestRule.onNodeWithText("Enter your name").assertIsDisplayed()
    }

    @Test
    fun hostDashboard_clickHome_navigatesToHomeScreen() {
        val testEvent = Event(
            id = "test_event_home",
            eventCode = "HOME12",
            hostId = "test_user_id",
            status = EventStatus.COLLECTING_AVAILABILITY
        )
        setupDashboard(testEvent)

        // Click the "Home" button
        composeTestRule.onNodeWithText("Home").performScrollTo().performClick()

        // Assert that the Home screen is displayed
        composeTestRule.onNodeWithText("Create Event").assertIsDisplayed()
    }

    @Test
    fun hostDashboard_clickShowEventCodes_navigatesToEventCreatedPage() {
        val testEvent = Event(
            id = "test_event_codes",
            eventCode = "CODES1",
            eventKey = "secret_key",
            hostId = "test_user_id",
            status = EventStatus.COLLECTING_AVAILABILITY
        )
        setupDashboard(testEvent)

        // Click the "Show Event Codes" button
        composeTestRule.onNodeWithText("Show Event Codes").performScrollTo().performClick()

        // Wait for the success screen to appear
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithText("Event Created").fetchSemanticsNodes().isNotEmpty()
        }

        // Assert that the Event Created page is displayed with correct codes
        composeTestRule.onNodeWithText("Event Created").assertIsDisplayed()
        composeTestRule.onNodeWithText("CODES1", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("secret_key", substring = true).assertIsDisplayed()
    }
}
