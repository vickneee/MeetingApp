package com.meetup.meetingapp.data.repositories

import com.meetup.meetingapp.data.model.Restaurant

/**
 * Repository interface for fetching restaurant data from external sources,
 * primarily the Google Places API.
 *
 * This abstraction allows:
 *  - Clean separation between data layer and ViewModel/UI
 *  - Easy mocking in unit tests
 *  - Swappable implementations (e.g., real API, fake API, cached API)
 *
 * The implementation is responsible for:
 *  - Executing the Places API query
 *  - Mapping API responses into domain models
 *  - Wrapping results in a [Result] to safely propagate errors
 */
interface PlacesRepository {


    /**
     * Fetches a list of restaurants based on a text query.
     *
     * Example queries:
     *  - "sushi restaurant in Helsinki"
     *  - "cafe in Espoo"
     *  - "italian restaurant near Kamppi"
     *
     * The implementation may use:
     *  - Google Places Text Search API
     *  - Nearby Search API
     *  - Any other external data source
     *
     * @param query A human-readable search query describing the desired restaurants.
     * @return A [Result] containing a list of [Restaurant] on success,
     *         or an exception on failure (network error, API error, etc.).
     */
    suspend fun fetchRestaurants(
        query: String,
    ): Result<List<Restaurant>>
}