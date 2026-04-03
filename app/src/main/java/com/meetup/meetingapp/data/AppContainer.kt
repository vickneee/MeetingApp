package com.meetup.meetingapp.data

import android.content.Context
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.meetup.meetingapp.data.db.MeetingAppDatabase
import com.meetup.meetingapp.data.repositories.EventRepository
import com.meetup.meetingapp.data.repositories.EventRepositoryImp
import com.meetup.meetingapp.data.repositories.UserRepository
import com.meetup.meetingapp.data.repositories.UserRepositoryImp

/**
 * Dependency Injection container at the application level.
 */
interface AppContainer {
    val userRepository: UserRepository
    val eventRepository: EventRepository
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
            MeetingAppDatabase.getDatabase(context).eventDao()
        )
    }
}
