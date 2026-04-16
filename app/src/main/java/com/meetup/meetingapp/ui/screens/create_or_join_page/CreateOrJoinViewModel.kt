package com.meetup.meetingapp.ui.screens.create_or_join_page

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.meetup.meetingapp.data.repositories.UserRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * ViewModel for the Create or Join screen.
 * @param db Firebase Firestore database instance.
 * @param userRepository Repository for user-related operations.
 * @property code The code entered by the user.
 * @property key The key entered by the user.
 * @property navigateToEventsListPage Whether to navigate to the past events list page.
 * @property navigateToParticipantPage Whether to navigate to the participant page.
 */
class CreateOrJoinViewModel(
    private val db: FirebaseFirestore,
    private val userRepository: UserRepository
) : ViewModel() {

    /**
     * Firebase Authentication instance.
     */
    private val auth = FirebaseAuth.getInstance()

    /**
     * The code entered by the user.
     */
    var code by mutableStateOf("")

    /**
     * The key entered by the user.
     */
    var key by mutableStateOf("")

    /**
     * Updates the code entered by the user.
     */
    fun updateCode(newCode: String) {
        code = newCode
    }

    /**
     * Updates the key entered by the user.
     */
    fun updateKey(newKey: String) {
        key = newKey
    }

    /**
     * Navigates to the events list page.
     */
    var navigateToEventsListPage by mutableStateOf(false)
        private set

    /**
     * Navigates to the participant page.
     */
    var navigateToParticipantPage by mutableStateOf<Pair<String, String>?>(null)
        private set

    /**
     * Joins an event with the provided code and key.
     */
    fun onNavigatedToPastEvents() {
        navigateToEventsListPage = false
    }

    /**
     * Navigates to the participant page.
     */
    fun onNavigatedToParticipantPage() {
        navigateToParticipantPage = null
    }

    /**
     * Joins an event with the provided code and key.
     */
    fun joinEvent() {
        viewModelScope.launch {
            val uid = auth.currentUser?.uid ?: return@launch
            // Find event by code + key
            val snapshot = db.collection("events")
                .whereEqualTo("eventCode", code)
                .whereEqualTo("eventKey", key)
                .get()
                .await()

            if (snapshot.isEmpty) {
                // TODO: show error to user — wrong code/key
                Log.w("Join", "No event found for code=$code key=$key")
                return@launch
            }

            val eventId = snapshot.documents.first().id
            userRepository.addJoinedEvent(eventId = eventId, uid = uid)

            navigateToParticipantPage = code to key
            Log.d("Join", "Joined event: $eventId")
        }
    }
}