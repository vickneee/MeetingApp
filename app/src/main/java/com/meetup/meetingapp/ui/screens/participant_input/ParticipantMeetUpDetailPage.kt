package com.meetup.meetingapp.ui.screens.participant_input

import android.util.Log
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.meetup.meetingapp.R
import com.meetup.meetingapp.data.model.DateTime
import com.meetup.meetingapp.data.model.Event
import com.meetup.meetingapp.data.model.FoodCategory
import com.meetup.meetingapp.ui.AppViewModelProvider
import com.meetup.meetingapp.ui.navigation.NavigationDestination
import com.meetup.meetingapp.ui.screens.event_created_page.ErrorScreen
import com.meetup.meetingapp.ui.screens.event_created_page.LoadingScreen

object ParticipantMeetUpDetailDestination : NavigationDestination {
    override val route = "participant_meetUp_detail"
    override val titleRes = R.string.title_meetup_details_page
    const val eventCodeArg = "eventCode"
    val routeWithArgs = "$route/{$eventCodeArg}"
}

@Composable
fun ParticipantMeetUpDetailPage(
    onBack: () -> Unit,
    viewModel: ParticipantViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val fetchState by viewModel.fetchState.collectAsStateWithLifecycle(FetchState.Loading)
    val event by viewModel.event.collectAsStateWithLifecycle(null)
    val participantState by viewModel.participantState.collectAsStateWithLifecycle(ParticipantInputState())

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
                onToggleDateTime = viewModel::toggleDateTime,
                onToggleFoodCategory = viewModel::toggleFoodCategory,
                onNameChange = viewModel::updateName,
                onBack = onBack
            )
        }
    }
    Log.d("Participant", "ParticipantMeetUpDetailPage loaded")
}

@Composable
fun ParticipantMeetUpDetailContent(
    event: Event,
    participantState: ParticipantInputState,
    onToggleDateTime: (DateTime) -> Unit,
    onToggleFoodCategory: (FoodCategory) -> Unit,
    onNameChange: (String) -> Unit,
    onBack: () -> Unit
) {
    // UI driven entirely by event data from Firestore
    // event.timeSlots, event.dateRange, event.placeTypeOptions etc.
}