package com.meetup.meetingapp

import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.meetup.meetingapp.ui.screens.joinpage.JoinContent
import com.meetup.meetingapp.ui.theme.MeetingAppTheme
import io.mockk.confirmVerified
import io.mockk.mockk
import io.mockk.verify
import org.junit.Rule
import org.junit.Test

class JoinPageTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun joinPage_emptyFields_showsErrorMessages() {
        // Define mock error messages that the ViewModel would normally provide
        val codeError = "Code is required"
        val keyError = "Key is required"

        composeTestRule.setContent {
            MeetingAppTheme {
                JoinContent(
                    code = "",
                    codeError = codeError,
                    onCodeChange = {},
                    key = "",
                    keyError = keyError,
                    onKeyChange = {},
                    onBack = {},
                    onJoinEventClick = {},
                    onEventsClick = {},
                )
            }
        }
        // Assert that the error messages are displayed
        composeTestRule.onNodeWithText(codeError).assertIsDisplayed()
        composeTestRule.onNodeWithText(keyError).assertIsDisplayed()
    }

    @Test
    fun joinPage_correctSubmission_triggersJoinAction() {
        val onJoinEventClick = mockk<() -> Unit>(relaxed = true)
        val testCode = "ABCDEF"
        val testKey = "123456"

        composeTestRule.setContent {
            MeetingAppTheme {
                JoinContent(
                    code = testCode,
                    codeError = null,
                    onCodeChange = {},
                    key = testKey,
                    keyError = null,
                    onKeyChange = {},
                    onBack = {},
                    onJoinEventClick = onJoinEventClick,
                    onEventsClick = {},
                )
            }
        }
        // Find the button and click it
        composeTestRule
            .onNode(
                hasText("Join Event") and
                    SemanticsMatcher.expectValue(
                        SemanticsProperties.Role,
                        Role.Button,
                    ),
            ).performClick()
        // Verify the callback was triggered
        verify(exactly = 1) { onJoinEventClick() }
        confirmVerified(onJoinEventClick)
    }

    @Test
    fun joinPage_typingConvertsToUppercase() {
        var capturedCode = ""

        composeTestRule.setContent {
            MeetingAppTheme {
                JoinContent(
                    code = "",
                    codeError = null,
                    onCodeChange = { capturedCode = it },
                    key = "",
                    keyError = null,
                    onKeyChange = {},
                    onBack = {},
                    onJoinEventClick = {},
                    onEventsClick = {},
                )
            }
        }

        // Find the TextField by its label
        composeTestRule.onNodeWithText("Enter code").performTextInput("abc")

        // Check if the logic inside JoinContent calls onCodeChange with uppercase
        composeTestRule.runOnIdle {
            assert(capturedCode == "ABC")
        }
    }
}
