package com.meetup.meetingapp.ui.screens.vote_for_place_flow

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import com.meetup.meetingapp.ui.screens.create_event_flow.LoadingScreen

/**
 * Navigation destination for the Participant MeetUp Detail screen.
 */
object ChooseDateAndAreaDestination : NavigationDestination {
    override val route = "choose_date_and_area"
    override val titleRes = R.string.title_participant_dashboard_waiting
    const val eventIdArg = "eventId"
    val routeWithArgs = "$route/{$eventIdArg}"
}

/**
 * Participant MeetUp Detail Page
 * @param onBack Navigate back.
 * @param onNavigateToChooseDatePage Navigate to the availability page.
 * @param viewModel [PlaceViewModel] to retrieve event data.
 * @see PlaceViewModel for retrieving event data.
 */
@Composable
fun ChooseDateAndAreaPage(
    onBack: () -> Unit,
    onNavigateToChooseDatePage: () -> Unit,
    viewModel: PlaceViewModel = viewModel(
        factory = AppViewModelProvider.Factory
    )
) {
    val event by viewModel.event.collectAsStateWithLifecycle()
    val restaurantState by viewModel.restaurantState.collectAsStateWithLifecycle()

    when(restaurantState){
        is RestaurantState.Loading -> LoadingScreen(modifier = Modifier.fillMaxSize())

        is RestaurantState.Available -> event?.let {
            ChooseDateAndAreaContent(
                event = it,
                onBack = onBack,
                onVoteForRestaurantClick = onNavigateToChooseDatePage,
                buttonEnabled = true
            )
        } ?: LoadingScreen(modifier = Modifier.fillMaxSize())

        is RestaurantState.Error -> event?.let {
            ChooseDateAndAreaContent(
                event = it,
                onBack = onBack,
                onVoteForRestaurantClick = {},
                buttonEnabled = false
            )
        } ?: LoadingScreen(modifier = Modifier.fillMaxSize())

        else -> event?.let {
            ChooseDateAndAreaContent(
                event = it,
                onBack = onBack,
                onVoteForRestaurantClick = {},
                buttonEnabled = false
            )
        } ?: LoadingScreen(modifier = Modifier.fillMaxSize())
    }
}

/**
 * Content for the ChooseDateAndAreaPage.
 * @param modifier Modifier.
 * @param event Event data.
 * @param onBack Navigate back.
 * @param onVoteForRestaurantClick Navigate to the availability page.
 * @param buttonEnabled Whether the button should be enabled.
 * @param modifier Modifier.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChooseDateAndAreaContent(
    modifier: Modifier = Modifier,
    event: Event,
    onBack: () -> Unit,
    onVoteForRestaurantClick: () -> Unit,
    buttonEnabled: Boolean = true,
) {
    Scaffold(
        topBar = {
            MeetingAppTopAppBar(
                title = "Dashboard / ${event.eventCode}",
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
                    Text(buildAnnotatedString {
                        append("Event Code: ")
                        withStyle(SpanStyle(fontWeight = FontWeight.SemiBold)) {
                            append(event.eventCode)
                        }
                    }, fontSize = 20.sp)

                    Text(buildAnnotatedString {
                        append("State: ")
                        withStyle(SpanStyle(fontWeight = FontWeight.SemiBold)) {
                            append(event.status.displayName)
                        }
                    }, fontSize = 20.sp)

                    Text(buildAnnotatedString {
                        append("Event Title: ")
                        withStyle(SpanStyle(fontWeight = FontWeight.SemiBold)) {
                            append(event.eventTitle)
                        }
                    }, fontSize = 20.sp)

                    Text(buildAnnotatedString {
                        append("Host: ")
                        withStyle(SpanStyle(fontWeight = FontWeight.SemiBold)) {
                            append(event.hostName)
                        }
                    }, fontSize = 20.sp)
                }
                Spacer(modifier = Modifier.padding(24.dp))
            }

            item {
                Spacer(modifier = Modifier.padding(32.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = onVoteForRestaurantClick,
                        enabled = buttonEnabled,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                    ) {
                        Text(
                            "Choose Date & Area",
                            fontSize = 18.sp,
                            modifier = Modifier.padding(6.dp)
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ChooseDateAndAreaPagePreview() {
    ChooseDateAndAreaContent(
        event = Event(
            eventCode = "A7F9K2",
            eventTitle = "Meet & Chat",
            hostName = "Julia",
        ),
        onBack = {},
        onVoteForRestaurantClick = {},
        buttonEnabled = true
    )
}
