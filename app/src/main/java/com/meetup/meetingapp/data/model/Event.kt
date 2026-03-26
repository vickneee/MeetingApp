package com.meetup.meetingapp.data.model
import java.time.LocalDate
import com.google.firebase.Timestamp



// Represents a combination of a specific date and a selected time slot.
data class DateTime(
    val date: LocalDate,
    val timeSlot: TimeSlot
)

// Core event model containing host settings, user voting results, and final selections.
data class Event (
    // Public event code shared with participants.
    val eventCode: String,

    // Private key required to join the event.
    val eventKey: String,

    // Reference to the host user document.
    val hostId: String,

    // Firestore document ID for this event.
    val id: String = "",

    // Current status of the event.
    val status: EventStatus = EventStatus.CREATED,

    // Host-defined settings for the event.
    val eventTitle: String,
    val hostName: String,
    val dateRange: DateRange,
    val timeSlots: List<TimeSlot>,
    val locationOptions: LocationOption,
    val placeTypeOptions: List<PlaceType>,

    // Participants and their chosen display names.
    val participants: List<String> = listOf(),
    val participantNames: Map<String, String> = mapOf(),

    // Voting results.
    val dateTimeCandidates: List<DateTime> = listOf(),
    val locationCandidates: List<String> = listOf(),
    val foodCategoryCandidates: List<FoodCategory> = listOf(),
    val restaurantCandidates: List<Restaurant> = listOf(),

    // Final selections.
    val finalTime: DateTime? = null,
    val finalPlace: Restaurant? = null,

    val createdAt: Timestamp = Timestamp.now()
)
