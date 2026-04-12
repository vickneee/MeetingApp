package com.meetup.meetingapp.ui.screens.place_details_page

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

data class PlaceDetailsUiState(
    val name: String = "",
    val rating: Double = 0.0,
    val reviewCount: Int = 0,
    val category: String = "",
    val priceRange: String = "",
    val distance: String = "",
    val openUntil: String = "",
    val address: String = "",
    val photoUrl: String? = null
)

class PlaceDetailsViewModel : ViewModel() {

    var uiState by mutableStateOf(PlaceDetailsUiState())
        private set

    init {
        loadData()
    }

    private fun loadData() {
        uiState = PlaceDetailsUiState(
            name = "Ravintola Aino",
            rating = 4.5,
            reviewCount = 230,
            category = "Italian",
            priceRange = "$$",
            distance = "1.2 km",
            openUntil = "22:00",
            address = "Iso Omena, Piispansilta 11, Espoo",
            photoUrl = "https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?auto=format&fit=crop&w=800&q=80"
        )
    }

    fun onVoteClicked() {
        // TODO: Implement voting logic (e.g., Firestore update)
        println("Voted for ${uiState.name}")
    }

    fun onViewOnMaps() {
        // TODO: Implement navigation to Google Maps
        println("Navigating to Maps for ${uiState.address}")
    }
}