package com.meetup.meetingapp.data.db.mapper

import com.meetup.meetingapp.data.db.entities.CityEntity


data class FirestoreCity(
    val name: String = "",
    val search_query: String = ""
)

data class FirestoreCityList(
    val items: List<FirestoreCity> = emptyList()
)


object CityMapper {

    fun FirestoreCity.toEntity(country: String) = CityEntity(
        name = name,
        searchQuery = search_query,
        country = country
    )

}
