package com.meetup.meetingapp.ui

import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.google.android.gms.location.LocationServices
import com.meetup.meetingapp.MeetingApplication
import com.meetup.meetingapp.ui.screens.create_event_flow.EventViewModel
import com.meetup.meetingapp.ui.screens.home.HomeViewModel
import com.meetup.meetingapp.ui.screens.create_or_join_page.JoinViewModel
import com.meetup.meetingapp.ui.screens.host_dashboard.HostDashboardViewModel
import com.meetup.meetingapp.ui.screens.participant_dashboard.ParticipantDashboardViewModel
import com.meetup.meetingapp.ui.screens.participant_input_flow.ParticipantViewModel
import com.meetup.meetingapp.ui.screens.vote_for_place_flow.PlaceViewModel

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
                meetingApplication().container.userRepository
            )
        }

        /**
         * Initializer for CreateOrJoinViewModel
         */
        initializer {
            JoinViewModel(
                meetingApplication().container.db,
                meetingApplication().container.userRepository
            )
        }

        /**
         * Initializer for EventViewModel
         */
        initializer {
            EventViewModel(
                meetingApplication().container.eventRepository
            )
        }

        /**
         * Initializer for HostDashboardViewModel
         */
        initializer {
            HostDashboardViewModel(
                meetingApplication().container.eventRepository,
                this.createSavedStateHandle()
            )
        }

        /**
         * Initializer for ParticipantViewModel
         */
        initializer {
            ParticipantViewModel(
                meetingApplication().container.eventRepository,
                this.createSavedStateHandle()
            )
        }

        /**
         * Initializer for PlaceViewModel (formerly RestaurantViewModel)
         */
        initializer {
            val app = meetingApplication()

            // Create the location client using the application context
            val locationClient = LocationServices
                .getFusedLocationProviderClient(app)

            PlaceViewModel(
                eventRepository = app.container.eventRepository,
                apiKey = app.container.placesApiKey,
                fusedLocationClient = locationClient,           // Added this
                savedStateHandle = this.createSavedStateHandle() // Moved this to the end
            )
        }

        /**
         * Initializer for ParticipantDashboardViewModel
         */
        initializer {
            ParticipantDashboardViewModel(
                meetingApplication().container.eventRepository,
                this.createSavedStateHandle()
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
