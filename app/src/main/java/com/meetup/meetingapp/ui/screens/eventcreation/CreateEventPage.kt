package com.meetup.meetingapp.ui.screens.eventcreation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.meetup.meetingapp.MeetingAppTopAppBar
import com.meetup.meetingapp.R
import com.meetup.meetingapp.data.model.PlaceType
import com.meetup.meetingapp.ui.navigation.NavigationDestination
import com.meetup.meetingapp.ui.theme.AppPadding
import com.meetup.meetingapp.ui.theme.AppSize
import com.meetup.meetingapp.ui.theme.AppSpacing

/**
 * Navigation destination for the place type selection screen.
 */
object CreateEventDestination : NavigationDestination {
    override val route = "create_event_button"
    override val titleRes = R.string.title_create_event_page
}

/**
 * Entry point composable for the place type selection page.
 * @param onBack Navigate back to the previous screen.
 * @param viewModel [EventViewModel] that provides and manages the UI state for creating an event.
 * @param onCreatedEvent Callback to be invoked when the event is created.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEventPage(
    onBack: () -> Unit,
    viewModel: EventViewModel,
    onCreatedEvent: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()

    CreateEventContent(
        placeTypes = uiState.placeTypes,
        onPlaceTypeToggle = { type, selected ->
            if (selected) {
                viewModel.addPlaceType(type)
            } else {
                viewModel.removePlaceType(type)
            }
        },
        onBack = onBack,
        onCreatedEvent = {
            viewModel.createEvent()
            onCreatedEvent()
        },
    )
}

/**
 * UI content for the place type selection page.
 * @param placeTypes List of selected place types.
 * @param onPlaceTypeToggle Callback to toggle the selection state of a place type.
 * @param onBack Navigate back to the previous screen.
 * @param onCreatedEvent Callback to be invoked when the event is created.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEventContent(
    placeTypes: List<PlaceType>,
    onPlaceTypeToggle: (PlaceType, Boolean) -> Unit,
    onBack: () -> Unit,
    onCreatedEvent: () -> Unit,
) {
    Scaffold(
        topBar = {
            MeetingAppTopAppBar(
                title = stringResource(id = R.string.title_create_event_page),
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
            verticalArrangement = Arrangement.Center,
        ) {
            item {
                Text(
                    text = "Choose allowed place types:",
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(bottom = AppSpacing.md),
                )
            }
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(AppSize.xl),
                ) {
                    PlaceTypeItem(
                        title = "Restaurant",
                        checked = placeTypes.contains(PlaceType.RESTAURANT),
                        onCheckedChange = { selected ->
                            onPlaceTypeToggle(PlaceType.RESTAURANT, selected)
                        },
                    )
                    PlaceTypeItem(
                        title = "Cafe",
                        checked = placeTypes.contains(PlaceType.CAFE),
                        onCheckedChange = { selected ->
                            onPlaceTypeToggle(PlaceType.CAFE, selected)
                        },
                    )

                    PlaceTypeItem(
                        title = "Bar",
                        checked = placeTypes.contains(PlaceType.BAR),
                        onCheckedChange = { selected ->
                            onPlaceTypeToggle(PlaceType.BAR, selected)
                        },
                    )
                }
            }

            item {
                val isAnySelected = placeTypes.isNotEmpty()

                Spacer(modifier = Modifier.height(AppSpacing.xl))

                Button(
                    onClick = onCreatedEvent,
                    enabled = isAnySelected,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.fillMaxWidth(AppSize.lg),
                    contentPadding = PaddingValues(vertical = AppSpacing.md),
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "Create Event",
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        }
    }
}

/**
 * Composable for a single place type item.
 * @param title The title of the place type.
 * @param checked Whether the place type is currently selected.
 * @param onCheckedChange Callback to be invoked when the selection state changes.
 */
@Composable
fun PlaceTypeItem(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
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
 * Preview for the [CreateEventContent] composable.
 */
@Preview(showBackground = true)
@Composable
fun CreateEventPagePreview() {
    CreateEventContent(
        placeTypes = listOf(),
        onPlaceTypeToggle = { _, _ -> },
        onBack = {},
        onCreatedEvent = {},
    )
}
