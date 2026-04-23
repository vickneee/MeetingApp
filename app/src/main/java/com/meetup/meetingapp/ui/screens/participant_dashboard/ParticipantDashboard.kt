package com.meetup.meetingapp.ui.screens.participant_dashboard

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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
 * Navigation destination for the Participant Dashboard screen.
 */
object ParticipantDashboardDestination : NavigationDestination {
    override val route = "participant_dashboard_waiting"
    override val titleRes = R.string.title_participant_dashboard
    const val EVENTIDARG = "eventId"
    val routeWithArgs = "$route/{$EVENTIDARG}"
}

/**
 * Entry point composable for the Participant Dashboard screen.
 *
 * @param onBack Callback invoked when the user navigates back.
 * @param onVoteForRestaurantClick Callback invoked when the user clicks on the "Vote for a Time & Place" button.
 * @param onFinalPlanClick Callback invoked when the user clicks on the "View Final Plan" button.
 * @param onNavigateToHome Callback invoked when the user navigates to the home screen.
 * @param onFillAvailability Callback to navigate to availability flow.
 * @param viewModel The ViewModel providing event and submission data.
 */
@Composable
fun ParticipantDashboardPage(
    onBack: () -> Unit,
    onVoteForRestaurantClick: () -> Unit,
    onFinalPlanClick: (String) -> Unit,
    onNavigateToHome: () -> Unit,
    onFillAvailability: (String, String) -> Unit,
    viewModel: ParticipantDashboardViewModel =
        viewModel(
            factory = AppViewModelProvider.Factory,
        ),
) {
    val event by viewModel.event.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Re-check vote status every time screen resumes
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer =
            LifecycleEventObserver { _, lifecycleEvent ->
                if (lifecycleEvent == Lifecycle.Event.ON_RESUME) {
                    viewModel.fetchUserVote()
                }
            }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    event?.let {
        ParticipantDashboardContent(
            event = it,
            submissionsCount = uiState.submissionsCount,
            totalParticipants = uiState.totalParticipants,
            attendees = uiState.attendees,
            currentUserName = uiState.currentUserName,
            hasSubmittedAvailability = uiState.hasSubmittedAvailability,
            onBack = onBack,
            onVoteForRestaurantClick = onVoteForRestaurantClick,
            onFinalPlanClick = onFinalPlanClick,
            onNavigateToHome = onNavigateToHome,
            onFillAvailabilityClick = { onFillAvailability(it.eventCode, it.eventKey) },
        )
    } ?: LoadingScreen(modifier = Modifier.fillMaxSize())
}

