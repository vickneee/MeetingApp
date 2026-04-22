package com.meetup.meetingapp.data.db.mapper

import com.google.firebase.firestore.PropertyName
import com.meetup.meetingapp.data.db.entities.CityEntity

/**
 * Represents a city in the Firestore database.
 * @property name The name of the city.
 * @property search_query The search query associated with the city.
 * @constructor Creates a new instance of the FirestoreCity object.
 */
data class FirestoreCity(
    @get:PropertyName("name")
    @set:PropertyName("name")
    var name: String = "",
    @get:PropertyName("search_query")
    @set:PropertyName("search_query")
    var search_query: String = "",
)

/**
 * Represents a list of cities in the Firestore database.
 * @property items The list of FirestoreCity objects.
 * @constructor Creates a new instance of the FirestoreCityList object.
 */
data class FirestoreCityList(
    @get:PropertyName("items")
    @set:PropertyName("items")
    var items: List<FirestoreCity> = emptyList(),
)

/**
 * Mapper object for converting FirestoreCity objects to CityEntity objects.
 * @constructor Creates a new instance of the CityMapper object.
 */
object CityMapper {
    fun FirestoreCity.toEntity(country: String) =
        CityEntity(
            name = name,
            searchQuery = search_query,
            country = country,
        )
}
