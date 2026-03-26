package com.meetup.meetingapp.ui.screens.create_or_join_page

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.meetup.meetingapp.data.repositories.ExampleRepository

class CreateOrJoinViewModel(private val exampleRepository: ExampleRepository) : ViewModel() {

    var code by mutableStateOf("")
    var key by mutableStateOf("")

    fun updateCode(newCode: String) {
        code = newCode
    }

    fun updateKey(newKey: String) {
        key = newKey
    }
}