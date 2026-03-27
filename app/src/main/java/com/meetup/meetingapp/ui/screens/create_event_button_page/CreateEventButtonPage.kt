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

object CreateEventButtonDestination : NavigationDestination {
    override val route = "create_event_button"
    override val titleRes = R.string.title_create_event_button_page
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEventButtonPage(
    onBack: () -> Unit,
    viewModel: CreateEventButtonViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    CreateEventButtonContent(
        restaurant = viewModel.restaurant,
        cafe = viewModel.cafe,
        bar = viewModel.bar,
        onRestaurantChange = viewModel::updateRestaurant,
        onCafeChange = viewModel::updateCafe,
        onBarChange = viewModel::updateBar,
        onBack = onBack,
        onCreateEvent = { viewModel.createEvent() }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEventButtonContent(
    restaurant: Boolean,
    cafe: Boolean,
    bar: Boolean,
    onRestaurantChange: (Boolean) -> Unit,
    onCafeChange: (Boolean) -> Unit,
    onBarChange: (Boolean) -> Unit,
    onBack: () -> Unit,
    onCreateEvent: () -> Unit
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

            PlaceTypeItem(title = "Restaurant", checked = restaurant, onCheckedChange = onRestaurantChange)
            PlaceTypeItem(title = "Cafe", checked = cafe, onCheckedChange = onCafeChange)
            PlaceTypeItem(title = "Bar", checked = bar, onCheckedChange = onBarChange)

            val isAnySelected = restaurant || cafe || bar

            Spacer(modifier = Modifier.height(160.dp))

            Button(
                onClick = onCreateEvent,
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
            modifier = Modifier.padding(start = 18.dp),
            fontSize = 16.sp
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CreateEventButtonPagePreview() {
    CreateEventButtonContent(
        restaurant = true,
        cafe = false,
        bar = true,
        onRestaurantChange = {},
        onCafeChange = {},
        onBarChange = {},
        onBack = {},
        onCreateEvent = {}
    )
}