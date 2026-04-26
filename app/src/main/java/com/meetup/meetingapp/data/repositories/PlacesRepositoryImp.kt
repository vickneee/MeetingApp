package com.meetup.meetingapp.data.repositories

import android.util.Log
import com.meetup.meetingapp.data.model.DateTime
import com.meetup.meetingapp.data.model.Restaurant
import com.meetup.meetingapp.network.GooglePlacesApiService

/**
 * Concrete implementation of [PlacesRepository] that fetches restaurant data
 * from the Google Places API using Retrofit.
 *
 * @property api The Retrofit service used to make API requests.
 * @property apiKey The Google Maps Platform API key used for authentication.
 */
class PlacesRepositoryImp(
    private val api: GooglePlacesApiService,
    private val apiKey: String,
) : PlacesRepository {

    override suspend fun fetchRestaurants(
        query: String,
        targetTime: DateTime?,
        lat: Double?,
        lng: Double?,
        components: String?,
    ): Result<List<Restaurant>> {

        return try {

            val hasLocation = lat != null && lng != null && lat != 0.0
            val locationString = if (hasLocation) "$lat,$lng" else null

            // FINLAND FILTER HERE
            val textSearchResponse = api.textSearch(
                query = query,
                location = locationString,
                radius = if (hasLocation) 10000 else null,
                components = "country:fi",
                apiKey = apiKey,
            )

            val places = textSearchResponse.results ?: emptyList()
            if (places.isEmpty()) return Result.success(emptyList())

            // LIMIT API CALLS (VERY IMPORTANT)
            val topPlaces = places
                .sortedByDescending { it.rating ?: 0.0 }
                .take(5)

            val allowedTypes = setOf("restaurant", "cafe", "bar", "food", "meal_takeaway")

            val restaurants = topPlaces.mapNotNull { place ->

                val placeId = place.place_id ?: return@mapNotNull null

                val details = api.placeDetails(
                    placeId = placeId,
                    fields = "name,rating,geometry,opening_hours,photos,formatted_address,types,user_ratings_total,price_level,address_components",
                    apiKey = apiKey,
                    ).result ?: return@mapNotNull null
                val types = details.types ?: emptyList()
                // TYPE FILTER
                if (types.none { it in allowedTypes }) return@mapNotNull null
                // COUNTRY SAFETY FILTER (backup)
                val countryCode = details.addressComponents
                    ?.firstOrNull { it.types?.contains("country") == true }
                    ?.shortName
                if (countryCode != "FI") return@mapNotNull null

                Restaurant(
                    placeId = placeId,
                    name = details.name ?: place.name ?: "Unknown",
                    address = details.formatted_address ?: "",
                    rating = details.rating ?: 0.0,
                    userRatingCount = details.user_ratings_total ?: 0,
                    priceLevel = details.price_level ?: 0,
                    types = types,
                    latitude = details.geometry?.location?.lat ?: 0.0,
                    longitude = details.geometry?.location?.lng ?: 0.0,
                    openingHours = details.opening_hours?.weekday_text,
                    photoReference = details.photos?.firstOrNull()?.photo_reference
                )
            }

            if (restaurants.isEmpty()) {
                return Result.success(emptyList())
            }

            // Prefer open restaurants, but fall back to all if none are open
            val openRestaurants = restaurants.filter { isOpenAtPlannedTime(it, targetTime) }
            val candidates = openRestaurants.ifEmpty { restaurants }

            val best = candidates.maxByOrNull {
                val rating = it.rating ?: 0.0
                val reviews = it.userRatingCount ?: 0
                rating * kotlin.math.ln((reviews + 1).toDouble())
            } ?: return  Result.success(emptyList())

            Result.success(listOf(best))

        } catch (e: Exception) {
            Log.e("fetchRestaurants", "Error", e)
            Result.failure(e)
        }
    }

    private fun isOpenAtPlannedTime(
        restaurant: Restaurant,
        targetTime: DateTime?,
        ): Boolean {
        if (targetTime == null) return true
        val hours = restaurant.openingHours ?: return true
        val day = targetTime.toDayOfWeekName()
        val schedule = hours.find {
            it.startsWith(day, ignoreCase = true)
        }
        return schedule?.contains("Closed", ignoreCase = true) == false

    }
}

/**
 * Converts the DateTime object into a full weekday name (e.g., "Monday").
 */
fun DateTime.toDayOfWeekName(): String =
    this.toLocalDate().dayOfWeek.getDisplayName(
        java.time.format.TextStyle.FULL,
        java.util.Locale.ENGLISH,
    )
