package com.meetup.meetingapp.data.repositories

import com.meetup.meetingapp.data.model.DateTime
import com.meetup.meetingapp.data.model.Restaurant

/**
 * Repository interface for fetching restaurant data from external sources,
 * primarily the Google Places API.
 */
interface PlacesRepository {
    /**
     * Fetches a list of restaurants based on a text query, filtered by location and time.
     *
     * @param query A human-readable search query.
     * @param targetTime The future meeting time to validate availability. Defaults to null.
     * @param lat Optional Latitude of the search center. If null or 0.0, location biasing is skipped.
     * @param lng Optional Longitude of the search center. If null or 0.0, location biasing is skipped.
     * @return A [Result] containing a list of [Restaurant] on success.
     */
    suspend fun fetchRestaurants(
        query: String,
        targetTime: DateTime? = null,
        lat: Double? = null,
        lng: Double? = null,
        components: String? = null,
    ): Result<List<Restaurant>>
}
