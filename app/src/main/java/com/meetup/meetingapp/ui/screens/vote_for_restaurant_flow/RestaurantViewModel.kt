package com.meetup.meetingapp.ui.screens.vote_for_restaurant_flow

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meetup.meetingapp.data.model.Restaurant
import com.meetup.meetingapp.data.repositories.EventRepository
import com.meetup.meetingapp.ui.screens.participant_dashboard.ParticipantDashboardDestination
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RestaurantViewModel(
    private val eventRepository: EventRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val eventId: String = savedStateHandle[ParticipantDashboardDestination.eventIdArg] ?: ""

    private val _uiState = MutableStateFlow(RestaurantUiState())
    val uiState: StateFlow<RestaurantUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            // load restaurants to vote on
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