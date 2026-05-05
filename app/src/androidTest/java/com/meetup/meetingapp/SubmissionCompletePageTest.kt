package com.meetup.meetingapp

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.meetup.meetingapp.data.model.Event
import com.meetup.meetingapp.ui.screens.participantinput.ParticipantViewModel
import com.meetup.meetingapp.ui.screens.participantinput.SubmissionCompletePage
import com.meetup.meetingapp.ui.screens.participantinput.SubmitState
import com.meetup.meetingapp.ui.theme.MeetingAppTheme
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SubmissionCompletePageTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var mockViewModel: ParticipantViewModel
    private val submitStateFlow = MutableStateFlow<SubmitState>(SubmitState.Success)
    private val isHostFlow = MutableStateFlow(false)
    private val eventFlow = MutableStateFlow<Event?>(
        Event(id = "test_event_id", eventCode = "ABCDEF", eventTitle = "Test Event", hostName = "Host")
    )

    @Before
    fun setUp() {
        mockViewModel = mockk(relaxed = true)
        every { mockViewModel.submitState } returns submitStateFlow
        every { mockViewModel.isHost } returns isHostFlow
        every { mockViewModel.event } returns eventFlow
    }

    @Test
    fun submissionCompletePage_displaysSuccessContent() {
        composeTestRule.setContent {
            MeetingAppTheme {
                SubmissionCompletePage(
                    viewModel = mockViewModel,
                    onHomeClick = {},
                    onNavigateToHostDashboard = {},
                    onNavigateToParticipantDashboard = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Thank you!").assertIsDisplayed()
        composeTestRule.onNodeWithText("Your availability and preferences").assertIsDisplayed()
        composeTestRule.onNodeWithText("Go to Dashboard").assertIsDisplayed()
    }

    @Test
    fun submissionCompletePage_homeNavigation() {
        var homeClicked = false
        composeTestRule.setContent {
            MeetingAppTheme {
                SubmissionCompletePage(
                    viewModel = mockViewModel,
                    onHomeClick = { homeClicked = true },
                    onNavigateToHostDashboard = {},
                    onNavigateToParticipantDashboard = {}
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Home").performClick()
        assert(homeClicked)
    }

    @Test
    fun submissionCompletePage_navigateToParticipantDashboard_whenNotHost() {
        isHostFlow.value = false
        var navigatedToParticipantDashboard = false
        var capturedEventId = ""

        composeTestRule.setContent {
            MeetingAppTheme {
                SubmissionCompletePage(
                    viewModel = mockViewModel,
                    onHomeClick = {},
                    onNavigateToHostDashboard = {},
                    onNavigateToParticipantDashboard = { id ->
                        navigatedToParticipantDashboard = true
                        capturedEventId = id
                    }
                )
            }
        }

        composeTestRule.onNodeWithText("Go to Dashboard").performClick()
        
        assert(navigatedToParticipantDashboard)
        assert(capturedEventId == "test_event_id")
    }

    @Test
    fun submissionCompletePage_navigateToHostDashboard_whenHost() {
        isHostFlow.value = true
        var navigatedToHostDashboard = false
        var capturedEventId = ""

        composeTestRule.setContent {
            MeetingAppTheme {
                SubmissionCompletePage(
                    viewModel = mockViewModel,
                    onHomeClick = {},
                    onNavigateToHostDashboard = { id ->
                        navigatedToHostDashboard = true
                        capturedEventId = id
                    },
                    onNavigateToParticipantDashboard = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Go to Dashboard").performClick()
        
        assert(navigatedToHostDashboard)
        assert(capturedEventId == "test_event_id")
    }
}
