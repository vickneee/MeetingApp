package com.meetup.meetingapp.ui.screens.vote_for_place_flow

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.meetup.meetingapp.data.model.DateTime
import com.meetup.meetingapp.data.model.Event
import com.meetup.meetingapp.data.model.TimeSlot
import com.meetup.meetingapp.data.model.Restaurant
import com.meetup.meetingapp.data.repositories.EventRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.format.TextStyle
import java.util.Locale
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import android.annotation.SuppressLint
import android.location.Location
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.take

/**
 * ViewModel responsible for managing restaurant details, voting state,
 * and Google Places–related data for the Place Details screen.
 */
class PlaceViewModel(
    private val eventRepository: EventRepository,
    private val apiKey: String,
    private val fusedLocationClient: FusedLocationProviderClient,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    private val _restaurantDistance = MutableStateFlow<String?>(null)
    val restaurantDistance: StateFlow<String?> = _restaurantDistance.asStateFlow()

    private val eventId: String =
        savedStateHandle[ChooseDateAndAreaDestination.eventIdArg] ?: ""

    private val _event = MutableStateFlow<Event?>(null)
    val event: StateFlow<Event?> = _event.asStateFlow()

    private val _voteState = MutableStateFlow(VoteState())
    val voteState = _voteState.asStateFlow()

    private val _voteResultState = MutableStateFlow<VoteResultState?>(null)
    val voteResultState = _voteResultState.asStateFlow()

    private val _dateAndAreaState = MutableStateFlow(DateAndAreaState())
    val dateAndAreaState = _dateAndAreaState.asStateFlow()

    private val _restaurantState = MutableStateFlow<RestaurantState>(RestaurantState.Loading)
    val restaurantState = _restaurantState.asStateFlow()

    val selectedTiming = MutableStateFlow<DateTime?>(null)
    val selectedLocation = MutableStateFlow<String?>(null)

    private val _allRestaurants = MutableStateFlow(AllRestaurantState())
    val allRestaurants = _allRestaurants.asStateFlow()

    private var restaurantsLoaded = false

    init {
        viewModelScope.launch {
            if (eventId.isEmpty()) {
                Log.e("PlaceViewModel", "Event ID is missing!")
                _restaurantState.value = RestaurantState.Error(Exception("Event ID is missing"))
                return@launch
            }

            eventRepository.observeEventById(eventId).collect { event ->
                _event.value = event
                if (event != null) {
                    buildDateLocationOptions(event.dateTimeCandidates, event.locationCandidates)

                    if (!restaurantsLoaded && eventRepository.hasRestaurantCandidates(event.id)) {
                        restaurantsLoaded = true
                        getAllRestaurant(event.id)
                        _restaurantState.value = RestaurantState.Available
                    } else if (!restaurantsLoaded) {
                        _restaurantState.value = RestaurantState.Empty
                    }
                }
            }
        }
    }

    /**
     * Orchestrates the loading of place-specific data.
     */
    fun loadPlaceData(placeId: String, lat: Double?, lng: Double?) {
        fetchUserVote(placeId)
        updateDistanceToRestaurant(lat, lng)
    }

    /**
     * Performs a one-time GPS request and calculates distance.
     */
    @SuppressLint("MissingPermission")
    fun updateDistanceToRestaurant(destLat: Double?, destLng: Double?) {
        if (destLat == null || destLng == null) {
            _restaurantDistance.value = "Unknown distance"
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val location: Location? = fusedLocationClient.getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    CancellationTokenSource().token
                ).await()

                location?.let { userLoc ->
                    val results = FloatArray(1)
                    Location.distanceBetween(
                        userLoc.latitude, userLoc.longitude,
                        destLat, destLng,
                        results
                    )

                    val distanceInMeters = results[0]

                    _restaurantDistance.value = if (distanceInMeters < 1000) {
                        "${distanceInMeters.toInt()} m"
                    } else {
                        "%.1f km".format(distanceInMeters / 1000)
                    }
                } ?: run {
                    _restaurantDistance.value = "GPS unavailable"
                }
            } catch (e: Exception) {
                Log.e("PlaceViewModel", "Distance calculation failed", e)
                _restaurantDistance.value = "Error getting distance"
            }
        }
    }

    fun debugLoad() {
        viewModelScope.launch {
            Log.d("DEBUG", "=== Starting debug load ===")
            val hasCandidates = eventRepository.hasRestaurantCandidates(eventId)
            Log.d("DEBUG", "hasRestaurantCandidates: $hasCandidates")

            eventRepository.getRestaurants(eventId)
                .take(1)
                .collect { restaurants ->
                    Log.d("DEBUG", "getRestaurants returned ${restaurants.size} items")
                }
        }
    }

    private fun getAllRestaurant(eventId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            eventRepository.syncRestaurants(eventId)
            eventRepository.getRestaurants(eventId)
                .collect { restaurants ->
                    _allRestaurants.update { it.copy(allRestaurants = restaurants) }
                    _restaurantState.value = if (restaurants.isNotEmpty()) {
                        RestaurantState.Available
                    } else {
                        RestaurantState.Empty
                    }
                }
        }
    }

    val filteredRestaurants: StateFlow<List<Restaurant>> =
        combine(
            allRestaurants,
            selectedTiming,
            selectedLocation
        ) { allState, timing, location ->
            val all = allState.allRestaurants
            all.filter { restaurant ->
                val query = location?.trim() ?: ""
                val locationMatch = query.isEmpty() ||
                        (restaurant.address?.contains(query, ignoreCase = true) == true) ||
                        (restaurant.name.contains(query, ignoreCase = true))

                val timingMatch = timing == null || isRestaurantOpenForTiming(restaurant, timing)
                locationMatch && timingMatch
            }
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    fun DateTime.toDayAbbrev(): String {
        val localDate = this.toLocalDate()
        return localDate.dayOfWeek.name.take(3).lowercase().replaceFirstChar { it.uppercase() }
    }

    fun parseDays(hours: String): List<String> {
        val daysOfWeek = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        val normalized = hours.takeWhile { it != ':' }

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
                        daysOfWeek.subList(startIdx, 7) + daysOfWeek.subList(0, endIdx + 1)
                    }
                }
            }
        }

        val found = daysOfWeek.filter { normalized.contains(it, ignoreCase = true) }
        if (found.isNotEmpty()) return found

        val dayRegex = Regex("([A-Za-z]+):")
        val match = dayRegex.find(hours) ?: return emptyList()
        return listOf(match.groupValues[1].take(3).lowercase().replaceFirstChar { it.uppercase() })
    }

    fun normalizeHours(raw: String): String {
        return raw.replace("–", "-")
            .replace(Regex("[\\u202F\\u2009\\u200A\\u200B\\uFEFF\\u00A0]"), "")
            .replace("AM", " AM").replace("PM", " PM").trim()
    }

    fun extractTimeRange(hours: String): Pair<String, String>? {
        val normalized = normalizeHours(hours)
        val regex = Regex("(\\d{1,2}:\\d{2}\\s?[AP]M)\\s?-\\s?(\\d{1,2}:\\d{2}\\s?[AP]M)")
        val match = regex.find(normalized) ?: return null
        val (start, end) = match.destructured
        return convertTo24(start) to convertTo24(end)
    }

    fun convertTo24(time: String): String {
        val formatter12 = DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH)
        val formatter24 = DateTimeFormatter.ofPattern("HH:mm")
        return LocalTime.parse(time.uppercase(), formatter12).format(formatter24)
    }

    fun hasOverlap(oStartStr: String, oEndStr: String, tStartStr: String, tEndStr: String): Boolean {
        fun toMin(t: String): Int {
            val p = t.split(":")
            return (p[0].toIntOrNull() ?: 0) * 60 + (p[1].toIntOrNull() ?: 0)
        }
        val oStart = toMin(oStartStr)
        val oEnd = toMin(oEndStr)
        val tStart = toMin(tStartStr)
        val tEnd = toMin(tEndStr)
        val actualOEnd = if (oEnd <= oStart) oEnd + 1440 else oEnd
        return minOf(actualOEnd, tEnd) > maxOf(oStart, tStart)
    }

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

    fun setFilter(timing: DateTime, location: String) {
        selectedTiming.value = timing
        selectedLocation.value = location
    }

    fun buildDateLocationOptions(dateTimes: List<DateTime>, locations: List<String>) {
        val options = dateTimes.flatMap { dt ->
            locations.map { loc -> DateLocationOption(timing = dt, location = loc) }
        }
        _dateAndAreaState.value = DateAndAreaState(dateLocationOptions = options)
        if (selectedTiming.value == null && options.isNotEmpty()) {
            selectedTiming.value = options.first().timing
            selectedLocation.value = options.first().location
        }
    }

    fun submitVote(placeId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            selectedTiming.value?.let { timing ->
                eventRepository.submitVote(eventId, placeId, userId, timing)
                    .onSuccess {
                        _voteState.update { it.copy(isVoted = true) }
                        _voteResultState.value = VoteResultState.VoteSuccess
                    }.onFailure { e ->
                        _voteResultState.value = VoteResultState.VoteError("Vote failed")
                    }
            }
        }
    }

    fun fetchRestaurantDetail(placeId: String): Flow<Restaurant?> {
        return allRestaurants.map { state -> state.allRestaurants.find { it.placeId == placeId } }
    }

    fun getOpenLabel(restaurant: Restaurant, timing: DateTime): String? {
        val day = timing.toLocalDate().dayOfWeek.getDisplayName(TextStyle.FULL, Locale.ENGLISH)
        val hours = restaurant.openingHours?.firstOrNull { it.startsWith(day) } ?: return null
        val range = extractTimeRange(hours) ?: return null
        return "${format24ToAmPm(range.first)} – ${format24ToAmPm(range.second)}"
    }

    fun format24ToAmPm(time: String): String {
        val f24 = DateTimeFormatter.ofPattern("HH:mm")
        val f12 = DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH)
        return LocalTime.parse(time, f24).format(f12)
    }

    fun formatPriceLevel(level: Int?): String = if (level == null || level < 0) "" else "€".repeat(level + 1)

    fun buildPhotoUrl(photoReference: String?): String? {
        if (photoReference.isNullOrEmpty()) return null
        return "https://maps.googleapis.com/maps/api/place/photo?maxwidth=800&photo_reference=$photoReference&key=$apiKey"
    }

    fun fetchUserVote(placeId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            selectedTiming.value?.let { timing ->
                eventRepository.getUserVote(eventId, placeId, userId, timing)
                    .onSuccess { exists -> _voteState.update { it.copy(isVoted = exists) } }
            }
        }
    }
}

