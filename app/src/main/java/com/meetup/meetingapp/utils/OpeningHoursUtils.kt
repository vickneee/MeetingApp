package com.meetup.meetingapp.utils

import com.meetup.meetingapp.data.model.DateTime
import com.meetup.meetingapp.data.model.Restaurant
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.format.TextStyle
import java.util.Locale
import android.util.Log

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
 * Converts "9:00 AM" or "9:00AM" → "09:00" (24-hour format)
 */
fun convertTo24(time: String): String {
    val flexibleFormatter = DateTimeFormatterBuilder()
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
 * Internal helper to convert any time string (12h or 24h) to minutes since midnight.
 * This makes hasOverlap much more robust.
 */
private fun timeToMinutes(t: String): Int {
    return try {
        // If it's 12-hour format (contains AM/PM), convert it first
        val normalizedTime = if (t.contains(Regex("[AP]M", RegexOption.IGNORE_CASE))) {
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
    // Use the new helper to safely get minutes
    val oStart = timeToMinutes(oStartStr)
    val oEnd = timeToMinutes(oEndStr)
    val tStart = timeToMinutes(tStartStr)
    val tEnd = timeToMinutes(tEndStr)

    fun toSegments(start: Int, end: Int): List<Pair<Int, Int>> =
        if (end > start) listOf(start to end)
        else listOf(start to 1440, 0 to end)

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
 * Determines whether a restaurant is open during the selected timing.
 */
fun isRestaurantOpenForTiming(
    restaurant: Restaurant,
    timing: DateTime,
): Boolean {
    val targetDay = timing.toDayAbbrev()
    val hoursList = restaurant.openingHours ?: return true
    return hoursList.any { hours ->
        val days = parseDays(hours)
        if (!days.contains(targetDay)) return@any false
        val range = extractTimeRange(hours) ?: return@any false
        hasOverlap(range.first, range.second, timing.timeSlot.start, timing.timeSlot.end)
    }
}

/**
 * Builds a human‑readable opening hours label for the given restaurant
 * based on the selected timing.
 */
fun getOpenLabel(
    restaurant: Restaurant,
    timing: DateTime,
): String? {
    val day = timing.toLocalDate().dayOfWeek.getDisplayName(TextStyle.FULL, Locale.ENGLISH)
    val hours = restaurant.openingHours?.firstOrNull { it.startsWith(day) } ?: return null
    val range = extractTimeRange(hours) ?: return null
    return "${format24ToAmPm(range.first)} – ${format24ToAmPm(range.second)}"
}

/**
 * Converts a 24‑hour time string (e.g., `"18:30"`) into a 12‑hour AM/PM format.
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