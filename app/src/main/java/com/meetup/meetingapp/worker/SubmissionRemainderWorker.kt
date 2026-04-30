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
import com.meetup.meetingapp.R

/**
 * Worker that checks if all participants have submitted their input for an event.
 * If so, it triggers a notification to close voting.
 */
class SubmissionRemainderWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        try {
            val eventId = inputData.getString(KEY_EVENT_ID) ?: return Result.failure()
            Log.d(TAG, "doWork started — eventId: $eventId")

            val app = applicationContext as? MeetingApplication
            if (app?.container == null) {
                Log.e(TAG, "Container not initialized — retrying")
                return Result.retry()
            }

            val repository = app.container.eventRepository
            val allSubmitted = repository.isAllSubmitted(eventId)
            Log.d(TAG, "allSubmitted: $allSubmitted")

            if (allSubmitted) {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                    ContextCompat.checkSelfPermission(
                        applicationContext,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    Log.d(TAG, "All submitted — firing notification")
                    makeSubmissionReminderNotification(
                        applicationContext.getString(R.string.time_to_close_voting),
                        applicationContext
                    )
                } else {
                    Log.w(TAG, "POST_NOTIFICATIONS permission not granted — skipping")
                }
            } else {
                Log.d(TAG, "Not all submitted yet — no notification")
            }

            return Result.success()

        } catch (e: Throwable) {
            Log.e(TAG, "doWork crashed", e)  // ← this will show the real error
            return Result.failure()
        }
    }

    companion object {
        const val KEY_EVENT_ID = "KEY_EVENT_ID"
        private const val TAG = "SubmissionRemainderWorker"
    }
}
