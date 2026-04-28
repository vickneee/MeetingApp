package com.meetup.meetingapp.utils

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource


class RestaurantUtilsTest {

    @ParameterizedTest
    @CsvSource(
        "0, €",
        "1, €€",
        "2, €€€"
    )
    fun `formatPriceLevel returns correct number of Euro signs for valid levels`(level: Int, expected: String) {
        val result = formatPriceLevel(level)
        assertEquals(expected, result)
    }

    @Test
    fun `formatPriceLevel returns empty string for null or negative values`() {
        assertEquals("", formatPriceLevel(null))
        assertEquals("", formatPriceLevel(-1))
        assertEquals("", formatPriceLevel(-100))
    }

    @Test
    fun `buildPhotoUrl returns full URL when reference is valid`() {
        val photoRef = "ref123"
        val key = "AIza_fake_key"
        val expected = "https://maps.googleapis.com/maps/api/place/photo?maxwidth=800&photo_reference=ref123&key=AIza_fake_key"

        assertEquals(expected, buildPhotoUrl(photoRef, key))
    }

    @Test
    fun `buildPhotoUrl returns null when reference is null or empty`() {
        val key = "AIza_fake_key"

        assertNull(buildPhotoUrl(null, key))
        assertNull(buildPhotoUrl("", key))
    }
}