/**
 * Main UI layout for the Participant Dashboard screen.
 *
 * @param event The event being displayed.
 * @param submissionsCount Number of participant submissions.
 * @param totalParticipants Total number of participants.
 * @param hasSubmittedAvailability Whether the user has shared their availability.
 * @param onBack Callback to navigate back.
 * @param onVoteForRestaurantClick Callback for navigating to restaurant voting.
 * @param onNavigateToHome Callback for navigating to the home screen.
 * @param onFillAvailabilityClick Callback to trigger availability submission.
 * @param modifier Optional modifier for layout customization.
 *
 * @see ParticipantDashboardViewModel ParticipantDashboardViewModel
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParticipantDashboardContent(
    event: Event,
    submissionsCount: Int,
    totalParticipants: Int,
    attendees: List<String>,
    currentUserName: String,
    hasSubmittedAvailability: Boolean,
    onBack: () -> Unit,
    onVoteForRestaurantClick: () -> Unit,
    onFinalPlanClick: (String) -> Unit,
    onNavigateToHome: () -> Unit,
    onFillAvailabilityClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            MeetingAppTopAppBar(
                title = "${stringResource(id = R.string.title_participant_dashboard)} / ${event.eventCode}",
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
            contentPadding = AppPadding.pagePadding,
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
                    )

                    Text(
                        buildAnnotatedString {
                            append("State: ")
                            withStyle(SpanStyle(fontWeight = FontWeight.SemiBold)) {
                                append(event.status.displayName)
                            }
                        },
                        style = MaterialTheme.typography.bodyLarge,
                    )

                    Text(
                        buildAnnotatedString {
                            append("Event Title: ")
                            withStyle(SpanStyle(fontWeight = FontWeight.SemiBold)) {
                                append(event.eventTitle)
                            }
                        },
                        style = MaterialTheme.typography.bodyLarge,
                    )

                    Text(
                        buildAnnotatedString {
                            append("Host: ")
                            withStyle(SpanStyle(fontWeight = FontWeight.SemiBold)) {
                                append(event.hostName)
                            }
                        },
                        style = MaterialTheme.typography.bodyLarge,
                    )

                    Text(
                        text = buildAnnotatedString {
                            append(if (totalParticipants > 0) "Place Votes: " else "Availability: ")
                            withStyle(
                                SpanStyle(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                append("$submissionsCount")
                            }
                            if (totalParticipants > 0) {
                                append(" / $totalParticipants")
                            }
                        },
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Spacer(modifier = Modifier.height(1.dp))
                }
            }

            items(attendees) { name ->
                ParticipantItemRow(name = name, modifier = Modifier.padding(start = 16.dp))
            }

            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Spacer(modifier = Modifier.height(AppSpacing.lg))

                    if (!hasSubmittedAvailability) {
                        Text(
                            "Please fill your availability.",
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    } else {
                        when (event.status) {
                            EventStatus.COLLECTING_AVAILABILITY -> {
                                Text(
                                    "Waiting for host to start",
                                    color = MaterialTheme.colorScheme.onSurface,
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.padding(top = 4.dp),
                                )
                                Spacer(modifier = Modifier.padding(AppSpacing.xxs))
                                Text(
                                    "the place voting...",
                                    color = MaterialTheme.colorScheme.onSurface,
                                    style = MaterialTheme.typography.bodyLarge,
                                )
                            }

                            EventStatus.FIRST_VOTING_CLOSED, EventStatus.COLLECTING_RESTAURANT_VOTES -> {

                                Text(
                                    text =
                                        buildAnnotatedString {
                                            if (currentUserName.isNotEmpty()) {
                                                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                                                    append(currentUserName)
                                                }
                                                append(", you can vote now.")
                                            } else {
                                                append("You can vote now.")
                                            }
                                        },
                                    color = MaterialTheme.colorScheme.onSurface,
                                    style = MaterialTheme.typography.bodyLarge,
                                )
                                Text(
                                    text = "Choose all options that suit you.",
                                    color = MaterialTheme.colorScheme.onSurface,
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }

                            EventStatus.FINALIZED -> {
                                Text(
                                    "The event has been finalized!",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(bottom = 4.dp),
                                )
                                Text(
                                    "Check the final plan.",
                                    color = MaterialTheme.colorScheme.onSurface,
                                    style = MaterialTheme.typography.bodyLarge,
                                )
                            }

                            else ->
                                Text(
                                    "Please wait...",
                                    color = MaterialTheme.colorScheme.onSurface,
                                    style = MaterialTheme.typography.bodyLarge,
                                )
                        }
                    }
                }
            }

            item {
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
                        enabled = hasSubmittedAvailability && ((event.status == EventStatus.FINALIZED) || (event.status != EventStatus.COLLECTING_AVAILABILITY)),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth(AppSize.lg),
                        contentPadding = PaddingValues(vertical = AppSpacing.md),
                    ) {
                        Text(
                            when {
                                !hasSubmittedAvailability -> "Vote Time & Place"
                                event.status == EventStatus.COLLECTING_AVAILABILITY -> "Voting Not Open"
                                event.status == EventStatus.FINALIZED -> "View Final Plan"
                                else -> "Vote Time & Place"
                            },
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }

                    if (!hasSubmittedAvailability) {
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
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
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
 * Preview for the [ParticipantDashboardContent] composable.
 */
@Preview(showBackground = true)
@Composable
fun ParticipantDashboardPreview() {
    MeetingAppTheme {
        ParticipantDashboardContent(
            event =
                Event(
                    eventCode = "A7F9K2",
                    status = EventStatus.COLLECTING_AVAILABILITY,
                    eventTitle = "Meet & Chat",
                    hostName = "Julia",
                ),
            submissionsCount = 4,
            totalParticipants = 0,
            attendees = listOf("Alice", "Bob"),
            currentUserName = "Julia",
            hasSubmittedAvailability = false,
            onBack = {},
            onFinalPlanClick = {},
            onVoteForRestaurantClick = {},
            onNavigateToHome = {},
            onFillAvailabilityClick = {},
        )
    }
}
