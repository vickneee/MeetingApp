package com.meetup.meetingapp.utils

import com.meetup.meetingapp.data.model.DateTime
import com.meetup.meetingapp.data.model.Restaurant
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale


/**
 * Normalizes Google Places openingHours strings.
 */
fun normalizeHours(raw: String): String {
    return raw.replace("–", "-")
        .replace(Regex("[\\u202F\\u2009\\u200A\\u200B\\uFEFF\\u00A0]"), "")
        .replace("AM", " AM").replace("PM", " PM").trim()
}

/**
 * Converts a DateTime into a 3-letter weekday abbreviation (Mon, Tue…)
 */
fun DateTime.toDayAbbrev(): String {
    val localDate = this.toLocalDate()
    return localDate.dayOfWeek.name.take(3).lowercase().replaceFirstChar { it.uppercase() }
}

/**
 * Extracts weekday from openingHours string, handling ranges like "Mon-Fri".
 */
fun parseDays(hours: String): List<String> {
    val daysOfWeek = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    val normalized = hours.takeWhile { it != ':' }

    // Handle ranges like "Monday-Friday"
    if (normalized.contains("-") || normalized.contains("–")) {
        val parts = normalized.split(Regex("[-–]")).map {
            it.trim().take(3).lowercase().replaceFirstChar { c -> c.uppercase() }
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
    return listOf(match.groupValues[1].take(3).lowercase().replaceFirstChar { it.uppercase() })
}



/**
 * Extracts start/end time from openingHours string.
 */
fun extractTimeRange(hours: String): Pair<String, String>? {
    // Normalize weird Unicode spaces to regular spaces
    val cleaned = hours.replace(Regex("[\\u202F\\u2009\\u200A\\u200B\\uFEFF\\u00A0]"), " ")

    // Match any dash-like character: -, –, —, ‑, −
    val regex = Regex(
        "(\\d{1,2}:\\d{2}\\s*[AP]M)\\s*[\\-–—‑−]\\s*(\\d{1,2}:\\d{2}\\s*[AP]M)"
    )

    val match = regex.find(cleaned) ?: return null
    val (start, end) = match.destructured
    return convertTo24(start) to convertTo24(end)
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
    tEndStr: String
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
    fun toSegments(start: Int, end: Int): List<Pair<Int, Int>> {
        return if (end > start) {
            listOf(start to end)
        } else {
            listOf(start to 1440, 0 to end)
        }
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
    return maxOverlap >= 60
}



/**
 * Determines whether a restaurant is open during the selected timing.
 */
fun isRestaurantOpenForTiming(restaurant: Restaurant, timing: DateTime): Boolean {
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
 *
 * Example output: `"10:00 AM – 8:00 PM"`
 *
 * @param restaurant The restaurant whose opening hours should be evaluated.
 * @param timing The selected date/time used to determine the correct weekday.
 * @return A formatted label or null if opening hours are unavailable.
 */
fun getOpenLabel(restaurant: Restaurant, timing: DateTime): String? {
    val day = timing.toLocalDate().dayOfWeek.getDisplayName(TextStyle.FULL, Locale.ENGLISH)
    val hours = restaurant.openingHours?.firstOrNull { it.startsWith(day) } ?: return null
    val range = extractTimeRange(hours) ?: return null
    return "${format24ToAmPm(range.first)} – ${format24ToAmPm(range.second)}"
}

/**
 * Converts a 24‑hour time string (e.g., `"18:30"`) into a 12‑hour AM/PM format.
 *
 * @param time A time string in `"HH:mm"` format.
 * @return A formatted time string in `"h:mm a"` format.
 */
fun format24ToAmPm(time: String): String {
    val f24 = DateTimeFormatter.ofPattern("HH:mm")
    val f12 = DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH)
    return LocalTime.parse(time, f24).format(f12)
}