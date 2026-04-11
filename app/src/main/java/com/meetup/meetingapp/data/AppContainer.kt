package com.meetup.meetingapp.data

import android.content.Context
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.meetup.meetingapp.data.db.MeetingAppDatabase
import com.meetup.meetingapp.data.repositories.EventRepository
import com.meetup.meetingapp.data.repositories.EventRepositoryImp
import com.meetup.meetingapp.data.repositories.PlacesRepository
import com.meetup.meetingapp.data.repositories.PlacesRepositoryImp
import com.meetup.meetingapp.data.repositories.UserRepository
import com.meetup.meetingapp.data.repositories.UserRepositoryImp
import com.meetup.meetingapp.network.retrofitService

/**
 * Dependency Injection container at the application level.
 */
interface AppContainer {
    val userRepository: UserRepository
    val eventRepository: EventRepository
    val placesRepository: PlacesRepository
    val db: FirebaseFirestore
}

/**
 * [AppContainer] implementation that provides instance of repositories
 */
class AppDataContainer(private val context: Context) : AppContainer {

    // Shared Firestore instance used across repositories.
    override val db = Firebase.firestore

    /** Provides the Firestore-based UserRepository implementation. */
    override val userRepository: UserRepository by lazy {
        UserRepositoryImp(
            db,
            userDao = MeetingAppDatabase.getDatabase(context).userDao()
        )
    }

    /**
     * Provides the Firestore-based EventRepository implementation.
     *
     * Note: EventRepository depends on UserRepository, so it is injected here.
     */
    override val eventRepository: EventRepository by lazy {
        EventRepositoryImp(
            db,
            userRepository,
            MeetingAppDatabase.getDatabase(context).eventDao(),
            MeetingAppDatabase.getDatabase(context).cityDao(),
            MeetingAppDatabase.getDatabase(context).participantResponseDao(),
            MeetingAppDatabase.getDatabase(context).restaurantDao()
        )
    }
    private val apiKey = loadApiKey(context)

    override val placesRepository: PlacesRepository by lazy {
        PlacesRepositoryImp(
            api = retrofitService,
            apiKey = apiKey
        )
    }

    private fun loadApiKey(context: Context): String {
        return try {
            val props = java.util.Properties()
            context.assets.open("secret.properties").use { stream ->
                props.load(stream)
            }
            props.getProperty("PLACES_API_KEY") ?: ""
        } catch (e: Exception) {
            Log.e("AppDataContainer", "Failed to load API key from secret.properties: ${e.message}")
            ""
        }
    }


}
