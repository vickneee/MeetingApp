package com.example.meetingapp.data.db.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.meetingapp.data.db.entities.ExampleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExampleDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(item: ExampleEntity)

    @Query("SELECT * from ExampleEntity ORDER BY id ASC")
    fun getAllItems(): Flow<List<ExampleEntity>>

    @Update
    suspend fun update(item: ExampleEntity)

    @Delete
    suspend fun delete(item: ExampleEntity)

    @Query("SELECT * from ExampleEntity WHERE id = :id")
    fun getItem(id: Int): Flow<ExampleEntity>
}
