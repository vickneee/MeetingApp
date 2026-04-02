package com.meetup.meetingapp.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.meetup.meetingapp.data.db.converter.Converters
import com.meetup.meetingapp.data.db.daos.EventDao
import com.meetup.meetingapp.data.db.daos.UserDao
import com.meetup.meetingapp.data.db.entities.EventEntity
import com.meetup.meetingapp.data.db.entities.UserEntity

@Database(entities = [EventEntity::class, UserEntity::class], version = 3, exportSchema = false)
@TypeConverters(Converters::class)
abstract class MeetingAppDatabase : RoomDatabase() {

    abstract fun eventDao(): EventDao
    abstract fun userDao(): UserDao

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
