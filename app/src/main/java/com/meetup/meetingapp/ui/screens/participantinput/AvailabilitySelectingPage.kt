package com.meetup.meetingapp.ui.screens.participantinput

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.meetup.meetingapp.MeetingAppTopAppBar
import com.meetup.meetingapp.R
import com.meetup.meetingapp.data.model.DateTime
import com.meetup.meetingapp.data.model.TimeSlot
import com.meetup.meetingapp.ui.navigation.NavigationDestination
import com.meetup.meetingapp.ui.screens.components.AppMultiSelectDropdown
import com.meetup.meetingapp.ui.screens.eventcreation.LoadingScreen
import com.meetup.meetingapp.ui.theme.AppPadding
import com.meetup.meetingapp.ui.theme.AppSize
import com.meetup.meetingapp.ui.theme.AppSpacing
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Represents a time slot with its associated information.
 *
 * @property id Unique identifier for the time slot.
 * @property timeRange The time range represented as a string (e.g., "10:00 - 12:00").
 * @property isSelected Indicates whether the time slot is currently selected.
 * @constructor Creates a new instance of [UiTimeSlot].
 */
data class UiTimeSlot(
    val id: Int,
    val timeRange: String,
    val isSelected: Boolean,
)

/**
 * Represents a date and its associated time slots.
 *
 * @property date The date represented as a string (e.g., "Mon, Apr 13").
 * @property timeSlots A list of [UiTimeSlot] representing the available time slots for that date.
 * @constructor Creates a new instance of [DateAvailability].
 * @see UiTimeSlot for more information about time slots.
 */
data class DateAvailability(
    val date: String,
    val timeSlots: List<UiTimeSlot>,
)

/**
 * Navigation destination for the Participant MeetUp Detail screen.
 */
object TimeAvailabilityDestination : NavigationDestination {
    override val route = "participant_time_availability"
    override val titleRes = R.string.title_time_availability_page
}

/**
 * Represents the content of the Availability Selecting Page.
 *
 * @param onBack Navigate back.
 * @param navigateToNextStep Navigate to the next step.
 * @param viewModel [ParticipantViewModel] to retrieve event data.
 */
@Composable
fun AvailabilitySelectingPage(
    onBack: () -> Unit,
    navigateToNextStep: () -> Unit,
    viewModel: ParticipantViewModel,
) {
    val allDateTimes by viewModel.allAvailableDateTimes.collectAsState()
    val participantState by viewModel.participantState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    AvailabilitySelectingPageContent(
        onBack = onBack,
        onNext = navigateToNextStep,
        allDateTimes = allDateTimes,
        selectedDateTimes = participantState.selectedDateTimes,
        isLoading = isLoading,
        onToggleDateTime = { viewModel.toggleDateTime(it) },
        modifier = Modifier,
    )
}

/**
 * Represents the content of the Availability Selecting Page.
 * @param modifier Modifier.
 * @param onBack Navigate back.
 * @param onNext Navigate to the next step.
 * @param allDateTimes List of all available date and time slots.
 * @param selectedDateTimes List of selected date and time slots.
 * @param isLoading Whether the content is loading.
 * @param onToggleDateTime Callback to toggle the selection of a date and time slot.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AvailabilitySelectingPageContent(
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    onNext: () -> Unit,
    allDateTimes: List<DateTime> = emptyList(),
    selectedDateTimes: List<DateTime> = emptyList(),
    isLoading: Boolean = false,
    onToggleDateTime: (DateTime) -> Unit = {},
) {
    Scaffold(
        topBar = {
            MeetingAppTopAppBar(
                title = stringResource(id = R.string.title_time_availability_page),
                canNavigateBack = true,
                navigateUp = onBack,
            )
        },
    ) { paddingValues ->
        Crossfade(targetState = isLoading, label = "availability_loading") { loading ->
            if (loading) {
                LoadingScreen(modifier = Modifier.fillMaxSize())
            } else {
                LazyColumn(
                    modifier =
                        modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background)
                            .padding(paddingValues),
                    contentPadding = AppPadding.pagePadding, // Padding values for the entire screen
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    item {
                        Text(
                            text = "Choose all dates and time",
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.titleMedium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Spacer(modifier = Modifier.height(AppSpacing.xs))

                        Text(
                            "slots you can join",
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.titleMedium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Spacer(modifier = Modifier.height(AppSpacing.lg))
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            AppMultiSelectDropdown(
                                options = allDateTimes,
                                selected = selectedDateTimes,
                                onToggle = onToggleDateTime,
                                label = "Availability",
                                instruction = "Select availability",
                                toText = { dateTime ->
                                    val displayDate =
                                        try {
                                            val localDate = LocalDate.parse(dateTime.date)
                                            localDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
                                        } catch (_: Exception) {
                                            dateTime.date
                                        }
                                    "$displayDate: ${dateTime.timeSlot.start} - ${dateTime.timeSlot.end}"
                                },
                            )
                            Spacer(modifier = Modifier.height(AppSpacing.xl))

                            Button(
                                onClick = onNext,
                                enabled = selectedDateTimes.isNotEmpty(),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth(AppSize.lg),
                                contentPadding = PaddingValues(vertical = AppSpacing.md),
                            ) {
                                Text(
                                    text = "Next",
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    style = MaterialTheme.typography.labelLarge,
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
 * Preview for the [AvailabilitySelectingPageContent] composable.
 */
@Preview(showBackground = true)
@Composable
fun AvailabilitySelectingPageContentPreview() {
    MaterialTheme {
        AvailabilitySelectingPageContent(
            isLoading = false,
            onBack = {},
            onNext = {},
            allDateTimes =
                listOf(
                    DateTime("2025-04-13", TimeSlot("11:00", "14:00")),
                    DateTime("2025-04-14", TimeSlot("09:00", "12:00")),
                ),
        )
    }
}
