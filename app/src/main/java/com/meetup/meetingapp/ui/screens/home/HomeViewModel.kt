package com.meetup.meetingapp.ui.screens.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.meetup.meetingapp.data.repositories.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the Home screen.
 */
class HomeViewModel(private val userRepository: UserRepository) : ViewModel() {

    private val auth = FirebaseAuth.getInstance()

    private val _homeUiState = MutableStateFlow(HomeUiState())
    val homeUiState: StateFlow<HomeUiState> = _homeUiState.asStateFlow()

    /**
     * The current user ID.
     */
    private val _currentUserId = MutableStateFlow(auth.currentUser?.uid ?: "")
    val currentUserId: StateFlow<String> = _currentUserId.asStateFlow()

    init {
        Log.d("Auth", "init called, currentUser: ${auth.currentUser?.uid}")
        signInAnonymously()
    }

    /**
     * Signs in anonymously using Firebase Authentication.
     */
    fun signInAnonymously() {
        Log.d("Auth", "signInAnonymously called, currentUser: ${auth.currentUser?.uid}")
        if (auth.currentUser != null) return

        auth.signInAnonymously()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("Auth", "sign in success: ${auth.currentUser?.uid}")
                    val user = auth.currentUser
                    if(user?.uid != null){
                        _currentUserId.value = user.uid // Update the current user ID
                        viewModelScope.launch{
                            userRepository.createUser(user.uid)
                        }
                    }
                    Log.d("Auth", "SUCCESS UID: ${user?.uid}")
                } else {
                    Log.e("Auth", "sign in failed: ${task.exception}")
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
data class HomeUiState(val message: String = "")
