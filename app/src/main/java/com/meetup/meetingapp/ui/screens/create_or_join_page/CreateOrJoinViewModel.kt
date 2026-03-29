package com.meetup.meetingapp.ui.screens.create_or_join_page

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.meetup.meetingapp.data.repositories.ExampleRepository
import com.meetup.meetingapp.data.repositories.UserRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class CreateOrJoinViewModel(
    private val exampleRepository: ExampleRepository,
    private val db: FirebaseFirestore,
    private val userRepository: UserRepository
) : ViewModel() {

    private val auth = FirebaseAuth.getInstance()

    var code by mutableStateOf("")
    var key by mutableStateOf("")

    fun updateCode(newCode: String) {
        code = newCode
    }

    fun updateKey(newKey: String) {
        key = newKey
    }

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
            Log.d("Join", "Joined event: $eventId")
        }
    }

}