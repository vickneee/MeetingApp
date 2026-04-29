package com.meetup.meetingapp.ui.screens.eventlist

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.meetup.meetingapp.MeetingAppTopAppBar
import com.meetup.meetingapp.R
import com.meetup.meetingapp.data.model.DateRange
import com.meetup.meetingapp.data.model.Event
import com.meetup.meetingapp.data.model.EventStatus
import com.meetup.meetingapp.ui.AppViewModelProvider
import com.meetup.meetingapp.ui.navigation.NavigationDestination
import com.meetup.meetingapp.ui.screens.eventcreation.EventViewModel
import com.meetup.meetingapp.ui.screens.eventcreation.LoadingScreen
import com.meetup.meetingapp.utils.toEuroDate
import java.time.LocalDate

/**
 * Navigation destination for the Past Events screen.
 */
object EventListDestination : NavigationDestination {
    override val route = "event-list"
    override val titleRes = R.string.title_event_list_page
}

/**
 * Past Events Page
 * @param onBack Navigate back
 * @param onNavigateToHostDashboard Navigate to the Host Dashboard
 * @param onNavigateToParticipantDashboard Navigate to the Participant Dashboard
 * @param currentUserId The current user's ID
 * @param modifier Modifier for the page
 * @param viewModel [EventViewModel] to retrieve all items in the Room database.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventsListPage(
    onBack: () -> Unit,
    onNavigateToHostDashboard: (eventId: String) -> Unit,
    onNavigateToParticipantDashboard: (eventId: String) -> Unit,
    currentUserId: String,
    modifier: Modifier = Modifier,
    viewModel: EventViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val events by viewModel.events.collectAsStateWithLifecycle()
    val isLoading by viewModel.isEventsLoading.collectAsStateWithLifecycle()
    val sortedEvents = events.sortedByDescending { it.createdAt }

    Scaffold(
        topBar = {
            MeetingAppTopAppBar(
                title = stringResource(id = R.string.title_event_list_page),
                canNavigateBack = true,
                navigateUp = onBack,
            )
        },
    ) { paddingValues ->
        Box(
            modifier =
                modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(paddingValues),
        ) {
            Crossfade(targetState = isLoading, label = "event_list_loading") { loading ->
                if (loading) {
                    LoadingScreen(modifier = Modifier.fillMaxSize())
                } else {
                    if (sortedEvents.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "No events found.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding =
                                PaddingValues(
                                    start = 32.dp,
                                    end = 32.dp,
                                    top = 32.dp,
                                    bottom = 32.dp,
                                ),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            items(sortedEvents) { event ->
                                EventItem(
                                    event = event,
                                    onItemClick = {
                                        if (event.hostId == currentUserId) {
                                            onNavigateToHostDashboard(event.id)
                                        } else {
                                            onNavigateToParticipantDashboard(event.id)
                                        }
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Event Item in the Past Events Page
 * @param event The event to display
 * @param onItemClick Callback to invoke when the event is clicked
 */
@Composable
fun EventItem(
    event: Event,
    onItemClick: (Event) -> Unit = {},
) {
    val labelText =
        if (event.status == EventStatus.FINALIZED && event.finalTime != null) {
            "${event.eventCode} / ${event.finalTime.date.toEuroDate()}"
        } else {
            "${event.eventCode} / Ongoing"
        }

    Card(
        onClick = { onItemClick(event) },
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
        shape = RoundedCornerShape(16.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Text(
            text = labelText,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 20.dp, horizontal = 16.dp),
            textAlign = TextAlign.Center,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

/**
 * Preview for the [EventsListPage] composable.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun EventsListPagePreview() {
    val events =
        listOf(
            Event(
                eventCode = "A7F9K2",
                eventKey = "48392",
                hostId = "user123",
                id = "event1",
                status = EventStatus.CREATED,
                eventTitle = "Team Lunch",
                hostName = "John Doe",
                dateRange =
                    DateRange(
                        LocalDate.now().toString(),
                        LocalDate.now().plusDays(7).toString(),
                    ),
            ),
            Event(
                eventCode = "D1L4P7",
                eventKey = "12345",
                hostId = "user123",
                id = "event2",
                status = EventStatus.COLLECTING_AVAILABILITY,
                eventTitle = "Team Dinner",
                hostName = "John Doe",
                dateRange =
                    DateRange(
                        LocalDate.now().toString(),
                        LocalDate.now().plusDays(7).toString(),
                    ),
            ),
        )

    Scaffold(
        topBar = {
            MeetingAppTopAppBar(
                title = "Event List",
                canNavigateBack = true,
                navigateUp = {},
            )
        },
    ) { paddingValues ->
        LazyColumn(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
            contentPadding =
                PaddingValues(
                    start = 32.dp,
                    end = 32.dp,
                    top = 32.dp,
                    bottom = 32.dp,
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            items(events) { event ->
                EventItem(event = event)
            }
        }
    }
}
