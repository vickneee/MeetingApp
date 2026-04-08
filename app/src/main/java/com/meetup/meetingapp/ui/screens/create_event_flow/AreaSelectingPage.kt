package com.meetup.meetingapp.ui.screens.create_event_flow

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.meetup.meetingapp.MeetingAppTopAppBar
import com.meetup.meetingapp.R
import com.meetup.meetingapp.data.model.CountryOption
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
 *
 * This screen allows the user to choose a meeting location by:
 * - Selecting a country
 * - Fetching the corresponding city list from Firestore/Room
 * - Selecting one or more cities
 *
 * The UI reacts to the city-fetching state:
 * - Shows a loading screen while fetching
 * - Displays the selection UI on success
 * - Shows an error screen with retry on failure
 *
 * @param onBack Callback to navigate back to the previous screen.
 * @param navigateToCreatingEventPage Callback to navigate to the CreatingEventPage after selections are complete.
 * @param viewModel The [EventViewModel] providing UI state, city data, and selection logic.
 */

@Composable
fun AreaSelectingPage(
    onBack: () -> Unit,
    navigateToCreatingEventPage: () -> Unit,
    viewModel: EventViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    val citiesState by viewModel.citiesState.collectAsState()

    val citiesFetchState by viewModel.citiesFetchState.collectAsState()

    when(citiesFetchState){
        is CitiesFetchState.Loading -> LoadingScreen(modifier = Modifier.fillMaxSize())

        is CitiesFetchState.Success ->
        AreaSelectingContent(
            onCountryChange = { viewModel.selectCountry(it) },
            selectedCountry = uiState.locations.country,
            countryOptions = CountryOption.entries,
            cityOptions = citiesState,
            selectedCities = uiState.locations.cities,
            onCityChange = {viewModel.toggleCity(it)},
            onBack = onBack,
            onNextClick = navigateToCreatingEventPage
        )

        is CitiesFetchState.Error -> {
            val state = citiesFetchState as CitiesFetchState.Error
            ErrorScreen(
                message = state.message,
                onRetry = { viewModel.observeCities(CountryOption.Finland) },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

/**
 * Area Selecting Page Content
 *
 * A stateless composable that allows the user to select a country and one or more cities.
 * The screen provides:
 * - A searchable dropdown for selecting a country.
 * - A searchable multi-select dropdown for choosing cities within that country.
 * - Navigation callbacks for going back or proceeding to the next step.
 *
 * @param onCountryChange Callback invoked when the user selects a country.
 * @param selectedCountry The currently selected country name.
 * @param countryOptions The list of available countries to display in the dropdown.
 * @param cityOptions The list of available cities for the selected country.
 * @param selectedCities The list of currently selected cities.
 * @param onCityChange Callback invoked when the user selects or deselects a city.
 * @param onBack Callback for the top bar back navigation.
 * @param onNextClick Callback for the primary action button.
 * @param modifier Modifier applied to the root layout.
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AreaSelectingContent(
    onCountryChange: (CountryOption) -> Unit,
    selectedCountry: String,
    countryOptions: List<CountryOption>,
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

                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ){
                ExposedDropdownMenuBox(
                    expanded = countryExpanded,
                    onExpandedChange = { countryExpanded = !countryExpanded }
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
                                    onCountryChange(option)
                                    countryExpanded = false
                                },
                                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                            )
                        }
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

                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    // Searchable Dropdown Menu
                    ExposedDropdownMenuBox(
                        expanded = cityExpanded,
                        onExpandedChange = { cityExpanded = true }
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

                        val scrollState = rememberScrollState()

                        ExposedDropdownMenu(
                            expanded = cityExpanded,
                            onDismissRequest = { cityExpanded = false },
                                    scrollState = scrollState,
                            modifier = Modifier
                                .heightIn(max = 300.dp)
                                .verticalScroll(scrollState)
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
                                            Spacer(modifier = Modifier.width(4.dp))
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