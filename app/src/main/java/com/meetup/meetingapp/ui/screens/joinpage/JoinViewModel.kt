package com.meetup.meetingapp.ui.screens.joinpage

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.meetup.meetingapp.data.model.EventStatus
import com.meetup.meetingapp.data.repositories.UserRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * ViewModel for the Create or Join screen.
 * @param db Firebase Firestore database instance.
 * @param userRepository Repository for user-related operations.
 * @property code The code entered by the user.
 * @property codeError The error message for the code field.
 * @property key The key entered by the user.
 * @property keyError The error message for the key field.
 * @property navigateToEventsListPage Whether to navigate to the past events list page.
 * @property navigateToParticipantPage Whether to navigate to the participant page.
 */
class JoinViewModel(
    private val db: FirebaseFirestore,
    private val userRepository: UserRepository,
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
        codeError = null // clear error on type
    }

    /**
     * Updates the key entered by the user.
     */
    fun updateKey(newKey: String) {
        key = newKey
        keyError = null // clear error on type
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
     * Error messages for the code and key fields.
     */
    var codeError by mutableStateOf<String?>(null)

    /**
     * Error messages for the code and key fields.
     */
    var keyError by mutableStateOf<String?>(null)

    /**
     * Joins an event with the provided code and key.
     */
    fun joinEvent() {
        // Reset errors
        codeError = null
        keyError = null

        // Validate empty fields
        if (code.isBlank()) {
            codeError = "Code cannot be empty"
            return
        }
        if (key.isBlank()) {
            keyError = "Key cannot be empty"
            return
        }

        viewModelScope.launch {
            val uid = auth.currentUser?.uid ?: return@launch
            // Find event by code + key
            val snapshot =
                db
                    .collection("events")
                    .whereEqualTo("eventCode", code)
                    .whereEqualTo("eventKey", key)
                    .get()
                    .await()

            if (snapshot.isEmpty) {
                keyError = "Wrong key or code"
                return@launch
            }

            val doc = snapshot.documents.first()
            val statusStr = doc.getString("status") ?: ""

            if (statusStr == EventStatus.FINALIZED.name) {
                keyError = "Not able to join, Event is finalized"
                return@launch
            }

            val eventId = doc.id
            userRepository.addJoinedEvent(eventId = eventId, uid = uid)
            navigateToParticipantPage = code to key
            Log.d("Join", "Joined event: $eventId")
        }
    }
}
