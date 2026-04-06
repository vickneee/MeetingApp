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

// Represents the combination of dateRangeStart, dateRangeEnd, and timeSlots
data class EventAvailabilityTuple(
    @ColumnInfo(name = "dateRangeStartString") val dateRangeStart: String,
    @ColumnInfo(name = "dateRangeEndString") val dateRangeEnd: String,
    @ColumnInfo(name = "timeSlotsJson") val timeSlotsJson: String
)

@Dao
interface EventDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: EventEntity)

    @Query("SELECT * FROM events")
    fun getAllEvents(): Flow<List<EventEntity>>

    @Query("SELECT * FROM events WHERE id = :id")
    fun getEventById(id: String): Flow<EventEntity?>

    @Query("SELECT * FROM events WHERE eventCode = :code")
    fun getEventByCode(code: String): Flow<EventEntity?>

    @Query("SELECT * FROM events WHERE status = :status")
    fun getEventsByStatus(status: String): Flow<List<EventEntity>>

    @Query("SELECT dateRangeStartString, dateRangeEndString, timeSlotsJson FROM events WHERE eventCode = :eventCode")
    fun getAvailabilityByEventCode(eventCode: String): Flow<EventAvailabilityTuple>

    @Upsert
    suspend fun upsertEvent(event: EventEntity)

    @Upsert
    suspend fun upsertEvents(events: List<EventEntity>)

    @Delete
    suspend fun deleteEvent(event: EventEntity)

    @Query("DELETE FROM events WHERE id = :id")
    suspend fun deleteEventById(id: String)
}