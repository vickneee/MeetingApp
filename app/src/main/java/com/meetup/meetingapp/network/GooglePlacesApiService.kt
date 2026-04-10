package com.meetup.meetingapp.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

import retrofit2.http.GET

import retrofit2.http.Query

private const val BASE_URL = "https://maps.googleapis.com/maps/api/"


val retrofit = Retrofit.Builder()
    .baseUrl(BASE_URL)
    .addConverterFactory(GsonConverterFactory.create())
    .build()

val retrofitService: GooglePlacesApiService =
    retrofit.create(GooglePlacesApiService::class.java)

interface GooglePlacesApiService {


    @GET("place/textsearch/json")
    suspend fun textSearch(
        @Query("query") query: String,
        @Query("key") apiKey: String
    ): PlacesTextSearchResponse

    @GET("place/details/json")
    suspend fun placeDetails(
        @Query("place_id") placeId: String,
        @Query("fields") fields: String =
            "opening_hours,formatted_address,name,geometry,price_level,rating,user_ratings_total,types,photos",
        @Query("key") apiKey: String
    ): PlaceDetailsResponse

}

