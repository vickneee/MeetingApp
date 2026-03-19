package com.example.meetingapp.ui.screens.home

import androidx.lifecycle.ViewModel
import com.example.meetingapp.data.db.entities.ExampleEntity
import com.example.meetingapp.data.repositories.ExampleRepository

/**
 * ViewModel to retrieve all items in the Room database.
 */
class HomeViewModel(private val exampleRepository: ExampleRepository) : ViewModel() {

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }
}

/**
 * Ui State for HomeScreen
 */
data class HomeUiState(val itemList: List<ExampleEntity> = listOf())
