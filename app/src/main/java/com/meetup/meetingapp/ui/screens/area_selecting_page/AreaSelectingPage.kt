package com.meetup.meetingapp.ui.screens.area_selecting_page

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.meetup.meetingapp.MeetingAppTopAppBar
import com.meetup.meetingapp.R
import com.meetup.meetingapp.ui.AppViewModelProvider
import com.meetup.meetingapp.ui.navigation.NavigationDestination

/**
 * Navigation destination for the Area Selecting screen.
 */
object AreaSelectingDestination : NavigationDestination {
    override val route = "area_selecting"
    override val titleRes = R.string.title_area_selecting_page
}

/**
 * Area Selecting Page
 * * This screen allows users to search and select a specific area (city)
 * from a searchable dropdown menu.
 *
 * @param onBack Callback to navigate back.
 * @param onNext Callback to navigate to the next screen after selection.
 * @param viewModel The [AreaSelectingViewModel] managing the selection state.
 */
@Composable
fun AreaSelectingPage(
    onBack: () -> Unit,
    onNext: () -> Unit,
    viewModel: AreaSelectingViewModel = viewModel(
        factory = AppViewModelProvider.Factory
    )
) {
    AreaSelectingContent(
        selectedArea = viewModel.selectedArea,
        onAreaChange = viewModel::updateArea,
        onBack = onBack,
        onNextClick = onNext
    )
}

/**
 * Area Selecting Page Content
 * * A stateless composable featuring a searchable Exposed Dropdown Menu.
 *
 * @param selectedArea The currently typed or selected area.
 * @param onAreaChange Callback when the text input changes.
 * @param onBack Callback for the top bar navigation.
 * @param onNextClick Callback for the primary action button.
 * @param modifier The [Modifier] to be applied to the layout.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AreaSelectingContent(
    selectedArea: String,
    onAreaChange: (String) -> Unit,
    onBack: () -> Unit,
    onNextClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val options = listOf("Espoo", "Helsinki", "Vantaa")
    var expanded by remember { mutableStateOf(false) }

    // Filter options based on user input
    val filteredOptions = options.filter { it.contains(selectedArea, ignoreCase = true) }

    Scaffold(
        topBar = {
            MeetingAppTopAppBar(
                title = "Choose Meeting Location",
                canNavigateBack = true,
                navigateUp = onBack
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            item {
                Text(
                    text = "Where is the event?",
                    modifier = Modifier.padding(16.dp),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.padding(8.dp))

                // Searchable Dropdown Menu
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded },
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth(0.8f)
                ) {
                    OutlinedTextField(
                        value = selectedArea,
                        onValueChange = {
                            onAreaChange(it)
                            expanded = true
                        },
                        label = { Text("Search City") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                        modifier = Modifier.menuAnchor(),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )

                    if (filteredOptions.isNotEmpty()) {
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            filteredOptions.forEach { selectionOption ->
                                DropdownMenuItem(
                                    text = { Text(selectionOption) },
                                    onClick = {
                                        onAreaChange(selectionOption)
                                        expanded = false
                                    },
                                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.padding(24.dp))

                Button(
                    onClick = onNextClick,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier,
                    enabled = options.contains(selectedArea) // Only enable if a valid city is selected
                ) {
                    Text(
                        text = "Next",
                        fontSize = 18.sp,
                        modifier = Modifier.padding(4.dp)
                    )
                }
            }
        }
    }
}

/**
 * Preview for the [AreaSelectingContent] composable.
 */
@Preview(showBackground = true)
@Composable
fun AreaSelectingPagePreview() {
    AreaSelectingContent(
        selectedArea = "",
        onAreaChange = {},
        onBack = {},
        onNextClick = {}
    )
}