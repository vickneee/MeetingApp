package com.meetup.meetingapp.ui.screens.vote_for_place_flow

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.meetup.meetingapp.ui.screens.create_event_flow.LoadingScreen
import com.meetup.meetingapp.ui.theme.MeetingAppTheme

/**
 * Navigation destination for the choose date and area button screen.
 */
object ChooseDateAndAreaDestination : NavigationDestination {
    override val route = "choose_date_and_area"
    override val titleRes = R.string.title_participant_dashboard
    const val eventIdArg = "eventId"
    val routeWithArgs = "$route/{$eventIdArg}"
}

/**
 * Participant MeetUp Detail Page
 * @param onBack Navigate back.
 * @param onNavigateToChooseDatePage Navigate to the availability page.
 * @param onNavigateToHome Navigate to the home page.
 * @param viewModel [PlaceViewModel] to retrieve event data.
 * @see PlaceViewModel for retrieving event data.
 */
@Composable
fun ChooseDateAndAreaPage(
    onBack: () -> Unit,
    onNavigateToChooseDatePage: () -> Unit,
    onNavigateToHome: () -> Unit,
    viewModel: PlaceViewModel = viewModel(
        factory = AppViewModelProvider.Factory
    )
) {
    val event by viewModel.event.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val restaurantState by viewModel.restaurantState.collectAsStateWithLifecycle()

    when (restaurantState) {
        is RestaurantState.Loading -> LoadingScreen(modifier = Modifier.fillMaxSize())

        is RestaurantState.Available -> event?.let {
            ChooseDateAndAreaContent(
                event = it,
                submissionsCount = uiState.submissionsCount,
                isLoading = false,
                onBack = onBack,
                onVoteForRestaurantClick = onNavigateToChooseDatePage,
                onNavigateToHome = onNavigateToHome,
                buttonEnabled = true
            )
        } ?: LoadingScreen(modifier = Modifier.fillMaxSize())

        is RestaurantState.Error -> event?.let {
            ChooseDateAndAreaContent(
                event = it,
                submissionsCount = uiState.submissionsCount,
                isLoading = false,
                onBack = onBack,
                onVoteForRestaurantClick = {},
                onNavigateToHome = {},
                buttonEnabled = false
            )
        } ?: LoadingScreen(modifier = Modifier.fillMaxSize())

        else -> event?.let {
            ChooseDateAndAreaContent(
                event = it,
                submissionsCount = uiState.submissionsCount,
                isLoading = true, // Show loading indicator
                onBack = onBack,
                onVoteForRestaurantClick = {},
                onNavigateToHome = {},
                buttonEnabled = false
            )
        } ?: LoadingScreen(modifier = Modifier.fillMaxSize())
    }
}

/**
 * Content for the ChooseDateAndAreaPage.
 * @param modifier Modifier.
 * @param event Event data.
 * @param submissionsCount Number of submissions.
 * @param isLoading Whether the content is loading.
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
    submissionsCount: Int,
    isLoading: Boolean = false,
    onBack: () -> Unit,
    onVoteForRestaurantClick: () -> Unit,
    onNavigateToHome: () -> Unit,
    buttonEnabled: Boolean = true,
) {
    Scaffold(
        topBar = {
            MeetingAppTopAppBar(
                title = "${stringResource(id = R.string.title_participant_dashboard)} / ${event.eventCode}",
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
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            item {
                Spacer(modifier = Modifier.padding(24.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
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

                    Spacer(modifier = Modifier.padding(12.dp))

                    Text(
                        text = "Submissions: $submissionsCount",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.padding(24.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = onVoteForRestaurantClick,
                        enabled = buttonEnabled,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth(0.9f)
                    ) {
                        Text(
                            "Choose Date & Area",
                            fontSize = 18.sp,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(36.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    OutlinedButton(
                        onClick = onNavigateToHome,
                        border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                    ) {
                        Text(
                            "Home",
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(6.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(48.dp))
                }
            }
        }
    }
}

/**
 * Preview for thr [ChooseDateAndAreaPage].
 */
@Preview(showBackground = true)
@Composable
fun ChooseDateAndAreaPagePreview() {
    MeetingAppTheme {
        ChooseDateAndAreaContent(
            event = Event(
                eventCode = "A7F9K2",
                eventTitle = "Meet & Chat",
                hostName = "Julia",
            ),
            onBack = {},
            submissionsCount = 0,
            isLoading = true,
            onVoteForRestaurantClick = {},
            onNavigateToHome = {},
            buttonEnabled = true
        )
    }
}
