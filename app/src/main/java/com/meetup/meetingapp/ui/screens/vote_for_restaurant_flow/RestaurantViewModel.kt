package com.meetup.meetingapp.ui.screens.vote_for_restaurant_flow

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meetup.meetingapp.data.model.Event
import com.meetup.meetingapp.data.model.Restaurant
import com.meetup.meetingapp.data.repositories.EventRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RestaurantViewModel(
    private val eventRepository: EventRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val eventId: String =
        savedStateHandle[ParticipantDashChooseDateAndAreaDestination.eventIdArg] ?: ""

    private val _event = MutableStateFlow<Event?>(null)
    val event: StateFlow<Event?> = _event.asStateFlow()

    private val _uiState = MutableStateFlow(RestaurantUiState())
    val uiState: StateFlow<RestaurantUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            eventRepository.syncEventById(eventId) // Sync from Firestore to Room first
            eventRepository.getEventById(eventId).collect { event ->
                _event.value = event
            }
        }
    }

    fun submitVote(restaurantId: String) {
        // handle vote submission
    }
}

data class RestaurantUiState(
    val restaurants: List<Restaurant> = emptyList(),
    val selectedRestaurantId: String? = null,
    val isSubmitting: Boolean = false,
    val isSubmitted: Boolean = false
)