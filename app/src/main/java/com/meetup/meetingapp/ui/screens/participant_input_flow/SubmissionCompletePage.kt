package com.meetup.meetingapp.ui.screens.participant_input_flow

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.meetup.meetingapp.MeetingAppTopAppBar
import com.meetup.meetingapp.R
import com.meetup.meetingapp.ui.navigation.NavigationDestination
import com.meetup.meetingapp.ui.screens.create_event_flow.ErrorScreen
import com.meetup.meetingapp.ui.screens.create_event_flow.LoadingScreen

/**
 * Navigation destination for the Submission Complete screen.
 */
object SubmissionCompleteDestination: NavigationDestination {
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
 * @param onBack Callback invoked when navigating back.
 * @param viewModel The [ParticipantViewModel] providing submission state.
 * @param onNavigateToHostDashboard Callback invoked when navigating to the host dashboard.
 * @param onNavigateToParticipantDashboard Callback invoked when navigating to the participant dashboard.
 */
@Composable
fun SubmissionCompletePage(
    onBack: () -> Unit,
    viewModel: ParticipantViewModel,
    onNavigateToHostDashboard: (String) -> Unit,
    onNavigateToParticipantDashboard: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val submitState by viewModel.submitState.collectAsStateWithLifecycle()

    // Handle different submission states
    when(submitState){
        is SubmitState.Loading -> LoadingScreen(modifier = Modifier.fillMaxSize())

        is SubmitState.Success -> SubmissionCompleteContent(
            onBack = onBack,
            viewModel = viewModel,
            onNavigateToHostDashboard = onNavigateToHostDashboard,
            onNavigateToParticipantDashboard = onNavigateToParticipantDashboard,
            modifier = modifier
        )

        is SubmitState.Error -> {
            val state = submitState as SubmitState.Error
            ErrorScreen(
                message = state.error.message ?: "Something went wrong",
                onRetry = { viewModel.submitParticipantInput() },
                modifier = Modifier.fillMaxSize()
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
 * @param onBack Callback invoked when navigating back.
 * @param viewModel The [ParticipantViewModel] used to access event data.
 * @param onNavigateToHostDashboard Callback invoked when navigating to the host dashboard.
 * @param onNavigateToParticipantDashboard Callback invoked when navigating to the participant dashboard.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubmissionCompleteContent(
    onBack: () -> Unit,
    viewModel: ParticipantViewModel,
    onNavigateToHostDashboard: (String) -> Unit,
    onNavigateToParticipantDashboard: (String) -> Unit,
    modifier: Modifier = Modifier
){

    val isHost by viewModel.isHost.collectAsStateWithLifecycle(false)
    val event by viewModel.event.collectAsStateWithLifecycle(null)

    Scaffold(
        topBar = {
            MeetingAppTopAppBar(
                title = stringResource(id= R.string.title_submission_complete),
                canNavigateBack = true,
                navigateUp = onBack
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            item {
                Spacer(modifier = Modifier.height(48.dp))
                Text(
                    "Thank you!",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                Spacer(modifier = Modifier.height(48.dp))
                Text(
                    "Your availability and preferences",
                    fontSize = 20.sp,
                )
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "have been submitted.",
                    fontSize = 20.sp,
                )
            }

            item {
                Spacer(modifier = Modifier.height(28.dp))
                Text(
                    "Please wait for the host to close",
                    fontSize = 20.sp,
                )
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "the voting.",
                    fontSize = 20.sp,
                )
            }

            item {
                Spacer(modifier = Modifier.height(100.dp))

                Button(
                    onClick = {  val eventId = event?.id ?: return@Button
                        if (isHost) {
                            onNavigateToHostDashboard(eventId)
                        } else {
                            onNavigateToParticipantDashboard(eventId)
                        } },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth(0.7f)
                ) {
                    Text("Go to Dashboard", fontSize = 18.sp, modifier = Modifier.padding(8.dp))
                }
            }
        }
    }
}
