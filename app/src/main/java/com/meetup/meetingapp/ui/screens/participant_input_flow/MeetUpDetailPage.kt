package com.meetup.meetingapp.ui.screens.participant_input_flow

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.meetup.meetingapp.MeetingAppTopAppBar
import com.meetup.meetingapp.R
import com.meetup.meetingapp.data.model.Event
import com.meetup.meetingapp.ui.AppViewModelProvider
import com.meetup.meetingapp.ui.navigation.NavigationDestination
import com.meetup.meetingapp.ui.screens.create_event_flow.ErrorScreen
import com.meetup.meetingapp.ui.screens.create_event_flow.LoadingScreen
import com.meetup.meetingapp.ui.theme.MeetingAppTheme

/**
 * Navigation destination for the Participant MeetUp Detail screen.
 */
object MeetUpDetailDestination : NavigationDestination {
    override val route = "participant_meetUp_detail"
    override val titleRes = R.string.title_meetup_details_page
    const val eventCodeArg = "eventCode"
    const val eventKeyArg = "eventKey"

    val routeWithArgs = "$route/{$eventCodeArg}/{$eventKeyArg}"
}

/**
 * Participant MeetUp Detail Page
 * @param modifier Modifier.
 * @param onBack Navigate back.
 * @param eventCode The unique code for the event.
 * @param onNavigateToTimeAvailability Navigate to the availability page.
 * @param viewModel [ParticipantViewModel] to retrieve event data.
 */
@Composable
fun MeetUpDetailPage(
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    eventCode: String,
    onNavigateToTimeAvailability: () -> Unit,
    viewModel: ParticipantViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val fetchState by viewModel.fetchState.collectAsStateWithLifecycle(FetchState.Loading)
    val event by viewModel.event.collectAsStateWithLifecycle(null)
    val participantState by viewModel.participantState.collectAsStateWithLifecycle(
        ParticipantInputState()
    )
    val isHost by viewModel.isHost.collectAsStateWithLifecycle(false)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when (val currentFetchState = fetchState) {
        is FetchState.Loading -> LoadingScreen(modifier = Modifier.fillMaxSize())
        is FetchState.Error -> ErrorScreen(
            message = currentFetchState.message,
            onRetry = {},
            modifier = Modifier.fillMaxSize()
        )

        is FetchState.Success -> event?.let {
            MeetUpDetailContent(
                event = it,
                submissionsCount = uiState.submissionsCount,
                participantState = participantState,
                onNameChange = viewModel::updateName,
                onBack = onBack,
                onNavigateToTimeAvailability = onNavigateToTimeAvailability,
                isHost = isHost,
                modifier = modifier
            )
        }
    }
    Log.d("Participant", "ParticipantMeetUpDetailPage loaded")
}

/**
 * Participant MeetUp Detail Content
 * @param modifier Modifier.
 * @param event The event data.
 * @param submissionsCount The number of submissions.
 * @param participantState The participant input state.
 * @param onNameChange Callback to update the participant name.
 * @param onBack Navigate back.
 * @param onNavigateToTimeAvailability Navigate to the availability page.
 * @param isHost Whether the current user is the host.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeetUpDetailContent(
    modifier: Modifier = Modifier,
    event: Event,
    submissionsCount: Int,
    participantState: ParticipantInputState,
    onNameChange: (String) -> Unit,
    onBack: () -> Unit,
    onNavigateToTimeAvailability: () -> Unit,
    isHost: Boolean = false,
) {
    Scaffold(
        topBar = {
            MeetingAppTopAppBar(
                title = stringResource(id = R.string.title_meetup_details_page),
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
                    Text(
                        "You've joined this meetup!",
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        buildAnnotatedString {
                            append("Event Code: ")
                            withStyle(SpanStyle(fontWeight = FontWeight.SemiBold)) {
                                append(event.eventCode)
                            }
                        },
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        buildAnnotatedString {
                            append("Event Title: ")
                            withStyle(SpanStyle(fontWeight = FontWeight.SemiBold)) {
                                append(event.eventTitle)
                            }
                        },
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        buildAnnotatedString {
                            append("Host: ")
                            withStyle(SpanStyle(fontWeight = FontWeight.SemiBold)) {
                                append(event.hostName)
                            }
                        },
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.padding(24.dp))

                Text(
                    text = "Submissions: $submissionsCount",
                    fontSize = 22.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.padding(vertical = 24.dp))

                Text(
                    text = "Your Name",
                    modifier = Modifier.padding(vertical = 5.dp),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Input for participant name
                OutlinedTextField(
                    value = if (isHost) event.hostName else participantState.participantName,
                    onValueChange = onNameChange,
                    label = { Text("Enter your name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        cursorColor = MaterialTheme.colorScheme.primary
                    )
                )
            }

            item {
                Spacer(modifier = Modifier.padding(24.dp))

                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Button(
                        onClick = onNavigateToTimeAvailability,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                    ) {
                        Text(
                            text = "Next",
                            fontSize = 18.sp,
                            modifier = Modifier.padding(vertical = 6.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Preview for the [MeetUpDetailContent] composable.
 */
@Preview(showBackground = true)
@Composable
fun MeetUpDetailPreview() {
    // Sample Event
    val sampleEvent = Event(
        eventCode = "AX4C2G",
        eventTitle = "Team Meetup",
        hostName = "Victoria"
    )

    MeetingAppTheme {
        MeetUpDetailContent(
            event = sampleEvent,
            submissionsCount = 0,
            participantState = ParticipantInputState(),
            onNameChange = {},
            onBack = {},
            onNavigateToTimeAvailability = {},
            modifier = Modifier
        )
    }
}