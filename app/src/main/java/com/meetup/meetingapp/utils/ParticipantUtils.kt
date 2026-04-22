package com.meetup.meetingapp.utils

import com.meetup.meetingapp.data.model.DateTime
import com.meetup.meetingapp.data.model.Event
import com.meetup.meetingapp.ui.screens.participant_input_flow.DateAvailability
import com.meetup.meetingapp.ui.screens.participant_input_flow.UiTimeSlot

/**
 * Generates a flattened list of all available date and time slot combinations for an event.
 *
 * @param event The event containing the date range and time slots.
 * @return A list of [DateTime] objects representing all possible meeting options.
 */
fun buildAllAvailableDateTimes(event: Event): List<DateTime> {
    val start = event.dateRange.startDate()
    val end = event.dateRange.endDate()

    val allDates =
        generateSequence(start) { it.plusDays(1) }
            .takeWhile { !it.isAfter(end) }
            .toList()

    return allDates.flatMap { date ->
        event.timeSlots.map { slot ->
            DateTime(date.toString(), slot)
        }
    }
}

/**
 * Builds a list of [DateAvailability] based on the provided event and selected date times.
 *
 * @param event The event containing time slots.
 * @param selectedDateTimes The list of selected date and time slots.
 * @return A list of [DateAvailability] representing available dates and time slots.
 * @see UiTimeSlot for more information about time slots.
 */
fun buildDateAvailability(
    event: Event,
    selectedDateTimes: List<DateTime>,
): List<DateAvailability> {
    val start = event.dateRange.startDate()
    val end = event.dateRange.endDate()
    val allDates =
        generateSequence(start) { it.plusDays(1) }
            .takeWhile { !it.isAfter(end) }
            .toList()

    return allDates.map { date ->
        val dateString = date.toString()
        DateAvailability(
            date = dateString,
            timeSlots =
                event.timeSlots.mapIndexed { index, slot ->
                    val isSelected = selectedDateTimes.any { it.date == dateString && it.timeSlot == slot }
                    UiTimeSlot(
                        id = index,
                        timeRange = "${slot.start} - ${slot.end}",
                        isSelected = isSelected,
                    )
                },
        )
    }
}
