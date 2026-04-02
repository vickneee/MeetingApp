package com.meetup.meetingapp.ui.screens.host_dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.meetup.meetingapp.MeetingAppTopAppBar
import com.meetup.meetingapp.R
import com.meetup.meetingapp.ui.AppViewModelProvider
import com.meetup.meetingapp.ui.navigation.NavigationDestination
import com.meetup.meetingapp.ui.screens.event_created_page.LoadingScreen

/**
 * Navigation destination for the Host Dashboard screen.
 */
object HostDashboardDestination : NavigationDestination {
    override val route = "host_dashboard"
    override val titleRes = R.string.title_host_dashboard
    const val eventIdArg = "eventId"
    val routeWithArgs = "$route/{$eventIdArg}"
}

/**
 * Host Dashboard Page
 * @param onBack Navigate back
 * @param viewModel [HostDashboardViewModel] to retrieve event data and attendee list.
 */
@Composable
fun HostDashboardPage(
    onBack: () -> Unit,
    viewModel: HostDashboardViewModel = viewModel(
        factory = AppViewModelProvider.Factory
    )
) {
    val event by viewModel.event.collectAsStateWithLifecycle()

    event?.let {
    HostDashboardContent(
        eventCode = it.eventCode,
        eventTitle = it.eventTitle,
        hostName = it.hostName,
        submissionsCount = 0,
        attendees = emptyList(),
        onBack = onBack,
        onCloseVotingClick = viewModel::closeVoting
    )
    } ?: LoadingScreen(modifier = Modifier.fillMaxSize())
}

/**
 * Host Dashboard Content
 * @param eventCode The unique code for the event
 * @param eventTitle The title of the event
 * @param hostName The name of the host
 * @param submissionsCount Total number of participants
 * @param attendees List of participant names
 * @param onBack Navigate back
 * @param onCloseVotingClick Action to finalize the event
 * @param modifier Modifier
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HostDashboardContent(
    eventCode: String,
    eventTitle: String,
    hostName: String,
    submissionsCount: Int,
    attendees: List<String>,
    onBack: () -> Unit,
    onCloseVotingClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            MeetingAppTopAppBar(
                title = "Dashboard / $eventCode",
                canNavigateBack = true,
                navigateUp = onBack
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .padding(horizontal = 48.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Top
        ) {
            item {
                Spacer(modifier = Modifier.padding(24.dp))

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(text = "Event Code: $eventCode", fontSize = 20.sp)
                    Text(text = "Event Title: $eventTitle", fontSize = 20.sp)
                    Text(text = "Host: $hostName", fontSize = 20.sp)
                }

                Spacer(modifier = Modifier.padding(24.dp))

                Text(
                    text = "Submissions: $submissionsCount",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.padding(20.dp))

                Text(
                    text = "Submitted by:",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.padding(4.dp))
            }

            items(attendees) { name ->
                Text(
                    text = "•  $name",
                    fontSize = 20.sp,
                    modifier = Modifier.padding(start = 16.dp, top = 4.dp, bottom = 4.dp)
                )
            }

            item {
                Spacer(modifier = Modifier.padding(32.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(
                        onClick = onCloseVotingClick,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth(0.8f)
                    ) {
                        Text(
                            text = "Close Voting",
                            fontSize = 20.sp,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HostDashboardPreview() {
    HostDashboardContent(
        eventCode = "A7F9K2",
        eventTitle = "Meet & Chat",
        hostName = "Julia",
        submissionsCount = 4,
        attendees = listOf("Alice", "Bob", "Charlie", "Diana"),
        onBack = {},
        onCloseVotingClick = {}
    )
}