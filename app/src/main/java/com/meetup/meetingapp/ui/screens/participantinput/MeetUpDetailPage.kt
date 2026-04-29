package com.meetup.meetingapp.ui.screens.participantinput

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.meetup.meetingapp.MeetingAppTopAppBar
import com.meetup.meetingapp.R
import com.meetup.meetingapp.data.model.Event
import com.meetup.meetingapp.ui.AppViewModelProvider
import com.meetup.meetingapp.ui.navigation.NavigationDestination
import com.meetup.meetingapp.ui.screens.components.ParticipantItemRow
import com.meetup.meetingapp.ui.screens.eventcreation.ErrorScreen
import com.meetup.meetingapp.ui.screens.eventcreation.LoadingScreen
import com.meetup.meetingapp.ui.theme.AppPadding
import com.meetup.meetingapp.ui.theme.AppSize
import com.meetup.meetingapp.ui.theme.AppSpacing
import com.meetup.meetingapp.ui.theme.MeetingAppTheme
import kotlin.text.ifEmpty

/**
 * Navigation destination for the Participant MeetUp Detail screen.
 */
object MeetUpDetailDestination : NavigationDestination {
    override val route = "participant_meetUp_detail"
    override val titleRes = R.string.title_meetup_details_page
    const val EVENT_CODE_ARG = "eventCode"
    const val EVENT_KEY_ARG = "eventKey"

    val routeWithArgs = "$route/{$EVENT_CODE_ARG}/{$EVENT_KEY_ARG}"
}

/**
 * Participant MeetUp Detail Page
 * @param modifier Modifier.
 * @param onBack Navigate back.
 * @param onNavigateToHome Navigate to the home screen.
 * @param onNavigateToTimeAvailability Navigate to the availability page.
 * @param viewModel [ParticipantViewModel] to retrieve event data.
 */
@Composable
fun MeetUpDetailPage(
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToTimeAvailability: () -> Unit,
    viewModel: ParticipantViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val fetchState by viewModel.fetchState.collectAsStateWithLifecycle(FetchState.Loading)
    val event by viewModel.event.collectAsStateWithLifecycle(null)
    val participantState by viewModel.participantState.collectAsStateWithLifecycle(
        ParticipantInputState(),
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Crossfade(targetState = fetchState, label = "meetup_detail_loading") { currentFetchState ->
        when (currentFetchState) {
            is FetchState.Loading -> LoadingScreen(modifier = Modifier.fillMaxSize())
            is FetchState.Error ->
                ErrorScreen(
                    message = currentFetchState.message,
                    onRetry = {},
                    modifier = Modifier.fillMaxSize(),
                )

            is FetchState.Success ->
                event?.let {
                    MeetUpDetailContent(
                        event = it,
                        submissionsCount = uiState.submissionsCount,
                        attendees = uiState.attendees,
                        isAlreadySubmitted = uiState.isAlreadySubmitted,
                        currentUserName = participantState.participantName,
                        participantState = participantState,
                        onNameChange = viewModel::updateName,
                        onBack = onBack,
                        onHomeClick = onNavigateToHome,
                        onNavigateToTimeAvailability = onNavigateToTimeAvailability,
                        modifier = modifier,
                    )
                }
        }
    }
}

/**
 * Participant MeetUp Detail Content
 * @param modifier Modifier.
 * @param event The event data.
 * @param submissionsCount The number of submissions.
 * @param attendees List of participant names.
 * @param isAlreadySubmitted Whether the user has already submitted.
 * @param currentUserName The name of the current user.
 * @param participantState The participant input state.
 * @param onNameChange Callback to update the participant name.
 * @param onBack Navigate back.
 * @param onHomeClick Navigate home.
 * @param onNavigateToTimeAvailability Navigate to the availability page.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeetUpDetailContent(
    modifier: Modifier = Modifier,
    event: Event,
    submissionsCount: Int,
    attendees: List<String>,
    isAlreadySubmitted: Boolean = false,
    currentUserName: String = "",
    participantState: ParticipantInputState,
    onNameChange: (String) -> Unit,
    onBack: () -> Unit,
    onHomeClick: () -> Unit,
    onNavigateToTimeAvailability: () -> Unit,
) {
    Scaffold(
        topBar = {
            MeetingAppTopAppBar(
                title = stringResource(id = R.string.title_meetup_details_page),
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
            verticalArrangement = Arrangement.Center,
        ) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(AppSize.xl),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        text =
                            buildAnnotatedString {
                                append("Hi, ")
                                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                                    append(currentUserName.ifEmpty { "there" })
                                }
                                append("!")
                            },
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Normal,
                        style = MaterialTheme.typography.titleLarge,
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(bottom = 4.dp),
                        textAlign = TextAlign.Center,
                    )
                    Spacer(modifier = Modifier.height(AppSpacing.xxs))
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

                    Spacer(modifier = Modifier.height(AppSpacing.xxs))
                    Text(
                        text =
                            buildAnnotatedString {
                                append("Availability: ")
                                withStyle(
                                    SpanStyle(
                                        fontWeight = FontWeight.Bold,
                                    ),
                                ) {
                                    append("$submissionsCount")
                                }
                            },
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(modifier = Modifier.height(AppSpacing.xxs))
                }
            }

            // List of attendees
            items(attendees) { name ->
                ParticipantItemRow(name = name, modifier = Modifier.padding(start = 16.dp))
            }

            item {
                Spacer(modifier = Modifier.height(AppSpacing.lg))
                Text(
                    text = "Your Name",
                    modifier =
                        Modifier
                            .padding(vertical = AppSpacing.xxs)
                            .fillMaxWidth(AppSize.xl),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                // Input for participant name
                OutlinedTextField(
                    value = participantState.participantName,
                    onValueChange = onNameChange,
                    label = { Text("Enter your name") },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyMedium,
                    enabled = !isAlreadySubmitted, // Disable if already submitted
                    modifier = Modifier.fillMaxWidth(AppSize.xl),
                    shape = RoundedCornerShape(8.dp),
                    colors =
                        OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            cursorColor = MaterialTheme.colorScheme.primary,
                            disabledTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            disabledBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                            disabledLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        ),
                )
            }

            item {
                Spacer(modifier = Modifier.padding(AppSpacing.md))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Button(
                        onClick = onNavigateToTimeAvailability,
                        enabled = participantState.participantName.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth(AppSize.lg),
                        contentPadding = PaddingValues(vertical = AppSpacing.md),
                    ) {
                        Text(
                            text = if (isAlreadySubmitted) "Edit Your Availability" else "Next",
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }

                    Spacer(modifier = Modifier.height(AppSpacing.lg))

                    OutlinedButton(
                        onClick = onHomeClick,
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
 * Preview for the [MeetUpDetailContent] composable.
 */
@Preview(showBackground = true)
@Composable
fun MeetUpDetailPreview() {
    // Sample Event
    val sampleEvent =
        Event(
            eventCode = "AX4C2G",
            eventTitle = "Team Meetup",
            hostName = "Victoria",
        )

    MeetingAppTheme {
        MeetUpDetailContent(
            event = sampleEvent,
            isAlreadySubmitted = false,
            currentUserName = "Julia",
            submissionsCount = 3,
            attendees = listOf("Victoria", "Alice", "Bob"),
            participantState = ParticipantInputState(participantName = "Julia"),
            onNameChange = {},
            onBack = {},
            onHomeClick = {},
            onNavigateToTimeAvailability = {},
            modifier = Modifier,
        )
    }
}
