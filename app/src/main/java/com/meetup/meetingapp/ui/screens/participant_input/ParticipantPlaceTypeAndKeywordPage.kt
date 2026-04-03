package com.meetup.meetingapp.ui.screens.participant_input

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExposedDropdownMenuDefaults.TrailingIcon
import androidx.compose.material3.MaterialTheme

import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf

import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource

import androidx.compose.ui.text.style.TextAlign

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.meetup.meetingapp.MeetingAppTopAppBar
import com.meetup.meetingapp.R
import com.meetup.meetingapp.data.model.Event
import com.meetup.meetingapp.data.model.FoodCategory

import com.meetup.meetingapp.ui.AppViewModelProvider
import com.meetup.meetingapp.ui.navigation.NavigationDestination

object ParticipantPlaceTypeAndKeywordDestination: NavigationDestination {
    override val route = "participant_placeType_and_keyword"
    override val titleRes = R.string.title_submission_complete
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
fun ParticipantPlaceTypeAndKeywordPage(
    onBack: () -> Unit,
    viewModel: ParticipantViewModel = viewModel(factory = AppViewModelProvider.Factory),
    onNavigateToSubmissionCompletePage: () -> Unit,
    modifier: Modifier = Modifier
) {
    val event by viewModel.event.collectAsStateWithLifecycle(null)
    val participantState by viewModel.participantState.collectAsStateWithLifecycle(
        ParticipantInputState()
    )

    event?.let {
        ParticipantPlaceTypeAndKeywordContent(
            event = it,
            participantState = participantState,
            onBack = onBack,
            viewModel = viewModel,
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
 * @param viewModel The ViewModel used to update participant selections.
 * @param onSubmit Callback invoked when the user presses the submit button.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParticipantPlaceTypeAndKeywordContent(
    event: Event,
    participantState: ParticipantInputState,
    onBack: () -> Unit,
    viewModel: ParticipantViewModel,
    onSubmit: () -> Unit,
    modifier: Modifier
){
    Scaffold(
        topBar = {
            MeetingAppTopAppBar(
                title = stringResource(id = R.string.title_placetype_and_keyword),
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

            item{
                Spacer(modifier = Modifier.height(48.dp))

                Text(
                    "Choose a place type and",
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item{
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    "a food category",
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))

                MultiSelectDropdown(
                    options = event.placeTypeOptions,
                    selected = participantState.selectedPlaceTypes,
                    onToggle = {viewModel.togglePlaceType(it)},
                    label = "Place type",
                    instruction = "Select place type",
                    toText = {it.toString()}
                )
            }

            item{
                Spacer(modifier = Modifier.height(132.dp))

                MultiSelectDropdown(
                    options = FoodCategory.entries,
                    selected = participantState.selectedFoodCategories,
                    onToggle = {viewModel.toggleFoodCategory(it)},
                    label = "Food category",
                    instruction = "Select food category",
                    toText = {it.name}
                )
            }

            item {
                Spacer(modifier = Modifier.padding(108.dp))

                // Center the button and make it only as wide as its content
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Button(
                        onClick = onSubmit,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .wrapContentWidth() // only as wide as text
                    ) {
                        Text(
                            text = "Submit",
                            fontSize = 18.sp,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }
    }

}

/**
 * A reusable multi-select dropdown component.
 *
 * Displays a read-only text field that expands into a dropdown menu containing
 * checkboxes for each option. Selecting an item toggles its presence in the
 * selected list.
 *
 * @param options The full list of selectable items.
 * @param selected The currently selected items.
 * @param onToggle Callback invoked when an item is selected or deselected.
 * @param label A label displayed above the dropdown.
 * @param instruction Placeholder text shown inside the collapsed field.
 * @param toText Converts an option into a displayable string.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> MultiSelectDropdown(
    options: List<T>,
    selected: List<T>,
    onToggle: (T) -> Unit,
    label: String,
    instruction: String,
    toText: (T) -> String
){
    var expanded by rememberSaveable() { mutableStateOf(false) }
    Column {
        Text(text = label)
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            TextField(
                value = "",
                onValueChange = {},
                readOnly = true,
                placeholder = { Text(instruction) },
                trailingIcon = {
                    TrailingIcon(expanded)
                },
                colors = ExposedDropdownMenuDefaults.textFieldColors(),
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .heightIn(max = 200.dp)
            ) {
                options.forEach { option ->
                    val isSelected = option in selected

                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically){
                                Checkbox(
                                    checked = isSelected,
                                    onCheckedChange = null
                                )
                                Text(toText(option))
                            }
                        },
                        onClick = {
                            onToggle(option)
                        }
                    )
                }
            }
        }
    }
}
