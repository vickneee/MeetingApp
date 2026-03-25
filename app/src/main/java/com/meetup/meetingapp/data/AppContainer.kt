package com.meetup.meetingapp.data

import com.meetup.meetingapp.data.db.MeetingAppDatabase
import com.meetup.meetingapp.data.repositories.ExampleRepository
import com.meetup.meetingapp.data.offlinerepositories.OfflineExampleRepository

/**
 * Dependency Injection container at the application level.
 */
interface AppContainer {
    val exampleRepository: ExampleRepository
}

/**
 * [AppContainer] implementation that provides instance of [ExampleRepository]
 */
class AppDataContainer(private val context: android.content.Context) : AppContainer {
    /**
     * Implementation for [ExampleRepository]
     */
    override val exampleRepository: ExampleRepository by lazy {
        OfflineExampleRepository(MeetingAppDatabase.getDatabase(context).exampleDao())
    }
}
