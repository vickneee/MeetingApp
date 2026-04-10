package com.meetup.meetingapp.ui.screens.vote_for_place_flow

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.meetup.meetingapp.R
import com.meetup.meetingapp.ui.navigation.NavigationDestination

object RestaurantListPageDestination : NavigationDestination {
    override val route = "restaurant-list"
    override val titleRes = R.string.title_restaurant_list
    const val dateTimeArg = "dateTime"
    const val locationArg = "location"
    val routeWithArgs = "$route/{$dateTimeArg}/{$locationArg}"
}

@Composable
fun RestaurantListPage(
    onBack: () -> Unit,
    viewModel: PlaceViewModel,
    onNavigateToRestaurantDetailPage: () -> Unit,
    modifier: Modifier = Modifier
) {
    val restaurants by viewModel.filteredRestaurants.collectAsState()

}



