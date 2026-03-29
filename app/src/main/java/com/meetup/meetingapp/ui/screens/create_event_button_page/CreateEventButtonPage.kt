package com.meetup.meetingapp.ui.screens.create_event_button_page

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.meetup.meetingapp.MeetingAppTopAppBar
import com.meetup.meetingapp.R
import com.meetup.meetingapp.ui.AppViewModelProvider
import com.meetup.meetingapp.ui.navigation.NavigationDestination
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.tooling.preview.Preview
import com.meetup.meetingapp.data.model.PlaceType
import com.meetup.meetingapp.ui.screens.EventViewModel

object CreateEventButtonDestination : NavigationDestination {
    override val route = "create_event_button"
    override val titleRes = R.string.title_create_event_button_page
}

/**
 * Entry point composable for the place type selection page.
 *
 * This screen allows the user to choose allowed place types (e.g., restaurant, cafe, bar)
 * as part of the event creation flow.
 *
 * @param onBack Navigate back to the previous screen.
 * @param viewModel [EventViewModel] that manages the event creation UI state.
 * @param onCreatedEvent Callback invoked after triggering the event creation process.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEventButtonPage(
    onBack: () -> Unit,
    viewModel: EventViewModel,
    onCreatedEvent: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    CreateEventButtonContent(
        placeTypes = uiState.placeTypes,
        onPlaceTypeToggle = { type, selected ->
            if (selected) viewModel.addPlaceType(type)
            else viewModel.removePlaceType(type)
        },
        onBack = onBack,
        onCreatedEvent = {
            viewModel.createEvent()
            onCreatedEvent()
        }
    )
}

/**
 * UI content for selecting allowed place types during event creation.
 *
 * @param placeTypes The currently selected place types.
 * @param onPlaceTypeToggle Callback invoked when a place type is selected or deselected.
 * @param onBack Navigate back to the previous screen.
 * @param onCreatedEvent Trigger event creation and navigate forward.
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEventButtonContent(
    placeTypes: List<PlaceType>,
    onPlaceTypeToggle: (PlaceType, Boolean) -> Unit,
    onBack: () -> Unit,
    onCreatedEvent: () -> Unit
) {
    Scaffold(
        topBar = {
            MeetingAppTopAppBar(
                title = "Choose Place Type",
                canNavigateBack = true,
                navigateUp = onBack
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .padding(horizontal = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Choose allowed place types:",
                fontSize = 20.sp,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            PlaceTypeItem(
                title = "Restaurant",
                checked = placeTypes.contains(PlaceType.RESTAURANT),
                onCheckedChange = { selected ->
                    onPlaceTypeToggle(PlaceType.RESTAURANT, selected)
                }
            )

            PlaceTypeItem(
                title = "Cafe",
                checked = placeTypes.contains(PlaceType.CAFE),
                onCheckedChange = { selected ->
                    onPlaceTypeToggle(PlaceType.CAFE, selected)
                }
            )

            PlaceTypeItem(
                title = "Bar",
                checked = placeTypes.contains(PlaceType.BAR),
                onCheckedChange = { selected ->
                    onPlaceTypeToggle(PlaceType.BAR, selected)
                }
            )
            val isAnySelected = placeTypes.isNotEmpty()


            Spacer(modifier = Modifier.height(160.dp))

            Button(
                onClick = onCreatedEvent,
                enabled = isAnySelected,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Create Event",
                    fontSize = 18.sp,
                    modifier = Modifier.padding(4.dp))
            }
        }
    }
}

/**
 * Reusable row component representing a single selectable place type.
 *
 * @param title Display name of the place type.
 * @param checked Whether this place type is currently selected.
 * @param onCheckedChange Callback invoked when the selection state changes.
 */

@Composable
fun PlaceTypeItem(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .toggleable(
                value = checked,
                onValueChange = { onCheckedChange(it) }
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = null
        )
        Text(
            text = title,
            modifier = Modifier.padding(start = 18.dp),
            fontSize = 16.sp
        )
    }
}

/**
 * Preview of the place type selection UI.
 */
@Preview(showBackground = true)
@Composable
fun CreateEventButtonPagePreview() {
    CreateEventButtonContent(
        placeTypes = listOf(),
        onPlaceTypeToggle = { _, _ -> },
        onBack = {},
        onCreatedEvent = {}
    )
}