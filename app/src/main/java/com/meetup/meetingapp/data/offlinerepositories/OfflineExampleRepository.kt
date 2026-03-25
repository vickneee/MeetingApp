package com.meetup.meetingapp.data.offlinerepositories

import com.meetup.meetingapp.data.db.daos.ExampleDao
import com.meetup.meetingapp.data.db.entities.ExampleEntity
import com.meetup.meetingapp.data.repositories.ExampleRepository
import kotlinx.coroutines.flow.Flow

class OfflineExampleRepository(private val exampleDao: ExampleDao) : ExampleRepository {

    override fun getAllItems(): Flow<List<ExampleEntity>> = exampleDao.getAllItems()

    override suspend fun insertItem(item: ExampleEntity) = exampleDao.insert(item)
}
