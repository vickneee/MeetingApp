package com.meetup.meetingapp

import android.app.Application
import com.meetup.meetingapp.data.AppContainer
import com.meetup.meetingapp.data.AppDataContainer

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
