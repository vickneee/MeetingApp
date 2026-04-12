package com.meetup.meetingapp.ui.screens.place_details_page

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.meetup.meetingapp.MeetingAppTopAppBar
import com.meetup.meetingapp.ui.AppViewModelProvider
import com.meetup.meetingapp.ui.theme.MeetingAppTheme

/**
 * Entry point for the Place Details screen.
 * This Composable connects the [PlaceDetailsViewModel] to the [PlaceDetailsContent].
 *
 * @param onBack Callback invoked when the user clicks the back button in the top bar.
 * @param viewModel The state holder for this screen, injected via [AppViewModelProvider.Factory].
 */
@Composable
fun PlaceDetailsPage(
    onBack: () -> Unit,
    viewModel: PlaceDetailsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    PlaceDetailsContent(
        uiState = viewModel.uiState,
        onBack = onBack,
        onVoteClick = { viewModel.onVoteClicked() },
        onMapsClick = { viewModel.onViewOnMaps() }
    )
}

/**
 * The stateless UI content for the Place Details screen.
 * Displays information about a specific venue, including an image, rating, and location.
 *
 * @param uiState The data state containing venue details like name, rating, and photo URL.
 * @param onBack Callback for the navigation back action.
 * @param onVoteClick Callback for when the user clicks the vote button.
 * @param onMapsClick Callback for when the user wants to view the location on Google Maps.
 * @param modifier [Modifier] to be applied to the root layout.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaceDetailsContent(
    uiState: PlaceDetailsUiState,
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
        containerColor = Color(0xFFF8F9FA)
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
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(28.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        // Image
                        if (!uiState.photoUrl.isNullOrEmpty()) {
                            AsyncImage(
                                model = uiState.photoUrl,
                                contentDescription = "Visual of ${uiState.name}",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(240.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }

                        // Info Section
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(text = uiState.name, fontSize = 24.sp, fontWeight = FontWeight.Bold)

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = Color(0xFFFFC107),
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "${uiState.rating} (${uiState.reviewCount} reviews)",
                                    fontSize = 16.sp
                                )
                            }
                            Text(
                                text = "${uiState.category} · ${uiState.priceRange}",
                                color = Color.Gray
                            )
                        }

                        HorizontalDivider(color = Color(0xFFF0F0F0), thickness = 1.dp)

                        // Location/Status Section
                        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            Text(
                                text = "Distance: ${uiState.distance}",
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Open until ${uiState.openUntil}",
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Address: ${uiState.address}",
                                fontSize = 15.sp,
                                lineHeight = 24.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

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
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6))
                            ) {
                                Text(
                                    text = "Vote for this restaurant",
                                    fontSize = 18.sp,
                                    modifier = Modifier.padding(6.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Preview provider for [PlaceDetailsContent].
 * Generates a mock restaurant view within the application's theme.
 */
@Preview(showBackground = true)
@Composable
fun PlaceDetailsPreview() {
    MeetingAppTheme {
        PlaceDetailsContent(
            uiState = PlaceDetailsUiState(
                name = "Ravintola Aino",
                rating = 4.5,
                reviewCount = 230,
                category = "Italian",
                priceRange = "$$",
                distance = "1.2 km",
                openUntil = "22:00",
                address = "Iso Omena, Piispansilta 11, Espoo",
                photoUrl = "https://images.unsplash.com/photo-1517248135467-4c7edcad34c4"
            ),
            onBack = {},
            onVoteClick = {},
            onMapsClick = {}
        )
    }
}