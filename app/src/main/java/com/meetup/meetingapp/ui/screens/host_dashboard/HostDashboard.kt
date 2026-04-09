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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.meetup.meetingapp.MeetingAppTopAppBar
import com.meetup.meetingapp.R
import com.meetup.meetingapp.data.model.Event
import com.meetup.meetingapp.data.model.EventStatus
import com.meetup.meetingapp.ui.AppViewModelProvider
import com.meetup.meetingapp.ui.navigation.NavigationDestination
import com.meetup.meetingapp.ui.screens.create_event_flow.LoadingScreen

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
 * Entry point composable for the Host Dashboard screen.
 *
 * This composable:
 * - Retrieves the HostDashboardViewModel instance.
 * - Collects event data, UI state, and close-voting state from the ViewModel.
 * - Displays a loading screen until the event is available.
 * - Delegates UI rendering to [HostDashboardContent].
 *
 * @param onBack Callback invoked when the user navigates back.
 * @param onVoteForRestaurantClick Callback invoked when the user selects
 *        "Vote for Restaurant".
 * @param viewModel The ViewModel providing event and submission data.
 */

@Composable
fun HostDashboardPage(
    onBack: () -> Unit,
    onVoteForRestaurantClick: () -> Unit,
    viewModel: HostDashboardViewModel = viewModel(
        factory = AppViewModelProvider.Factory
    )
) {
    val event by viewModel.event.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val closeVotingState by viewModel.closeVotingState.collectAsStateWithLifecycle()

    event?.let {
    HostDashboardContent(
        event = it,
        submissionsCount = uiState.submissionsCount,
        attendees = uiState.attendees,
        onBack = onBack,

        closeVotingState = closeVotingState,
        onCloseVotingClick = viewModel::closeVoting,
        onVoteForRestaurantClick = onVoteForRestaurantClick
    )
    } ?: LoadingScreen(modifier = Modifier.fillMaxSize())
}

/**
 * Main UI layout for the Host Dashboard screen.
 *
 * This composable displays:
 * - Event metadata (code, title, host, status)
 * - Submission count and attendee list
 * - Actions for voting on restaurants and closing the voting phase
 *
 * The "Close Voting" button is enabled or disabled based on:
 * - The event's current status (cannot close again once FIRST_VOTING_CLOSED)
 * - The in-progress state of the close-voting operation
 *
 * @param event The event being displayed.
 * @param submissionsCount Number of participant submissions.
 * @param attendees List of participant names who submitted availability.
 * @param onBack Callback to navigate back.
 * @param closeVotingState UI state for the close-voting action.
 * @param onVoteForRestaurantClick Callback for navigating to restaurant voting.
 * @param onCloseVotingClick Callback to trigger the close-voting operation.
 * @param modifier Optional modifier for layout customization.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HostDashboardContent(
    event: Event,
    submissionsCount: Int,
    attendees: List<String>,
    onBack: () -> Unit,
    closeVotingState: CloseVotingState,
    onVoteForRestaurantClick: () -> Unit,
    onCloseVotingClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            MeetingAppTopAppBar(
                title = "Dashboard / ${event.eventCode}",
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

                    Text(buildAnnotatedString {
                        append("Event Code: ")
                        withStyle(SpanStyle(fontWeight = FontWeight.SemiBold)) {
                            append(event.eventCode)
                        }
                    },
                        fontSize = 20.sp
                    )

                    Text(buildAnnotatedString {
                        append("State: ")
                        withStyle(SpanStyle(fontWeight = FontWeight.SemiBold)) {
                            append(event.status.displayName)
                        }
                    },
                        fontSize = 20.sp
                    )

                    Text(
                        buildAnnotatedString {
                            append("Event Title: ")
                            withStyle(SpanStyle(fontWeight = FontWeight.SemiBold)) {
                                append(event.eventTitle)
                            }
                        },
                        fontSize = 20.sp
                    )

                    Text(
                        buildAnnotatedString {
                            append("Host: ")
                            withStyle(SpanStyle(fontWeight = FontWeight.SemiBold)) {
                                append(event.hostName)
                            }
                        },
                        fontSize = 20.sp
                    )
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
                    text = "• $name",
                    fontSize = 20.sp,
                    modifier = Modifier.padding(start = 16.dp, top = 4.dp, bottom = 4.dp)
                )
            }

            item {
                val buttonText = when (closeVotingState) {
                    CloseVotingState.Success -> "Voting Closed"
                    else -> "Close Voting"
                }

                Spacer(modifier = Modifier.padding(32.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = onVoteForRestaurantClick,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth(0.8f)
                    ) {
                        Text(
                            "Vote for Restaurant",
                            fontSize = 18.sp,
                            modifier = Modifier.padding(8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = onCloseVotingClick,
                        enabled = event.status != EventStatus.FIRST_VOTING_CLOSED
                                && closeVotingState != CloseVotingState.Loading,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth(0.8f)
                    ) {
                        Text(
                            text = buttonText,
                            fontSize = 18.sp,
                            modifier = Modifier.padding(vertical = 6.dp)
                        )
                    }

                    if (closeVotingState is CloseVotingState.Error) {
                        Text(
                            text = closeVotingState.error.message ?: "Unknown error, retry close voting",
                            color = Color.Red,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(top = 12.dp)
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
        event = Event(
            eventCode = "A7F9K2",
            eventTitle = "Meet & Chat",
            hostName = "Julia",
        ),
        submissionsCount = 4,
        attendees = listOf("Alice", "Bob", "Charlie", "Diana"),
        onBack = {},
        closeVotingState = CloseVotingState.Idle,
        onVoteForRestaurantClick = {},
        onCloseVotingClick = {}
    )
}