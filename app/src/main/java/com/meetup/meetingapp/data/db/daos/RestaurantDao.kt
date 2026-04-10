package com.meetup.meetingapp.data.db.daos

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.meetup.meetingapp.data.db.entities.RestaurantEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RestaurantDao {

    @Query("SELECT * FROM restaurants WHERE eventId = :eventId")
    fun getRestaurants(eventId: String): Flow<List<RestaurantEntity>>

    @Query("SELECT * FROM restaurants WHERE placeId = :placeId")
    fun getRestaurant(placeId: String): Flow<RestaurantEntity?>

    @Upsert
    suspend fun upsertRestaurants(restaurants: List<RestaurantEntity>)
}
