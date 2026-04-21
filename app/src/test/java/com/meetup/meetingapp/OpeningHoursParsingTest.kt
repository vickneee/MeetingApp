package com.meetup.meetingapp

import com.meetup.meetingapp.data.model.DateTime
import com.meetup.meetingapp.data.model.Restaurant
import com.meetup.meetingapp.data.model.TimeSlot
import com.meetup.meetingapp.utils.convertTo24
import com.meetup.meetingapp.utils.extractTimeRange
import com.meetup.meetingapp.utils.getOpenLabel
import com.meetup.meetingapp.utils.hasOverlap
import com.meetup.meetingapp.utils.isRestaurantOpenForTiming
import com.meetup.meetingapp.utils.parseDays
import com.meetup.meetingapp.utils.toDayAbbrev
import org.junit.Assert.*
import org.junit.Test

class OpeningHoursParsingTest {

    @Test
    fun testExtractTimeRange() {
        val hours = "Mon: 10:00 AM – 8:00 PM"
        val range = extractTimeRange(hours)

        assertNotNull(range)
        assertEquals("10:00", range!!.first)
        assertEquals("20:00", range.second)
    }

    @Test
    fun testParseDays() {
        val days = parseDays("Mon-Fri: 10:00 AM - 8:00 PM")
        assertEquals(listOf("Mon", "Tue", "Wed", "Thu", "Fri"), days)
    }

    @Test
    fun testParseDays_FullNames() {
        // Should correctly handle full weekday names
        val days = parseDays("Monday-Friday: 10:00 AM - 8:00 PM")
        assertEquals(listOf("Mon", "Tue", "Wed", "Thu", "Fri"), days)
    }

    @Test
    fun testParseDays_SingleDay() {
        // Should return a single day when no range is provided
        val days = parseDays("Tue: 10:00 AM - 8:00 PM")
        assertEquals(listOf("Tue"), days)
    }

    @Test
    fun testParseDays_WrapAround() {
        // Should correctly wrap around the weekend
        val days = parseDays("Fri-Mon: 10:00 AM - 8:00 PM")
        assertEquals(listOf("Fri", "Sat", "Sun", "Mon"), days)
    }

    @Test
    fun testParseDays_UnicodeDash() {
        // Should handle unicode en-dash
        val days = parseDays("Mon–Wed: 10:00 AM - 8:00 PM")
        assertEquals(listOf("Mon", "Tue", "Wed"), days)
    }

    @Test
    fun testHasOverlap_1hour() {
        assertTrue(hasOverlap("10:00", "20:00", "18:30", "20:00"))
        assertFalse(hasOverlap("10:00", "20:00", "19:30", "20:00"))
        assertTrue(hasOverlap("18:00", "02:00", "01:00", "02:00"))
    }

    @Test
    fun testHasOverlap_CrossMidnight() {
        // Restaurant: 18:00–02:00 (crosses midnight)
        // User: 23:00–01:00 → 2 hours overlap → should be true
        assertTrue(hasOverlap("18:00", "02:00", "23:00", "01:00"))

        // Restaurant: 18:00–02:00
        // User: 01:30–02:00 → only 30 minutes overlap → should be false
        assertFalse(hasOverlap("18:00", "02:00", "01:30", "02:00"))
    }


    @Test
    fun testConvertTo24() {
        assertEquals("00:00", convertTo24("12:00 AM"))
        assertEquals("12:00", convertTo24("12:00 PM"))
        assertEquals("13:00", convertTo24("1:00 PM"))
    }

    @Test
    fun testIsRestaurantOpenForTiming() {
        val restaurant = Restaurant(
            openingHours = listOf("Mon: 10:00 AM - 8:00 PM")
        )
        val timing = DateTime("2025-01-06", TimeSlot("12:00", "14:00")) // Monday

        assertTrue(isRestaurantOpenForTiming(restaurant, timing))
    }

    @Test
    fun testIsRestaurantOpenForTiming_CrossMidnight_True() {
        // Restaurant is open from Monday 6:00 PM until Tuesday 2:00 AM
        val restaurant = Restaurant(
            openingHours = listOf("Mon: 6:00 PM - 2:00 AM")
        )

        // User selects Monday 23:00–01:00 → within the open hours
        val timing = DateTime("2025-01-06", TimeSlot("23:00", "01:00"))

        assertTrue(isRestaurantOpenForTiming(restaurant, timing))
    }


    @Test
    fun testIsRestaurantOpenForTiming_False() {
        val restaurant = Restaurant(
            openingHours = listOf("Mon: 10:00 AM - 8:00 PM")
        )
        val timing = DateTime("2025-01-07", TimeSlot("12:00", "14:00")) // Tuesday

        assertFalse(isRestaurantOpenForTiming(restaurant, timing))
    }

    @Test
    fun testIsRestaurantOpenForTiming_CrossMidnight_False() {
        // Restaurant is open from Monday 6:00 PM until Tuesday 2:00 AM
        val restaurant = Restaurant(
            openingHours = listOf("Mon: 6:00 PM - 2:00 AM")
        )

        // User selects Monday 03:00–04:00 → after closing time
        val timing = DateTime("2025-01-06", TimeSlot("03:00", "04:00"))

        assertFalse(isRestaurantOpenForTiming(restaurant, timing))
    }



    @Test
    fun testToDayAbbrev() {
        val dt = DateTime("2025-01-06", TimeSlot("10:00", "12:00")) // Monday
        assertEquals("Mon", dt.toDayAbbrev())
    }

    @Test
    fun testGetOpenLabel() {
        val restaurant = Restaurant(
            openingHours = listOf("Monday: 10:00 AM - 8:00 PM")
        )
        val timing = DateTime("2025-01-06", TimeSlot("12:00", "14:00"))

        assertEquals("10:00 AM – 8:00 PM", getOpenLabel(restaurant, timing))
    }

    @Test
    fun testGetOpenLabel_CrossMidnight() {
        // Restaurant open from Monday 6:00 PM to Tuesday 2:00 AM
        val restaurant = Restaurant(
            openingHours = listOf("Monday: 6:00 PM - 2:00 AM")
        )

        // Any Monday timing should return the correct label
        val timing = DateTime("2025-01-06", TimeSlot("23:00", "01:00"))

        assertEquals("6:00 PM – 2:00 AM", getOpenLabel(restaurant, timing))
    }


}