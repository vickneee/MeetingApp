package com.meetup.meetingapp.data.db.daos

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.meetup.meetingapp.data.db.entities.ParticipantResponseEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) for managing participant responses in the Room database.
 * This interface defines methods for inserting, updating, and retrieving participant responses.
 * @constructor Creates a new instance of the ParticipantResponseDao.
 */
@Dao
interface ParticipantResponseDao {
    /**
     * Inserts or updates a list of participant responses in the database.
     * @param responses The list of participant responses to be inserted or updated.
     */
    @Upsert
    suspend fun upsertResponses(responses: List<ParticipantResponseEntity>)

    /**
     * Retrieves a list of participant responses for a specific event ID.
     *
     * @param eventId The ID of the event for which to retrieve participant responses.
     * @return A [Flow] emitting a list of [ParticipantResponseEntity] objects.
     * The flow will emit new values whenever the data in the database changes.
     */
    @Query("SELECT * FROM participant_responses WHERE eventId = :eventId")
    fun getResponsesByEventId(eventId: String): Flow<List<ParticipantResponseEntity>>

    /**
     * Deletes all participant responses associated with a specific event ID.
     *
     * @param eventId The ID of the event for which to delete participant responses.
     */
    @Query("DELETE FROM participant_responses WHERE eventId = :eventId")
    suspend fun deleteByEventId(eventId: String)
}
