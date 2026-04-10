package com.meetup.meetingapp.network


data class Geometry(
    val location: LatLngLiteral?
)

data class LatLngLiteral(
    val lat: Double?,
    val lng: Double?
)
