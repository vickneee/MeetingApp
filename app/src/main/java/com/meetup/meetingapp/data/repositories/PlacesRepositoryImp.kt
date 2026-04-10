package com.meetup.meetingapp.data.repositories


import android.util.Log

import com.meetup.meetingapp.data.model.Restaurant
import com.meetup.meetingapp.network.GooglePlacesApiService

import retrofit2.HttpException


class PlacesRepositoryImp(
    private val api: GooglePlacesApiService,
    private val apiKey: String
) : PlacesRepository {

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

