package com.meetup.meetingapp.ui.screens.participant_input

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.meetup.meetingapp.R
import com.meetup.meetingapp.ui.AppViewModelProvider
import com.meetup.meetingapp.ui.navigation.NavigationDestination

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

}