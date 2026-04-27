package com.meetup.meetingapp.utils

import com.meetup.meetingapp.data.model.DateTime
import com.meetup.meetingapp.data.model.Restaurant
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

private const val MINIMUM_OVERLAP_MINUTES = 60

/**
 * Converts a DateTime into a 3-letter weekday abbreviation (Mon, Tue…)
 */
fun DateTime.toDayAbbrev(): String {
    val localDate = this.toLocalDate()
    return localDate.dayOfWeek.name
        .take(3)
        .lowercase()
        .replaceFirstChar { it.uppercase() }
}

/**
 * Extracts weekday from openingHours string, handling ranges like "Mon-Fri".
 */
fun parseDays(hours: String): List<String> {
    val daysOfWeek = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    val normalized = hours.takeWhile { it != ':' }

    // Handle ranges like "Monday-Friday"
    if (normalized.contains("-") || normalized.contains("–")) {
        val parts =
            normalized.split(Regex("[-–]")).map {
                it
                    .trim()
                    .take(3)
                    .lowercase()
                    .replaceFirstChar { c -> c.uppercase() }
            }
        if (parts.size == 2) {
            val startIdx = daysOfWeek.indexOf(parts[0])
            val endIdx = daysOfWeek.indexOf(parts[1])
            if (startIdx != -1 && endIdx != -1) {
                return if (startIdx <= endIdx) {
                    daysOfWeek.subList(startIdx, endIdx + 1)
                } else {
                    // Wraps around weekend
                    daysOfWeek.subList(startIdx, 7) + daysOfWeek.subList(0, endIdx + 1)
                }
            }
        }
    }

    /**
     * Handle exact matches like "Monday"
     */
    val found = daysOfWeek.filter { normalized.contains(it, ignoreCase = true) }
    if (found.isNotEmpty()) return found

    /**
     * Handle single-letter matches like "M"
     */
    val dayRegex = Regex("([A-Za-z]+):")
    val match = dayRegex.find(hours) ?: return emptyList()
    return listOf(
        match.groupValues[1]
            .take(3)
            .lowercase()
            .replaceFirstChar { it.uppercase() },
    )
}

/**
 * Extracts start/end time from openingHours string.
 */
fun extractTimeRange(hours: String): Pair<String, String>? {
    // Normalize weird Unicode spaces to regular spaces
    val cleaned = hours.replace(Regex("[\\u202F\\u2009\\u200A\\u200B\\uFEFF\\u00A0]"), " ")

    // 1) Try 24-hour format first: "10:00-20:00"
    val regex24 = Regex("(\\d{1,2}:\\d{2})\\s*[\\-–—‑−]\\s*(\\d{1,2}:\\d{2})")
    regex24.find(cleaned)?.let {
        val (start, end) = it.destructured
        return start to end
    }

    // 2) Try 12-hour AM/PM format: "10:00 AM - 8:00 PM"
    val regex12 = Regex("(\\d{1,2}:\\d{2}\\s*[AP]M)\\s*[\\-–—‑−]\\s*(\\d{1,2}:\\d{2}\\s*[AP]M)")
    regex12.find(cleaned)?.let {
        val (start, end) = it.destructured
        return convertTo24(start) to convertTo24(end)
    }

    // No valid time range found
    return null
}

/**
 * Converts "9:00 AM" → "09:00" (24-hour format)
 */
fun convertTo24(time: String): String {
    val formatter12 = DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH)
    val formatter24 = DateTimeFormatter.ofPattern("HH:mm")
    return LocalTime.parse(time.uppercase(), formatter12).format(formatter24)
}

/**
 * Checks if the restaurant is open for at least 1 hour
 * during the target time slot.
 */
