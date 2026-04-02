package com.meetup.meetingapp.ui.screens.participant_input

import androidx.compose.runtime.Composable
import com.meetup.meetingapp.R
import com.meetup.meetingapp.ui.navigation.NavigationDestination

object ParticipantMeetUpDetailDestination : NavigationDestination {
    override val route = "participant_meetUp_detail"
    override val titleRes = R.string.title_meetup_details_page


@Composable
fun ParticipantMeetUpDetailPage(
    onBack: () -> Unit,
){
}

@Composable
fun ParticipantMeetUpDetailContent(

){
}
}