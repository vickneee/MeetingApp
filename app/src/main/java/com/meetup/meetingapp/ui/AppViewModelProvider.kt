package com.meetup.meetingapp.ui

import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.meetup.meetingapp.MeetingApplication
import com.meetup.meetingapp.ui.screens.home.HomeViewModel
import com.meetup.meetingapp.ui.screens.create_or_join_page.CreateOrJoinViewModel

/**
 * Provides Factory to create instance of ViewModel for the entire app
 */
object AppViewModelProvider {
    val Factory = viewModelFactory {

        /**
         * Initializer for HomeViewModel
         */
        initializer {
            HomeViewModel(
                meetingApplication().container.exampleRepository
            )
        }

        /**
         * Initializer for CreateOrJoinViewModel
         */
        initializer {
            CreateOrJoinViewModel(
                meetingApplication().container.exampleRepository
            )
        }
    }
}

/**
 * Extension function to queries for [android.app.Application] object and returns an instance of
 * [MeetingApplication].
 */
fun CreationExtras.meetingApplication(): MeetingApplication =
    (this[AndroidViewModelFactory.APPLICATION_KEY] as MeetingApplication)
