package com.meetup.meetingapp

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.printToLog
import androidx.navigation.compose.rememberNavController
import androidx.test.platform.app.InstrumentationRegistry
import com.meetup.meetingapp.data.AppContainer
import com.meetup.meetingapp.data.repositories.EventRepository
import com.meetup.meetingapp.data.repositories.PlacesRepository
import com.meetup.meetingapp.data.repositories.UserRepository
import com.meetup.meetingapp.ui.navigation.MeetingAppNavHost
import com.meetup.meetingapp.ui.theme.MeetingAppTheme
import com.google.firebase.firestore.FirebaseFirestore
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import net.bytebuddy.matcher.ElementMatchers.returns
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class EventCreationFlowTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var mockEventRepository: EventRepository
    private lateinit var mockUserRepository: UserRepository

    @Before
    fun setUp() {
        // 1. Get the application instance
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val app = instrumentation.targetContext.applicationContext as MeetingApplication
        
        // 2. Create mocks
        mockEventRepository = mockk(relaxed = true)
        mockUserRepository = mockk(relaxed = true)
        
        // 3. Define mock behavior for the repository
        every { mockEventRepository.getCitiesByCountry(any()) } returns flowOf(listOf("Helsinki", "Tampere", "Espoo"))
        every { mockEventRepository.getEvents() } returns flowOf(emptyList())
        every { mockEventRepository.getEventById(any()) } returns flowOf(null)
        every { mockEventRepository.observeEventById(any()) } returns flowOf(null)
        coEvery { mockEventRepository.hasUserSubmittedAvailability(any(), any()) } returns false
        
        // 4. Mock the event creation response
        coEvery { mockEventRepository.createEvent(any()) } answers {
//            returns Result.success(Triple("ABCDEF", "12345", "test_event_id"))
            Result.success(Triple("ABCDEF", "12345", "test_event_id"))
        }

        // 5. Replace the real container with a test container providing mocks
        app.container = object : AppContainer {
            override val userRepository = mockUserRepository
            override val eventRepository = mockEventRepository
            override val placesRepository = mockk<PlacesRepository>(relaxed = true)
            override val db = mockk<FirebaseFirestore>(relaxed = true)
            override val placesApiKey = "test_api_key"
        }
    }

    @Test
    fun eventCreationFlow_stepsInOrder() {
        composeTestRule.setContent {
            MeetingAppTheme {
                val navController = rememberNavController()
                MeetingAppNavHost(navController = navController)
            }
        }

        // 1. Home Screen - Click Create Event
        composeTestRule.onNodeWithText("Create Event").performClick()

        // 2. CreatingEventPage (Step 1: Event Details)
        composeTestRule.onNodeWithText("Create an Event").assertIsDisplayed()
        composeTestRule.onNodeWithText("Enter title").performTextInput("Test Team Lunch")
        composeTestRule.onNodeWithText("Enter host name").performTextInput("Bobby")

        // Select Date Range (Simplified for test)
        composeTestRule.onNodeWithText("Select New Date Range").performClick()
        composeTestRule.onNodeWithText("SAVE").performClick()

        composeTestRule.onNodeWithText("Next").assertIsEnabled().performClick()

        // 3. AddTimeSlotsPage (Step 2: Time Slots)
        composeTestRule.onNodeWithText("Add Time Slots").assertIsDisplayed()
        composeTestRule.onNodeWithText("Next").performClick()

        // 4. AreaSelectingPage (Step 3: Location)
        // Wait for the location selection screen to be fully loaded
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithText("Choose Meeting Location").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("Choose Meeting Location").assertIsDisplayed()

        // Select country finland
        composeTestRule.onNodeWithText("Search Countries")
            .performScrollTo()
            .performClick()

        composeTestRule.onNodeWithText("Finland").performClick()

        // Search and select Helsinki
        composeTestRule.onNodeWithText("Type city's name")
            .performScrollTo()
            .performClick()
        
        composeTestRule.onNodeWithText("Helsinki").performClick()
        composeTestRule.onNodeWithText("Next").performClick()

        // 5. CreateEventPage (Step 4: Place Types)
        composeTestRule.onNodeWithText("Choose Place Type").assertIsDisplayed()
        composeTestRule.onNodeWithText("Restaurant").performClick()
        composeTestRule.onNodeWithText("Create Event").performClick()


        // 6. EventCreatedPage (Success)
        // Wait for the success screen to appear by checking for the mocked event code
//        composeTestRule.onRoot().printToLog("DEBUG_TREE")
//        composeTestRule.waitUntil(timeoutMillis = 10000) {
//            composeTestRule.onAllNodesWithText("Event Created").fetchSemanticsNodes().isNotEmpty()
//        }
//
//        composeTestRule.onNodeWithText("Event Created").assertIsDisplayed()
//        composeTestRule.onNodeWithText("ABCDEF", substring = true).assertIsDisplayed()
//        composeTestRule.onNodeWithText("12345").assertIsDisplayed()
//        composeTestRule.onNodeWithText("Your Event Code").assertIsDisplayed()
    }
}
