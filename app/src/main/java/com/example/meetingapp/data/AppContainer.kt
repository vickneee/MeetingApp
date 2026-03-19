package com.example.meetingapp.data

import com.example.meetingapp.data.db.MeetingAppDatabase
import com.example.meetingapp.data.repositories.ExampleRepository
import com.example.meetingapp.data.offlinerepositories.OfflineExampleRepository

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
