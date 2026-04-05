package com.meetup.meetingapp.data.db.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.meetup.meetingapp.data.db.entities.CityEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CityDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCities(cities: List<CityEntity>)


    @Query("SELECT * FROM cities WHERE country = :country")
    fun getCitiesByCountry(country: String): Flow<List<CityEntity>>
}