package com.meetup.meetingapp.ui.screens.participant_input_flow

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.Timestamp
import com.meetup.meetingapp.MeetingAppTopAppBar
import com.meetup.meetingapp.R
import com.meetup.meetingapp.data.model.DateRange
import com.meetup.meetingapp.data.model.Event
import com.meetup.meetingapp.data.model.EventStatus
import com.meetup.meetingapp.data.model.FoodCategory
import com.meetup.meetingapp.data.model.LocationOption
import com.meetup.meetingapp.data.model.PlaceType
import com.meetup.meetingapp.ui.AppViewModelProvider
import com.meetup.meetingapp.ui.navigation.NavigationDestination
import com.meetup.meetingapp.ui.screens.components.AppMultiSelectDropdown

object PlaceTypeAndKeywordDestination : NavigationDestination {
    override val route = "participant_placeType_and_keyword"
    override val titleRes = R.string.title_place_type_and_keyword
}

/**
 * Screen entry point for selecting place types and food categories as part of
 * the participant input flow.
 *
 * This composable observes the ViewModel state, displays the UI once the event
 * is loaded, and triggers navigation to the submission completion page after
 * the participant submits their choices.
 *
 * @param onBack Callback invoked when the user navigates back.
 * @param viewModel The [ParticipantViewModel] providing UI state and actions.
 * @param onNavigateToSubmissionCompletePage Callback invoked after successful submission.
 */
@Composable
fun PlaceTypeAndKeywordPage(
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    viewModel: ParticipantViewModel = viewModel(factory = AppViewModelProvider.Factory),
    onNavigateToSubmissionCompletePage: () -> Unit,
) {
    val event by viewModel.event.collectAsStateWithLifecycle(null)
    val participantState by viewModel.participantState.collectAsStateWithLifecycle(
        ParticipantInputState()
    )

    event?.let {
        PlaceTypeAndKeywordContent(
            event = it,
            participantState = participantState,
            onBack = onBack,
            onTogglePlaceType = { viewModel.togglePlaceType(it) },
            onToggleFoodCategory = { viewModel.toggleFoodCategory(it) },
            onSubmit = {
                viewModel.submitParticipantInput()
                onNavigateToSubmissionCompletePage()
            },
            modifier = modifier
        )
    }
}

/**
 * UI content for selecting place types and food categories.
 *
 * Displays dropdowns for multi-select options and a submit button. The caller
 * provides the event data, current participant state, and callbacks for
 * navigation and submission.
 *
 * @param event The event containing available place types and food categories.
 * @param participantState Current participant selections.
 * @param onBack Callback invoked when navigating back.
 * @param onTogglePlaceType Callback invoked when a place type is toggled.
 * @param onToggleFoodCategory Callback invoked when a food category is toggled.
 * @param onSubmit Callback invoked when the user presses the submit button.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaceTypeAndKeywordContent(
    event: Event,
    participantState: ParticipantInputState,
    onBack: () -> Unit,
    onTogglePlaceType: (PlaceType) -> Unit,
    onToggleFoodCategory: (FoodCategory) -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier
) {
    Scaffold(
        topBar = {
            MeetingAppTopAppBar(
                title = stringResource(id = R.string.title_place_type_and_keyword),
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
                    "Choose a place type and",
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    "a food category",
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                Spacer(modifier = Modifier.height(48.dp))

                AppMultiSelectDropdown(
                    options = event.placeTypeOptions,
                    selected = participantState.selectedPlaceTypes,
                    onToggle = { onTogglePlaceType(it) },
                    label = "Place type",
                    instruction = "Select place type",
                    toText = { it.name.lowercase().replaceFirstChar { char -> char.uppercase() } }
                )
            }

            item {
                Spacer(modifier = Modifier.height(48.dp))

                AppMultiSelectDropdown(
                    options = FoodCategory.entries,
                    selected = participantState.selectedFoodCategories,
                    onToggle = { onToggleFoodCategory(it) },
                    label = "Food category",
                    instruction = "Select food category",
                    toText = { it.name.lowercase().replaceFirstChar { char -> char.uppercase() } }
                )
            }

            item {
                Spacer(modifier = Modifier.padding(24.dp))

                Button(
                    onClick = onSubmit,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .wrapContentWidth()
                ) {
                    Text(
                        text = "Submit",
                        fontSize = 18.sp,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }
                Spacer(modifier = Modifier.padding(48.dp))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PlaceTypeAndKeywordContentPreview() {
    MaterialTheme {
        PlaceTypeAndKeywordContent(
            event = Event(
                id = "1",
                eventCode = "ABC123",
                eventKey = "key",
                hostId = "host1",
                status = EventStatus.COLLECTING_AVAILABILITY,
                eventTitle = "Team Lunch",
                hostName = "Alice",
                dateRange = DateRange("2026-04-01", "2026-04-07"),
                timeSlots = emptyList(),
                locationOptions = LocationOption(),
                placeTypeOptions = listOf(PlaceType.RESTAURANT, PlaceType.CAFE),
                dateTimeCandidates = emptyList(),
                locationCandidates = emptyList(),
                foodCategoryCandidates = emptyList(),
                restaurantCandidates = emptyList(),
                finalTime = null,
                finalPlace = null,
                createdAt = Timestamp.now()
            ),
            participantState = ParticipantInputState(
                selectedPlaceTypes = listOf(PlaceType.RESTAURANT),
                selectedFoodCategories = listOf(FoodCategory.ITALIAN)
            ),
            onBack = {},
            onTogglePlaceType = {},
            onToggleFoodCategory = {},
            onSubmit = {},
            modifier = Modifier
        )
    }
}