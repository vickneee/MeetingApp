package com.meetup.meetingapp.ui.screens.vote_for_place_flow

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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

/**
 * Navigation destination for the Date & Area selection page.
 */
object DateAndAreaPageDestination: NavigationDestination {
    override val route = "date-and-area"
    override val titleRes = R.string.title_date_and_area
}

/**
 * Top-level composable for the Date & Area selection screen.
 */
@Composable
fun DateAndAreaPage(
    onBack: () -> Unit,
    viewModel: PlaceViewModel,
    onNavigateToRestaurantListPage: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val dateAndAreaState by viewModel.dateAndAreaState.collectAsStateWithLifecycle()

    DateAndAreaContent(
        onBack = onBack,
        dateLocationOptions = dateAndAreaState.dateLocationOptions,
        navigateToRestaurantListPage = { timing, location ->
            viewModel.setFilter(timing.toDateTime(), location)
            onNavigateToRestaurantListPage(timing, location)
        },
        modifier = modifier
    )
}

/**
 * Displays the list of selectable date–time–area combinations.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateAndAreaContent(
    onBack: () -> Unit,
    dateLocationOptions: List<DateLocationOption>,
    navigateToRestaurantListPage: (String, String) -> Unit,
    modifier: Modifier = Modifier
){
    Scaffold(
        topBar = {
            MeetingAppTopAppBar(
                title = stringResource(id = R.string.title_date_and_area),
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
                Spacer(modifier = Modifier.height(48.dp))

                Text(
                    "Choose a date, time & area",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier
                        .padding(bottom = 14.dp)
                )
            }
            items(dateLocationOptions){option ->
                Card(
                    onClick = {navigateToRestaurantListPage(option.timingArg, option.location)},
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Text(
                        text = option.label,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 20.dp, horizontal = 16.dp),
                        textAlign = TextAlign.Center,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DateAndAreaContentPreview() {
    val sampleOptions = listOf(
        DateLocationOption(
            timing = DateTime(
                date = "2024-04-12",
                timeSlot = com.meetup.meetingapp.data.model.TimeSlot("09:00", "12:00")
            ),
            location = "Helsinki"
        )
    )

    DateAndAreaContent(
        onBack = {},
        dateLocationOptions = sampleOptions,
        navigateToRestaurantListPage = { _, _ -> }
    )
}
