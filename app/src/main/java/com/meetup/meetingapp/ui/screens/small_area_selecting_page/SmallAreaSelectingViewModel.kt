package com.meetup.meetingapp.ui.screens.small_area_selecting_page

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class SmallAreaSelectingViewModel : ViewModel() {

    // State for the checkboxes
    var espooSelected by mutableStateOf(false)
    var helsinkiSelected by mutableStateOf(false)
    var vantaaSelected by mutableStateOf(false)

    fun updateEspoo(selected: Boolean) {
        espooSelected = selected
    }

    fun updateHelsinki(selected: Boolean) {
        helsinkiSelected = selected
    }

    fun updateVantaa(selected: Boolean) {
        vantaaSelected = selected
    }

    fun isAnySelected(): Boolean {
        return espooSelected || helsinkiSelected || vantaaSelected
    }

    // This will be changed to a database call or something


    fun createEvent() {
        // Logic to save these selections to a database or shared repository
        println("Areas Selected - Espoo: $espooSelected, Helsinki: $helsinkiSelected, Vantaa: $vantaaSelected")
    }
}