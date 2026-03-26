package com.meetup.meetingapp.data.repositories

import com.google.firebase.auth.FirebaseAuth
import com.meetup.meetingapp.data.model.DateRange
import com.meetup.meetingapp.data.model.Event
import com.meetup.meetingapp.data.model.LocationOption
import com.meetup.meetingapp.data.model.PlaceType
import com.meetup.meetingapp.data.model.TimeSlot

class EventRepositoryImp {

    private val auth = FirebaseAuth.getInstance()
    private val uid = auth.currentUser?.uid

    fun createEvent(eventTitle: String, hostName:String, dataRange: DateRange, timeSlots: List<TimeSlot>, locations: LocationOption, placeTypes: PlaceType){

    }

}