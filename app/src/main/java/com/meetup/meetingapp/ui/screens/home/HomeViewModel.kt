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
import kotlinx.coroutines.flow.update

/**
 * ViewModel for the Home screen.
 */
class HomeViewModel(
    private val userRepository: UserRepository,
    private val fusedLocationClient: com.google.android.gms.location.FusedLocationProviderClient,
    private val geocoder: android.location.Geocoder // Passing geocoder makes it easier to test
) : ViewModel() {

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
        if (auth.currentUser != null) {
            initDefaultCountry()
            return
        }

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

    /**
     * Attempts to fetch location once, determine country, and save it to the repository.
     */
    fun initDefaultCountry() {
        viewModelScope.launch {
            try {
                // Update UI state to show the terminal is working
                _homeUiState.update { it.copy(detectedCountry = "ESTABLISHING_UPLINK...") }

                val coords = com.meetup.meetingapp.data.location.fetchCurrentCoordinates(fusedLocationClient)

                if (coords != null) {
                    val (lat, lng) = coords
                    val addresses = geocoder.getFromLocation(lat, lng, 1)
                    val countryName = addresses?.firstOrNull()?.countryName

                    if (!countryName.isNullOrEmpty()) {
                        userRepository.saveDefaultCountry(countryName)

                        // Update the UI state with the "Terminal" text in Uppercase
                        _homeUiState.update { currentState ->
                            currentState.copy(detectedCountry = countryName.uppercase())
                        }

                        Log.d("Location", "Default country set to: $countryName")
                    }
                } else {
                    _homeUiState.update { it.copy(detectedCountry = "GPS_SIGNAL_OFFLINE") }
                }
            } catch (e: Exception) {
                Log.e("Location", "Failed to determine country: ${e.message}")
                _homeUiState.update { it.copy(detectedCountry = "ERROR_GEOLOC_FAILED") }
            }
        }
    }
}

/**
 * Ui State for HomeScreen
 */
data class HomeUiState(
    val message: String = "",
    val detectedCountry: String = "WAITING_FOR_GPS..."
)