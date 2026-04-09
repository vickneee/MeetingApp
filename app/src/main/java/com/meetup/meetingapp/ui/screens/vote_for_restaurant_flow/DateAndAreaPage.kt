package com.meetup.meetingapp.ui.screens.vote_for_restaurant_flow

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

import androidx.compose.ui.res.stringResource

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.meetup.meetingapp.MeetingAppTopAppBar
import com.meetup.meetingapp.R
import com.meetup.meetingapp.data.model.DateTime
import com.meetup.meetingapp.ui.navigation.NavigationDestination
import androidx.compose.material3.*

object DateAndAreaPageDestination: NavigationDestination {
    override val route = "date-and-area"
    override val titleRes = R.string.title_date_and_area
}



@Composable
fun DateAndAreaPage(
    onBack: () -> Unit,
    viewModel: RestaurantViewModel,
    onNavigateToRestaurantListPage: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateAndAreaState by viewModel.dateAndAreaState.collectAsStateWithLifecycle(null)

    dateAndAreaState?.let {
        DateAndAreaContent(
            onBack = onBack,
            dateLocationOptions = it.dateLocationOptions,
            navigateToRestaurantListPage = { timing, location -> viewModel.getRestaurants(timing, location)},
            modifier = modifier
        )
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateAndAreaContent(
    onBack: () -> Unit,
    dateLocationOptions: List<DateLocationOption>,
    navigateToRestaurantListPage: (DateTime, String) -> Unit,
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

            item{
                Spacer(modifier = Modifier.height(48.dp))

                Text(
                    "Choose a date & area",
                    fontSize = 20.sp,
                )
            }

            items(dateLocationOptions){option ->
                Button(
                    onClick = { navigateToRestaurantListPage(option.timing, option.location) }
                ) {
                    Text(option.label)
                }
            }

        }
    }

}