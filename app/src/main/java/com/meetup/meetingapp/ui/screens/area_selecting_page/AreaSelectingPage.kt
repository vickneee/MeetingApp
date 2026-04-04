package com.meetup.meetingapp.ui.screens.area_selecting_page

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.ExposedDropdownMenuDefaults.TrailingIcon
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
import com.meetup.meetingapp.data.model.CountryOptions
import com.meetup.meetingapp.ui.AppViewModelProvider
import com.meetup.meetingapp.ui.navigation.NavigationDestination
import com.meetup.meetingapp.ui.screens.EventViewModel

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
 * @param navigateToCreatingEventPage Callback to navigate to the CreatingEventPage screen after selection.
 * @param viewModel The [AreaSelectingViewModel] managing the selection state.
 */
@Composable
fun AreaSelectingPage(
    onBack: () -> Unit,
    navigateToCreatingEventPage: () -> Unit,
    viewModel: EventViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    AreaSelectingContent(
        onCountryChange = { viewModel.selectCountry(it) },
        selectedCountry = uiState.locations.country,
        countryOptions = CountryOptions.entries,
        cityOptions = listOf("Helsinki", "Espoo", "Vantaa"),
        selectedCities = uiState.locations.cities,
        onCityChange = {viewModel.toggleCity(it)},
        onBack = onBack,
        onNextClick = navigateToCreatingEventPage
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
    onCountryChange: (String) -> Unit,
    selectedCountry: String,
    countryOptions: List<CountryOptions>,
    cityOptions: List<String>,
    selectedCities: List<String>,
    onCityChange: (String) -> Unit,
    onBack: () -> Unit,
    onNextClick: () -> Unit,
    modifier: Modifier = Modifier
) {

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
            verticalArrangement = Arrangement.Top
        ) {
            item {
                Spacer(modifier = Modifier.padding(32.dp))

                Text(
                    text = "Select Country",
                    modifier = Modifier.padding(16.dp),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            item{
                Spacer(modifier = Modifier.padding(8.dp))

                var countryExpanded by remember { mutableStateOf(false) }

                // Searchable Dropdown Menu
                ExposedDropdownMenuBox(
                    expanded = countryExpanded,
                    onExpandedChange = { countryExpanded = !countryExpanded },
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth(0.8f)
                ) {
                    TextField(
                        value = selectedCountry,
                        onValueChange = {},
                        label = { Text(text = "Search Country") },
                        trailingIcon = { TrailingIcon(expanded = countryExpanded) },
                        colors = ExposedDropdownMenuDefaults.textFieldColors(),
                        modifier = Modifier.menuAnchor(),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )


                    ExposedDropdownMenu(
                        expanded = countryExpanded,
                        onDismissRequest = { countryExpanded = false }
                    ) {
                        countryOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option.toString()) },
                                onClick = {
                                    onCountryChange(option.toString())
                                    countryExpanded = false
                                },
                                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                            )
                        }
                    }
                    }
                }

            item {
                Spacer(modifier = Modifier.padding(32.dp))

                Text(
                    text = "Select Cities",
                    modifier = Modifier.padding(16.dp),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            item {
                Spacer(modifier = Modifier.padding(8.dp))

                var cityExpanded by remember { mutableStateOf(false) }
                var cityQuery by remember { mutableStateOf("") }

                // Searchable Dropdown Menu
                ExposedDropdownMenuBox(
                    expanded = cityExpanded,
                    onExpandedChange = { cityExpanded = true},
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth(0.8f)
                ) {
                    TextField(
                        value = cityQuery,
                        onValueChange = {
                            cityQuery = it
                            cityExpanded = true
                        },
                        readOnly = false,
                        placeholder = { Text("Type city’s name") },
                        trailingIcon = { TrailingIcon(expanded = cityExpanded) },
                        colors = ExposedDropdownMenuDefaults.textFieldColors(),
                        modifier = Modifier.menuAnchor(),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )

                    val filteredCities = cityOptions.filter {
                        it.contains(cityQuery, ignoreCase = true)
                    }

                    ExposedDropdownMenu(
                        expanded = cityExpanded,
                        onDismissRequest = { cityExpanded = false }
                    ) {
                        filteredCities.forEach { option ->
                            val isSelected = option in selectedCities
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Checkbox(
                                            checked = isSelected,
                                            onCheckedChange = null
                                        )
                                        Text(option)
                                    }
                                },
                                onClick = {
                                    onCityChange(option)
                                },
                                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                            )
                        }
                    }
                }

            }
            item {
                Spacer(modifier = Modifier.padding(48.dp))

                Button(
                    onClick = onNextClick,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier,
                    enabled = selectedCities.isNotEmpty() // Only enable if a valid city is selected
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
        onCountryChange = {},
        selectedCountry = "",
        countryOptions = listOf(),
        cityOptions = listOf(),
        selectedCities = listOf(),
        onCityChange = {},
        onBack = {},
        onNextClick = {},
        modifier = Modifier
    )
}