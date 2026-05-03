package com.meetup.meetingapp.data

import android.content.Context
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.meetup.meetingapp.data.db.MeetingAppDatabase
import com.meetup.meetingapp.data.repositories.EventRepository
import com.meetup.meetingapp.data.repositories.EventRepositoryImp
import com.meetup.meetingapp.data.repositories.PlacesRepository
import com.meetup.meetingapp.data.repositories.PlacesRepositoryImp
import com.meetup.meetingapp.data.repositories.SubmissionRepository
import com.meetup.meetingapp.data.repositories.SubmissionRepositoryImp
import com.meetup.meetingapp.data.repositories.UserRepository
import com.meetup.meetingapp.data.repositories.UserRepositoryImp
import com.meetup.meetingapp.network.retrofitService

/**
 * Dependency Injection container for application‑wide services and repositories.
 *
 * This interface defines the core dependencies used throughout the app, including:
 * - Firestore database access
 * - Repository implementations for users, events, and Places API
 * - The Google Places API key loaded from secure storage
 *
 * Implementations of this container are responsible for constructing and providing
 * singleton instances of these dependencies.
 */
interface AppContainer {
    val userRepository: UserRepository
    val eventRepository: EventRepository
    val placesRepository: PlacesRepository
    val submissionRepository: SubmissionRepository
    val db: FirebaseFirestore
    val placesApiKey: String
}

/**
 * Concrete implementation of [AppContainer] that initializes and provides
 * application‑level dependencies.
 *
 * This container:
 * - Creates a shared Firestore instance
 * - Builds Room DAOs through [MeetingAppDatabase]
 * - Loads the Google Places API key from `secret.properties`
 * - Provides repository implementations with proper dependency wiring
 * - Ensures lazy initialization to avoid unnecessary startup cost
 *
 * @property context Application context used for database initialization and asset loading.
 */
class AppDataContainer(
    private val context: Context,
) : AppContainer {
    // Shared Firestore instance used across repositories.
    override val db = Firebase.firestore

    /** Provides the Firestore-based UserRepository implementation. */
    override val userRepository: UserRepository by lazy {
        UserRepositoryImp(
            db,
            userDao = MeetingAppDatabase.getDatabase(context).userDao(),
        )
    }

    /**
     * Provides the Firestore‑backed implementation of [EventRepository].
     *
     * This repository depends on:
     * - Firestore
     * - Room DAOs
     * - [UserRepository]
     * - [PlacesRepository]
     * - [FirebaseAuth] for current user access
     */
    override val eventRepository: EventRepository by lazy {
        EventRepositoryImp(
            db,
            userRepository,
            MeetingAppDatabase.getDatabase(context).eventDao(),
            MeetingAppDatabase.getDatabase(context).cityDao(),
            MeetingAppDatabase.getDatabase(context).participantResponseDao(),
            MeetingAppDatabase.getDatabase(context).restaurantDao(),
            placesRepository,
            FirebaseAuth.getInstance(),
        )
    }

    /** Lazily loads the Google Places API key from `secret.properties`. */
    override val placesApiKey: String by lazy {
        loadApiKey(context)
    }

    /** Provides the Retrofit‑based implementation of [PlacesRepository]. */
    override val placesRepository: PlacesRepository by lazy {
        PlacesRepositoryImp(
            api = retrofitService,
            apiKey = placesApiKey,
        )
    }

    /** Provides the WorkManager-based implementation of [SubmissionRepository]. */
    override val submissionRepository: SubmissionRepository by lazy {
        SubmissionRepositoryImp(context)
    }

    /**
     * Loads the Google Places API key from the `secret.properties` file in assets.
     *
     * Returns an empty string if loading fails, and logs the error.
     */
    private fun loadApiKey(context: Context): String =
        try {
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
