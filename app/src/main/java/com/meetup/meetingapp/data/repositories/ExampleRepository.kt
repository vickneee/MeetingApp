package com.meetup.meetingapp.data.repositories

import com.meetup.meetingapp.data.db.entities.ExampleEntity
import kotlinx.coroutines.flow.Flow

interface ExampleRepository {
    fun getAllItems(): Flow<List<ExampleEntity>>
    suspend fun insertItem(item: ExampleEntity)
}
