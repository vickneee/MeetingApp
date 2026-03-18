package com.example.meetingapp.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.meetingapp.repositories.ExampleRepository
import kotlinx.coroutines.launch

class MeetingAppViewModel(private val exampleRepository: ExampleRepository) : ViewModel() {

    /**
     * Background Refresh of the data.
     * @property refreshData The function to refresh the data.
     */
    init {
        refreshData()
    }

    /**
     * Refresh the data from the API.
     * @property viewModelScope The scope of the view model.
     * @property exampleRepository The repository to use for the view model.
     * @property refreshData The function to refresh the data.
     */
    private fun refreshData() {
        viewModelScope.launch {
            try {
                // exampleRepository.refreshData()
            } catch (e: Exception) {
                Log.e("MeetingAppViewModel", "Error refreshing members: ${e.message}")
            }
        }
    }

    /**
     * Factory for creating [MeetingAppViewModel].
     * @property exampleRepository The repository to use for the view model.
     */
    companion object {
        /**
         * Factory for creating [MeetingAppViewModel].
         * @param exampleRepository The repository to use for the view model.
         */
        fun Factory(
            exampleRepository: ExampleRepository,
        ) = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return MeetingAppViewModel(exampleRepository) as T
            }
        }
    }
}
