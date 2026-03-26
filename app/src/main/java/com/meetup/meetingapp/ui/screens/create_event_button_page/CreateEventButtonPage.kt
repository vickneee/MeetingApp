package com.meetup.meetingapp.ui.screens.create_event_button_page

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.meetup.meetingapp.MeetingAppTopAppBar
import com.meetup.meetingapp.R
import com.meetup.meetingapp.ui.AppViewModelProvider
import com.meetup.meetingapp.ui.navigation.NavigationDestination

object CreateEventButtonDestination : NavigationDestination {
    override val route = "create_event_button"
    override val titleRes = R.string.title_create_event_button_page
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEventButtonPage(
    onBack: () -> Unit,
    // Connect to the ViewModel and the Factory
    viewModel: CreateEventButtonViewModel = viewModel(factory = AppViewModelProvider.Factory)
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
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Center
        ) {

            Text(
                text = "Choose allowed place types:",
                fontSize = 18.sp,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            PlaceTypeItem(
                title = "Restaurant",
                checked = viewModel.restaurant,
                onCheckedChange = { viewModel.updateRestaurant(it) }
            )

            PlaceTypeItem(
                title = "Cafe",
                checked = viewModel.cafe,
                onCheckedChange = { viewModel.updateCafe(it) }
            )

            PlaceTypeItem(
                title = "Bar",
                checked = viewModel.bar,
                onCheckedChange = { viewModel.updateBar(it) }
            )

            val isAnySelected = viewModel.restaurant || viewModel.cafe || viewModel.bar

            Spacer(modifier = Modifier.height(40.dp))

            Button(
                onClick = { viewModel.createEvent() },
                enabled = isAnySelected, // Button dims if nothing is checked
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF3B82F6)
                ),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Create Event")
            }
        }
    }
}

/**
 * Reusable row for the checkboxes
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
            modifier = Modifier.padding(start = 16.dp),
            fontSize = 16.sp
        )
    }
}