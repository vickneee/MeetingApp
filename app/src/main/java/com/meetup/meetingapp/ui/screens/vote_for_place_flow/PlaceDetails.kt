package com.meetup.meetingapp.ui.screens.vote_for_place_flow

import android.R.attr.fontWeight
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.meetup.meetingapp.MeetingAppTopAppBar
import com.meetup.meetingapp.R
import com.meetup.meetingapp.data.model.Restaurant
import com.meetup.meetingapp.ui.navigation.NavigationDestination
import com.meetup.meetingapp.ui.theme.MeetingAppTheme

/**
 * Navigation destination for the Participant MeetUp Detail screen.
 */
object PlaceDetailsDestination : NavigationDestination {
    override val route = "place_detail"
    override val titleRes = R.string.title_participant_dashboard_waiting
    const val placeIdArg = "placeId"
    val routeWithArgs = "$route/{$placeIdArg}"
}

/**
 * High‑level screen that handles logic, Intents, and data collection.
 */
@Composable
fun PlaceDetailsPage(
    onBack: () -> Unit,
    onNavigateToHostDashboard: (String) -> Unit,
    onNavigateToParticipantDashboard: (String) -> Unit,
    viewModel: PlaceViewModel,
    placeId: String
) {
    val context = LocalContext.current

    // Collect restaurant data first to get coordinates
    val restaurant by viewModel.fetchRestaurantDetail(placeId).collectAsState(null)
    val distanceLabel by viewModel.restaurantDistance.collectAsState(initial = null)
    val voteState by viewModel.voteState.collectAsState()
    val voteResultState by viewModel.voteResultState.collectAsState()
    val timing by viewModel.selectedTiming.collectAsState()
    val event by viewModel.event.collectAsState()

    // Handle navigation after successful vote
    LaunchedEffect(voteResultState) {
        if (voteResultState is VoteResultState.VoteSuccess) {
            val currentEvent = event ?: return@LaunchedEffect
            val currentUserId = viewModel.userId
            
            if (currentEvent.hostId == currentUserId) {
                onNavigateToHostDashboard(currentEvent.id)
            } else {
                onNavigateToParticipantDashboard(currentEvent.id)
            }
        }
    }

    // Trigger data loading and GPS calculation
    LaunchedEffect(placeId, restaurant) {
        // This calls the new function in viewmodel to fetch data
        viewModel.loadPlaceData(
            placeId = placeId,
            lat = restaurant?.latitude, // Ensure your Restaurant model has these
            lng = restaurant?.longitude
        )
    }

    val openLabel = if (restaurant != null && timing != null) {
        viewModel.getOpenLabel(restaurant!!, timing!!)
    } else null

    val priceLabel = restaurant?.priceLevel
        ?.let { viewModel.formatPriceLevel(it) }
        ?: ""

    val photoUrl = restaurant?.photoReference
        ?.let { viewModel.buildPhotoUrl(it) }
        ?: ""

    restaurant?.let { r ->
        openLabel?.let { label ->
            PlaceDetailsContent(
                restaurantDetail = r,
                openLabel = label,
                priceLabel = priceLabel,
                photoUrl = photoUrl,
                distanceLabel = distanceLabel ?: "Calculating distance...", // Real GPS data
                isVoted = voteState.isVoted,
                voteResultState = voteResultState,
                onBack = onBack,
                onVoteClick = { viewModel.submitVote(placeId) },
                onMapsClick = {
                    val encodedAddress = Uri.encode(r.address)
                    val gmmIntentUri = Uri.parse("geo:0,0?q=$encodedAddress")
                    val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri).apply {
                        setPackage("com.google.android.apps.maps")
                    }
                    try {
                        context.startActivity(mapIntent)
                    } catch (e: Exception) {
                        val webIntent = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://www.google.com/maps/search/?api=1&query=$encodedAddress")
                        )
                        context.startActivity(webIntent)
                    }
                }
            )
        }
    }
}

/**
 * UI for displaying detailed information about a selected restaurant.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaceDetailsContent(
    restaurantDetail: Restaurant,
    openLabel: String,
    priceLabel: String,
    photoUrl: String,
    distanceLabel: String, // New parameter
    isVoted: Boolean,
    voteResultState: VoteResultState?,
    onBack: () -> Unit,
    onVoteClick: () -> Unit,
    onMapsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            MeetingAppTopAppBar(
                title = "Place Details",
                canNavigateBack = true,
                navigateUp = onBack
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(28.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        // Image Section
                        if (photoUrl.isNotEmpty()) {
                            AsyncImage(
                                model = photoUrl,
                                contentDescription = "Visual of ${restaurantDetail.name}",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(240.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }

                        // Info Section
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(
                                text = restaurantDetail.name,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = Color(0xFFFFC107),
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "${restaurantDetail.rating} (${restaurantDetail.userRatingCount} reviews)",
                                    fontSize = 16.sp
                                )
                            }
                            Text(
                                text = "${restaurantDetail.types?.firstOrNull() ?: "Restaurant"} · $priceLabel",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant,
                            thickness = 1.dp)

                        // Location/Status Section
                        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            Text(
                                text = "Distance: $distanceLabel", // Showing dynamic distance
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Open: $openLabel",
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Address: ${restaurantDetail.address}",
                                fontSize = 15.sp,
                                lineHeight = 24.sp
                            )
                        }

                        // Action Buttons
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            OutlinedButton(
                                onClick = onMapsClick,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(2.dp, Color(0xFF3B82F6))
                            ) {
                                Text(
                                    text = "View on Google Maps",
                                    fontSize = 18.sp,
                                    modifier = Modifier.padding(6.dp)
                                )
                            }

                            Button(
                                onClick = onVoteClick,
                                enabled = !isVoted,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Text(
                                    text = if (isVoted) "Voted" else "Vote for this restaurant",
                                    fontSize = 18.sp,
                                    modifier = Modifier.padding(6.dp)
                                )
                            }

                            if (voteResultState is VoteResultState.VoteError) {
                                Text(
                                    text = voteResultState.message,
                                    color = Color.Red,
                                    fontSize = 14.sp,
                                    modifier = Modifier.align(Alignment.CenterHorizontally)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PlaceDetailsPreview() {
    MeetingAppTheme {
        PlaceDetailsContent(
            restaurantDetail = Restaurant(
                placeId = "123",
                name = "Ravintola Aino",
                rating = 4.5,
                userRatingCount = 230,
                types = listOf("Italian"),
                priceLevel = 2,
                openingHours = listOf("Monday: 4:00PM – 2:00AM"),
                address = "Iso Omena, Piispansilta 11, Espoo",
                photoReference = ""
            ),
            openLabel = "4:00PM – 2:00AM",
            priceLabel = "€€",
            photoUrl = "",
            distanceLabel = "1.2 km",
            isVoted = false,
            voteResultState = null,
            onBack = {},
            onVoteClick = {},
            onMapsClick = {}
        )
    }
}
