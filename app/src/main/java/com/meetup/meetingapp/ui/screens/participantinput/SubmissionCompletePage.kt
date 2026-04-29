package com.meetup.meetingapp.ui.screens.participantinput

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.meetup.meetingapp.R
import com.meetup.meetingapp.ui.navigation.NavigationDestination
import com.meetup.meetingapp.ui.screens.eventcreation.ErrorScreen
import com.meetup.meetingapp.ui.screens.eventcreation.LoadingScreen
import com.meetup.meetingapp.ui.theme.AppPadding
import com.meetup.meetingapp.ui.theme.AppSize
import com.meetup.meetingapp.ui.theme.AppSpacing
import com.meetup.meetingapp.ui.theme.MeetingAppTheme

/**
 * Navigation destination for the Submission Complete screen.
 */
object SubmissionCompleteDestination : NavigationDestination {
    override val route = "submission-complete"
    override val titleRes = R.string.title_submission_complete
}

/**
 * Entry point screen shown after a participant successfully submits their
 * availability and preferences.
 *
 * Observes the submission state from [ParticipantViewModel] and displays:
 * - a loading screen while submission is in progress,
 * - a success screen when submission completes,
 * - an error screen with retry support if submission fails.
 *
 * @param viewModel The [ParticipantViewModel] providing submission state.
 * @param onNavigateToHostDashboard Callback invoked when navigating to the host dashboard.
 * @param onNavigateToParticipantDashboard Callback invoked when navigating to the participant dashboard.
 * @param modifier Modifier for styling.
 */
@Composable
fun SubmissionCompletePage(
    viewModel: ParticipantViewModel,
    onHomeClick: () -> Unit,
    onNavigateToHostDashboard: (String) -> Unit,
    onNavigateToParticipantDashboard: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val submitState by viewModel.submitState.collectAsStateWithLifecycle()
    val isHost by viewModel.isHost.collectAsStateWithLifecycle(false)
    val event by viewModel.event.collectAsStateWithLifecycle(null)

    Crossfade(targetState = submitState, label = "submission_complete_loading") { state ->
        when (state) {
            is SubmitState.Idle, is SubmitState.Success ->
                SubmissionCompleteContent(
                    isHost = isHost,
                    eventId = event?.id,
                    onHomeClick = onHomeClick,
                    onNavigateToHostDashboard = onNavigateToHostDashboard,
                    onNavigateToParticipantDashboard = onNavigateToParticipantDashboard,
                    modifier = modifier,
                )

            is SubmitState.Loading -> LoadingScreen(modifier = Modifier.fillMaxSize())

            is SubmitState.Error ->
                ErrorScreen(
                    message = state.error.message ?: "Something went wrong",
                    onRetry = { viewModel.submitParticipantInput() },
                    modifier = Modifier.fillMaxSize(),
                )
        }
    }
}

/**
 * UI content displayed when participant submission succeeds.
 *
 * Shows a confirmation message and a button that navigates the user to the
 * event dashboard. The event ID is retrieved from the ViewModel.
 *
 * @param isHost Whether the current user is a host.
 * @param eventId The ID of the event.
 * @param onHomeClick Callback invoked when navigating to the home screen.
 * @param onNavigateToHostDashboard Callback invoked when navigating to the host dashboard.
 * @param onNavigateToParticipantDashboard Callback invoked when navigating to the participant dashboard.
 * @param modifier Modifier for styling.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubmissionCompleteContent(
    isHost: Boolean,
    eventId: String?,
    onHomeClick: () -> Unit,
    onNavigateToHostDashboard: (String) -> Unit,
    onNavigateToParticipantDashboard: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.title_submission_complete),
                        fontWeight = FontWeight.Bold,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onHomeClick) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = "Home",
                        )
                    }
                },
                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface,
                        navigationIconContentColor = MaterialTheme.colorScheme.primary,
                    ),
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
            verticalArrangement = Arrangement.Center,
        ) {
            item {
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_foreground),
                    contentDescription = "MeetUp Logo",
                    modifier =
                        Modifier
                            .size(120.dp)
                            .padding(bottom = AppSpacing.xl),
                )
            }
            item {
                Text(
                    "Thank you!",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
            }

            item {
                Spacer(modifier = Modifier.height(AppSpacing.xl))
                Text(
                    "Your availability and preferences",
                    style = MaterialTheme.typography.bodyLarge,
                )
            }

            item {
                Spacer(modifier = Modifier.height(AppSpacing.xs))
                Text(
                    "have been submitted.",
                    style = MaterialTheme.typography.bodyLarge,
                )
            }

            item {
                Spacer(modifier = Modifier.height(AppSpacing.lg))
                Text(
                    "Please wait for the host",
                    style = MaterialTheme.typography.bodyLarge,
                )
            }

            item {
                Spacer(modifier = Modifier.height(AppSpacing.xs))
                Text(
                    "start the place voting.",
                    style = MaterialTheme.typography.bodyLarge,
                )
            }

            item {
                Spacer(modifier = Modifier.height(AppSpacing.xxl))
                Button(
                    onClick = {
                        val id = eventId ?: return@Button
                        if (isHost) {
                            onNavigateToHostDashboard(id)
                        } else {
                            onNavigateToParticipantDashboard(id)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(8.dp),
                    modifier =
                        Modifier
                            .fillMaxWidth(AppSize.lg),
                    contentPadding = PaddingValues(vertical = AppSpacing.md),
                ) {
                    Text(
                        "Go to Dashboard",
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        }
    }
}

/**
 * Preview for [SubmissionCompleteContent].
 */
@Preview(showBackground = true)
@Composable
fun SubmissionCompleteContentPreview() {
    MeetingAppTheme {
        SubmissionCompleteContent(
            isHost = false,
            eventId = "event123",
            onHomeClick = {},
            onNavigateToHostDashboard = {},
            onNavigateToParticipantDashboard = {},
        )
    }
}
