package com.meetup.meetingapp.ui.screens.participant_input_flow

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.meetup.meetingapp.MeetingAppTopAppBar
import com.meetup.meetingapp.R
import com.meetup.meetingapp.ui.navigation.NavigationDestination
import com.meetup.meetingapp.ui.theme.AppPadding
import com.meetup.meetingapp.ui.theme.AppSize
import com.meetup.meetingapp.ui.theme.AppSpacing

/**
 * Navigation destination for the Small Area Selection screen.
 */
object SmallAreaSelectingDestination : NavigationDestination {
    override val route = "small_area_selecting"
    override val titleRes = R.string.title_small_area_selection_page
}

/**
 * Small Area Selecting Page
 *
 * This screen displays a list of smaller areas (e.g., districts within a city)
 * and allows the user to select one or more of them. The screen:
 *
 * - Retrieves the event and participant state from [ParticipantViewModel]
 * - Displays a list of available areas based on the selected city
 * - Allows toggling selections
 * - Navigates forward only when at least one area is selected
 *
 * @param onBack Callback to navigate back to the previous screen.
 * @param viewModel The [ParticipantViewModel] providing event and participant state.
 * @param onNext Callback to navigate to the next screen after area selection.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmallAreaSelectingPage(
    onBack: () -> Unit,
    viewModel: ParticipantViewModel,
    onNext: () -> Unit,
) {
    val event by viewModel.event.collectAsStateWithLifecycle(null)
    val participantState by viewModel.participantState.collectAsStateWithLifecycle(
        ParticipantInputState(),
    )

    event?.let {
        SmallAreaSelectingContent(
            cityOptions = it.locationOptions.cities,
            selectedAreas = participantState.selectedLocations,
            onAreaToggle = { viewModel.toggleLocation(it) },
            onBack = onBack,
            onNext = onNext,
        )
    }
}

/**
 * UI content for selecting smaller areas within a city.
 *
 * This composable:
 * - Shows a header explaining the selection
 * - Displays a list of areas using a LazyColumn
 * - Allows selecting or deselecting areas via checkboxes
 * - Enables the "Next" button only when at least one area is selected
 *
 * @param cityOptions List of available areas (strings) to display.
 * @param selectedAreas List of currently selected areas.
 * @param onAreaToggle Callback invoked when an area is toggled.
 * @param onBack Callback for navigating back.
 * @param onNext Callback for navigating to the next step.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmallAreaSelectingContent(
    cityOptions: List<String>,
    selectedAreas: List<String>,
    onAreaToggle: (String) -> Unit,
    onBack: () -> Unit,
    onNext: () -> Unit,
) {
    Scaffold(
        topBar = {
            MeetingAppTopAppBar(
                title = stringResource(id = R.string.title_small_area_selection_page),
                canNavigateBack = true,
                navigateUp = onBack,
            )
        },
    ) { paddingValues ->
        LazyColumn(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(paddingValues),
            contentPadding = AppPadding.pagePadding, // Padding values for the entire screen
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
        ) {
            item {
                Text(
                    text = "Choose the area where you",
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                    modifier =
                        Modifier
                            .fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "prefer to meet",
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(bottom = AppSpacing.md),
                )
            }
            items(cityOptions) { city ->
                AreaItem(
                    title = city,
                    checked = selectedAreas.contains(city),
                    onCheckedChange = { onAreaToggle(city) },
                )
            }

            item {
                val isAnySelected = selectedAreas.isNotEmpty()

                Spacer(modifier = Modifier.height(AppSpacing.lg))

                Button(
                    onClick = onNext,
                    enabled = isAnySelected,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.fillMaxWidth(AppSize.lg),
                    contentPadding = PaddingValues(vertical = AppSpacing.md),
                ) {
                    Text(
                        text = "Next",
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        }
    }
}

/**
 * A reusable row component representing a selectable area.
 *
 * Displays:
 * - A checkbox indicating whether the area is selected
 * - The area's name
 *
 * The entire row is toggleable for better usability.
 *
 * @param title The name of the area.
 * @param checked Whether the area is currently selected.
 * @param onCheckedChange Callback invoked when the selection state changes.
 */

@Composable
fun AreaItem(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth(AppSize.lg)
                .height(56.dp)
                .toggleable(
                    value = checked,
                    onValueChange = { onCheckedChange(it) },
                ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = null,
        )
        Text(
            text = title,
            modifier = Modifier.padding(start = 18.dp),
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

/**
 * Preview for the [SmallAreaSelectingContent] composable.
 */
@Preview(showBackground = true)
@Composable
fun SmallAreaSelectingPreview() {
    SmallAreaSelectingContent(
        cityOptions =
            listOf(
                "City A",
                "City B",
                "City C",
            ),
        selectedAreas = listOf(),
        onAreaToggle = {},
        onBack = {},
        onNext = {},
    )
}
