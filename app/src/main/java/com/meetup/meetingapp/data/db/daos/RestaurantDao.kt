package com.meetup.meetingapp.data.db.daos

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.meetup.meetingapp.data.db.entities.RestaurantEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) for restaurant data stored in the local Room database.
 *
 * This DAO provides:
 *  - Reactive streams of restaurant candidates for a specific event
 *  - Lookup of a single restaurant by its Google Places ID
 *  - Upsert operations to keep local data in sync with Firestore
 *
 * Room acts as the single source of truth for restaurant data.
 * Firestore → Room synchronization is handled in the repository layer.
 */
@Dao
interface RestaurantDao {
    /**
     * Returns a reactive stream of all restaurants associated with the given event.
     *
     * Behavior:
     *  - Emits updates automatically whenever the underlying Room table changes.
     *  - Used by ViewModel to observe restaurant candidates in real time.
     *
     * @param eventId The ID of the event whose restaurants should be observed.
     * @return A Flow emitting the list of RestaurantEntity objects.
     */
    @Query("SELECT * FROM restaurants WHERE eventId = :eventId")
    fun getRestaurants(eventId: String): Flow<List<RestaurantEntity>>

    /**
     * Returns a reactive stream of a single restaurant by its Google Places ID.
     *
     * Behavior:
     *  - Emits null if the restaurant does not exist.
     *  - Useful for detail screens or voting logic.
     *
     * @param placeId The Google Places ID of the restaurant.
     * @return A Flow emitting the RestaurantEntity or null.
     */
    @Query("SELECT * FROM restaurants WHERE placeId = :placeId")
    fun getRestaurant(placeId: String): Flow<RestaurantEntity?>

    /**
     * Inserts or updates a list of restaurant entities in the local database.
     *
     * Behavior:
     *  - Uses Room's @Upsert to insert new rows or update existing ones.
     *  - Ensures local data always matches the latest Firestore snapshot.
     *
     * @param restaurants The list of RestaurantEntity objects to upsert.
     */
    @Upsert
    suspend fun upsertRestaurants(restaurants: List<RestaurantEntity>)
}
