package com.meetup.meetingapp.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.meetup.meetingapp.data.db.daos.ExampleDao
import com.meetup.meetingapp.data.db.entities.ExampleEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [ExampleEntity::class], version = 1, exportSchema = false)
abstract class MeetingAppDatabase : RoomDatabase() {

    abstract fun exampleDao(): ExampleDao

    companion object {
        @Volatile
        private var INSTANCE: MeetingAppDatabase? = null

        fun getDatabase(context: Context): MeetingAppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MeetingAppDatabase::class.java,
                    "meeting_app"
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .addCallback(DatabaseCallback(context))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(private val context: Context) : RoomDatabase.Callback() {
            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                CoroutineScope(Dispatchers.IO).launch {
                    val dao = getDatabase(context).exampleDao()
                    // Insert test data if it doesn't exist (OnConflictStrategy.IGNORE handles this)
                    dao.insert(ExampleEntity(id = 1, name = "Test Item 1"))
                    dao.insert(ExampleEntity(id = 2, name = "Test Item 2"))
                    dao.insert(ExampleEntity(id = 3, name = "Test Item 3"))
                }
            }
        }
    }
}
