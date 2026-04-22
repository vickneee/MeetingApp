package com.meetup.meetingapp.data.model
import com.google.firebase.Timestamp

/**
 * Core event model containing host settings, user voting results, and final selections.
 *
 * This model represents the main event document stored in Firestore.
 * It includes host-defined configurations, available options, aggregated voting results,
 * and the final confirmed choices for time and location.
 *
 * @property eventCode Public event code shared with participants.
 * @property eventKey Private key required for participants to join the event.
 * @property hostId Firestore document ID of the user who created the event.
 * @property id Firestore document ID for this event.
 * @property status Current lifecycle status of the event.
 *
 * @property eventTitle Title of the event defined by the host.
 * @property hostName Display name of the host.
 * @property dateRange Range of dates available for voting.
 * @property timeSlots List of time slots available for voting.
 * @property locationOptions Location-related options provided by the host.
 * @property placeTypeOptions Types of places participants can vote on.
 *
 * @property dateTimeCandidates Aggregated voting results for date/time combinations.
 * @property locationCandidates Aggregated voting results for location options.
 * @property placeTypeCandidates Aggregated voting results for placeTypes.
 * @property foodCategoryCandidates Aggregated voting results for food categories.
 * @property restaurantCandidates Aggregated voting results for restaurant choices.
 *
 * @property finalTime Final selected date/time after voting is completed.
 * @property finalPlace Final selected place after voting is completed.
 *
 * @property createdAt Timestamp indicating when the event was created.
 */
data class Event(
    // Public event code shared with participants.
    val eventCode: String = "",
    // Private key required to join the event.
    val eventKey: String = "",
    // Reference to the host user document.
    val hostId: String = "",
    // Firestore document ID for this event.
    val id: String = "",
    // Current status of the event.
    val status: EventStatus = EventStatus.CREATED,
    // Host-defined settings for the event.
    val eventTitle: String = "",
    val hostName: String = "",
    val dateRange: DateRange = DateRange(),
    val timeSlots: List<TimeSlot> = listOf(),
    val locationOptions: LocationOption = LocationOption(),
    val placeTypeOptions: List<PlaceType> = listOf(),
    // The first voting results.
    val dateTimeCandidates: List<DateTime> = listOf(),
    val locationCandidates: List<String> = listOf(),
    val placeTypeCandidates: List<PlaceType> = listOf(),
    val foodCategoryCandidates: List<FoodCategory> = listOf(),
    // The second voting result
    val restaurantCandidates: List<String> = listOf(),
    // Final selections.
    val finalTime: DateTime? = null,
    val finalPlace: String? = null,
    // Selected city coordinates
    val selectedLocationLat: Double? = null,
    val selectedLocationLng: Double? = null,
    val createdAt: Timestamp = Timestamp.now(),
)
