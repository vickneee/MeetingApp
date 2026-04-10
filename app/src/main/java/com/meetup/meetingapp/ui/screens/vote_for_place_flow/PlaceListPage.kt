package com.meetup.meetingapp.ui.screens.vote_for_place_flow

import android.R.attr.label
import android.R.attr.name
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import com.meetup.meetingapp.R
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.meetup.meetingapp.MeetingAppTopAppBar
import com.meetup.meetingapp.data.model.DateTime
import com.meetup.meetingapp.data.model.Restaurant
import com.meetup.meetingapp.ui.navigation.NavigationDestination


object PlaceListPageDestination : NavigationDestination {
    override val route = "place_list"
    override val titleRes = R.string.title_place_list
}

@Composable
fun PlaceListPage(
    onBack: () -> Unit,
    viewModel: PlaceViewModel,
//    restaurantId: String,
    onNavigateToPlaceDetails: () -> Unit,
    modifier: Modifier = Modifier
) {
    val placeListState by viewModel.placeListState.collectAsStateWithLifecycle(null)

    placeListState?.let {
        PlaceListContent(
            onBack = onBack,
            placeListState = it,
//            navigateToPlaceDetails = { /*TODO*/ },
            modifier = modifier
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaceListContent(
    onBack: () -> Unit,
    placeListState: List<Restaurant>,
//    navigateToPlaceDetails = { /*TODO*/ },
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            MeetingAppTopAppBar(
                title = stringResource(id = R.string.title_place_list),
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

            item() {
                Spacer(modifier = Modifier.height(48.dp))

                Text(
                    text = "Apr 14 (11:00-14:00)\nEspoo",
                    fontSize = 20.sp,
                    modifier = Modifier
                        .padding(bottom = 14.dp)
                )
            }
            item {
                Spacer(modifier = Modifier.height(48.dp))

                Text(
                    "Places",
                    fontSize = 20.sp,
                    modifier = Modifier
                        .padding(bottom = 14.dp)
                )
            }
            items(placeListState) { option ->
                Card(
                    onClick = { }, // navigateToPlaceDetails()
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Text(
                        text = option.name,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 20.dp, horizontal = 16.dp),
                        textAlign = TextAlign.Center,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PLaceListContentPreview() {
    val sampleOptions = listOf(
        Restaurant(
//            id = "1",
            name = "Restaurant A"),
//            address = "Iso Omena"
            Restaurant(name = "Restaurant B")
    )

    PlaceListContent(
        onBack = {},
        placeListState = sampleOptions,
    )
}
