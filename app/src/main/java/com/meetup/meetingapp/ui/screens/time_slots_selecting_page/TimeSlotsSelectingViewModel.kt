package com.meetup.meetingapp.ui.screens.time_slots_selecting_page

import androidx.lifecycle.ViewModel

class TimeSlotsSelectingViewModel : ViewModel() {
    private val _selectedTimeSlots = mutableListOf<String>()
    val selectedTimeSlots: List<String> = _selectedTimeSlots

    fun updateTimeSlot(timeSlotStart: String, timeSlotEnd: String) {
        if (!_selectedTimeSlots.contains("$timeSlotStart - $timeSlotEnd")) {
            _selectedTimeSlots.add("$timeSlotStart - $timeSlotEnd")
        }
    }

    fun deleteTimeSlot(timeSlot: String) {
        if (_selectedTimeSlots.contains(timeSlot)) {
            _selectedTimeSlots.remove(timeSlot)
        }

    }
}