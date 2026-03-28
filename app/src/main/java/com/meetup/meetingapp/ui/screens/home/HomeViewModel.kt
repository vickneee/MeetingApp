package com.meetup.meetingapp.ui.screens.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.meetup.meetingapp.data.db.entities.ExampleEntity
import com.meetup.meetingapp.data.repositories.ExampleRepository
import com.meetup.meetingapp.data.repositories.UserRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel to retrieve all items in the Room database.
 */
class HomeViewModel(private val exampleRepository: ExampleRepository, private val userRepository: UserRepository) : ViewModel() {

    private val auth = FirebaseAuth.getInstance()

    val homeUiState: StateFlow<HomeUiState> =
        exampleRepository.getAllItems().map { HomeUiState(it) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
                initialValue = HomeUiState()
            )

    /**
     * Signs in anonymously using Firebase Authentication.
     *
     * - If a user is already signed in, the function returns immediately.
     * - On successful first-time sign-in, a new user document is created in Firestore.
     * - This ensures every anonymous user has a corresponding user record in the database.
     */
    fun signInAnonymously() {
        if (auth.currentUser != null) return

        auth.signInAnonymously()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if(user?.uid != null){
                        viewModelScope.launch{
                            userRepository.createUser(user.uid)
                        }
                    }
                    Log.d("Auth", "SUCCESS UID: ${user?.uid}")
                } else {
                    Log.w("Auth", "ERROR: ${task.exception}")
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
