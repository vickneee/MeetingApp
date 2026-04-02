package com.meetup.meetingapp.ui.screens.participant_input

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
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
            navigateToNextPage = onNavigateToSubmissionCompletePage,
            viewModel = viewModel,
            modifier = modifier
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParticipantPlaceTypeAndKeywordContent(
    event: Event,
    participantState: ParticipantInputState,
    onBack: () -> Unit,
    navigateToNextPage: () -> Unit,
    viewModel: ParticipantViewModel,
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
            item { Spacer(modifier = Modifier.height(48.dp)) }

            item{
                Text(
                    "Choose a place type and",
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item { Spacer(modifier = Modifier.height(8.dp)) }

            item{
                Text(
                    "a food category",
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }

            item {
                MultiSelectDropdown(
                    options = event.placeTypeOptions,
                    selected = participantState.selectedPlaceTypes,
                    onToggle = {viewModel.togglePlaceType(it)},
                    label = "Place type",
                    instruction = "Select place type",
                    toText = {it.toString()}
                )
            }

            item { Spacer(modifier = Modifier.height(132.dp)) }

            item{
                MultiSelectDropdown(
                    options = FoodCategory.entries,
                    selected = participantState.selectedFoodCategories,
                    onToggle = {viewModel.toggleFoodCategory(it)},
                    label = "Food category",
                    instruction = "Select food category",
                    toText = {it.name}
                )
            }
        }
    }

}

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
