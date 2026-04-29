package com.meetup.meetingapp.data.db.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.meetup.meetingapp.data.db.entities.CityEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) for accessing and modifying city data in the local Room database.
 *
 * This DAO provides:
 * - An upsert operation for inserting or replacing a list of [CityEntity] records.
 * - A query for observing all cities that belong to a specific country as a [Flow].
 *
 * Used by repositories to persist and retrieve city information efficiently.
 */
@Dao
interface CityDao {
    /**
     * Inserts or updates the given list of [CityEntity] objects.
     *
     * If a city already exists with the same primary key, it will be replaced.
     *
     * @param cities The list of cities to insert or update.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCities(cities: List<CityEntity>)

    /**
     * Returns a [Flow] that emits all cities belonging to the specified country.
     *
     * The flow automatically updates when the underlying database table changes.
     *
     * @param country The country name used to filter cities.
     * @return A flow emitting the list of matching [CityEntity] objects.
     */
    @Query("SELECT * FROM cities WHERE country = :country")
    fun getCitiesByCountry(country: String): Flow<List<CityEntity>>
}
