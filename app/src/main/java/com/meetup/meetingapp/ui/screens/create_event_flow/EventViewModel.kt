package com.meetup.meetingapp.ui.screens.create_event_flow

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meetup.meetingapp.data.model.CountryOption
import com.meetup.meetingapp.data.model.DateRange
import com.meetup.meetingapp.data.model.Event
import com.meetup.meetingapp.data.model.PlaceType
import com.meetup.meetingapp.data.model.TimeSlot
import com.meetup.meetingapp.data.repositories.EventRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.ZoneId

/**
 * ViewModel responsible for managing the event creation flow.
 */
class EventViewModel(private val eventRepository: EventRepository):  ViewModel(){

    private val _uiState = MutableStateFlow(
        EventUiState(
            timeSlots = mutableListOf(TimeSlot("11:00", "13:00"))
        )
    )

    val uiState = _uiState.asStateFlow()

    private val _eventState = MutableStateFlow<EventState>(EventState.Loading)

    val eventState = _eventState.asStateFlow()

    // Cities fetch state
    private val _citiesFetchState = MutableStateFlow<CitiesFetchState>(CitiesFetchState.Loading)

    val citiesFetchState = _citiesFetchState.asStateFlow()

    private val _citiesState = MutableStateFlow(listOf<String>())

    val citiesState = _citiesState.asStateFlow()

