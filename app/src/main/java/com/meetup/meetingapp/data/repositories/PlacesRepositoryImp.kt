package com.meetup.meetingapp.data.repositories

import android.util.Log
import com.meetup.meetingapp.data.model.DateTime
import com.meetup.meetingapp.data.model.Restaurant
import com.meetup.meetingapp.network.GooglePlacesApiService
import retrofit2.HttpException

/**
 * Concrete implementation of [PlacesRepository] that fetches restaurant data
 * from the Google Places API using Retrofit.
 */
class PlacesRepositoryImp(
    private val api: GooglePlacesApiService,
    private val apiKey: String,
) : PlacesRepository {
    /**
     * Fetches restaurant candidates using Google Places Text Search + Details API.
     * Includes dynamic location biasing and future availability validation.
     *
     * @param query A human-readable search query.
     * @param targetTime The future meeting time to validate availability.
     * @param lat Latitude of the previously voted city.
     * @param lng Longitude of the previously voted city.
     */
    override suspend fun fetchRestaurants(
        query: String,
        targetTime: DateTime?,
        lat: Double?,
        lng: Double?,
    ): Result<List<Restaurant>> {
        return try {
            val hasLocation = lat != null && lng != null && lat != 0.0
            val locationString = if (hasLocation) "$lat,$lng" else null

            val textSearchResponse =
                api.textSearch(
                    query = query,
                    location = locationString,
                    radius = if (hasLocation) 10000 else null,
                    apiKey = apiKey,
                )

            Log.d("fetchRestaurants", "TextSearch = $textSearchResponse")

            val places = textSearchResponse.results ?: emptyList()
            if (places.isEmpty()) return Result.success(emptyList())

            val bannedTypes =
                setOf(
                    "shopping_mall",
                    "department_store",
                    "clothing_store",
                    "electronics_store",
                    "supermarket",
                    "store",
                    "grocery_or_supermarket",
                )

            val filteredPlaces =
                places.filter { place ->
                    val types = place.types ?: emptyList()
                    types.none { it in bannedTypes }
                }

            if (filteredPlaces.isEmpty()) return Result.success(emptyList())

            val sorted =
                filteredPlaces.sortedByDescending { place ->
                    val rating = place.rating ?: 0.0
                    val reviews = place.user_ratings_total ?: 0
                    rating * kotlin.math.ln((reviews + 1).toDouble())
                }

            val firstPlace = sorted.first()
            val placeId = firstPlace.place_id ?: return Result.success(emptyList())

            // Requesting specific fields including opening_hours to optimize billing
            val detailsResponse =
                api.placeDetails(
                    placeId = placeId,
                    fields = "name,rating,formatted_address,opening_hours,photos,price_level,types,geometry",
                    apiKey = apiKey,
                )
            val details =
                detailsResponse.result
                    ?: return Result.success(emptyList())

            val detailTypes = details.types ?: emptyList()

            if (detailTypes.any { it in bannedTypes }) {
                return Result.success(emptyList())
            }

            val allowedTypes = setOf("restaurant", "cafe", "bar")

            if (detailTypes.none { it in allowedTypes }) {
                return Result.success(emptyList())
            }

            val photoReference =
                details.photos?.firstOrNull()?.photo_reference
                    ?: firstPlace.photos?.firstOrNull()?.photo_reference

            val restaurant =
                Restaurant(
                    placeId = placeId,
                    name =
                        details.name
                            ?: firstPlace.name
                            ?: "Unknown",
                    address =
                        details.formatted_address
                            ?: firstPlace.formatted_address
                            ?: "",
                    rating =
                        details.rating
                            ?: firstPlace.rating
                            ?: 0.0,
                    userRatingCount =
                        details.user_ratings_total
                            ?: firstPlace.user_ratings_total
                            ?: 0,
                    priceLevel =
                        details.price_level
                            ?: firstPlace.price_level
                            ?: 0,
                    types =
                        details.types
                            ?: firstPlace.types
                            ?: emptyList(),
                    latitude =
                        details.geometry?.location?.lat
                            ?: firstPlace.geometry?.location?.lat
                            ?: 0.0,
                    longitude =
                        details.geometry?.location?.lng
                            ?: firstPlace.geometry?.location?.lng
                            ?: 0.0,
                    openingHours = details.opening_hours?.weekday_text,
                    photoReference = photoReference,
                )

            // Validate that the restaurant is open on the planned day/time
            if (isOpenAtPlannedTime(restaurant, targetTime)) {
                Result.success(listOf(restaurant))
            } else {
                Log.d("fetchRestaurants", "Restaurant closed at planned time: ${restaurant.name}")
                Result.success(emptyList())
            }
        } catch (e: HttpException) {
            if (e.code() == 404) {
                Log.d("fetchRestaurants", "No results (404)")
                return Result.success(emptyList())
            }

            Log.e("fetchRestaurants", "Http error", e)
            Result.failure(e)
        } catch (e: Exception) {
            Log.e("fetchRestaurants", "Unknown error", e)
            Result.failure(e)
        }
    }

    /**
     * Validates if the restaurant is open based on the future target time.
     */
    private fun isOpenAtPlannedTime(
        restaurant: Restaurant,
        targetTime: DateTime?,
    ): Boolean {
        // If we don't have a target time, we can't validate, so just return true
        if (targetTime == null) return true

        if (restaurant.openingHours == null) return true

        val plannedDay = targetTime.toDayOfWeekName()
        val schedule =
            restaurant.openingHours.find {
                it.startsWith(plannedDay, ignoreCase = true)
            }

        // Returns false only if the schedule explicitly says "Closed"
        return schedule?.contains("Closed", ignoreCase = true) == false
    }

    /**
     * Converts the DateTime object into a full weekday name (e.g., "Monday").
     */
    fun DateTime.toDayOfWeekName(): String =
        this.toLocalDate().dayOfWeek.getDisplayName(
            java.time.format.TextStyle.FULL,
            java.util.Locale.ENGLISH,
        )
}
