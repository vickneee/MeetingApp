package com.meetup.meetingapp.data.repositories


import com.meetup.meetingapp.data.model.*
import io.mockk.*

import org.junit.Test
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue

class EventRepositoryImpTest {

    private val repo = EventRepositoryImp(
        db = mockk(relaxed = true),
        userRepository = mockk(relaxed = true),
        eventDao = mockk(relaxed = true),
        cityDao = mockk(relaxed = true),
        participantResponseDao = mockk(relaxed = true),
        restaurantDao = mockk(relaxed = true),
        placesRepository = mockk(relaxed = true),
        auth = mockk(relaxed = true)
    )

    @Test
    fun `returns majority candidates for all categories`() {
        val responses = listOf(
            ParticipantResponse(
                dateTimes = listOf(
                    DateTime("2026-04-23", TimeSlot("11:00", "13:00")),
                    DateTime("2026-04-24", TimeSlot("13:00", "16:00"))
                ),
                locations = listOf("Helsinki", "Vantaa"),
                placeTypes = listOf(PlaceType.RESTAURANT),
                foodCategories = listOf(FoodCategory.SUSHI, FoodCategory.STEAK)
            ),
            ParticipantResponse(
                dateTimes = listOf(
                    DateTime("2026-04-23", TimeSlot("11:00", "13:00"))
                ),
                locations = listOf("Helsinki", "Espoo"),
                placeTypes = listOf(PlaceType.RESTAURANT, PlaceType.CAFE),
                foodCategories = listOf(FoodCategory.STEAK)
            )
        )

        val result = repo.aggregateCandidatesFromResponses(responses)

        assertEquals(
            listOf(DateTime("2026-04-23", TimeSlot("11:00", "13:00"))),
            result.dateTimeCandidates
        )
        assertEquals(listOf("Helsinki"), result.locationCandidates)
        assertEquals(listOf(PlaceType.RESTAURANT), result.placeTypeCandidates)
        assertEquals(listOf(FoodCategory.STEAK), result.foodCategoryCandidates)
    }

    @Test
    fun `returns all top candidates when counts are tied`() {
        val responses = listOf(
            ParticipantResponse(
                dateTimes = listOf(DateTime("2026-04-23", TimeSlot("11:00", "13:00"))),
                locations = listOf("Helsinki"),
                placeTypes = listOf(PlaceType.RESTAURANT),
                foodCategories = listOf(FoodCategory.SUSHI)
            ),
            ParticipantResponse(
                dateTimes = listOf(DateTime("2026-04-24", TimeSlot("13:00", "16:00"))),
                locations = listOf("Espoo"),
                placeTypes = listOf(PlaceType.CAFE),
                foodCategories = listOf(FoodCategory.STEAK)
            )
        )

        val result = repo.aggregateCandidatesFromResponses(responses)

        assertEquals(
            listOf(
                DateTime("2026-04-23", TimeSlot("11:00", "13:00")),
                DateTime("2026-04-24", TimeSlot("13:00", "16:00"))
            ),
            result.dateTimeCandidates
        )
        assertEquals(listOf("Helsinki", "Espoo"), result.locationCandidates)
        assertEquals(listOf(PlaceType.RESTAURANT, PlaceType.CAFE), result.placeTypeCandidates)
        assertEquals(listOf(FoodCategory.SUSHI, FoodCategory.STEAK), result.foodCategoryCandidates)
    }

    @Test
    fun `returns candidates from single participant`() {
        val responses = listOf(
            ParticipantResponse(
                dateTimes = listOf(DateTime("2026-04-23", TimeSlot("11:00", "13:00"))),
                locations = listOf("Helsinki"),
                placeTypes = listOf(PlaceType.RESTAURANT),
                foodCategories = listOf(FoodCategory.SUSHI)
            )
        )

        val result = repo.aggregateCandidatesFromResponses(responses)

        assertEquals(
            listOf(DateTime("2026-04-23", TimeSlot("11:00", "13:00"))),
            result.dateTimeCandidates
        )
        assertEquals(listOf("Helsinki"), result.locationCandidates)
        assertEquals(listOf(PlaceType.RESTAURANT), result.placeTypeCandidates)
        assertEquals(listOf(FoodCategory.SUSHI), result.foodCategoryCandidates)
    }

    @Test
    fun `pickWinningPlace returns the only winner`() {
        val result = repo.pickWinningPlace(mapOf("A" to 5, "B" to 2))
        assertEquals("A", result)
    }

    @Test
    fun `pickWinningPlace returns one of tied winners`() {
        val winners = mutableSetOf<String>()
        repeat(20) {
            winners += repo.pickWinningPlace(mapOf("A" to 3, "B" to 3))
        }
        assertTrue(winners.all { it == "A" || it == "B" })
    }

    @Test(expected = IllegalArgumentException::class)
    fun `pickWinningPlace throws when no votes exist`() {
        repo.pickWinningPlace(emptyMap())
    }


    @Test
    fun `pickWinningTime returns the clear winner`() {
        val t1 = DateTime("2026-04-23", TimeSlot("11:00", "13:00"))
        val t2 = DateTime("2026-04-24", TimeSlot("13:00", "16:00"))

        val votes = listOf(
            Vote(dateTime = t1),
            Vote(dateTime = t1),
            Vote(dateTime = t1),
            Vote(dateTime = t2)
        )

        repeat(10) {
            val result = repo.pickWinningTime(votes)
            assertEquals(t1, result)
        }
    }


    @Test
    fun `pickWinningTime returns one of tied times`() {
        val t1 = DateTime("2026-04-23", TimeSlot("11:00", "13:00"))
        val t2 = DateTime("2026-04-24", TimeSlot("13:00", "16:00"))

        val votes = listOf(
            Vote(dateTime = t1),
            Vote(dateTime = t1),
            Vote(dateTime = t2),
            Vote(dateTime = t2)
        )

        val results = (1..20).map { repo.pickWinningTime(votes) }
        assertTrue(results.all { it == t1 || it == t2 })
    }

    @Test(expected = IllegalArgumentException::class)
    fun `pickWinningTime throws when no timings exist`() {
        repo.pickWinningTime(emptyList())
    }

    @Test
    fun `resolveUserName returns participant name when available`() {
        val response = ParticipantResponse(name = "Alice", userId = "user1")
        val event = Event(hostId = "host1", hostName = "HostUser")

        val result = repo.resolveUserName(
            participantResponse = response,
            event = event,
            currentUserName = null,
            userId = "user1"
        )

        assertEquals("Alice", result)
    }

    @Test
    fun `resolveUserName returns hostName when user is host`() {
        val response = ParticipantResponse(name = "", userId = "host1")
        val event = Event(hostId = "host1", hostName = "HostUser")

        val result = repo.resolveUserName(
            participantResponse = response,
            event = event,
            currentUserName = null,
            userId = "host1"
        )

        assertEquals("HostUser", result)
    }

    @Test
    fun `resolveUserName returns currentUserName when participant and host names unavailable`() {
        val event = Event(hostId = "host1", hostName = "HostUser")

        val result = repo.resolveUserName(
            participantResponse = null,
            event = event,
            currentUserName = "FirebaseUser",
            userId = "user1"
        )

        assertEquals("FirebaseUser", result)
    }

    @Test
    fun `resolveUserName returns Unknown when no name sources available`() {
        val event = Event(hostId = "host1", hostName = "HostUser")

        val result = repo.resolveUserName(
            participantResponse = null,
            event = event,
            currentUserName = null,
            userId = "user1"
        )

        assertEquals("Unknown", result)
    }
}

