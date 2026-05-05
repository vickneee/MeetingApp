package com.meetup.meetingapp.data.repositories

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.meetup.meetingapp.worker.SubmissionCheckWorker

/**
 * Repository implementation for managing background submission checks.
 *
 * @param context The application context.
 *
 * [WorkManager] based implementation of [SubmissionRepository].
 */
class SubmissionRepositoryImp(
    context: Context,
) : SubmissionRepository {
    private val workManager = WorkManager.getInstance(context)

    /**
     * Schedules a background WorkManager task to check if all participants have submitted
     * for the given event.
     *
     * @param eventId The ID of the event to check.
     *
     * @see SubmissionCheckWorker for more information.
     */
    override fun scheduleSubmissionCheck(eventId: String) {
        val workRequest =
            OneTimeWorkRequestBuilder<SubmissionCheckWorker>()
                .setInputData(workDataOf("eventId" to eventId))
                .build()

        workManager.enqueueUniqueWork(
            "submission_check_$eventId", // unique name per event
            ExistingWorkPolicy.KEEP, // ignore new requests if one is already running
            workRequest,
        )
    }
}
