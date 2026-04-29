package com.meetup.meetingapp.data.location

import android.annotation.SuppressLint
import android.location.Location
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.tasks.await

/**
 * Fetches the user's current GPS coordinates using a one‑time high‑accuracy location request.
 *
 * This function:
 * - Uses [Priority.PRIORITY_HIGH_ACCURACY] for the most precise reading.
 * - Suspends until the location is returned via `await()` from play‑services‑coroutines.
 * - Returns `null` if the location cannot be retrieved (e.g., GPS disabled, timeout, or exception).
 * - Wraps the call in a try/catch to avoid crashes from location failures.
 *
 * @param fusedLocationClient The FusedLocationProviderClient used to request the location.
 * @return A [Pair] of (latitude, longitude), or `null` if unavailable.
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
    } catch (_: Exception) {
        null // Handle cases like GPS being turned off
    }