fun hasOverlap(
    oStartStr: String,
    oEndStr: String,
    tStartStr: String,
    tEndStr: String,
): Boolean {
    fun toMin(t: String): Int {
        val p = t.split(":")
        return (p[0].toInt() * 60) + p[1].toInt()
    }

    // Convert "HH:mm" to minutes from 00:00
    val oStart = toMin(oStartStr)
    val oEnd = toMin(oEndStr)
    val tStart = toMin(tStartStr)
    val tEnd = toMin(tEndStr)

    // Split an interval into one or two segments on [0, 1440)
    // If it crosses midnight, it becomes [start, 1440) and [0, end)
    fun toSegments(
        start: Int,
        end: Int,
    ): List<Pair<Int, Int>> =
        if (end > start) {
            listOf(start to end)
        } else {
            listOf(start to 1440, 0 to end)
        }

    val oSegments = toSegments(oStart, oEnd)
    val tSegments = toSegments(tStart, tEnd)

    var maxOverlap = 0

    // Compute maximum overlap between any pair of segments
    for ((os, oe) in oSegments) {
        for ((ts, te) in tSegments) {
            val overlap = minOf(oe, te) - maxOf(os, ts)
            if (overlap > maxOverlap) {
                maxOverlap = overlap
            }
        }
    }

    // Require at least 60 minutes of overlap
    return maxOverlap >= MINIMUM_OVERLAP_MINUTES
}

/**
 * Determines whether a restaurant is open during the selected timing.
 */
fun isRestaurantOpenForTiming(
    restaurant: Restaurant,
    timing: DateTime,
): Boolean {
    val targetDay = timing.toDayAbbrev()
    val hoursList = restaurant.openingHours ?: return true

    // Find the entry for the target day (e.g., "Thursday: Open 24 hours")
    val dailySchedule = hoursList.find { hours ->
        parseDays(hours).contains(targetDay)
    } ?: return true // If day not found, don't hide it

    // Explicitly check for 24-hour availability
    if (dailySchedule.contains("24", ignoreCase = true)) {
        return true
    }

    // Explicitly check if it's closed
    if (dailySchedule.contains("Closed", ignoreCase = true)) {
        return false
    }

    // Attempt to parse the time range
    val range = extractTimeRange(dailySchedule) ?: return true

    // If we have a range, check the actual overlap
    return hasOverlap(range.first, range.second, timing.timeSlot.start, timing.timeSlot.end)
}

/**
 * Builds a human‑readable opening hours label for the given restaurant
 * based on the selected timing.
 *
 * Example output: `"10:00 AM – 8:00 PM"`
 *
 * @param restaurant The restaurant whose opening hours should be evaluated.
 * @param timing The selected date/time used to determine the correct weekday.
 * @return A formatted label or null if opening hours are unavailable.
 */
fun getOpenLabel(
    restaurant: Restaurant,
    timing: DateTime,
): String? {
    val day = timing.toLocalDate().dayOfWeek.getDisplayName(TextStyle.FULL, Locale.ENGLISH)
    // Efficiency: Find the string once.
    val rawHours = restaurant.openingHours?.firstOrNull {
        it.startsWith(day, ignoreCase = true)
    } ?: return null

    // Optimization: Fast-path for 24-hour places (Common case for your issue)
    if (rawHours.contains("24", ignoreCase = true)) {
        return "Open 24 hours"
    }

    // Fast-path for explicitly Closed
    if (rawHours.contains("Closed", ignoreCase = true)) {
        return "Closed"
    }

    // Try to parse the time range for pretty formatting (AM/PM)
    val range = extractTimeRange(rawHours)

    return if (range != null) {
        // Return pretty format: "9:00 AM – 5:00 PM"
        "${format24ToAmPm(range.first)} – ${format24ToAmPm(range.second)}"
    } else {
        // FALLBACK (Optimization): If parsing fails, don't show "Unavailable".
        // Instead, strip the "Thursday:" prefix and show the raw string from Google.
        // This ensures the user sees something useful even if the regex missed it.
        val cleanValue = rawHours.substringAfter(":").trim()
        cleanValue.ifEmpty { "Hours unavailable" }
    }
}

/**
 * Converts a 24‑hour time string (e.g., `"18:30"`) into a 12‑hour AM/PM format.
 *
 * @param time A time string in `"HH:mm"` format.
 * @return A formatted time string in `"h:mm a"` format.
 */
fun format24ToAmPm(time: String): String {
    val f24 = DateTimeFormatter.ofPattern("H:mm")
    val f12 = DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH)
    return LocalTime.parse(time, f24).format(f12)
}
