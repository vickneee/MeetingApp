package com.meetup.meetingapp.data.repositories

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.meetup.meetingapp.worker.SubmissionCheckWorker

/**
 * [WorkManager] based implementation of [SubmissionRepository].
 */
class WorkManagerSubmissionRepository(context: Context) : SubmissionRepository {
    private val workManager = WorkManager.getInstance(context)

    override fun scheduleSubmissionCheck(eventId: String) {
        val workRequest = OneTimeWorkRequestBuilder<SubmissionCheckWorker>()
            .setInputData(workDataOf("eventId" to eventId))
            .build()

        workManager.enqueueUniqueWork(
            "submission_check_$eventId", // unique name per event
            ExistingWorkPolicy.KEEP, // ignore new requests if one is already running
            workRequest
        )
    }
}
