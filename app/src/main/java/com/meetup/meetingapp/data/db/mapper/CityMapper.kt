package com.meetup.meetingapp.data.db.mapper

import com.meetup.meetingapp.data.db.entities.CityEntity

/**
 * Represents a city in the Firestore database.
 * @property name The name of the city.
 * @property search_query The search query associated with the city.
 */
data class FirestoreCity(
    val name: String = "",
    val search_query: String = ""
)

/**
 * Represents a list of cities in the Firestore database.
 * @property items The list of cities.
 * @constructor Creates a new instance of the FirestoreCityList class.
 */
data class FirestoreCityList(
    val items: List<FirestoreCity> = emptyList()
)

/**
 * Mapper object for converting FirestoreCity objects to CityEntity objects.
 * @constructor Creates a new instance of the CityMapper object.
 * @see CityEntity for the mapping logic.
 * @see CityMapper for the mapping logic.
 */
object CityMapper {

    fun FirestoreCity.toEntity(country: String) = CityEntity(
        name = name,
        searchQuery = search_query,
        country = country
    )
}
