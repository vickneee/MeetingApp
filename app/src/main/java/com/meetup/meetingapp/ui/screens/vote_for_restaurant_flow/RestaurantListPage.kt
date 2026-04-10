package com.meetup.meetingapp.ui.screens.vote_for_restaurant_flow

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
    val restaurants by viewModel.filteredRestaurants.collectAsState()

}



