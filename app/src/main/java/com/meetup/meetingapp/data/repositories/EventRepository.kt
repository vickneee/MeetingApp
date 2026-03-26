package com.meetup.meetingapp.data.repositories


import com.meetup.meetingapp.data.model.DateRange
import com.meetup.meetingapp.data.model.LocationOption
import com.meetup.meetingapp.data.model.PlaceType
import com.meetup.meetingapp.data.model.TimeSlot

/**
 * Repository interface for managing event-related operations.
 *
 * Creates a new event and returns the generated event code and event key.
 * Implementations of this interface handle communication with the underlying
 * data source (e.g., Cloud Firestore).
 */
interface EventRepository {
    suspend fun createEvent(
        eventTitle: String,
        hostName:String,
        dataRange: DateRange,
        timeSlots: List<TimeSlot>,
        locations: LocationOption,
        placeTypes:
        List<PlaceType>
    ): Result<Pair<String, String>>
}