package com.meetup.meetingapp.utils

import android.util.Log
import com.meetup.meetingapp.data.model.DateTime
import com.meetup.meetingapp.data.model.Restaurant
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.format.TextStyle
import java.util.Locale

private const val MINIMUM_OVERLAP_MINUTES = 60

/**
 * Converts a [DateTime] into a 3‑letter weekday abbreviation (e.g., "Mon", "Tue").
 *
 * Used when matching restaurant opening hours against the selected timing.
 */
fun DateTime.toDayAbbrev(): String {
    val localDate = this.toLocalDate()
    return localDate.dayOfWeek.name
        .take(3)
        .lowercase()
        .replaceFirstChar { it.uppercase() }
}

/**
 * Extracts one or more weekday abbreviations from an opening‑hours string.
 *
 * Supports:
 * - Ranges like `"Mon-Fri"` or `"Monday–Friday"`
 * - Single days like `"Monday"`
 * - Fallback parsing for formats like `"M:"`
 *
 * @return A list of 3‑letter weekday abbreviations (e.g., `["Mon", "Tue"]`).
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
 * Extracts a start/end time range from an opening‑hours string.
 *
 * Supports:
 * - 24‑hour format: `"10:00-20:00"`
 * - 12‑hour format: `"10:00 AM – 8:00 PM"` (converted to 24‑hour)
 *
 * @return A pair of `"HH:mm"` strings, or `null` if no valid range is found.
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
 * Converts a 12‑hour time string (e.g., `"9:00 AM"`) into `"HH:mm"` 24‑hour format.
 *
 * Falls back to `"00:00"` if parsing fails.
 */
fun convertTo24(time: String): String {
    val flexibleFormatter =
        DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .appendPattern("h:mm")
            .optionalStart()
            .appendLiteral(" ")
            .optionalEnd()
            .appendPattern("a")
            .toFormatter(Locale.ENGLISH)

    val formatter24 = DateTimeFormatter.ofPattern("HH:mm")

    return try {
        val sanitizedTime = time.trim().uppercase()
        LocalTime.parse(sanitizedTime, flexibleFormatter).format(formatter24)
    } catch (e: Exception) {
        Log.w("OpeningHoursUtils", "Failed to convert time: $time", e)
        "00:00"
    }
}

/**
 * Converts any time string (12h or 24h) into minutes since midnight.
 *
 * Used internally to compute overlap between restaurant hours and user‑selected time slots.
 */
private fun timeToMinutes(t: String): Int =
    try {
        // If it's 12-hour format (contains AM/PM), convert it first
        val normalizedTime =
            if (t.contains(Regex("[AP]M", RegexOption.IGNORE_CASE))) {
                convertTo24(t)
            } else {
                t
            }

        val parts = normalizedTime.split(":")
        val hours = parts[0].trim().toInt()
        val minutes = parts[1].trim().toInt()
        (hours * 60) + minutes
    } catch (e: Exception) {
        Log.w("OpeningHoursUtils", "Failed to parse time to minutes: $t", e)
        0
    }

/**
 * Checks whether a restaurant is open for at least [MINIMUM_OVERLAP_MINUTES]
 * during the target time slot.
 *
 * Handles cross‑midnight ranges (e.g., `"22:00-02:00"`).
 *
 * @return `true` if the overlap is ≥ 60 minutes.
 */
fun hasOverlap(
    oStartStr: String,
    oEndStr: String,
    tStartStr: String,
    tEndStr: String,
): Boolean {
    // Use the new helper to safely get minutes
    val oStart = timeToMinutes(oStartStr)
    val oEnd = timeToMinutes(oEndStr)
    val tStart = timeToMinutes(tStartStr)
    val tEnd = timeToMinutes(tEndStr)

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
    for ((os, oe) in oSegments) {
        for ((ts, te) in tSegments) {
            val overlap = minOf(oe, te) - maxOf(os, ts)
            if (overlap > maxOverlap) maxOverlap = overlap
        }
    }

    return maxOverlap >= MINIMUM_OVERLAP_MINUTES
}

/**
 * Determines whether a restaurant is open during the selected [timing].
 *
 * Steps:
 * - Match the weekday (Mon, Tue…) using [parseDays]
 * - Handle 24‑hour and Closed cases
 * - Parse the time range and check overlap using [hasOverlap]
 */
fun isRestaurantOpenForTiming(
    restaurant: Restaurant,
    timing: DateTime,
): Boolean {
    val targetDay = timing.toDayAbbrev()
    val hoursList = restaurant.openingHours ?: return true

    // Find the entry for the target day (e.g., "Thursday: Open 24 hours")
    val dailySchedule =
        hoursList.find { hours ->
            parseDays(hours).contains(targetDay)
        } ?: return false

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
 * Builds a human‑readable opening‑hours label for the given restaurant
 * based on the selected [timing].
 *
 * Examples:
 * - `"Open 24 hours"`
 * - `"Closed"`
 * - `"9:00 AM – 5:00 PM"`
 * - Falls back to the raw Google Places string if parsing fails.
 */
fun getOpenLabel(
    restaurant: Restaurant,
    timing: DateTime,
): String? {
    val day = timing.toLocalDate().dayOfWeek.getDisplayName(TextStyle.FULL, Locale.ENGLISH)
    // Efficiency: Find the string once.
    val rawHours =
        restaurant.openingHours?.firstOrNull {
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
 * Falls back to the original string if parsing fails.
 */
fun format24ToAmPm(time: String): String {
    val f24 = DateTimeFormatter.ofPattern("H:mm")
    val f12 = DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH)
    return try {
        LocalTime.parse(time, f24).format(f12)
    } catch (e: Exception) {
        Log.w("OpeningHoursUtils", "Failed to format time: $time", e)
        time // Fallback to original string if parsing fails
    }
}
