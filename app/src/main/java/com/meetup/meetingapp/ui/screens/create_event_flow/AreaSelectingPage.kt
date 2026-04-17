package com.meetup.meetingapp.ui.screens.create_event_flow

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.meetup.meetingapp.MeetingAppTopAppBar
import com.meetup.meetingapp.R
import com.meetup.meetingapp.data.model.CountryOption
import com.meetup.meetingapp.ui.navigation.NavigationDestination
import com.meetup.meetingapp.ui.screens.components.AppMultiSelectDropdown
import com.meetup.meetingapp.ui.theme.MeetingAppTheme

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
 * - Selecting one or more countries
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
 *
 * @see EventViewModel
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

    when (citiesFetchState) {
        is CitiesFetchState.Loading -> LoadingScreen(modifier = Modifier.fillMaxSize())

        is CitiesFetchState.Success ->
            AreaSelectingContent(
                onCountryToggle = { viewModel.toggleCountry(it) },
                selectedCountries = uiState.locations.countries,
                countryOptions = CountryOption.entries,
                cityOptions = citiesState,
                selectedCities = uiState.locations.cities,
                onCityChange = { viewModel.toggleCity(it) },
                onBack = onBack,
                onNextClick = navigateToCreatingEventPage
            )

        is CitiesFetchState.Error -> {
            val state = citiesFetchState as CitiesFetchState.Error
            ErrorScreen(
                message = state.message,
                onRetry = { viewModel.observeCities(listOf(CountryOption.Finland)) },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

/**
 * Area Selecting Page Content
 *
 * A stateless composable that allows the user to select one or more countries and cities.
 * The screen provides:
 * - A searchable multi-select dropdown for selecting countries.
 * - A searchable multi-select dropdown for choosing cities within the selected countries.
 * - Navigation callbacks for going back or proceeding to the next step.
 *
 * @param onCountryToggle Callback invoked when the user toggles a country.
 * @param selectedCountries The currently selected country names.
 * @param countryOptions The list of available countries to display in the dropdown.
 * @param cityOptions The list of available cities for the selected countries.
 * @param selectedCities The list of currently selected cities.
 * @param onCityChange Callback invoked when the user selects or deselects a city.
 * @param onBack Callback for the top bar back navigation.
 * @param onNextClick Callback for the primary action button.
 * @param modifier Modifier applied to the root layout.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AreaSelectingContent(
    onCountryToggle: (CountryOption) -> Unit,
    selectedCountries: List<String>,
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
                .padding(paddingValues)
                .padding(horizontal = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            item {
                Spacer(modifier = Modifier.height(36.dp))

                Text(
                    "Choose a country and",
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    "select cities",
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                Spacer(modifier = Modifier.padding(24.dp))

                AppMultiSelectDropdown(
                    options = countryOptions,
                    selected = countryOptions.filter { it.name in selectedCountries },
                    onToggle = onCountryToggle,
                    label = "Select Countries",
                    instruction = "Search Countries",
                    toText = { it.toString() }
                )
            }

            item {
                Spacer(modifier = Modifier.padding(24.dp))

                AppMultiSelectDropdown(
                    options = cityOptions,
                    selected = selectedCities,
                    onToggle = onCityChange,
                    label = "Select Cities",
                    instruction = "Type city's name",
                    toText = { it }
                )
            }

            item {
                Spacer(modifier = Modifier.padding(24.dp))

                Button(
                    onClick = onNextClick,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
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
                Spacer(modifier = Modifier.padding(48.dp))
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
    MeetingAppTheme {
        AreaSelectingContent(
            onCountryToggle = {},
            selectedCountries = listOf(),
            countryOptions = listOf(),
            cityOptions = listOf(),
            selectedCities = listOf(),
            onCityChange = {},
            onBack = {},
            onNextClick = {},
            modifier = Modifier
        )
    }
}