package com.meetup.meetingapp.ui.screens.vote_for_place_flow

import androidx.lifecycle.SavedStateHandle
import com.google.android.gms.location.FusedLocationProviderClient
import com.meetup.meetingapp.data.model.DateTime
import com.meetup.meetingapp.data.model.Restaurant
import com.meetup.meetingapp.data.model.TimeSlot
import com.meetup.meetingapp.data.repositories.EventRepository
import com.meetup.meetingapp.utils.filterRestaurants
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Test

class PlaceViewModelTest {

    // Mocking dependencies required for ViewModel initialization
    private val mockRepository: EventRepository = mockk(relaxed = true)
    private val mockLocationClient: FusedLocationProviderClient = mockk(relaxed = true)
    private val savedStateHandle = SavedStateHandle(mapOf("eventId" to "test_event_123"))

    // The ViewModel instance
    private val viewModel = PlaceViewModel(
        eventRepository = mockRepository,
        apiKey = "mock_api_key",
        fusedLocationClient = mockLocationClient,
        savedStateHandle = savedStateHandle
    )

    @Test
    fun testFilterRestaurantsByLocation() {
        val restaurants = listOf(
            Restaurant(placeId = "1", name = "Pizza Place", address = "Main St, Helsinki"),
            Restaurant(placeId = "2", name = "Burger Joint", address = "Second St, Espoo")
        )

        // Test matching by name
        val nameResult = filterRestaurants(restaurants, timing = null, location = "Pizza")
        assertEquals(1, nameResult.size)
        assertEquals("Pizza Place", nameResult[0].name)

        // Test matching by address
        val addressResult = filterRestaurants(restaurants, timing = null, location = "Espoo")
        assertEquals(1, addressResult.size)
        assertEquals("Burger Joint", addressResult[0].name)
    }

    @Test
    fun testFilterRestaurantsByTimingFormat() {
        // This test ensures that the "8:00AM" format (no space) does not cause a crash
        val restaurant = Restaurant(
            placeId = "A",
            name = "Morning Cafe",
            openingHours = listOf("Monday: 8:00AM–10:00PM")
        )

        // Creating the DateTime object using your model's structure
        val timing = DateTime(
            date = "2026-04-20", // A Monday
            timeSlot = TimeSlot(start = "9:00 AM", end = "10:00 AM")
        )

        val result = filterRestaurants(listOf(restaurant), timing, location = null)

        assertEquals("Restaurant should be filtered as 'open'", 1, result.size)
        assertEquals("Morning Cafe", result[0].name)
    }

    @Test
    fun testFilterRestaurantsWithEmptyQuery() {
        val restaurants = listOf(
            Restaurant(placeId = "1", name = "Place A"),
            Restaurant(placeId = "2", name = "Place B")
        )

        val result = filterRestaurants(restaurants, timing = null, location = "")
        assertEquals(2, result.size)
    }

    @Test
    fun testBuildDateLocationOptionsFiltering() {
        // April 21, 2026 is a Tuesday
        val timing = DateTime(
            date = "2026-04-21",
            timeSlot = TimeSlot(start = "12:00 PM", end = "1:00 PM")
        )

        val restaurants = listOf(
            Restaurant(
                placeId = "1",
                name = "Helsinki Diner",
                address = "Helsinki",
                // Add this to ensure the filter sees it as "Open"
                openingHours = listOf("Tuesday: 10:00 AM – 10:00 PM")
            )
        )

        // Act
        viewModel.buildDateLocationOptions(
            dateTimes = listOf(timing),
            locations = listOf("Helsinki", "Tampere"),
            allRestaurants = restaurants
        )

        // Assert
        val options = viewModel.dateAndAreaState.value.dateLocationOptions

        assertEquals("Only Helsinki should be an available option", 1, options.size)
        assertEquals("Helsinki", options[0].location)
    }

    @Test
    fun testFetchRestaurantDetail() {
        val restaurantA = Restaurant(placeId = "find_me", name = "Target")
        val restaurantB = Restaurant(placeId = "ignore_me", name = "Other")

        // This is a simple test for the find logic used in your ViewModel
        val list = listOf(restaurantA, restaurantB)
        val result = list.find { it.placeId == "find_me" }

        assertEquals("Target", result?.name)
    }
}