    /**
     * Synchronizes events from the repository.
     */
    val events: StateFlow<List<Event>> = eventRepository.getEvents()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        syncCities()
        observeCities(listOf(CountryOption.Finland))
    }

    /**
     *  Synchronizes cities from the repository.
     */
    private fun syncCities(){
        viewModelScope.launch(Dispatchers.IO){
            eventRepository.syncCities()
        }
    }

    fun observeCities(countries: List<CountryOption>) {
        viewModelScope.launch {
            try {
                // For now, let's just collect cities for all selected countries and merge them
                // Or if the repository supports multiple countries, use that.
                // Assuming it might take a while, showing loading.
                _citiesFetchState.value = CitiesFetchState.Loading
                
                val allCities = mutableListOf<String>()
                countries.forEach { country ->
                    // This is a bit naive if we have multiple flows, but for now we take the first snapshot or use a combine.
                    // Simplified: just get for each and add to list.
                    // Better would be to have a repository method that takes a list.
                    // For now, let's stick to the current API and just handle the first one if only one is expected, 
                    // or collect all.
                    eventRepository.getCitiesByCountry(country).collect { cities ->
                        allCities.addAll(cities)
                        _citiesState.value = allCities.distinct().sorted()
                        _citiesFetchState.value = CitiesFetchState.Success
                    }
                }
                
                if (countries.isEmpty()) {
                    _citiesState.value = emptyList()
                    _citiesFetchState.value = CitiesFetchState.Success
                }

            } catch (e: Exception) {
                Log.e("EventViewModel", "Error observing cities", e)
                _citiesFetchState.value = CitiesFetchState.Error("Cities not found")
            }
        }
    }

    /**
     * Attempts to create a new event using the current UI state.
     */
    fun createEvent(){
        _eventState.value = EventState.Loading
        viewModelScope.launch(Dispatchers.IO)  {
            try {
                val(eventCode, eventKey, eventId) = eventRepository.createEvent(uiState.value).getOrThrow()

                withContext(Dispatchers.Main) {
                    _eventState.value = EventState.Success(eventCode, eventKey, eventId)
                }
            } catch (e: Throwable){
                withContext(Dispatchers.Main) {
                    _eventState.value = EventState.Error(e)
                }
            }
        }
    }

    /**
     * Updates the event title field in the UI state.
     */
    fun updateTitle(title: String){
        _uiState.update {current ->
            current.copy(
                eventTitle = title
            )
        }
    }

    /**
     * Updates the host name field in the UI state.
     */
    fun updateHostName(name: String){
        _uiState.update {current ->
            current.copy(
                hostName = name
            )
        }
    }

    /**
     * Updates the selected date range for the event.
     */
    fun updateDateRange(dateRange1: Long?, dateRange2: Long?){
        if (dateRange1 != null && dateRange2 != null) {
            val start = Instant.ofEpochMilli(dateRange1).atZone(ZoneId.systemDefault()).toLocalDate()
            val end = Instant.ofEpochMilli(dateRange2).atZone(ZoneId.systemDefault()).toLocalDate()
            _uiState.update { current ->
                current.copy(
                    dateRange = DateRange(start.toString(), end.toString()),
                    hasSelectedDateRange = true
                )
            }
        }
    }

    /**
     * Adds a new time slot to the event.
     */
    fun addTimeSlot(start: String, end: String) {
        val current = _uiState.value.timeSlots.toMutableList()
        current.add(TimeSlot(start, end))
        _uiState.update { it.copy(timeSlots = current) }
    }

    /**
     * Updates an existing time slot in the event.
     */
    fun updateTimeSlot(index: Int, start: String, end: String) {
        val current = _uiState.value.timeSlots.toMutableList()
        current[index] = TimeSlot(start, end)
        _uiState.update { it.copy(timeSlots = current) }
    }

    /**
     * Removes a time slot from the event.
     */
    fun removeTimeSlot(slot: TimeSlot){
        _uiState.update {current ->
            current.copy(
                timeSlots = current.timeSlots - slot
            )
        }
    }

    fun toggleCountry(country: CountryOption) {
        _uiState.update { current ->
            val updatedCountries = if (current.locations.countries.contains(country.name))
                current.locations.countries - country.name
            else
                current.locations.countries + country.name
            
            current.copy(
                locations = current.locations.copy(
                    countries = updatedCountries
                )
            )
        }
        
        // Refresh cities based on all selected countries
        val selectedCountryOptions = _uiState.value.locations.countries.mapNotNull { name ->
            CountryOption.entries.find { it.name == name }
        }
        observeCities(selectedCountryOptions)
    }

    fun toggleCity(city: String) {
        _uiState.update { current ->
            val updated = if (current.locations.cities.contains(city))
                current.locations.cities - city
            else
                current.locations.cities + city
            current.copy(
                locations = current.locations.copy(
                    cities = updated
                )
            )
        }
    }

    /**
     * Adds a city to the list of selected cities in the location options.
     */
    fun addCity(city: String) {
        _uiState.update { current ->
            current.copy(
                locations = current.locations.copy(
                    cities = current.locations.cities + city
                )
            )
        }
    }

    /**
     * Removes a city from the list of selected cities.
     */
    fun removeCity(city: String) {
        _uiState.update { current ->
            current.copy(
                locations = current.locations.copy(
                    cities = current.locations.cities - city
                )
            )
        }
    }

    /**
     * Adds a place type (e.g., RESTAURANT, CAFE) to the selected list.
     */
    fun addPlaceType(placeType: PlaceType){
        _uiState.update {current ->
            current.copy(
                placeTypes = current.placeTypes + placeType
            )
        }
    }

    /**
     * Removes a place type from the selected list.
     */
    fun removePlaceType(placeType: PlaceType){
        _uiState.update {current ->
            current.copy(
                placeTypes = current.placeTypes - placeType
            )
        }
    }
}

data class EventUiState(
    val eventTitle: String = "",
    val hostName: String = "",
    val dateRange: DateRange = DateRange(),
    val hasSelectedDateRange: Boolean = false,
    val timeSlots: List<TimeSlot> = emptyList(),
    val locations: com.meetup.meetingapp.data.model.LocationOption = com.meetup.meetingapp.data.model.LocationOption(),
    val placeTypes: List<PlaceType> = emptyList()
)

sealed interface EventState {
    object Loading : EventState
    data class Success(val eventCode: String, val eventKey: String, val eventId: String) : EventState
    data class Error(val error: Throwable) : EventState
}

sealed interface CitiesFetchState {
    object Loading : CitiesFetchState
    object Success : CitiesFetchState
    data class Error(val message: String) : CitiesFetchState
}
