package com.example.meetingapp.data.repositories

import com.example.meetingapp.data.db.entities.ExampleEntity
import kotlinx.coroutines.flow.Flow

interface ExampleRepository {
    fun getAllItems(): Flow<List<ExampleEntity>>
    suspend fun insertItem(item: ExampleEntity)
}
