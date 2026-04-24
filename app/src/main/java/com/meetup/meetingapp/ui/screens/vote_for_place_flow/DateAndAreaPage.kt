package com.meetup.meetingapp.ui.screens.vote_for_place_flow

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.meetup.meetingapp.MeetingAppTopAppBar
import com.meetup.meetingapp.R
import com.meetup.meetingapp.data.model.DateTime
import com.meetup.meetingapp.ui.navigation.NavigationDestination
import com.meetup.meetingapp.ui.screens.create_event_flow.LoadingScreen
import com.meetup.meetingapp.ui.theme.AppPadding
import com.meetup.meetingapp.ui.theme.AppSpacing
import com.meetup.meetingapp.ui.theme.MeetingAppTheme

/**
 * Navigation destination for the Date & Area selection page.
 */
object DateAndAreaPageDestination : NavigationDestination {
    override val route = "date-and-area"
    override val titleRes = R.string.title_date_and_area
    const val EVENTIDARG = "eventId"
    val routeWithArgs = "$route/{$EVENTIDARG}"
}

/**
 * Top-level composable for the Date & Area selection screen.
 * @param onBack Callback to navigate back.
 * @param viewModel ViewModel for managing state and data.
 * @param onNavigateToRestaurantListPage Callback to navigate to the restaurant list page.
 * @param modifier Modifier for styling.
 */
@Composable
fun DateAndAreaPage(
    onBack: () -> Unit,
    viewModel: PlaceViewModel,
    onNavigateToRestaurantListPage: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val dateAndAreaState by viewModel.dateAndAreaState.collectAsStateWithLifecycle()
    val restaurantState by viewModel.restaurantState.collectAsStateWithLifecycle()

    Crossfade(targetState = restaurantState, label = "date_area_loading") { state ->
        when (state) {
            is RestaurantState.Loading -> LoadingScreen(modifier = Modifier.fillMaxSize())
            is RestaurantState.Available if dateAndAreaState.dateLocationOptions.isEmpty() -> {
                LoadingScreen(modifier = Modifier.fillMaxSize())
            }

            else -> {
                DateAndAreaContent(
                    onBack = onBack,
                    dateLocationOptions = dateAndAreaState.dateLocationOptions,
                    navigateToRestaurantListPage = { timing, location ->
                        viewModel.setFilter(timing, location)
                        onNavigateToRestaurantListPage()
                    },
                    modifier = modifier,
                )
            }
        }
    }
}

/**
 * Displays the list of selectable date–time–area combinations.
 * @param onBack Callback to navigate back.
 * @param dateLocationOptions List of available date–time–area combinations.
 * @param navigateToRestaurantListPage Callback to navigate to the restaurant list page.
 * @param modifier Modifier for styling.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateAndAreaContent(
    onBack: () -> Unit,
    dateLocationOptions: List<DateLocationOption>,
    // Signature updated to accept the DateTime object directly
    navigateToRestaurantListPage: (DateTime, String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            MeetingAppTopAppBar(
                title = stringResource(id = R.string.title_date_and_area),
                canNavigateBack = true,
                navigateUp = onBack,
            )
        },
    ) { paddingValues ->
        Box(
            modifier =
                modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(paddingValues),
        ) {
            if (dateLocationOptions.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "No options found.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = AppPadding.pagePadding, // Padding values for the entire screen
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Top,
                ) {
                    item {
                        Text(
                            "Choose a date, time & area",
                            style = MaterialTheme.typography.titleMedium,
                            modifier =
                                Modifier
                                    .padding(bottom = AppSpacing.lg),
                        )
                    }
                    items(dateLocationOptions) { option ->
                        Card(
                            onClick = { navigateToRestaurantListPage(option.timing, option.location) },
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors =
                                CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface,
                                ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        ) {
                            Text(
                                text = option.label,
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 20.dp, horizontal = 16.dp),
                                textAlign = TextAlign.Center,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Preview for the [DateAndAreaContent] composable.
 */
@Preview(showBackground = true)
@Composable
fun DateAndAreaContentPreview() {
    val sampleOptions =
        listOf(
            DateLocationOption(
                timing =
                    DateTime(
                        date = "2024-04-12",
                        timeSlot =
                            com.meetup.meetingapp.data.model
                                .TimeSlot("09:00", "12:00"),
                    ),
                location = "Helsinki",
            ),
            DateLocationOption(
                timing =
                    DateTime(
                        date = "2024-04-12",
                        timeSlot =
                            com.meetup.meetingapp.data.model
                                .TimeSlot("13:00,", "16:00"),
                    ),
                location = "Tampere",
            ),
        )

    MeetingAppTheme {
        DateAndAreaContent(
            onBack = {},
            dateLocationOptions = sampleOptions,
            navigateToRestaurantListPage = { _, _ -> },
        )
    }
}
