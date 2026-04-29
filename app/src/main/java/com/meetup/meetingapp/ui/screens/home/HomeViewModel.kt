package com.meetup.meetingapp.ui.screens.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.meetup.meetingapp.data.repositories.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for the Home screen.
 *
 * Responsibilities:
 * - Handles anonymous Firebase Authentication on app launch
 * - Exposes the current user ID as a StateFlow
 * - Determines the user's default country using GPS + Geocoder
 * - Saves the detected country to the [UserRepository]
 *
 * This ViewModel initializes immediately by attempting to sign in anonymously.
 * Once authenticated, it triggers a one‑time location lookup to infer the user's
 * country and store it for later use in event creation and filtering.
 *
 * @property userRepository Repository used to sync and update user data.
 * @property fusedLocationClient Client for retrieving the device's last known location.
 * @property geocoder Used to convert coordinates into a human‑readable country name.
 */
@Suppress("DEPRECATION")
class HomeViewModel(
    private val userRepository: UserRepository,
    private val fusedLocationClient: com.google.android.gms.location.FusedLocationProviderClient,
    private val geocoder: android.location.Geocoder, // Passing geocoder makes it easier to test
) : ViewModel() {
    private val auth = FirebaseAuth.getInstance()

    private val homeUiState = MutableStateFlow(HomeUiState())

    /**
     * The current Firebase user ID.
     *
     * Updated after successful anonymous sign‑in.
     */
    private val _currentUserId = MutableStateFlow(auth.currentUser?.uid ?: "")
    val currentUserId: StateFlow<String> = _currentUserId.asStateFlow()

    init {
        Log.d("Auth", "init called, currentUser: ${auth.currentUser?.uid}")
        signInAnonymously()
    }

    /**
     * Signs in anonymously using Firebase Authentication.
     *
     * - If already signed in, immediately initializes the default country.
     * - On first‑time sign‑in, syncs the user with the repository.
     */
    fun signInAnonymously() {
        Log.d("Auth", "signInAnonymously called, currentUser: ${auth.currentUser?.uid}")
        if (auth.currentUser != null) {
            initDefaultCountry()
            return
        }

        auth
            .signInAnonymously()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("Auth", "sign in success: ${auth.currentUser?.uid}")
                    val user = auth.currentUser
                    if (user?.uid != null) {
                        _currentUserId.value = user.uid // Update the current user ID
                        viewModelScope.launch {
                            userRepository.syncUser(user.uid)
                        }
                    }
                    Log.d("Auth", "SUCCESS UID: ${user?.uid}")
                } else {
                    Log.e("Auth", "sign in failed: ${task.exception}")
                    Log.w("Auth", "ERROR: ${task.exception}")
                }
            }
    }

//    companion object {
//        private const val TIMEOUT_MILLIS = 5_000L
//    }

    /**
     * Attempts to determine the user's country using GPS coordinates.
     *
     * Steps:
     * 1. Fetches the current coordinates using [fusedLocationClient].
     * 2. Uses [geocoder] to resolve the country name.
     * 3. Saves the detected country to the [UserRepository].
     * 4. Updates the UI state with a terminal‑style status message.
     *
     * If GPS is unavailable or geocoding fails, the UI state is updated with
     * an appropriate fallback message.
     */
    fun initDefaultCountry() {
        viewModelScope.launch {
            try {
                // Update UI state to show the terminal is working
                homeUiState.update { it.copy(detectedCountry = "ESTABLISHING_UPLINK...") }

                val coords =
                    com.meetup.meetingapp.data.location
                        .fetchCurrentCoordinates(fusedLocationClient)

                if (coords != null) {
                    val (lat, lng) = coords
                    val addresses = geocoder.getFromLocation(lat, lng, 1)
                    val countryName = addresses?.firstOrNull()?.countryName

                    if (!countryName.isNullOrEmpty()) {
                        userRepository.saveDefaultCountry(countryName)

                        // Update the UI state with the "Terminal" text in Uppercase
                        homeUiState.update { currentState ->
                            currentState.copy(detectedCountry = countryName.uppercase())
                        }

                        Log.d("Location", "Default country set to: $countryName")
                    }
                } else {
                    homeUiState.update { it.copy(detectedCountry = "GPS_SIGNAL_OFFLINE") }
                }
            } catch (e: Exception) {
                Log.e("Location", "Failed to determine country: ${e.message}")
                homeUiState.update { it.copy(detectedCountry = "ERROR_GEOLOC_FAILED") }
            }
        }
    }
}

/**
 * UI state for the Home screen.
 *
 * @property message Optional message for status or debugging.
 * @property detectedCountry The detected country name or a terminal‑style status string.
 */
data class HomeUiState(
    val message: String = "",
    val detectedCountry: String = "WAITING_FOR_GPS...",
)
