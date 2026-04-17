package com.meetup.meetingapp.utils

import com.meetup.meetingapp.data.model.DateTime
import com.meetup.meetingapp.data.model.Restaurant

/**
 * Filters a list of restaurants based on:
 *
 * 1. **Location query**
 *    - Matches if the query is empty
 *    - OR the restaurant's address contains the query (case‑insensitive)
 *    - OR the restaurant's name contains the query (case‑insensitive)
 *
 * 2. **Selected timing**
 *    - If `timing` is null → timing filter is skipped
 *    - Otherwise checks if the restaurant is open during the selected time slot
 *      using [isRestaurantOpenForTiming]
 *
 * This function is pure and contains no side effects, making it easy to test.
 *
 * @param restaurants The full list of restaurants to filter.
 * @param timing The selected date and time slot used for opening‑hours filtering.
 * @param location The user‑entered location query string.
 *
 * @return A list of restaurants that match both the location and timing filters.
 */
fun filterRestaurants(
    restaurants: List<Restaurant>,
    timing: DateTime?,
    location: String?
): List<Restaurant> {
    val query = location?.trim() ?: ""
    return restaurants.filter { restaurant ->
        val locationMatch = query.isEmpty() ||
                (restaurant.address?.contains(query, ignoreCase = true) == true) ||
                (restaurant.name.contains(query, ignoreCase = true))

        val timingMatch = timing == null || isRestaurantOpenForTiming(restaurant, timing)
        locationMatch && timingMatch
    }
}