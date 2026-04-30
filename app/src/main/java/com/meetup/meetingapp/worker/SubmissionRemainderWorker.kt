package com.meetup.meetingapp.worker

import android.Manifest
import android.content.Context
import android.util.Log
import androidx.annotation.RequiresPermission
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

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override suspend fun doWork(): Result {
        val eventId = inputData.getString(KEY_EVENT_ID) ?: return Result.failure()
        Log.d(TAG, "doWork started — eventId: $eventId")

        val repository = (applicationContext as MeetingApplication).container.eventRepository
        Log.d(TAG, "Checking if all submitted for event: $eventId")

        val allSubmitted = repository.isAllSubmitted(eventId)
        Log.d(TAG, "allSubmitted: $allSubmitted")

        if (allSubmitted) {
            Log.d(TAG, "All submitted — firing notification")
            makeSubmissionReminderNotification(
                applicationContext.getString(R.string.time_to_close_voting),
                applicationContext
            )
        }
        else {
            Log.d(TAG, "Not all submitted yet — no notification")
        }


        return Result.success()
    }

    companion object {
        const val KEY_EVENT_ID = "KEY_EVENT_ID"
        private const val TAG = "SubmissionWorker"
    }
}
