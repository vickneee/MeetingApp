package com.meetup.meetingapp

import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.navigation.compose.rememberNavController
import androidx.test.platform.app.InstrumentationRegistry
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.meetup.meetingapp.data.AppContainer
import com.meetup.meetingapp.data.model.DateRange
import com.meetup.meetingapp.data.model.Event
import com.meetup.meetingapp.data.model.LocationOption
import com.meetup.meetingapp.data.model.PlaceType
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
import kotlinx.coroutines.tasks.await
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ParticipantInputFlowTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var mockEventRepository: EventRepository
    private lateinit var mockUserRepository: UserRepository

    @Before
    fun setUp() {
        mockkStatic("kotlinx.coroutines.tasks.TasksKt")
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val app = instrumentation.targetContext.applicationContext as MeetingApplication

        mockEventRepository = mockk(relaxed = true)
        mockUserRepository = mockk(relaxed = true)

        val testEvent = Event(
            id = "test_event_id",
            eventCode = "ABCDEF",
            eventKey = "12345",
            hostName = "Bobby",
            hostId = "host_id",
            eventTitle = "Test Event",
            dateRange = DateRange("2025-04-13", "2025-04-13"),
            timeSlots = listOf(TimeSlot("10:00", "12:00")),
            locationOptions = LocationOption(cities = listOf("Helsinki")),
            placeTypeOptions = listOf(PlaceType.RESTAURANT)
        )

        every { mockEventRepository.getEventByEventCode("ABCDEF") } returns flowOf(testEvent)
        every { mockEventRepository.getEventById("test_event_id") } returns flowOf(testEvent)
        every { mockEventRepository.observeSubmissions("test_event_id") } returns flowOf(emptyList())
        coEvery { mockEventRepository.getParticipantResponse(any(), any()) } returns null
        coEvery { mockEventRepository.syncEventByEventCodeAndKey(any(), any()) } returns Unit
        coEvery { mockEventRepository.createParticipantAvailability(any()) } returns Result.success(Unit)

        val mockDb = mockk<FirebaseFirestore>(relaxed = true)
        val mockCollection = mockk<CollectionReference>(relaxed = true)
        val mockQuery = mockk<Query>(relaxed = true)
        val mockSnapshot = mockk<QuerySnapshot>(relaxed = true)
        val mockDocument = mockk<DocumentSnapshot>(relaxed = true)

        every { mockDb.collection("events") } returns mockCollection
        every { mockCollection.whereEqualTo(any<String>(), any()) } returns mockQuery
        every { mockQuery.whereEqualTo(any<String>(), any()) } returns mockQuery
        
        val mockTask = mockk<com.google.android.gms.tasks.Task<QuerySnapshot>>(relaxed = true)
        every { mockQuery.get() } returns mockTask
        coEvery { mockTask.await() } returns mockSnapshot

        every { mockSnapshot.isEmpty } returns false
        every { mockSnapshot.documents } returns listOf(mockDocument)
        every { mockDocument.id } returns "test_event_id"
        every { mockDocument.getString("status") } returns "COLLECTING_AVAILABILITY"

        app.container = object : AppContainer {
            override val userRepository = mockUserRepository
            override val eventRepository = mockEventRepository
            override val placesRepository = mockk<PlacesRepository>(relaxed = true)
            override val submissionRepository = mockk<SubmissionRepository>(relaxed = true)
            override val db = mockDb
            override val placesApiKey = "test_api_key"
        }
    }

    @Test
    fun participantInputFlow_stepsInOrder() {
        composeTestRule.setContent {
            MeetingAppTheme {
                val navController = rememberNavController()
                MeetingAppNavHost(navController = navController)
            }
        }

        // 1. Home Screen - Click Join Event
        composeTestRule.onNodeWithText("Join Event").performClick()

        // 2. Join Page - Enter Code and Key
        composeTestRule.onNodeWithText("Join an Event").assertIsDisplayed()
        composeTestRule.onNodeWithText("Enter code").performTextInput("ABCDEF")
        composeTestRule.onNodeWithText("Enter key").performTextInput("12345")
        composeTestRule
            .onNode(hasText("Join Event") and
                SemanticsMatcher.expectValue(
                    SemanticsProperties.Role,
                    Role.Button,
                    ),
            ).performClick()

        // 3. MeetUpDetailPage (Step 1)
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithText("Hi, there!").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("Hi, there!").assertIsDisplayed()
        composeTestRule.onNodeWithText("Enter your name").performTextInput("Alice")
        composeTestRule.onNodeWithText("Next").performClick()

        // 4. AvailabilitySelectingPage (Step 2)
        composeTestRule.onNodeWithText("Choose all dates and time").assertIsDisplayed()
        // Select availability (dropdown)
        composeTestRule.onNodeWithText("Select availability").performClick()
        composeTestRule.onNodeWithText("13.04.2025: 10:00 - 12:00").performClick()
        // Click outside or find Next
        composeTestRule.onNodeWithText("Next").performClick()

        // 5. SmallAreaSelectingPage (Step 3)
        composeTestRule.onNodeWithText("Choose the area where you").assertIsDisplayed()
        composeTestRule.onNodeWithText("Helsinki").performClick()
        composeTestRule.onNodeWithText("Next").performClick()

        // 6. PlaceTypeAndKeywordPage (Step 4)
        composeTestRule.onNodeWithText("Choose a place type and").assertIsDisplayed()
        composeTestRule.onNodeWithText("Select place type").performClick()
        composeTestRule.onNodeWithText("Restaurant").performClick()
        
        // Food category is required for Restaurant
        composeTestRule.onNodeWithText("Select food category").performClick()
        composeTestRule.onNodeWithText("Italian").performClick()
        
        composeTestRule.onNodeWithText("Submit").assertIsEnabled().performClick()

        // 7. SubmissionCompletePage (Success)
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithText("Thank you!").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("Thank you!").assertIsDisplayed()
        composeTestRule.onNodeWithText("Your availability and preferences").assertIsDisplayed()
        composeTestRule.onNodeWithText("have been submitted.").assertIsDisplayed()
    }
}
