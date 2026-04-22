package com.meetup.meetingapp.utils

import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Converts a date string (yyyy-MM-dd) to European format (dd.MM.yyyy).
 */
fun String.toEuroDate(): String =
    try {
        val date = LocalDate.parse(this)
        val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
        date.format(formatter)
    } catch (_: Exception) {
        this
    }
