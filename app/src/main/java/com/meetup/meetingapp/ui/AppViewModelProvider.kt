package com.meetup.meetingapp.ui

import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.meetup.meetingapp.MeetingApplication
import com.meetup.meetingapp.ui.screens.EventViewModel
import com.meetup.meetingapp.ui.screens.home.HomeViewModel
import com.meetup.meetingapp.ui.screens.create_or_join_page.CreateOrJoinViewModel
import com.meetup.meetingapp.ui.screens.create_event_button_page.CreateEventButtonViewModel
import com.meetup.meetingapp.ui.screens.create_creating_event_page.CreateCreatingEventPageViewModel
import com.meetup.meetingapp.ui.screens.event_created_page.EventCreatedViewModel
import com.meetup.meetingapp.ui.screens.host_dashboard.HostDashboardViewModel

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
                meetingApplication().container.exampleRepository,
                meetingApplication().container.userRepository
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

        /**
         * Initializer for EventViewModel
         */
        initializer {
            EventViewModel(
                meetingApplication().container.eventRepository
         
        /**      
         * Initializer for CreateCreatingEventPageViewModel
         */
        initializer {
            CreateCreatingEventPageViewModel(
                meetingApplication().container.exampleRepository
            )
        }

        /**
         * Initializer for CreateEventButtonViewModel
         */
        initializer {
            CreateEventButtonViewModel(
                meetingApplication().container.exampleRepository
            )
        }

        /**
         * Initializer for EventCreatedViewModel
         */
        initializer {
            EventCreatedViewModel(
                meetingApplication().container.exampleRepository
            )
        }

        /**
         * Initializer for HostDashboardViewModel
         */
        initializer {
            HostDashboardViewModel(
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
