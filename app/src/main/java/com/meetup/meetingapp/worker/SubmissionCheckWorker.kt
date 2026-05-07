/*
 * Copyright (C) 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.meetup.meetingapp.worker

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.meetup.meetingapp.MeetingApplication
import com.meetup.meetingapp.NOTIFICATION_TITLE_HOST
import com.meetup.meetingapp.R
import com.meetup.meetingapp.utils.makeSubmissionReminderNotification

/**
 * Worker that checks if all participants have submitted their input for an event.
 * If so, it triggers a notification to close voting.
 *
 * @param context The application context.
 * @param workerParams The worker parameters.
 */
class SubmissionCheckWorker(
    context: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(context, workerParams) {
    override suspend fun doWork(): Result {
        try {
            val eventId = inputData.getString("eventId") ?: return Result.failure()

            val app = applicationContext as? MeetingApplication
            val repository = app?.container?.eventRepository ?: return Result.failure()

            val allSubmitted = repository.isAllSubmitted(eventId)
            if (allSubmitted) {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                    ContextCompat.checkSelfPermission(
                        applicationContext,
                        Manifest.permission.POST_NOTIFICATIONS,
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    makeSubmissionReminderNotification(
                        title = NOTIFICATION_TITLE_HOST,
                        message = applicationContext.getString(R.string.time_to_close_voting),
                        applicationContext,
                    )
                } else {
                    Log.d(TAG, "Notification permission not granted")
                }
            }

            return Result.success()
        } catch (_: Exception) {
            return Result.failure()
        }
    }

    companion object {
        private const val TAG = "SubmissionCheckWorker"
    }
}
