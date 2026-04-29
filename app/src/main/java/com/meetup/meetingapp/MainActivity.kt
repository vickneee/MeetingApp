package com.meetup.meetingapp

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import com.meetup.meetingapp.ui.AppViewModelProvider
import com.meetup.meetingapp.ui.screens.home.HomeViewModel
import com.meetup.meetingapp.ui.theme.MeetingAppTheme

/**
 * Main entry point of the MeetingApp.
 *
 * Responsibilities:
 * - Requests location permissions on launch (fine + coarse)
 * - Initializes [HomeViewModel] and triggers default‑country detection when permission is granted
 * - Sets up the app’s Compose UI and theme
 *
 * The activity uses `registerForActivityResult` to request permissions before
 * rendering the UI. If the user grants either fine or coarse location access,
 * the ViewModel begins resolving the user's country using GPS + Geocoder.
 *
 * This activity hosts the root composable [MeetingApp], which contains the
 * navigation graph and all top‑level screens.
 */
class MainActivity : ComponentActivity() {
    private val homeViewModel: HomeViewModel by viewModels { AppViewModelProvider.Factory }

    private val requestLocationPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions(),
        ) { permissions ->
            val fineLocationGranted = permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false)
            val coarseLocationGranted = permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false)

            if (fineLocationGranted || coarseLocationGranted) {
                homeViewModel.initDefaultCountry()
            } else {
                // Handle the case where the user denies location permission
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        requestLocationPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            ),
        )

        setContent {
            MeetingAppTheme {
                MeetingApp()
            }
        }
    }
}
