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
