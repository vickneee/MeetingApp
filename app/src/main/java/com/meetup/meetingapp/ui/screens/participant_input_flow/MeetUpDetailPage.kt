package com.meetup.meetingapp.ui.screens.participant_input_flow

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import com.meetup.meetingapp.ui.theme.AppPadding
import com.meetup.meetingapp.ui.theme.AppSize
import com.meetup.meetingapp.ui.theme.AppSpacing
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
                isAlreadySubmitted = uiState.isAlreadySubmitted,
                submittedName = uiState.submittedName,
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
 * @param isAlreadySubmitted Whether the user has already submitted.
 * @param submittedName The name the user submitted with.
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
    isAlreadySubmitted: Boolean = false,
    submittedName: String = "",
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
                .padding(paddingValues),
            contentPadding = AppPadding.pagePadding, // Padding values for the entire screen
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Top
        ) {
            item {
                Column(
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (isAlreadySubmitted) {
                        Text(
                            text = buildAnnotatedString {
                                if (submittedName.isNotEmpty()) {
                                    withStyle(SpanStyle(fontWeight = FontWeight.SemiBold)) {
                                        append(submittedName)
                                    }
                                    append(", You already voted!")
                                } else {
                                    append("You already voted!")
                                }
                            },
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        Text(
                            "You've joined this meetup!",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        buildAnnotatedString {
                            append("Event Code: ")
                            withStyle(SpanStyle(fontWeight = FontWeight.SemiBold)) {
                                append(event.eventCode)
                            }
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        buildAnnotatedString {
                            append("State: ")
                            withStyle(SpanStyle(fontWeight = FontWeight.SemiBold)) {
                                append(event.status.displayName)
                            }
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        buildAnnotatedString {
                            append("Event Title: ")
                            withStyle(SpanStyle(fontWeight = FontWeight.SemiBold)) {
                                append(event.eventTitle)
                            }
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        buildAnnotatedString {
                            append("Host: ")
                            withStyle(SpanStyle(fontWeight = FontWeight.SemiBold)) {
                                append(event.hostName)
                            }
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.height(AppSpacing.lg))
                Text(
                    text = "Submissions: $submissionsCount",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.height(AppSpacing.md))
            }

            item {
                Text(
                    text = "Your Name",
                    modifier = Modifier.padding(vertical = AppSpacing.xxs),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Input for participant name
                OutlinedTextField(
                    value = participantState.participantName,
                    onValueChange = onNameChange,
                    label = { Text("Enter your name") },
                    singleLine = true,
                    enabled = !isAlreadySubmitted, // Disable if already submitted
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        cursorColor = MaterialTheme.colorScheme.primary,
                        disabledTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        disabledBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                        disabledLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                )
            }

            item {
                Spacer(modifier = Modifier.padding(AppSpacing.lg))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(
                        onClick = onNavigateToTimeAvailability,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth(AppSize.sm),
                        contentPadding = PaddingValues(vertical = AppSpacing.sm)
                    ) {
                        Text(
                            text = if (isAlreadySubmitted) "Edit Your Vote" else "Next",
                            style = MaterialTheme.typography.labelLarge

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
            participantState = ParticipantInputState(participantName = "Julia"),
            onNameChange = {},
            onBack = {},
            onNavigateToTimeAvailability = {},
            modifier = Modifier
        )
    }
}
