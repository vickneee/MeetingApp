package com.meetup.meetingapp.utils

import org.junit.Assert.assertEquals
import org.junit.Test

class DateUtilsTest {
    @Test
    fun `valid ISO date converts to euro format`() {
        val input = "2026-04-23"
        val result = input.toEuroDate()
        assertEquals("23.04.2026", result)
    }

    @Test
    fun `invalid date returns original string`() {
        val input = "not-a-date"
        val result = input.toEuroDate()
        assertEquals("not-a-date", result)
    }

    @Test
    fun `empty string returns empty string`() {
        val input = ""
        val result = input.toEuroDate()
        assertEquals("", result)
    }

    @Test
    fun `partial date returns original string`() {
        val input = "2026-04"
        val result = input.toEuroDate()
        assertEquals("2026-04", result)
    }
}
