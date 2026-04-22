package com.meetup.meetingapp.data.location

import android.annotation.SuppressLint
import android.location.Location
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.tasks.await

/**
 * Returns a Pair of Latitude and Longitude.
 * Uses Priority.PRIORITY_HIGH_ACCURACY for the best distance calculation.
 */
@SuppressLint("MissingPermission")
suspend fun fetchCurrentCoordinates(fusedLocationClient: FusedLocationProviderClient): Pair<Double, Double>? =
    try {
        val result: Location? =
            fusedLocationClient
                .getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    CancellationTokenSource().token,
                ).await() // This "await" comes from play-services-coroutines

        result?.let { it.latitude to it.longitude }
    } catch (e: Exception) {
        null // Handle cases like GPS being turned off
    }
