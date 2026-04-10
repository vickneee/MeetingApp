package com.meetup.meetingapp.data.repositories

import android.util.Log

import com.meetup.meetingapp.data.model.Restaurant
import com.meetup.meetingapp.network.GooglePlacesApiService

import retrofit2.HttpException

/**
 * Concrete implementation of [PlacesRepository] that fetches restaurant data
 * from the Google Places API using Retrofit.
 *
 * Responsibilities:
 *  - Execute a Places Text Search request based on a human-readable query
 *  - Filter out irrelevant or banned place types (e.g., shopping malls)
 *  - Rank results using a weighted score (rating × log(reviewCount))
 *  - Fetch detailed information for the top candidate using Place Details API
 *  - Construct a domain-level [Restaurant] model with merged data
 *  - Wrap all results in [Result] for safe error propagation
 *
 * This class does **not** cache results; persistence is handled by the repository
 * layer that stores results in Firestore and Room.
 */
class PlacesRepositoryImp(
    private val api: GooglePlacesApiService,
    private val apiKey: String
) : PlacesRepository {

    /**
     * Fetches restaurant candidates using Google Places Text Search + Details API.
     *
     * Workflow:
     *  1. Perform a Text Search request using the provided query.
     *  2. Filter out banned place types (e.g., shopping malls, supermarkets).
     *  3. Rank remaining places by a weighted score:
     *         score = rating × ln(reviewCount + 1)
     *  4. Select the top-ranked place and fetch full details via Place Details API.
     *  5. Validate that the place is a restaurant/cafe/bar.
     *  6. Build a [Restaurant] domain model combining Text Search + Details data.
     *
     * Error handling:
     *  - Returns `Result.success(emptyList())` for:
     *        • No results
     *        • Banned types
     *        • Missing details
     *        • 404 Not Found
     *  - Returns `Result.failure(e)` for:
     *        • Network errors
     *        • HTTP errors other than 404
     *        • Unexpected exceptions
     *
     * @param query A human-readable search query (e.g., "sushi restaurant in Helsinki").
     * @return A [Result] containing either:
     *         • A list with a single [Restaurant] candidate, or
     *         • An empty list if no valid restaurant is found, or
     *         • A failure if an unexpected error occurs.
     */
    override suspend fun fetchRestaurants(
        query: String
    ): Result<List<Restaurant>> {

        return try {

            val textSearchResponse = api.textSearch(
                query = query,
                apiKey = apiKey
            )

            Log.d("fetchRestaurants", "TextSearch = $textSearchResponse")

            val places = textSearchResponse.results ?: emptyList()
            if (places.isEmpty()) return Result.success(emptyList())

            val bannedTypes = setOf(
                "shopping_mall",
                "department_store",
                "clothing_store",
                "electronics_store",
                "supermarket",
                "store",
                "grocery_or_supermarket"
            )

            val filteredPlaces = places.filter { place ->
                val types = place.types ?: emptyList()
                types.none { it in bannedTypes }
            }

            if (filteredPlaces.isEmpty()) return Result.success(emptyList())

            val sorted = filteredPlaces.sortedByDescending { place ->
                val rating = place.rating ?: 0.0
                val reviews = place.user_ratings_total ?: 0
                rating * kotlin.math.ln((reviews + 1).toDouble())
            }

            val firstPlace = sorted.first()
            val placeId = firstPlace.place_id ?: return Result.success(emptyList())


            val detailsResponse = api.placeDetails(
                placeId = placeId,
                apiKey = apiKey
            )
            val details = detailsResponse.result
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


            val restaurant = Restaurant(
                placeId = placeId,

                name = details.name
                    ?: firstPlace.name
                    ?: "Unknown",

                address = details.formatted_address
                    ?: firstPlace.formatted_address
                    ?: "",

                rating = details.rating
                    ?: firstPlace.rating
                    ?: 0.0,

                userRatingCount = details.user_ratings_total
                    ?: firstPlace.user_ratings_total
                    ?: 0,

                priceLevel = details.price_level
                    ?: firstPlace.price_level
                    ?: 0,

                types = details.types
                    ?: firstPlace.types
                    ?: emptyList(),

                latitude = details.geometry?.location?.lat
                    ?: firstPlace.geometry?.location?.lat
                    ?: 0.0,

                longitude = details.geometry?.location?.lng
                    ?: firstPlace.geometry?.location?.lng
                    ?: 0.0,

                openingHours = details.opening_hours?.weekday_text,

                photoReference = photoReference
            )

            Result.success(listOf(restaurant))

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
}
