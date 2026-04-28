package com.meetup.meetingapp

import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.meetup.meetingapp.data.model.Restaurant
import com.meetup.meetingapp.ui.screens.vote_for_place_flow.PlaceDetailsContent
import com.meetup.meetingapp.ui.theme.MeetingAppTheme
import junit.framework.TestCase.assertTrue
import org.junit.Rule
import org.junit.Test

fun hasRole(role: Role): SemanticsMatcher {
    return SemanticsMatcher("Expected role: $role") {
        it.config.getOrNull(SemanticsProperties.Role) == role
    }
}

class VoteButtonTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val testAddress = "123 Test St, Helsinki"
    private val fakeRestaurant = Restaurant(
        placeId = "123",
        name = "Test Cafe",
        rating = 4.5,
        userRatingCount = 10,
        address = testAddress,
        types = listOf("Cafe"),
        priceLevel = 1,
        photoReference = ""
    )

    @Test
    fun whenNotVoted_buttonShowsVoteText_andIsEnabled() {
        composeTestRule.setContent {
            MeetingAppTheme {
                PlaceDetailsContent(
                    restaurantDetail = fakeRestaurant,
                    openLabel = "Open now",
                    priceLabel = "$",
                    photoUrl = "",
                    distanceLabel = "1.0 km",
                    isVoted = false,
                    isFinalized = false,
                    finalTime = null,
                    voteResultState = null,
                    onBack = {},
                    onHomeClick = {},
                    onVoteClick = {},
                    onMapsClick = {}
                )
            }
        }

        composeTestRule
            .onNode(hasText("Vote for this restaurant") and hasRole(Role.Button))
            .assertExists()
            .assertIsEnabled()
    }

    @Test
    fun whenVoted_buttonShowsVoteText_andIsDisabled() {
        composeTestRule.setContent {
            MeetingAppTheme {
                PlaceDetailsContent(
                    restaurantDetail = fakeRestaurant,
                    openLabel = "Open now",
                    priceLabel = "$",
                    photoUrl = "",
                    distanceLabel = "1.0 km",
                    isVoted = true,
                    isFinalized = false,
                    finalTime = null,
                    voteResultState = null,
                    onBack = {},
                    onHomeClick = {},
                    onVoteClick = {},
                    onMapsClick = {}
                )
            }
        }

        composeTestRule
            .onNode(hasText("Voted") and hasRole(Role.Button))
            .assertExists()
            .assertIsNotEnabled()
    }

    @Test
    fun clickingVoteButton_triggersCallback() {
        var voteClicked = false

        composeTestRule.setContent {
            MeetingAppTheme {
                PlaceDetailsContent(
                    restaurantDetail = fakeRestaurant,
                    openLabel = "Open now",
                    priceLabel = "$",
                    photoUrl = "",
                    distanceLabel = "1.0 km",
                    isVoted = false,
                    isFinalized = false,
                    finalTime = null,
                    voteResultState = null,
                    onBack = {},
                    onHomeClick = {},
                    onVoteClick = { voteClicked = true },
                    onMapsClick = {}
                )
            }
        }

        // Find the button using semantics and perform the click
        composeTestRule
            .onNode(hasText("Vote for this restaurant") and hasRole(Role.Button))
            .performClick()

        // Assert that the callback was triggered
        assertTrue("Vote callback was not triggered", voteClicked)
    }
}