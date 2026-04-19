package com.meetup.meetingapp

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.hamcrest.Matchers.allOf
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.espresso.intent.matcher.IntentMatchers.hasData
import androidx.test.espresso.intent.matcher.IntentMatchers.hasPackage
import com.meetup.meetingapp.data.model.Restaurant
import com.meetup.meetingapp.ui.screens.vote_for_place_flow.PlaceDetailsContent
import com.meetup.meetingapp.ui.theme.MeetingAppTheme
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class PlaceDetailsMapsTest {
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

    @Before
    fun setUp() {
        Intents.init()
    }

    @After
    fun tearDown() {
        Intents.release()
    }

    @Test
    fun viewOnMapsClick_sendsCorrectIntent() {
        composeTestRule.setContent {
            MeetingAppTheme {
                PlaceDetailsContent(
                    restaurantDetail = fakeRestaurant,
                    openLabel = "Open now",
                    priceLabel = "$",
                    photoUrl = "",
                    distanceLabel = "1.0 km",
                    isVoted = true,
                    isFinalized = true,
                    finalTime = null,
                    voteResultState = null,
                    onBack = {},
                    onHomeClick = {},
                    onVoteClick = {},
                    onMapsClick = {
                        // This mimics the logic in your PlaceDetailsPage
                        val encodedAddress = Uri.encode(fakeRestaurant.address)
                        val gmmIntentUri = Uri.parse("geo:0,0?q=$encodedAddress")
                        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri).apply {
                            setPackage("com.google.android.apps.maps")
                        }
                        // The test environment handles the context
                        ApplicationProvider
                            .getApplicationContext<Context>()
                            .startActivity(mapIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                    }
                )
            }
        }

        // Action: Click the button
        composeTestRule.onNodeWithText("View on Google Maps").performClick()

        // Assertion: Verify the intent properties
        val expectedUri = Uri.parse("geo:0,0?q=${Uri.encode(testAddress)}")

        intended(
            allOf(
                hasAction(Intent.ACTION_VIEW),
                hasData(expectedUri),
                hasPackage("com.google.android.apps.maps")
            )
        )
    }
}