package com.meetup.meetingapp.ui

import android.location.Geocoder
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.google.android.gms.location.LocationServices
import com.meetup.meetingapp.MeetingApplication
import com.meetup.meetingapp.ui.screens.eventcreation.EventViewModel
import com.meetup.meetingapp.ui.screens.home.HomeViewModel
import com.meetup.meetingapp.ui.screens.hostdashboard.HostDashboardViewModel
import com.meetup.meetingapp.ui.screens.joinpage.JoinViewModel
import com.meetup.meetingapp.ui.screens.participantdashboard.ParticipantDashboardViewModel
import com.meetup.meetingapp.ui.screens.participantinput.ParticipantViewModel
import com.meetup.meetingapp.ui.screens.placevote.PlaceViewModel
import java.util.Locale

/**
 * Provides Factory to create instance of ViewModel for the entire app
 */
object AppViewModelProvider {
    val Factory =
        viewModelFactory {

            /**
             * Initializer for HomeViewModel
             */
            initializer {
                val app = meetingApplication()
                HomeViewModel(
                    userRepository = app.container.userRepository,
                    fusedLocationClient = LocationServices.getFusedLocationProviderClient(app),
                    geocoder = Geocoder(app, Locale.getDefault()),
                )
            }

            /**
             * Initializer for CreateOrJoinViewModel
             */
            initializer {
                JoinViewModel(
                    meetingApplication().container.db,
                    meetingApplication().container.userRepository,
                )
            }

            /**
             * Initializer for EventViewModel
             */
            initializer {
                EventViewModel(
                    meetingApplication().container.eventRepository,
                )
            }

            /**
             * Initializer for HostDashboardViewModel
             */
            initializer {
                val app = meetingApplication()
                HostDashboardViewModel(
                    application = app,
                    meetingApplication().container.eventRepository,
                    meetingApplication().container.submissionRepository,
                    this.createSavedStateHandle(),
                )
            }

            /**
             * Initializer for ParticipantViewModel
             */
            initializer {
                ParticipantViewModel(
                    meetingApplication().container.eventRepository,
                    this.createSavedStateHandle(),
                )
            }

            /**
             * Initializer for PlaceViewModel (formerly RestaurantViewModel)
             */
            initializer {
                val app = meetingApplication()

                // Create the location client using the application context
                val locationClient =
                    LocationServices
                        .getFusedLocationProviderClient(app)

                PlaceViewModel(
                    eventRepository = app.container.eventRepository,
                    apiKey = app.container.placesApiKey,
                    fusedLocationClient = locationClient, // Added this
                    savedStateHandle = this.createSavedStateHandle(), // Moved this to the end
                )
            }

            /**
             * Initializer for ParticipantDashboardViewModel
             */
            initializer {
                val app = meetingApplication()
                ParticipantDashboardViewModel(
                    application = app,
                    eventRepository = app.container.eventRepository,
                    savedStateHandle = this.createSavedStateHandle(),
                )
            }
        }
}

/**
 * Extension function to queries for [android.app.Application] object and returns an instance of
 * [MeetingApplication].
 */
fun CreationExtras.meetingApplication(): MeetingApplication = (this[AndroidViewModelFactory.APPLICATION_KEY] as MeetingApplication)
