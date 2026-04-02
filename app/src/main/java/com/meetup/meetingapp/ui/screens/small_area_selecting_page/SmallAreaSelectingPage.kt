package com.meetup.meetingapp.ui.screens.small_area_selecting_page

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.meetup.meetingapp.MeetingAppTopAppBar
import com.meetup.meetingapp.R
import com.meetup.meetingapp.ui.navigation.NavigationDestination
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.tooling.preview.Preview
import com.meetup.meetingapp.data.model.PlaceType
import com.meetup.meetingapp.ui.screens.EventViewModel

object SmallAreaSelectingDestination : NavigationDestination {
    override val route = "small_area_selecting"
    override val titleRes = R.string.title_small_area_selection_page
}

/**
 * Screen for selecting specific metropolitan areas (Espoo, Helsinki, Vantaa).
 * Uses a LazyColumn for better performance and scrollability.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmallAreaSelectingPage(
    onBack: () -> Unit,
    viewModel: EventViewModel,
    onNext: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    SmallAreaSelectingContent(
        selectedAreas = uiState.placeTypes,
        onAreaToggle = { area, selected ->
            if (selected) viewModel.addPlaceType(area)
            else viewModel.removePlaceType(area)
        },
        onBack = onBack,
        onNext = onNext
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmallAreaSelectingContent(
    selectedAreas: List<PlaceType>,
    onAreaToggle: (PlaceType, Boolean) -> Unit,
    onBack: () -> Unit,
    onNext: () -> Unit
) {
    Scaffold(
        topBar = {
            MeetingAppTopAppBar(
                title = "Select Area",
                canNavigateBack = true,
                navigateUp = onBack
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .padding(horizontal = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Header Section
            item {
                Text(
                    text = "Choose area where you prefer to meet",
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 32.dp)
                )
            }

            // Espoo Selection
            item {
                AreaItem(
                    title = "Espoo",
                    checked = selectedAreas.contains(PlaceType.RESTAURANT),
                    onCheckedChange = { onAreaToggle(PlaceType.RESTAURANT, it) }
                )
            }

            // Helsinki Selection
            item {
                AreaItem(
                    title = "Helsinki",
                    checked = selectedAreas.contains(PlaceType.CAFE),
                    onCheckedChange = { onAreaToggle(PlaceType.CAFE, it) }
                )
            }

            // Vantaa Selection
            item {
                AreaItem(
                    title = "Vantaa",
                    checked = selectedAreas.contains(PlaceType.BAR),
                    onCheckedChange = { onAreaToggle(PlaceType.BAR, it) }
                )
            }

            // Footer Section (Button)
            item {
                val isAnySelected = selectedAreas.isNotEmpty()

                Spacer(modifier = Modifier.height(80.dp))

                Button(
                    onClick = onNext,
                    enabled = isAnySelected,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text(
                        text = "Next",
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(imageVector = Icons.Default.ArrowForward, contentDescription = null)
                }
            }
        }
    }
}

/**
 * Reusable row component for an area selection.
 */
@Composable
fun AreaItem(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
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
            fontSize = 18.sp
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SmallAreaSelectingPreview() {
    SmallAreaSelectingContent(
        selectedAreas = listOf(),
        onAreaToggle = { _, _ -> },
        onBack = {},
        onNext = {}
    )
}