package com.meetup.meetingapp.data.db.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import com.meetup.meetingapp.data.db.entities.UserEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) for managing user data in the Room database.
 *
 * Provides operations for:
 * - Inserting or updating user records
 * - Fetching users by UID as a reactive [Flow] or as a suspend function
 * - Updating existing user information
 * - Upserting user data using Room's @Upsert
 *
 * This DAO is typically used by repositories that handle authentication
 * or user profile management.
 */
@Dao
interface UserDao {
    /**
     * Inserts or replaces a single [UserEntity].
     *
     * If a user with the same UID already exists, it will be replaced.
     *
     * @param user The user to insert or update.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    /**
     * Returns a [Flow] that emits the user with the given UID.
     *
     * The flow updates automatically when the user record changes.
     *
     * @param uid The unique user ID.
     * @return A flow emitting the matching [UserEntity], or null if not found.
     */
    @Query("SELECT * FROM users WHERE uid = :uid")
    fun getUserByUid(uid: String): Flow<UserEntity?>

    /**
     * Retrieves the user with the given UID as a suspend function.
     *
     * Unlike [getUserByUid], this does not observe changes and returns a single value.
     *
     * @param uid The unique user ID.
     * @return The matching [UserEntity], or null if not found.
     */
    @Query("SELECT * FROM users WHERE uid = :uid")
    suspend fun getUser(uid: String): UserEntity?

    /**
     * Updates an existing user record.
     *
     * @param user The user entity containing updated fields.
     */
    @Update
    suspend fun updateUser(user: UserEntity)

    /**
     * Inserts or updates a user using Room's @Upsert.
     *
     * This is equivalent to "insert or replace" but more efficient and expressive.
     *
     * @param user The user to insert or update.
     */
    @Upsert
    suspend fun upsertUser(user: UserEntity)
}
