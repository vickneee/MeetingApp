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

/**
 * Worker that checks if all participants have submitted their input for an event.
 * If so, it triggers a notification to close voting.
 *
 * @param context The application context.
 * @param workerParams The worker parameters.
 */
class SubmissionCheckWorker(
    context: Context,
    workerParams: WorkerParameters
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
                        Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    makeSubmissionReminderNotification(
                        title = NOTIFICATION_TITLE_HOST,
                        message = applicationContext.getString(R.string.time_to_close_voting),
                        applicationContext
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
