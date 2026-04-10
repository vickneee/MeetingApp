package com.meetup.meetingapp.ui.screens.participant_dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
 * Navigation destination for the Participant Dashboard screen.
 *
 * @property route The route for navigating to this destination.
 * @property titleRes The resource ID for the title to be displayed on the screen.
 * @property eventIdArg The argument representing the event ID.
 * @property routeWithArgs The route with the eventId argument.
 */
object ParticipantDashboardDestination : NavigationDestination {
    override val route = "participant_dashboard_waiting"
    override val titleRes = R.string.title_participant_dashboard_waiting
    const val eventIdArg = "eventId"
    val routeWithArgs = "$route/{$eventIdArg}"
}

/**
 * Entry point composable for the Participant Dashboard screen.
 *
 * This composable:
 * - Retrieves the ParticipantDashboardViewModel instance.
 * - Collects event data, UI state, and close-voting state from the ViewModel.
 * - Displays a loading screen until the event is available.
 * - Delegates UI rendering to [ParticipantDashboardContent].
 *
 * @param onBack Callback invoked when the user navigates back.
 * @param onNavigateToChooseDatePage Callback invoked when the user navigates to the choose date page.
 * @param onNavigateToHome Callback invoked when the user navigates to the home screen.
 * @param viewModel The ViewModel providing event and submission data.
 *
 * @see ParticipantDashboardViewModel ParticipantDashboardViewModel
 */
@Composable
fun ParticipantDashboardPage(
    onBack: () -> Unit,
    onNavigateToChooseDatePage: () -> Unit,
    onNavigateToHome: () -> Unit,
    viewModel: ParticipantDashboardViewModel = viewModel(
        factory = AppViewModelProvider.Factory
    )
) {
    val event by viewModel.event.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    event?.let {
        ParticipantDashboardContent(
            event = it,
            submissionsCount = uiState.submissionsCount,
            onBack = onBack,
            onVoteForRestaurantClick = onNavigateToChooseDatePage,
            onNavigateToHome = onNavigateToHome
        )
    } ?: LoadingScreen(modifier = Modifier.fillMaxSize())
}

/**
 * Main UI layout for the Participant Dashboard screen.
 *
 * @param event The event being displayed.
 * @param submissionsCount Number of participant submissions.
 * @param onBack Callback to navigate back.
 * @param onVoteForRestaurantClick Callback for navigating to restaurant voting.
 * @param onNavigateToHome Callback for navigating to the home screen.
 * @param modifier Optional modifier for layout customization.
 *
 * @see ParticipantDashboardViewModel ParticipantDashboardViewModel
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParticipantDashboardContent(
    event: Event,
    submissionsCount: Int,
    onBack: () -> Unit,
    onVoteForRestaurantClick: () -> Unit,
    onNavigateToHome: () -> Unit,
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
                    }, fontSize = 20.sp)

                    Text(buildAnnotatedString {
                        append("State: ")
                        withStyle(SpanStyle(fontWeight = FontWeight.SemiBold)) {
                            append(event.status.displayName)
                        }
                    }, fontSize = 20.sp)

                    Text(buildAnnotatedString {
                        append("Event Title: ")
                        withStyle(SpanStyle(fontWeight = FontWeight.SemiBold)) {
                            append(event.eventTitle)
                        }
                    }, fontSize = 20.sp)

                    Text(buildAnnotatedString {
                        append("Host: ")
                        withStyle(SpanStyle(fontWeight = FontWeight.SemiBold)) {
                            append(event.hostName)
                        }
                    }, fontSize = 20.sp)
                }

                Spacer(modifier = Modifier.padding(24.dp))

                Text(
                    text = "Submissions: $submissionsCount",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.padding(36.dp))
                    if (event.status != EventStatus.FIRST_VOTING_CLOSED) {
                        Text(
                            text = "Waiting for host to close",
                            fontSize = 22.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                        Spacer(modifier = Modifier.padding(4.dp))
                        Text(
                            "the voting...",
                            fontSize = 22.sp
                        )
                    } else {
                        Text(
                            text = "Host has closed the voting!",
                            fontSize = 22.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                        Text(
                            "You can now vote.",
                            fontSize = 22.sp
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.padding(16.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = onVoteForRestaurantClick,
                        enabled = event.status == EventStatus.FIRST_VOTING_CLOSED,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                    ) {

                        Text(
                            "Vote for Time & Area",
                            fontSize = 18.sp,
                            modifier = Modifier.padding(6.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(36.dp))

                    OutlinedButton(
                        onClick = onNavigateToHome,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                    ) {
                        Text(
                            "Home",
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(6.dp)
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ParticipantDashboardPreview() {
    ParticipantDashboardContent(
        event = Event(
            eventCode = "A7F9K2",
            eventTitle = "Meet & Chat",
            hostName = "Julia",
        ),
        submissionsCount = 4,
        onBack = {},
        onVoteForRestaurantClick = {},
        onNavigateToHome = {}
    )
}
