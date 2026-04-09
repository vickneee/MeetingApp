package com.meetup.meetingapp.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.meetup.meetingapp.data.db.converter.Converters
import com.meetup.meetingapp.data.db.daos.CityDao
import com.meetup.meetingapp.data.db.daos.EventDao
import com.meetup.meetingapp.data.db.daos.ParticipantResponseDao
import com.meetup.meetingapp.data.db.daos.UserDao
import com.meetup.meetingapp.data.db.entities.CityEntity
import com.meetup.meetingapp.data.db.entities.EventEntity
import com.meetup.meetingapp.data.db.entities.ParticipantResponseEntity
import com.meetup.meetingapp.data.db.entities.UserEntity

/**
 * The Room database for the Meeting App.
 * This class defines the database configuration and serves as the
 * access point for the underlying storage.
 * @constructor Creates a new instance of the MeetingAppDatabase.
 */
@Database(entities = [EventEntity::class, UserEntity::class, CityEntity::class, ParticipantResponseEntity::class], version = 8, exportSchema = false)
@TypeConverters(Converters::class)
abstract class MeetingAppDatabase : RoomDatabase() {

    abstract fun eventDao(): EventDao
    abstract fun userDao(): UserDao

    abstract fun cityDao(): CityDao

    abstract fun participantResponseDao(): ParticipantResponseDao

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
