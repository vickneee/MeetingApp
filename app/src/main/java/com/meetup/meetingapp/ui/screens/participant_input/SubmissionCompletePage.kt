package com.meetup.meetingapp.ui.screens.participant_input

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.meetup.meetingapp.MeetingAppTopAppBar
import com.meetup.meetingapp.R
import com.meetup.meetingapp.ui.AppViewModelProvider
import com.meetup.meetingapp.ui.navigation.NavigationDestination
import com.meetup.meetingapp.ui.screens.event_created_page.ErrorScreen
import com.meetup.meetingapp.ui.screens.event_created_page.LoadingScreen

object SubmissionCompleteDestination: NavigationDestination {
    override val route = "submission-complete"
    override val titleRes = R.string.title_placetype_and_keyword
}

@Composable
fun SubmissionCompletePage(
    onBack: () -> Unit,
    viewModel: ParticipantViewModel,
    onNavigateToDashboard: (String) -> Unit,
    modifier: Modifier = Modifier
) {

    val submitState by viewModel.submitState.collectAsStateWithLifecycle()
    when(submitState){
        is SubmitState.Loading -> LoadingScreen(modifier = Modifier.fillMaxSize())

        is SubmitState.Success -> SubmissionCompleteContent(
            onBack = onBack,
            viewModel = viewModel,
            onNavigateToDashboard = onNavigateToDashboard,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubmissionCompleteContent(
    onBack: () -> Unit,
    viewModel: ParticipantViewModel,
    onNavigateToDashboard: (String) -> Unit,
    modifier: Modifier = Modifier
){
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
                    onClick = { onNavigateToDashboard(viewModel.event.value!!.id) },
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

/**
 * The screen displaying the loading message.
 */
@Composable
fun LoadingScreen(modifier: Modifier = Modifier) {
    Image(
        modifier = modifier.size(200.dp),
        painter = painterResource(R.drawable.loading_img),
        contentDescription = "Loading"
    )
}

/**
 * The screen displaying error message with re-attempt button.
 */
@Composable
fun ErrorScreen(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_connection_error), contentDescription = ""
        )
        Text(text = message, modifier = Modifier.padding(16.dp))
        Button(onClick = onRetry) {
            Text("Retry")
        }
    }
}