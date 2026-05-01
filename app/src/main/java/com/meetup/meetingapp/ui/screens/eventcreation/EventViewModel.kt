package com.meetup.meetingapp.ui.screens.eventcreation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meetup.meetingapp.data.model.CountryOption
import com.meetup.meetingapp.data.model.DateRange
import com.meetup.meetingapp.data.model.Event
import com.meetup.meetingapp.data.model.PlaceType
import com.meetup.meetingapp.data.model.TimeSlot
import com.meetup.meetingapp.data.repositories.EventRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.ZoneId

/**
 * ViewModel responsible for managing the event creation flow.
 *
 * @param eventRepository Repository for event-related operations.
 * @property _uiState Mutable state flow containing the current UI state.
 * @property uiState State flow exposing the current UI state.
 * @property _eventState Mutable state flow representing the event creation state.
 * @property eventState State flow exposing the event creation state.
 * @property _citiesFetchState Mutable state flow representing the state of fetching cities.
 * @property citiesFetchState State flow exposing the state of fetching cities.
 * @property selectedCountries Mutable state flow containing the selected countries.
 * @property citiesState State flow exposing the list of cities based on selected countries.
 * @property events State flow exposing the list of events.
 */
class EventViewModel(
    private val eventRepository: EventRepository,
) : ViewModel() {
    /**
     * Mutable state flow representing the current UI state.
     */
    private val _uiState =
        MutableStateFlow(
            EventUiState(
                timeSlots = mutableListOf(TimeSlot("11:00", "13:00")),
            ),
        )

    /**
     * State flow exposing the current UI state.
     */
    val uiState = _uiState.asStateFlow()

    /**
     * Mutable state flow representing the event creation state.
     */
    private val _eventState = MutableStateFlow<EventState>(EventState.Loading)

    /**
     * State flow exposing the event creation state.
     */
    val eventState = _eventState.asStateFlow()

    /**
     * Mutable state flow representing the state of fetching cities.
     */
    private val _citiesFetchState = MutableStateFlow<CitiesFetchState>(CitiesFetchState.Loading)

    /**
     * State flow exposing the state of fetching cities.
     */
    val citiesFetchState = _citiesFetchState.asStateFlow()

    /**
     * Mutable state flow containing the selected countries.
     */
    private val selectedCountries = MutableStateFlow(listOf(CountryOption.Finland))

    /**
     * Whether the "Next" button should be enabled or not.
     */
    val canProceed: Boolean
        get() = uiState.value.eventTitle.isNotBlank() && uiState.value.hostName.isNotBlank()

    /**
     * Reactively observe cities based on selected countries.
     * Uses Room as the single source of truth.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val citiesState: StateFlow<List<String>> =
        selectedCountries
            .flatMapLatest { countries ->
                if (countries.isEmpty()) {
                    _citiesFetchState.value = CitiesFetchState.Success
                    flowOf(emptyList())
                } else {
                    _citiesFetchState.value = CitiesFetchState.Loading
                    val flows = countries.map { eventRepository.getCitiesByCountry(it) }
                    combine(flows) { cityLists ->
                        cityLists.flatMap { it }.distinct().sorted()
                    }
                }
            }.flowOn(Dispatchers.Default)
            .map {
                _citiesFetchState.value = CitiesFetchState.Success
                it
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList(),
            )

    private val _isEventsLoading = MutableStateFlow(true)
    val isEventsLoading = _isEventsLoading.asStateFlow()

    private val _hasHostSubmitted = MutableStateFlow<Boolean?>(null)
    val hasHostSubmitted = _hasHostSubmitted.asStateFlow()

    /**
     * Synchronizes events from the repository.
     */
    val events: StateFlow<List<Event>> =
        eventRepository
            .getEvents()
            .onEach { _isEventsLoading.value = false }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList(),
            )

    /**
     * Initializes the ViewModel by synchronizing cities.
     */
    init {
        syncCities()
    }

    /**
     *  Synchronizes cities from the repository.
     */
    private fun syncCities() {
        viewModelScope.launch(Dispatchers.IO) {
            eventRepository.syncCities()
        }
    }

    /**
     * Refreshes the city list based on current selection.
     * (Retained for compatibility with UI retry logic)
     */
    fun observeCities(countries: List<CountryOption>) {
        selectedCountries.value = countries
    }

    /**
     * Attempts to create a new event using the current UI state.
     */
    fun createEvent() {
        _eventState.value = EventState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val(eventCode, eventKey, eventId) = eventRepository.createEvent(uiState.value)
                withContext(Dispatchers.Main) {
                    _eventState.value = EventState.Success(eventCode, eventKey, eventId)
                    _hasHostSubmitted.value = false
                }
            } catch (e: Throwable) {
                withContext(Dispatchers.Main) {
                    _eventState.value = EventState.Error(e)
                }
            }
        }
    }

    /**
     * Loads an existing event's details for the "Event Created" page.
     */
    fun loadExistingEvent(eventId: String) {
        _eventState.value = EventState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            try {
                eventRepository.syncEventById(eventId)
                eventRepository
                    .getEventById(eventId)
                    .onEach { e ->
                        if (e != null) {
                            _eventState.value = EventState.Success(e.eventCode, e.eventKey, e.id)
                            checkHostSubmission(e.id, e.hostId)
                        }
                    }.first()
            } catch (e: Throwable) {
                withContext(Dispatchers.Main) {
                    _eventState.value = EventState.Error(e)
                }
            }
        }
    }

    private fun checkHostSubmission(
        eventId: String,
        hostId: String,
    ) {
        viewModelScope.launch {
            val result = eventRepository.hasUserSubmittedAvailability(eventId, hostId)
            _hasHostSubmitted.value = result
        }
    }

    /**
     * Updates the event title field in the UI state.
     *
     * @param title New event title.
     */
    fun updateTitle(title: String) {
        _uiState.update { current ->
            current.copy(
                eventTitle = title,
            )
        }
    }

    /**
     * Updates the host name field in the UI state.
     *
     * @param name New host name.
     */
    fun updateHostName(name: String) {
        _uiState.update { current ->
            current.copy(
                hostName = name,
            )
        }
    }

    /**
     * Updates the selected date range for the event.
     *
     * @param dateRange1 Start date of the range.
     * @param dateRange2 End date of the range.
     */
    fun updateDateRange(
        dateRange1: Long?,
        dateRange2: Long?,
    ) {
        if (dateRange1 != null && dateRange2 != null) {
            val start = Instant.ofEpochMilli(dateRange1).atZone(ZoneId.systemDefault()).toLocalDate()
            val end = Instant.ofEpochMilli(dateRange2).atZone(ZoneId.systemDefault()).toLocalDate()
            _uiState.update { current ->
                current.copy(
                    dateRange = DateRange(start.toString(), end.toString()),
                    hasSelectedDateRange = true,
                )
            }
        }
    }

    /**
     * Adds a new time slot to the event.
     *
     * @param start Start time of the time slot.
     * @param end End time of the time slot.
     */
    fun addTimeSlot(
        start: String,
        end: String,
    ) {
        val current = _uiState.value.timeSlots.toMutableList()
        current.add(TimeSlot(start, end))
        _uiState.update { it.copy(timeSlots = current) }
    }

    /**
     * Updates an existing time slot in the event.
     *
     * @param index Index of the time slot to update.
     * @param start New start time of the time slot.
     * @param end New end time of the time slot.
     */
    fun updateTimeSlot(
        index: Int,
        start: String,
        end: String,
    ) {
        val current = _uiState.value.timeSlots.toMutableList()
        current[index] = TimeSlot(start, end)
        _uiState.update { it.copy(timeSlots = current) }
    }

    /**
     * Removes a time slot from the event.
     *
     * @param slot Time slot to remove.
     */
    fun removeTimeSlot(slot: TimeSlot) {
        _uiState.update { current ->
            current.copy(
                timeSlots = current.timeSlots - slot,
            )
        }
    }

    /**
     * Toggles the selection of a country in the UI state.
     *
     * @param country Country to toggle the selection for.
     */
    fun toggleCountry(country: CountryOption) {
        _uiState.update { current ->
            val updatedCountries =
                if (current.locations.countries.contains(country.code)) {
                    current.locations.countries - country.code
                } else {
                    current.locations.countries + country.code
                }

            current.copy(
                locations =
                    current.locations.copy(
                        countries = updatedCountries,
                    ),
            )
        }

        // Refresh cities based on all selected countries
        val selectedCountryOptions =
            _uiState.value.locations.countries.mapNotNull { code ->
                CountryOption.entries.find { it.code == code }
            }
        selectedCountries.value = selectedCountryOptions
    }

    /**
     * Toggles the selection of a city in the UI state.
     *
     * @param city City to toggle the selection for.
     */
    fun toggleCity(city: String) {
        _uiState.update { current ->
            val updated =
                if (current.locations.cities.contains(city)) {
                    current.locations.cities - city
                } else {
                    current.locations.cities + city
                }
            current.copy(
                locations =
                    current.locations.copy(
                        cities = updated,
                    ),
            )
        }
    }

    /**
     * Adds a place type (e.g., RESTAURANT, CAFE) to the selected list.
     *
     * @param placeType Place type to add.
     */
    fun addPlaceType(placeType: PlaceType) {
        _uiState.update { current ->
            current.copy(
                placeTypes = current.placeTypes + placeType,
            )
        }
    }

    /**
     * Removes a place type from the selected list.
     *
     * @param placeType Place type to remove.
     */
    fun removePlaceType(placeType: PlaceType) {
        _uiState.update { current ->
            current.copy(
                placeTypes = current.placeTypes - placeType,
            )
        }
    }
}

