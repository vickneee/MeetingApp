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
