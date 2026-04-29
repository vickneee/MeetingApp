package com.meetup.meetingapp

import android.app.Application
import com.meetup.meetingapp.data.AppContainer
import com.meetup.meetingapp.data.AppDataContainer

/**
 * Application class for initializing app‑wide dependencies.
 *
 * This class creates a single instance of [AppContainer] during application
 * startup, making it available to the rest of the app through dependency
 * injection. The container provides repositories, database access, and
 * API clients used throughout the application.
 *
 * The instance is stored in [container] and accessed via:
 *
 *     (applicationContext as MeetingApplication).container
 *
 * This ensures that all repositories and shared resources are created once
 * and reused across screens, ViewModels, and navigation flows.
 */
class MeetingApplication : Application() {
    /**
     * AppContainer instance used by the rest of the classes to obtain dependencies
     */
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppDataContainer(this)
    }
}
