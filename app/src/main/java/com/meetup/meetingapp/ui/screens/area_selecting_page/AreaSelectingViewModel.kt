package com.meetup.meetingapp.ui.screens.area_selecting_page

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.meetup.meetingapp.data.repositories.ExampleRepository

/**
 * ViewModel to manage the state of area selection.
 */
class AreaSelectingViewModel(private val exampleRepository: ExampleRepository) : ViewModel() {

    /**
     * The currently selected or typed area name.
     */
    var selectedArea by mutableStateOf("")
        private set

    /**
     * Updates the selected area state.
     */
    fun updateArea(newArea: String) {
        selectedArea = newArea
    }
}