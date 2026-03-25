package com.meetup.meetingapp.data.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Base URL for the API.
 */
private const val BASE_URL = ""


interface MeetingAppApiService {

    // TODO: @GET("")
    // TODO: suspend fun getMeetings(): List<Meeting>
}

/**
 * Singleton object for the API service.
 */
object MeetingAppApi {

    /**
     * Retrofit instance.
     * @property retrofit The Retrofit instance.
     * @property RETROFIT_SERVICE The API service.
     */
    private val retrofit by lazy {
        Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(BASE_URL)
            .build()
    }

    val retrofitService: MeetingAppApiService by lazy {
        retrofit.create(MeetingAppApiService::class.java)
    }
}

