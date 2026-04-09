package com.meetup.meetingapp.ui.screens.participant_input_flow

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.meetup.meetingapp.MeetingAppTopAppBar
import com.meetup.meetingapp.R
import com.meetup.meetingapp.ui.navigation.NavigationDestination
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
data class UiTimeSlot(val id: Int, val timeRange: String, val isSelected: Boolean)

/**
 * Represents a date and its associated time slots.
 *
 * @property date The date represented as a string (e.g., "Mon, Apr 13").
 * @property timeSlots A list of [UiTimeSlot] representing the available time slots for that date.
 * @constructor Creates a new instance of [DateAvailability].
 * @see UiTimeSlot for more information about time slots.
 */
data class DateAvailability(val date: String, val timeSlots: List<UiTimeSlot>)

/**
 * Navigation destination for the Participant MeetUp Detail screen.
 */
object TimeAvailabilityDestination : NavigationDestination {
    override val route = "participant_time_availability"
    override val titleRes = R.string.title_time_availability_page
}

@Composable
fun AvailabilitySelectingPage(
    onBack: () -> Unit,
    navigateToNextStep: () -> Unit,
    viewModel: ParticipantViewModel
) {
    val dates by viewModel.dates.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    AvailabilitySelectingPageContent(
        onBack = onBack,
        onNext = navigateToNextStep,
        dates = dates,
        isLoading = isLoading,
        onToggleTimeSlot = { date, slotIndex -> viewModel.toggleDateTime(date, slotIndex) },
        modifier = Modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AvailabilitySelectingPageContent(
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    onNext: () -> Unit,
    dates: List<DateAvailability> = emptyList(),
    isLoading: Boolean = false,
    onToggleTimeSlot: (String, Int) -> Unit = { _, _ -> }
) {
    Scaffold(
        topBar = {
            MeetingAppTopAppBar(
                title = "Select Your Availability",
                canNavigateBack = true,
                navigateUp = onBack
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
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
                    Spacer(modifier = Modifier.height(48.dp))

                    Text(
                        text = "Choose all dates and time\nslots you can join",
                        fontSize = 20.sp,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Medium
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(48.dp))
                }

                items(dates) { dateAvailability ->
                    DateCard(
                        availability = dateAvailability,
                        onToggleTimeSlot = { slotIndex -> onToggleTimeSlot(dateAvailability.date, slotIndex) }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = onNext,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .wrapContentWidth() // only as wide as text
                    ) {
                        Text(
                            text = "Next",
                            fontSize = 18.sp,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(48.dp))
                }
            }
        }
    }
}

@Composable
fun DateCard(
    availability: DateAvailability,
    onToggleTimeSlot: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    // Format the date to EU format (dd.MM.yyyy)
    val displayDate = remember(availability.date) {
        try {
            val localDate = LocalDate.parse(availability.date)
            localDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
        } catch (e: Exception) {
            availability.date
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
            .background(Color.White, RoundedCornerShape(8.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = displayDate,
                fontWeight = FontWeight.Bold
            )
            Icon(
                imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
        }

        if (expanded) {
            availability.timeSlots.forEachIndexed { index, slot ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onToggleTimeSlot(index) }
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = slot.isSelected,
                        onCheckedChange = null // Handled by Row clickable
                    )
                    Text(
                        text = slot.timeRange,
                        modifier = Modifier.padding(start = 8.dp),
                        fontSize = 14.sp,
                        color = Color.DarkGray
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun AvailabilitySelectingPageContentPreview() {
    MaterialTheme {
        AvailabilitySelectingPageContent(
            onBack = {},
            onNext = {},
            dates = listOf(
                DateAvailability("2025-04-13", listOf(UiTimeSlot(1, "11:00 - 14:00", false))),
                DateAvailability("2025-04-14", listOf(UiTimeSlot(2, "09:00 - 12:00", true)))
            )
        )
    }
}
