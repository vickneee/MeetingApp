package com.meetup.meetingapp.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

import retrofit2.http.GET

import retrofit2.http.Query

private const val BASE_URL = "https://maps.googleapis.com/maps/api/"

/**
 * Retrofit instance configured for the Google Maps Platform Web Service APIs.
 *
 * This Retrofit client is used for:
 *  - Places Text Search API
 *  - Places Details API
 *
 * Notes:
 *  - Uses Gson for JSON serialization/deserialization.
 *  - BASE_URL must end with a trailing slash for Retrofit to resolve endpoints correctly.
 */
val retrofit = Retrofit.Builder()
    .baseUrl(BASE_URL)
    .addConverterFactory(GsonConverterFactory.create())
    .build()
/**
 * Lazily created Retrofit service for accessing Google Places API endpoints.
 *
 * This service is injected into repository implementations such as
 * [PlacesRepositoryImp] to provide access to the Google Places API
 * and business logic.
 */
val retrofitService: GooglePlacesApiService =
    retrofit.create(GooglePlacesApiService::class.java)

/**
 * Retrofit interface defining the Google Places API endpoints used by the app.
 *
 * Endpoints included:
 *  - **Text Search API**: Searches for places using a natural-language query.
 *  - **Place Details API**: Retrieves detailed information about a specific place.
 *
 * All methods are `suspend` functions and must be called from a coroutine.
 */
interface GooglePlacesApiService {

    /**
     * Performs a Places Text Search request.
     *
     * This endpoint accepts a human-readable query such as:
     *   - "sushi restaurant in Helsinki"
     *   - "cafe near Kamppi"
     *   - "Italian food in Espoo"
     *
     * The API returns a list of candidate places with basic information
     * (name, rating, geometry, types, etc.).
     *
     * Official docs:
     * https://developers.google.com/maps/documentation/places/web-service/search-text
     *
     * @param query The text query describing the desired place(s).
     * @param apiKey Google Maps Platform API key.
     * @return A parsed [PlacesTextSearchResponse] object.
     */
    @GET("place/textsearch/json")
    suspend fun textSearch(
        @Query("query") query: String,
        @Query("key") apiKey: String
    ): PlacesTextSearchResponse

    /**
     * Fetches detailed information for a specific place using its place_id.
     *
     * The `fields` parameter limits the response to only the data needed by the app:
     *   - opening_hours
     *   - formatted_address
     *   - name
     *   - geometry (lat/lng)
     *   - price_level
     *   - rating
     *   - user_ratings_total
     *   - types
     *   - photos
     *
     * This endpoint is used after Text Search to enrich the top candidate
     * with full details.
     *
     * Official docs:
     * https://developers.google.com/maps/documentation/places/web-service/details
     *
     * @param placeId The unique Google Places ID of the place.
     * @param fields Comma-separated list of fields to return (defaults to required fields).
     * @param apiKey Google Maps Platform API key.
     * @return A parsed [PlaceDetailsResponse] object.
     */
    @GET("place/details/json")
    suspend fun placeDetails(
        @Query("place_id") placeId: String,
        @Query("fields") fields: String =
            "opening_hours,formatted_address,name,geometry,price_level,rating,user_ratings_total,types,photos",
        @Query("key") apiKey: String
    ): PlaceDetailsResponse

}