// Data classes and interfaces stay the same below...
data class AllRestaurantState(val allRestaurants: List<Restaurant> = listOf())
fun DateTime.toDisplayLabel(): String {
    val localDate = this.toLocalDate()
    return "${localDate.month.getDisplayName(TextStyle.SHORT, Locale.ENGLISH)} ${localDate.dayOfMonth} (${timeSlot.start}–${timeSlot.end})"
}
data class DateLocationOption(val timing: DateTime, val location: String) {
    val label: String get() = "${timing.toDisplayLabel()} — $location"
    val timingArg: String get() = timing.toSerializableString()
}
data class DateAndAreaState(val dateLocationOptions: List<DateLocationOption> = listOf())
fun DateTime.toSerializableString(): String = "$date|${timeSlot.start}-${timeSlot.end}"
fun String.toDateTime(): DateTime {
    val parts = split("|")
    val times = parts[1].split("-")
    return DateTime(date = parts[0], timeSlot = TimeSlot(times[0], times[1]))
}
sealed interface RestaurantState {
    object Loading : RestaurantState
    object Available : RestaurantState
    object Empty : RestaurantState
    data class Error(val error: Throwable) : RestaurantState
}
data class VoteState(val isVoted: Boolean = false)
sealed class VoteResultState {
    object VoteSuccess : VoteResultState()
    data class VoteError(val message: String) : VoteResultState()
}