package com.meetup.meetingapp.data

import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.meetup.meetingapp.data.db.MeetingAppDatabase
import com.meetup.meetingapp.data.repositories.ExampleRepository
import com.meetup.meetingapp.data.offlinerepositories.OfflineExampleRepository
import com.meetup.meetingapp.data.repositories.EventRepository
import com.meetup.meetingapp.data.repositories.EventRepositoryImp
import com.meetup.meetingapp.data.repositories.UserRepository
import com.meetup.meetingapp.data.repositories.UserRepositoryImp

/**
 * Dependency Injection container at the application level.
 */
interface AppContainer {
    val exampleRepository: ExampleRepository
    val userRepository: UserRepository
    val eventRepository: EventRepository
}

/**
 * [AppContainer] implementation that provides instance of [ExampleRepository]
 */
class AppDataContainer(private val context: android.content.Context) : AppContainer {

    // Shared Firestore instance used across repositories.
    private val db = Firebase.firestore

    /**
     * Implementation for [ExampleRepository]
     */
    override val exampleRepository: ExampleRepository by lazy {
        OfflineExampleRepository(MeetingAppDatabase.getDatabase(context).exampleDao())
    }

    /** Provides the Firestore-based UserRepository implementation. */
    override val userRepository: UserRepository by lazy {
        UserRepositoryImp(db)
    }

    /**
     * Provides the Firestore-based EventRepository implementation.
     *
     * Note: EventRepository depends on UserRepository, so it is injected here.
     */
    override val eventRepository: EventRepository by lazy {
        EventRepositoryImp(db, userRepository)
    }
}
