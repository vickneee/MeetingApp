package com.example.meetingapp.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.meetingapp.data.db.daos.ExampleDao
import com.example.meetingapp.data.db.entities.ExampleEntity

@Database(entities = [ExampleEntity::class], version = 1, exportSchema = false)
abstract class MeetingAppDatabase : RoomDatabase() {

    /**
     * DAO for the database.
     * @return The DAO for the database.
     */
    abstract fun exampleDao(): ExampleDao

    /**
     * Singleton instance of the database.
     */
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
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
