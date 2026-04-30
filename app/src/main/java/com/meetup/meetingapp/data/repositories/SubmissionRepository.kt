package com.meetup.meetingapp.data.repositories

import com.meetup.meetingapp.data.model.Event

/**
 * Repository interface for managing background submission checks.
 */
interface SubmissionRepository {

    /**
     * Schedules a background WorkManager task to check if all participants
     * have submitted for the given event.
     *
     * @param eventId The ID of the event to check.
     */
    fun scheduleSubmissionCheck(eventId: String)
}
