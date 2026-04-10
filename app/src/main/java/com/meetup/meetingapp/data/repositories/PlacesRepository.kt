package com.meetup.meetingapp.data.repositories

import com.meetup.meetingapp.data.model.Restaurant

interface PlacesRepository {

    suspend fun fetchRestaurants(
        query: String,
    ): Result<List<Restaurant>>
}