/**
 * Data class representing the current UI state of the event creation flow.
 *
 * @property eventTitle Title of the event.
 * @property hostName Name of the event host.
 * @property dateRange Date range of the event.
 * @property hasSelectedDateRange Whether a date range has been selected.
 * @property timeSlots List of time slots for the event.
 * @property locations Location options for the event.
 * @property placeTypes List of selected place types.
 */
data class EventUiState(
    val eventTitle: String = "",
    val hostName: String = "",
    val dateRange: DateRange = DateRange(),
    val hasSelectedDateRange: Boolean = false,
    val timeSlots: List<TimeSlot> = emptyList(),
    val locations: com.meetup.meetingapp.data.model.LocationOption =
        com.meetup.meetingapp.data.model
            .LocationOption(),
    val placeTypes: List<PlaceType> = emptyList(),
)

/**
 * Sealed interface representing the state of the event creation process.
 *
 * @property Loading Indicates that the event creation process is in progress.
 * @property Success Indicates that the event creation process was successful.
 * @property Error Indicates that an error occurred during the event creation process.
 */
sealed interface EventState {
    object Loading : EventState

    data class Success(
        val eventCode: String,
        val eventKey: String,
        val eventId: String,
    ) : EventState

    data class Error(
        val error: Throwable,
    ) : EventState
}

/**
 * Sealed interface representing the state of fetching cities.
 *
 * @property Loading Indicates that the city fetching process is in progress.
 * @property Success Indicates that the city fetching process was successful.
 * @property Error Indicates that an error occurred during the city fetching process.
 */
sealed interface CitiesFetchState {
    object Loading : CitiesFetchState

    object Success : CitiesFetchState

    data class Error(
        val message: String,
    ) : CitiesFetchState
}
