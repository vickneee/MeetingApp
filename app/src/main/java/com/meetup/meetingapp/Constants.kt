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

package com.meetup.meetingapp

// Submission notification channel

// Name of Notification Channel for verbose notifications of background work
val VERBOSE_NOTIFICATION_CHANNEL_NAME: CharSequence = "Voting Notifications"

// Description of Notification Channel for verbose notifications of background work
const val VERBOSE_NOTIFICATION_CHANNEL_DESCRIPTION = "Notifications about voting rounds and submissions"

// ID of Notification Channel for verbose notifications of background work
const val CHANNEL_ID = "VERBOSE_NOTIFICATION"

// ID of Notification for verbose notifications of background work
const val NOTIFICATION_ID = 1

// Submission notification titles
val NOTIFICATION_TITLE_HOST: CharSequence = "Time to Close Voting!"

val NOTIFICATION_TITLE_PARTICIPANTS: CharSequence = "Time to Vote!"

// Event Finalized Notification Channel constants

// Name of Notification Channel for event finalization
val EVENT_FINALIZED_CHANNEL_NAME: CharSequence = "Event Updates"

// Description of Notification Channel for event finalization
const val EVENT_FINALIZED_CHANNEL_DESCRIPTION = "Notifications when a meeting is finalized"

// Title of Notification for finalized events
val EVENT_FINALIZED_NOTIFICATION_TITLE: CharSequence = "Meeting Finalized!"

// ID of Notification Channel for finalized events
const val EVENT_CHANNEL_ID = "EVENT_FINALIZED_NOTIFICATION"

// Unique ID for the finalized notification (must be different from NOTIFICATION_ID)
const val EVENT_NOTIFICATION_ID = 2


// Request code for pending intent
const val REQUEST_CODE = 0
