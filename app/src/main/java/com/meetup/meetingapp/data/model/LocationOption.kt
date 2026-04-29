package com.meetup.meetingapp.data.model

/**
 * Represents the location settings selected for an event.
 *
 * This model stores:
 * - A list of allowed countries
 * - A region or broader area name
 * - A list of specific cities chosen by the host or participants
 *
 * These values are used when filtering restaurants, narrowing search areas,
 * or defining the geographic scope of an event.
 *
 * @property countries List of country codes or names included in the selection.
 * @property region A region or area name (e.g., "Uusimaa", "Kansai").
 * @property cities List of city names included in the selection.
 */
data class LocationOption(
    val countries: List<String> = listOf(),
    val region: String = "",
    val cities: List<String> = listOf(),
)
