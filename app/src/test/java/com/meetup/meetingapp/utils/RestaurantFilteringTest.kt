package com.meetup.meetingapp.utils

import com.meetup.meetingapp.data.model.DateTime
import com.meetup.meetingapp.data.model.Restaurant
import com.meetup.meetingapp.data.model.TimeSlot
import org.junit.Assert
import org.junit.Test

class RestaurantFilteringTest {
    @Test
    fun filters_restaurants_by_location_and_timing() {
        val r1 =
            Restaurant(
                placeId = "A",
                name = "A",
                address = "Tokyo",
                openingHours = listOf("Wed: 10:00-20:00"),
            )

        val r2 =
            Restaurant(
                placeId = "B",
                name = "B",
                address = "Osaka",
                openingHours = listOf("Wed: 00:00-01:00"),
            )

        val result =
            filterRestaurants(
                restaurants = listOf(r1, r2),
                timing = DateTime("2025-01-01", TimeSlot("12:00", "14:00")),
                location = "Tokyo",
            )

        Assert.assertEquals(1, result.size)
        Assert.assertEquals("A", result.first().name)
    }

    @Test
    fun filters_returns_empty_when_location_does_not_match() {
        val r1 =
            Restaurant(
                placeId = "A",
                name = "A",
                address = "Tokyo",
                openingHours = listOf("Wed: 10:00-20:00"),
            )

        val result =
            filterRestaurants(
                restaurants = listOf(r1),
                timing = DateTime("2025-01-01", TimeSlot("12:00", "14:00")),
                location = "Osaka",
            )

        Assert.assertEquals(0, result.size)
    }

    @Test
    fun filters_returns_empty_when_timing_does_not_overlap() {
        val r1 =
            Restaurant(
                placeId = "A",
                name = "A",
                address = "Tokyo",
                openingHours = listOf("Wed: 17:00-23:00"),
            )

        val result =
            filterRestaurants(
                restaurants = listOf(r1),
                timing = DateTime("2025-01-01", TimeSlot("12:00", "14:00")),
                location = "Tokyo",
            )

        Assert.assertEquals(0, result.size)
    }

    @Test
    fun filters_returns_empty_when_day_does_not_match() {
        val r1 =
            Restaurant(
                placeId = "A",
                name = "A",
                address = "Tokyo",
                openingHours = listOf("Thu: 10:00-20:00"),
            )

        val result =
            filterRestaurants(
                restaurants = listOf(r1),
                timing = DateTime("2025-01-01", TimeSlot("12:00", "14:00")),
                location = "Tokyo",
            )

        Assert.assertEquals(0, result.size)
    }

    @Test
    fun filters_handles_null_opening_hours() {
        val r1 =
            Restaurant(
                placeId = "A",
                name = "A",
                address = "Tokyo",
                openingHours = null,
            )

        val result =
            filterRestaurants(
                restaurants = listOf(r1),
                timing = DateTime("2025-01-01", TimeSlot("12:00", "14:00")),
                location = "Osaka",
            )

        Assert.assertEquals(0, result.size)
    }
}
