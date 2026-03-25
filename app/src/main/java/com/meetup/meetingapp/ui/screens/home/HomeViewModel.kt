package com.meetup.meetingapp.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.meetup.meetingapp.data.db.entities.ExampleEntity
import com.meetup.meetingapp.data.repositories.ExampleRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * ViewModel to retrieve all items in the Room database.
 */
class HomeViewModel(private val exampleRepository: ExampleRepository) : ViewModel() {

    private val auth = FirebaseAuth.getInstance()

    val homeUiState: StateFlow<HomeUiState> =
        exampleRepository.getAllItems().map { HomeUiState(it) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
                initialValue = HomeUiState()
            )

    /**
     * Sign in anonymously to the Firebase Realtime Database
     */
    fun signInAnonymously() {
        if (auth.currentUser != null) return

        auth.signInAnonymously()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    println("SUCCESS UID: ${user?.uid}")
                } else {
                    println("ERROR: ${task.exception}")
                }
            }
    }

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }
}

/**
 * Ui State for HomeScreen
 */
data class HomeUiState(val itemList: List<ExampleEntity> = listOf())
