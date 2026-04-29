package com.meetup.meetingapp.data.db.daos

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import com.meetup.meetingapp.data.db.entities.EventEntity
import kotlinx.coroutines.flow.Flow

/**
 * Represents a subset of event availability information stored in the database.
 *
 * This tuple is used to retrieve only the fields related to:
 * - The start of the date range
 * - The end of the date range
 * - The serialized list of time slots (stored as JSON)
 *
 * It is returned by queries that do not require the full [EventEntity] object.
 */
data class EventAvailabilityTuple(
    @ColumnInfo(name = "dateRangeStartString") val dateRangeStart: String,
    @ColumnInfo(name = "dateRangeEndString") val dateRangeEnd: String,
    @ColumnInfo(name = "timeSlotsJson") val timeSlotsJson: String,
)

/**
 * Data Access Object (DAO) for managing event data in the Room database.
 *
 * Provides operations for:
 * - Inserting, updating, and deleting events
 * - Querying events by ID, code, or status
 * - Observing event lists as reactive [Flow] streams
 * - Fetching availability-related fields for a specific event
 */

@Dao
interface EventDao {
    /**
     * Inserts or replaces a single [EventEntity].
     *
     * @param event The event to insert or update.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: EventEntity)

    /**
     * Returns a [Flow] emitting all events stored in the database.
     *
     * The flow updates automatically when the table changes.
     */
    @Query("SELECT * FROM events")
    fun getAllEvents(): Flow<List<EventEntity>>

    /**
     * Returns a [Flow] emitting the event with the given ID.
     *
     * @param id The event ID.
     * @return A flow emitting the event or null if not found.
     */
    @Query("SELECT * FROM events WHERE id = :id")
    fun getEventById(id: String): Flow<EventEntity?>

    /**
     * Returns a [Flow] emitting the event with the given event code.
     *
     * @param code The event code.
     */
    @Query("SELECT * FROM events WHERE eventCode = :code")
    fun getEventByCode(code: String): Flow<EventEntity?>

    /**
     * Returns a [Flow] emitting all events matching the given status.
     *
     * @param status The event status (e.g., CREATED, VOTING, FINALIZED).
     */
    @Query("SELECT * FROM events WHERE status = :status")
    fun getEventsByStatus(status: String): Flow<List<EventEntity>>

    /**
     * Returns availability-related fields for the event with the given code.
     *
     * This query returns only:
     * - dateRangeStartString
     * - dateRangeEndString
     * - timeSlotsJson
     *
     * @param eventCode The event code.
     */
    @Query("SELECT dateRangeStartString, dateRangeEndString, timeSlotsJson FROM events WHERE eventCode = :eventCode")
    fun getAvailabilityByEventCode(eventCode: String): Flow<EventAvailabilityTuple>

    /**
     * Updates the status of the event with the given ID.
     *
     * @param eventId The event ID.
     * @param status The new status value.
     */
    @Query("UPDATE events SET status = :status WHERE id = :eventId")
    suspend fun updateEventStatus(
        eventId: String,
        status: String,
    )

    /**
     * Inserts or updates a single event using Room's @Upsert.
     */
    @Upsert
    suspend fun upsertEvent(event: EventEntity)

    /**
     * Inserts or updates multiple events using Room's @Upsert.
     */
    @Upsert
    suspend fun upsertEvents(events: List<EventEntity>)

    /**
     * Deletes the given event.
     *
     * @param event The event to delete.
     */
    @Delete
    suspend fun deleteEvent(event: EventEntity)

    /**
     * Deletes the event with the given ID.
     *
     * @param id The event ID.
     */
    @Query("DELETE FROM events WHERE id = :id")
    suspend fun deleteEventById(id: String)
}
