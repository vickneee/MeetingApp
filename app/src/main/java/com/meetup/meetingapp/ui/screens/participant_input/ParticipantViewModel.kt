package com.meetup.meetingapp.ui.screens.participant_input

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meetup.meetingapp.data.model.DateTime
import com.meetup.meetingapp.data.model.Event
import com.meetup.meetingapp.data.model.FoodCategory
import com.meetup.meetingapp.data.repositories.EventRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ParticipantViewModel(
    private val eventRepository: EventRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // Event code entered by participant to join
    private val eventCode: String = savedStateHandle["eventCode"] ?: ""

    // The fetched event from Firestore
    private val _event = MutableStateFlow<Event?>(null)
    val event = _event.asStateFlow()

    // Participant input state
    private val _participantState = MutableStateFlow(ParticipantInputState())
    val participantState = _participantState.asStateFlow()

    // Fetch state
    private val _fetchState = MutableStateFlow<FetchState>(FetchState.Loading)
    val fetchState = _fetchState.asStateFlow()

    init {
        fetchEventByCode()
    }

    // Fetch event from Firestore by eventCode
    private fun fetchEventByCode() {
        viewModelScope.launch {
            try {
                val result = eventRepository.getEventByCode(eventCode)
                if (result != null) {
                    _event.value = result
                    _fetchState.value = FetchState.Success
                } else {
                    _fetchState.value = FetchState.Error("Event not found")
                }
            } catch (e: Exception) {
                _fetchState.value = FetchState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun toggleDateTime(dateTime: DateTime) {
        _participantState.update { current ->
            val updated = if (current.selectedDateTimes.contains(dateTime))
                current.selectedDateTimes - dateTime
            else
                current.selectedDateTimes + dateTime
            current.copy(selectedDateTimes = updated)
        }
    }

    fun toggleFoodCategory(category: FoodCategory) {
        _participantState.update { current ->
            val updated = if (current.selectedFoodCategories.contains(category))
                current.selectedFoodCategories - category
            else
                current.selectedFoodCategories + category
            current.copy(selectedFoodCategories = updated)
        }
    }

    fun updateName(name: String) {
        _participantState.update { it.copy(participantName = name) }
    }
}

// Participant input UI state
data class ParticipantInputState(
    val participantName: String = "",
    val selectedDateTimes: List<DateTime> = emptyList(),
    val selectedFoodCategories: List<FoodCategory> = emptyList()
)

// Fetch state
sealed interface FetchState {
    object Loading : FetchState
    object Success : FetchState
    data class Error(val message: String) : FetchState
}