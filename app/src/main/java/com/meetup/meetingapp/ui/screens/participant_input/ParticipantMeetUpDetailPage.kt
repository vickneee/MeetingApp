package com.meetup.meetingapp.ui.screens.participant_input

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import com.meetup.meetingapp.MeetingAppTopAppBar
import com.meetup.meetingapp.R
import com.meetup.meetingapp.data.model.Event
import com.meetup.meetingapp.ui.navigation.NavigationDestination
import com.meetup.meetingapp.ui.screens.event_created_page.ErrorScreen
import com.meetup.meetingapp.ui.screens.event_created_page.LoadingScreen

/**
 * Navigation destination for the Participant MeetUp Detail screen.
 */
object ParticipantMeetUpDetailDestination : NavigationDestination {
    override val route = "participant_meetUp_detail"
    override val titleRes = R.string.title_meetup_details_page
    const val eventCodeArg = "eventCode"
    val routeWithArgs = "$route/{$eventCodeArg}"
}

/**
 * Participant MeetUp Detail Page
 * @param modifier Modifier.
 * @param onBack Navigate back.
 * @param eventCode The unique code for the event.
 * @param onNavigateToAvailability Navigate to the availability page.
 * @param viewModel [ParticipantViewModel] to retrieve event data.
 */
@Composable
fun ParticipantMeetUpDetailPage(
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    eventCode: String,
    onNavigateToAvailability: () -> Unit,
    viewModel: ParticipantViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val fetchState by viewModel.fetchState.collectAsStateWithLifecycle(FetchState.Loading)
    val event by viewModel.event.collectAsStateWithLifecycle(null)
    val participantState by viewModel.participantState.collectAsStateWithLifecycle(
        ParticipantInputState()
    )

    when (val currentFetchState = fetchState) {
        is FetchState.Loading -> LoadingScreen(modifier = Modifier.fillMaxSize())
        is FetchState.Error -> ErrorScreen(
            message = currentFetchState.message,
            onRetry = {},
            modifier = Modifier.fillMaxSize()
        )

        is FetchState.Success -> event?.let {
            ParticipantMeetUpDetailContent(
                event = it,
                participantState = participantState,
                onNameChange = viewModel::updateName,
                onBack = onBack,
                onNavigateToAvailability = onNavigateToAvailability
            )
        }
    }
    Log.d("Participant", "ParticipantMeetUpDetailPage loaded")
}

/**
 * Participant MeetUp Detail Content
 * @param event The event data.
 * @param participantState The participant input state.
 * @param onNameChange Callback to update the participant name.
 * @param onBack Navigate back.
 * @param modifier Modifier.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParticipantMeetUpDetailContent(
    event: Event,
    participantState: ParticipantInputState,
    onNameChange: (String) -> Unit,
    onBack: () -> Unit,
    onNavigateToAvailability: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            MeetingAppTopAppBar(
                title = "MeetUp Details",
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
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        buildAnnotatedString {
                            append("Event Code: ")
                            withStyle(SpanStyle(fontWeight = FontWeight.SemiBold)) {
                                append(event.eventCode)
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

                Spacer(modifier = Modifier.padding(vertical = 64.dp))

                Text(
                    text = "Your Name",
                    modifier = Modifier.padding(vertical = 5.dp),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                // Input for participant name
                OutlinedTextField(
                    value = participantState.participantName,
                    onValueChange = onNameChange,
                    label = { Text("Enter your name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                Spacer(modifier = Modifier.padding(54.dp))

                // Center the button and make it only as wide as its content
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Button(
                        onClick = onNavigateToAvailability,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .wrapContentWidth() // only as wide as text
                    ) {
                        Text(
                            text = "Next",
                            fontSize = 18.sp,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ParticipantMeetUpDetailPreview() {
    // Sample Event
    val sampleEvent = Event(
        eventCode = "AX4C2G",
        eventTitle = "Team Meetup",
        hostName = "Victoria"
    )

    ParticipantMeetUpDetailContent(
        event = sampleEvent,
        participantState = ParticipantInputState(),
        onNameChange = {},
        onBack = {},
        onNavigateToAvailability = {},
        modifier = Modifier
    )
}