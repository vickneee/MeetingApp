package com.meetup.meetingapp.ui.screens.vote_for_restaurant_flow

import androidx.compose.runtime.Composable
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
    viewModel: RestaurantViewModel,
    onNavigateToRestaurantDetailPage: () -> Unit,
    modifier: Modifier = Modifier
) {
}