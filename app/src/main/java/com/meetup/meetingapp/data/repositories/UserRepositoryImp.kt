package com.meetup.meetingapp.data.repositories

import android.content.ContentValues.TAG
import android.util.Log
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.meetup.meetingapp.data.db.daos.UserDao
import com.meetup.meetingapp.data.db.entities.UserEntity
import com.meetup.meetingapp.data.model.User
import kotlinx.coroutines.tasks.await
import kotlin.collections.emptyList

/**
 * Implementation of [UserRepository] responsible for managing user-related
 * operations in Cloud Firestore.
 *
 * This class handles:
 * - Creating new user documents
 * - Adding event IDs to the user's created event list
 * - Adding event IDs to the user's joined event list
 *
 * All operations are performed asynchronously using Kotlin coroutines.
 *
 * @property db The Firestore instance used for user document operations.
 */
class UserRepositoryImp(
    private val db: FirebaseFirestore,
    private val userDao: UserDao
) : UserRepository {

    /**
     * Creates a new user document in Firestore.
     *
     * This is typically called when a user signs in for the first time.
     * The user document is initialized with default values defined in the [User] model.
     *
     * @param uid The unique identifier of the authenticated user.
     */
    override suspend fun createUser(uid: String) {
        val user = User(uid)

        // Save to Firestore.
        try {
            db.collection("users")
                .document(uid)
                .set(user)
                .await()

            // Save to Room
            val localUser = UserEntity(uid, emptyList(), emptyList())
            userDao.insertUser(localUser)

            Log.d(TAG, "User document created successfully for uid: $uid")
        } catch (e: Exception) {
            Log.w(TAG, "Failed to create user document for uid: $uid", e)
        }
    }

    /**
     * Adds an event ID to the user's list of created events.
     *
     * Uses Firestore's arrayUnion() to ensure the event ID is added only once
     * and avoids duplicates automatically.
     *
     * @param eventId The ID of the event created by the user.
     * @param uid The unique identifier of the user.
     */
    override suspend fun addCreatedEvent(eventId: String, uid: String) {
        try {
            // Firebase
            db.collection("users")
                .document(uid)
                .set(
                    mapOf("createdEventIds" to FieldValue.arrayUnion(eventId)),
                    SetOptions.merge()
                )
                .await()

            // Room
            val user = userDao.getUser(uid) ?: return
            val updatedList = user.createdEventIds + eventId

            userDao.updateUser(user.copy(createdEventIds = updatedList))

            Log.d(TAG, "Created event added: eventId=$eventId for uid=$uid")
        } catch (e: Exception) {
            Log.w(TAG, "Failed to add created event: eventId=$eventId for uid=$uid", e)
        }
    }

    /**
     * Adds an event ID to the user's list of joined events.
     *
     * Uses Firestore's arrayUnion() to ensure the event ID is added only once
     * and avoids duplicates automatically.
     *
     * @param eventId The ID of the event the user joined.
     * @param uid The unique identifier of the user.
     */
    override suspend fun addJoinedEvent(eventId: String, uid: String) {
        try {

            // Firebase
            db.collection("users")
                .document(uid)
                .set(
                    mapOf("joinedEventIds" to FieldValue.arrayUnion(eventId)),
                    SetOptions.merge()
                )
                .await()

            // Room
            var user = userDao.getUser(uid)

            if (user == null) {
                syncUser(uid)
                user = userDao.getUser(uid)
            }

            if (user == null) return

            val updatedList = (user.joinedEventIds + eventId).distinct()

            userDao.updateUser(user.copy(joinedEventIds = updatedList))

            Log.d("ROOM", "Updated Room: $updatedList")

            Log.d(TAG, "Joined event added: eventId=$eventId for uid=$uid")
        } catch (e: Exception) {
            Log.w(TAG, "Failed to add joined event: eventId=$eventId for uid=$uid", e)
        }
    }

    suspend fun syncUser(uid: String) {
        val snapshot = db.collection("users").document(uid).get().await()

        @Suppress("UNCHECKED_CAST")
        val created = snapshot.get("createdEventIds") as? List<String> ?: emptyList()
        @Suppress("UNCHECKED_CAST")
        val joined = snapshot.get("joinedEventIds") as? List<String> ?: emptyList()

        val user = UserEntity(uid, created, joined)
        userDao.insertUser(user)
    }
}
