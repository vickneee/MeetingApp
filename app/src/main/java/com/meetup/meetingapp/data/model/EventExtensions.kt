package com.meetup.meetingapp.data.model


import java.time.LocalDate
import java.time.LocalTime

/**
 * Converts an [Event] into a Firestore‑friendly Map.
 *
 * Firestore cannot store types like LocalDate, LocalTime, enums,
 * or our custom data classes directly. This helper prepares those
 * values in a simple format (Strings, Lists, Maps) so they can be
 * written safely.
 *
 * NOTE:
 * - This conversion is not currently used in the active event‑creation flow,
 *   since we are only saving a small subset of fields for now.
 * - Depending on how the full event model evolves, this mapping may become
 *   useful later when we start storing or loading complete Event objects.
 */
fun Event.toFirestoreMap(): Map<String, Any?> {
    return mapOf(
        "eventCode" to eventCode,
        "eventKey" to eventKey,
        "hostId" to hostId,
        "id" to id,
        "status" to status.name,
        "eventTitle" to eventTitle,
        "hostName" to hostName,
        "dateRange" to mapOf(
            "start" to dateRange.start.toString(),
            "end" to dateRange.end.toString()
        ),
        "timeSlots" to timeSlots.map {
            mapOf(
                "start" to it.start.toString(),
                "end" to it.end.toString()
            )
        },
        "locationOptions" to mapOf(
            "cities" to locationOptions.cities,
            "country" to locationOptions.country,
            "region" to locationOptions.region
        ),
        "placeTypeOptions" to placeTypeOptions.map { it.name },
        "createdAt" to createdAt
    )
}

/**
 * Reconstructs an [Event] from a Firestore document Map.
 *
 * Firestore returns raw Maps and Lists, so this function converts those
 * values back into our strongly‑typed Event model, including LocalDate,
 * LocalTime, enums, and custom classes.
 *
 * NOTE:
 * - This is also not used yet in the current flow, since we are not
 *   reading full Event objects back from Firestore at this stage.
 * - It may become relevant later once we start loading complete events
 *   (e.g., for dashboards or event detail screens).
 */
fun Map<String, Any?>.toEvent(): Event {

    val dateRangeMap = this["dateRange"] as? Map<*, *>
        ?: error("dateRange missing")

    val timeSlotsRaw = this["timeSlots"] as? List<*>
        ?: error("timeSlots missing")

    val locationMap = this["locationOptions"] as? Map<*, *>
        ?: error("locationOptions missing")

    val placeTypesRaw = this["placeTypeOptions"] as? List<*>
        ?: error("placeTypeOptions missing")

    return Event(
        eventCode = this["eventCode"] as String,
        eventKey = this["eventKey"] as String,
        hostId = this["hostId"] as String,
        id = this["id"] as String,
        status = EventStatus.valueOf(this["status"] as String),

        eventTitle = this["eventTitle"] as String,
        hostName = this["hostName"] as String,

        dateRange = DateRange(
            start = LocalDate.parse(dateRangeMap["start"] as String),
            end = LocalDate.parse(dateRangeMap["end"] as String)
        ),

        timeSlots = timeSlotsRaw.mapNotNull { raw ->
            val slot = raw as? Map<*, *> ?: return@mapNotNull null
            TimeSlot(
                start = LocalTime.parse(slot["start"] as String),
                end = LocalTime.parse(slot["end"] as String)
            )
        },

        locationOptions = LocationOption(
            cities = (locationMap["cities"] as? List<*>)?.map { it as String } ?: emptyList(),
            country = locationMap["country"] as String,
            region = locationMap["region"] as String
        ),

        placeTypeOptions = placeTypesRaw.mapNotNull { raw ->
            (raw as? String)?.let { PlaceType.valueOf(it) }
        }
    )
}


