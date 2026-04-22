package com.meetup.meetingapp.ui.screens.host_dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.meetup.meetingapp.MeetingAppTopAppBar
import com.meetup.meetingapp.R
import com.meetup.meetingapp.data.model.Event
import com.meetup.meetingapp.data.model.EventStatus
import com.meetup.meetingapp.ui.AppViewModelProvider
import com.meetup.meetingapp.ui.navigation.NavigationDestination
import com.meetup.meetingapp.ui.screens.components.ParticipantItemRow
import com.meetup.meetingapp.ui.screens.create_event_flow.LoadingScreen
import com.meetup.meetingapp.ui.theme.AppPadding
import com.meetup.meetingapp.ui.theme.AppSize
import com.meetup.meetingapp.ui.theme.AppSpacing
import com.meetup.meetingapp.ui.theme.MeetingAppTheme

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
 * @param onFinalPlanClick Callback invoked when the user clicks on the "Final Plan" button.
 * @param onNavigateToHome Callback invoked when the user navigates to the home screen.
 * @param viewModel The ViewModel providing event and submission data.
 */
@Composable
fun HostDashboardPage(
    onBack: () -> Unit,
    onVoteForRestaurantClick: () -> Unit,
    onFinalPlanClick: (String) -> Unit,
    onFillAvailability: (String, String) -> Unit,
    onNavigateToHome: () -> Unit,
    viewModel: HostDashboardViewModel =
        viewModel(
            factory = AppViewModelProvider.Factory,
        ),
) {
    val event by viewModel.event.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val closeVotingState by viewModel.closeVotingState.collectAsStateWithLifecycle()
    val hasVoted = uiState.hasVoted
    val hasHostSubmittedAvailability = uiState.hasHostSubmittedAvailability

    // Re-check vote status every time screen resumes
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer =
            LifecycleEventObserver { _, lifecycleEvent ->
                if (lifecycleEvent == Lifecycle.Event.ON_RESUME) {
                    viewModel.fetchUserVote()
                    viewModel.checkHostAvailability()
                }
            }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    event?.let {
        HostDashboardContent(
            event = it,
            submissionsCount = uiState.submissionsCount,
            attendees = uiState.attendees,
            hasVoted = hasVoted,
            hasAnyRestaurantVotes = uiState.hasAnyRestaurantVotes,
            onBack = onBack,
            closeVotingState = closeVotingState,
            onCloseVotingClick = { status ->
                if (status == EventStatus.FIRST_VOTING_CLOSED) {
                    viewModel.closeVoting()
                } else {
                    viewModel.updateEventStatus(status)
                }
            },
            onVoteForRestaurantClick = onVoteForRestaurantClick,
            onFinalPlanClick = onFinalPlanClick,
            hasHostSubmittedAvailability = hasHostSubmittedAvailability,
            onFillAvailabilityClick = { onFillAvailability(it.eventCode, it.eventKey) },
            onNavigateToHome = onNavigateToHome,
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
 * @param hasVoted Whether the user has voted in the current phase.
 * @param hasAnyRestaurantVotes Whether any restaurant votes have been cast.
 * @param onBack Callback to navigate back.
 * @param closeVotingState UI state for the close-voting action.
 * @param onFinalPlanClick Callback to trigger the final plan generation.
 * @param onCloseVotingClick Callback to trigger the close-voting operation.
 * @param onNavigateToHome Callback to navigate to the home screen.
 * @param hasHostSubmittedAvailability Whether the host has submitted availability.
 * @param onFillAvailabilityClick Callback to trigger the availability submission.
 * @param modifier Optional modifier for layout customization.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HostDashboardContent(
    event: Event,
    submissionsCount: Int,
    attendees: List<String>,
    hasVoted: Boolean,
    hasAnyRestaurantVotes: Boolean,
    onBack: () -> Unit,
    closeVotingState: CloseVotingState,
    onVoteForRestaurantClick: () -> Unit,
    onFinalPlanClick: (String) -> Unit,
    onCloseVotingClick: (EventStatus) -> Unit,
    onNavigateToHome: () -> Unit,
    hasHostSubmittedAvailability: Boolean,
    onFillAvailabilityClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            MeetingAppTopAppBar(
                title = "${stringResource(id = R.string.title_host_dashboard)} / ${event.eventCode}",
                canNavigateBack = true,
                navigateUp = onBack,
            )
        },
    ) { paddingValues ->
        LazyColumn(
            modifier =
                modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(paddingValues),
            contentPadding = AppPadding.pagePadding, // Padding values for the entire screen
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
        ) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(AppSize.lg),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        buildAnnotatedString {
                            append("Event Code: ")
                            withStyle(SpanStyle(fontWeight = FontWeight.SemiBold)) {
                                append(event.eventCode)
                            }
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )

                    Text(
                        buildAnnotatedString {
                            append("State: ")
                            withStyle(SpanStyle(fontWeight = FontWeight.SemiBold)) {
                                append(event.status.displayName)
                            }
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )

                    Text(
                        buildAnnotatedString {
                            append("Event Title: ")
                            withStyle(SpanStyle(fontWeight = FontWeight.SemiBold)) {
                                append(event.eventTitle)
                            }
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )

                    Text(
                        buildAnnotatedString {
                            append("Host: ")
                            withStyle(SpanStyle(fontWeight = FontWeight.SemiBold)) {
                                append(event.hostName)
                            }
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(modifier = Modifier.height(AppSpacing.sm))
                    Text(
                        text = "Submissions: $submissionsCount",
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Spacer(modifier = Modifier.height(AppSpacing.xxs))
                }
            }

            // List of attendees
            items(attendees) { name ->
                ParticipantItemRow(name = name, modifier = Modifier.padding(start = 16.dp))
            }

            item {
                val buttonText =
                    when (event.status) {
                        EventStatus.COLLECTING_AVAILABILITY -> "Start Place Voting"
                        EventStatus.FIRST_VOTING_CLOSED -> "First Voting Closed"
                        EventStatus.RESTAURANT_CANDIDATES_GENERATED -> "Start Place Voting"
                        EventStatus.COLLECTING_RESTAURANT_VOTES -> "Close Place Voting"
                        EventStatus.FINALIZED -> "Event Finalized"
                        else -> null
                    }

                val nextStatus =
                    when (event.status) {
                        EventStatus.COLLECTING_AVAILABILITY -> EventStatus.FIRST_VOTING_CLOSED
                        EventStatus.RESTAURANT_CANDIDATES_GENERATED -> EventStatus.COLLECTING_RESTAURANT_VOTES
                        EventStatus.COLLECTING_RESTAURANT_VOTES -> EventStatus.FINALIZED
                        else -> null
                    }

                // Close Voting button enabled logic
                val buttonEnabled =
                    when (event.status) {
                        EventStatus.COLLECTING_AVAILABILITY -> true
                        EventStatus.RESTAURANT_CANDIDATES_GENERATED -> false
                        EventStatus.COLLECTING_RESTAURANT_VOTES -> hasAnyRestaurantVotes
                        else -> false
                    } &&
                        closeVotingState != CloseVotingState.Loading

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Spacer(modifier = Modifier.height(AppSpacing.lg))

                    Button(
                        onClick = {
                            if (event.status == EventStatus.FINALIZED) {
                                event.finalPlace?.let { onFinalPlanClick(it) }
                            } else {
                                onVoteForRestaurantClick()
                            }
                        },
                        enabled = (event.status == EventStatus.FINALIZED) || (event.status != EventStatus.COLLECTING_AVAILABILITY),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth(AppSize.lg),
                        contentPadding = PaddingValues(vertical = AppSpacing.md),
                    ) {
                        Text(
                            when {
                                event.status == EventStatus.COLLECTING_AVAILABILITY -> "Voting Not Open"
                                event.status == EventStatus.FINALIZED -> "View Final Plan"
                                else -> "Vote Time & Place"
                            },
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }

                    Spacer(modifier = Modifier.height(AppSpacing.lg))

                    if (buttonText != null) {
                        Button(
                            onClick = {
                                nextStatus?.let {
                                    onCloseVotingClick(it)
                                }
                            },
                            enabled = buttonEnabled && nextStatus != null,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth(AppSize.lg),
                            contentPadding = PaddingValues(vertical = AppSpacing.sm),
                        ) {
                            Text(
                                text = buttonText,
                                style = MaterialTheme.typography.labelLarge,
                            )
                        }
                    }

                    if (closeVotingState is CloseVotingState.Error) {
                        Text(
                            text =
                                closeVotingState.error.message
                                    ?: "Unknown error, retry close voting",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(top = 12.dp),
                            textAlign = TextAlign.Start,
                        )
                    }

                    if (!hasHostSubmittedAvailability && event.status == EventStatus.COLLECTING_AVAILABILITY) {
                        Spacer(modifier = Modifier.height(AppSpacing.lg))
                        Button(
                            onClick = onFillAvailabilityClick,
                            modifier = Modifier.fillMaxWidth(AppSize.lg),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(vertical = AppSpacing.md),
                        ) {
                            Text(
                                "Fill My Availability",
                                style = MaterialTheme.typography.labelLarge,
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(AppSpacing.lg))
                    OutlinedButton(
                        onClick = onNavigateToHome,
                        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth(AppSize.lg),
                        contentPadding = PaddingValues(vertical = AppSpacing.md),
                    ) {
                        Text(
                            "Home",
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                }
            }
        }
    }
}

/**
 * Preview for the [HostDashboardContent] composable.
 */
@Preview(showBackground = true)
@Composable
fun HostDashboardPreview() {
    MeetingAppTheme {
        HostDashboardContent(
            event =
                Event(
                    eventCode = "A7F9K2",
                    status = EventStatus.COLLECTING_AVAILABILITY,
                    eventTitle = "Meet & Chat",
                    hostName = "Julia",
                ),
            submissionsCount = 4,
            attendees = listOf("Alice", "Bob", "Diana"),
            hasVoted = false,
            hasAnyRestaurantVotes = false,
            onBack = {},
            closeVotingState = CloseVotingState.Success,
            onFinalPlanClick = {},
            onVoteForRestaurantClick = {},
            onCloseVotingClick = {},
            hasHostSubmittedAvailability = false,
            onFillAvailabilityClick = {},
            onNavigateToHome = {},
        )
    }
}
