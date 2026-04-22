package com.meetup.meetingapp.utils

import android.location.Location
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.verify
import org.junit.Assert
import org.junit.Test

class DistanceUtilsTest {
    @Test
    fun `distance under 1000m is formatted in meters`() {
        val result = formatDistance(999f)
        Assert.assertEquals("999 m", result)
    }

    @Test
    fun `distance exactly 1000m is formatted in km`() {
        val result = formatDistance(1000f)
        Assert.assertEquals("1.0 km", result)
    }

    @Test
    fun `distance over 1000m is formatted in km`() {
        val result = formatDistance(1500f)
        Assert.assertEquals("1.5 km", result)
    }

    @Test
    fun `distance with decimals under 1000m is truncated`() {
        val result = formatDistance(999.9f)
        Assert.assertEquals("999 m", result)
    }

    @Test
    fun `calculateDistanceMeters returns mocked distance`() {
        mockkStatic(Location::class)

        every {
            Location.distanceBetween(any(), any(), any(), any(), any())
        } answers {
            val results = arg<FloatArray>(4)
            results[0] = 123.45f
        }

        val result = calculateDistanceMeters(1.0, 2.0, 3.0, 4.0)

        Assert.assertEquals(123.45f, result)
    }

    @Test
    fun `calculateDistanceMeters passes correct arguments to Location_distanceBetween`() {
        mockkStatic(Location::class)

        every {
            Location.distanceBetween(any(), any(), any(), any(), any())
        } answers {
            val results = arg<FloatArray>(4)
            results[0] = 50f
        }

        val userLat = 10.0
        val userLng = 20.0
        val destLat = 30.0
        val destLng = 40.0

        calculateDistanceMeters(userLat, userLng, destLat, destLng)

        verify {
            Location.distanceBetween(
                userLat,
                userLng,
                destLat,
                destLng,
                any(),
            )
        }
    }

    @Test
    fun `calculateDistanceMeters provides results array of size 1`() {
        mockkStatic(Location::class)

        every {
            Location.distanceBetween(any(), any(), any(), any(), any())
        } answers {
            val results = arg<FloatArray>(4)
            Assert.assertEquals(1, results.size)
            results[0] = 77.7f
        }

        val result = calculateDistanceMeters(1.0, 2.0, 3.0, 4.0)

        Assert.assertEquals(77.7f, result)
    }
}
