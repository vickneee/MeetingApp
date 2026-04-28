package com.meetup.meetingapp.utils

import com.meetup.meetingapp.data.model.DateRange
import com.meetup.meetingapp.data.model.DateTime
import com.meetup.meetingapp.data.model.Event
import com.meetup.meetingapp.data.model.TimeSlot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ParticipantUtilsTest {

    private fun createMockEvent(): Event {
        return Event(
            dateRange = DateRange("2026-04-23", "2026-04-24"),
            timeSlots = listOf(
                TimeSlot("09:00", "10:00"),
                TimeSlot("10:00", "11:00")
            )
        )
    }

    @Test
    fun buildAllAvailableDateTimes_isCorrect() {
        val event = createMockEvent()
        val result = buildAllAvailableDateTimes(event)
        assertEquals(4, result.size)
    }

    @Test
    fun buildDateAvailability_selectionCheck() {
        val event = createMockEvent()
        val selected = listOf(
            DateTime("2026-04-23", event.timeSlots[0])
        )

        val result = buildDateAvailability(event, selected)
        val day1 = result.find { it.date == "2026-04-23" }

        assertTrue(day1!!.timeSlots[0].isSelected)
        assertFalse(day1.timeSlots[1].isSelected)
    }
}
