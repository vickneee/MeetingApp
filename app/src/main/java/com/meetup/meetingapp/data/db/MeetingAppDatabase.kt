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
import com.meetup.meetingapp.data.db.daos.RestaurantDao
import com.meetup.meetingapp.data.db.daos.UserDao
import com.meetup.meetingapp.data.db.entities.CityEntity
import com.meetup.meetingapp.data.db.entities.EventEntity
import com.meetup.meetingapp.data.db.entities.ParticipantResponseEntity
import com.meetup.meetingapp.data.db.entities.RestaurantEntity
import com.meetup.meetingapp.data.db.entities.UserEntity

/**
 * The Room database for the Meeting App.
 *
 * This database stores all persistent application data, including:
 * - Events and their availability information
 * - Users and authentication‑related metadata
 * - Cities used for location selection
 * - Restaurants fetched from external APIs
 * - Participant responses for voting and availability
 *
 * The database:
 * - Defines all Room entities used by the app
 * - Exposes DAO accessors for each data type
 * - Uses a singleton pattern to ensure a single shared instance
 * - Applies `fallbackToDestructiveMigration` to reset the schema when incompatible
 *   migrations occur (useful during development)
 *
 * @constructor Not intended to be created manually. Use [getDatabase] to obtain the singleton instance.
 */
@Database(
    entities = [EventEntity::class, UserEntity::class, CityEntity::class, ParticipantResponseEntity::class, RestaurantEntity::class],
    version = 10,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class MeetingAppDatabase : RoomDatabase() {
    abstract fun eventDao(): EventDao

    abstract fun userDao(): UserDao

    abstract fun cityDao(): CityDao

    abstract fun restaurantDao(): RestaurantDao

    abstract fun participantResponseDao(): ParticipantResponseDao

    companion object {
        @Volatile
        private var instance: MeetingAppDatabase? = null

        /**
         * Returns the singleton instance of [MeetingAppDatabase].
         *
         * Uses double‑checked locking to ensure thread‑safe initialization.
         * The database is built with:
         * - Application context (to avoid leaking Activity)
         * - `fallbackToDestructiveMigration` for development convenience
         *
         * @param context The application context.
         */
        fun getDatabase(context: Context): MeetingAppDatabase =
            instance ?: synchronized(this) {
                val newInstance =
                    Room
                        .databaseBuilder(
                            context.applicationContext,
                            MeetingAppDatabase::class.java,
                            "meeting_app",
                        ).fallbackToDestructiveMigration(dropAllTables = true)
                        .build()
                instance = newInstance
                newInstance
            }
    }
}
