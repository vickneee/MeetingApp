package com.meetup.meetingapp.ui.screens.vote_for_place_flow

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.meetup.meetingapp.MeetingAppTopAppBar
import com.meetup.meetingapp.R
import com.meetup.meetingapp.data.model.DateTime
import com.meetup.meetingapp.data.model.Restaurant
import com.meetup.meetingapp.data.model.TimeSlot
import com.meetup.meetingapp.ui.navigation.NavigationDestination
import com.meetup.meetingapp.ui.screens.create_event_flow.LoadingScreen
import com.meetup.meetingapp.ui.theme.AppPadding
import com.meetup.meetingapp.ui.theme.AppSpacing

/**
 * Navigation destination for the Place List screen.
 * This destination is used to navigate to the Place List screen.
 *
 * @property route The route for the Place List screen.
 * @property titleRes The resource ID for the title of the Place List screen.
 */
object PlaceListPageDestination : NavigationDestination {
    override val route = "place_list"
    override val titleRes = R.string.title_place_list
}

/**
 * Top-level composable for the Place List screen.
 *
 * @param onBack Navigate back.
 * @param viewModel The [PlaceViewModel] to retrieve place list data.
 * @param onNavigateToPlaceDetails Navigate to the Place Details screen.
 * @param modifier Modifier.
 */
@Composable
fun PlaceListPage(
    onBack: () -> Unit,
    onEditSelection : () -> Unit,
    viewModel: PlaceViewModel,
    onNavigateToPlaceDetails: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val placeListState by viewModel.filteredRestaurants.collectAsStateWithLifecycle(emptyList())
    val selectedTiming by viewModel.selectedTiming.collectAsStateWithLifecycle()
    val selectedLocation by viewModel.selectedLocation.collectAsStateWithLifecycle()
    val restaurantState by viewModel.restaurantState.collectAsStateWithLifecycle()

    Crossfade(targetState = restaurantState, label = "place_list_loading") { state ->
        if (state is RestaurantState.Loading) {
            LoadingScreen(modifier = Modifier.fillMaxSize())
        } else {
            PlaceListContent(
                onBack = onBack,
                onEditSelection = onEditSelection,
                placeListState = placeListState,
                selectedTiming = selectedTiming,
                selectedLocation = selectedLocation,
                onNavigateToPlaceDetails = onNavigateToPlaceDetails,
                modifier = modifier,
            )
        }
    }
}

/**
 * Content for the Place List screen.
 *
 * @param onBack Navigate back.
 * @param placeListState The list of restaurants to display.
 * @param selectedTiming The selected timing.
 * @param selectedLocation The selected location.
 * @param onNavigateToPlaceDetails Navigate to the Place Details screen.
 * @param modifier Modifier.
 * @see PlaceViewModel for retrieving place list data.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaceListContent(
    onBack: () -> Unit,
    onEditSelection: () -> Unit,
    placeListState: List<Restaurant>,
    selectedTiming: DateTime?,
    selectedLocation: String?,
    onNavigateToPlaceDetails: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            MeetingAppTopAppBar(
                title = stringResource(id = R.string.title_place_list),
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
                if (selectedTiming != null && selectedLocation != null) {
                    Column(
                        Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = selectedTiming.toDisplayLabel(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = selectedLocation,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                        )
                    }
                } else {
                    Text(
                        text = "Selected Places",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                    )
                }
                Spacer(modifier = Modifier.height(AppSpacing.md))
            }

            if (placeListState.isEmpty()) {
                item {
                    Column(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(top = 160.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
                    ) {
                        Text(
                            text = "No places found for this selection.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )
                        Button(onClick = onBack) {
                            Text(text = stringResource(id = R.string.edit_selection))
                        }
                    }
                }
            } else {
                items(placeListState) { option ->
                    Card(
                        onClick = { onNavigateToPlaceDetails(option.placeId) },
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
                        Row(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 20.dp, horizontal = 24.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = option.name,
                                textAlign = TextAlign.Center,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false),
                            )
                            if (option.rating != null && option.rating > 0) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center,
                                ) {
                                    Text(
                                        text = "(",
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                    Icon(
                                        imageVector = Icons.Filled.Star,
                                        contentDescription = null,
                                        tint = Color(0xFFFFB400),
                                        modifier = Modifier.size(16.dp),
                                    )
                                    Spacer(modifier = Modifier.padding(2.dp))
                                    Text(
                                        text = "${option.rating}",
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                    Text(
                                        text = ")",
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Preview for the [PlaceListContent] composable.
 */
@Preview(showBackground = true)
@Composable
fun PLaceListContentPreview() {
    val sampleOptions =
        listOf(
            Restaurant(
                placeId = "1",
                name = "Woolshed Helsinki - Australian Gastropub",
                rating = 4.5,
                userRatingCount = 120,
            ),
            Restaurant(
                placeId = "2",
                name = "Restaurant B",
                rating = 3.8,
                userRatingCount = 85,
            ),
        )

    PlaceListContent(
        onBack = {},
        onEditSelection = {},
        placeListState = sampleOptions,
        selectedTiming = DateTime("2024-04-14", TimeSlot("11:00", "14:00")),
        selectedLocation = "Espoo",
        onNavigateToPlaceDetails = {},
    )
}
