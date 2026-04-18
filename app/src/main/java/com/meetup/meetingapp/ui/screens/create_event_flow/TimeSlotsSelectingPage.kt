package com.meetup.meetingapp.ui.screens.create_event_flow

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.meetup.meetingapp.data.model.TimeSlot
import com.meetup.meetingapp.ui.navigation.NavigationDestination
import com.meetup.meetingapp.ui.theme.MeetingAppTheme

/**
 * Navigation destination for the Time Slots Selecting screen.
 */
object TimeSlotsSelectingPageDestination : NavigationDestination {
    override val route = "time_slots_selecting_page"
    override val titleRes = R.string.title_time_slots_selecting_page
}

/**
 * Time Slots Selecting Page
 * * This screen allows users to select time slots for an event.
 *
 * @param onBack Navigate back to the previous screen.
 * @param navigateToTimeEditPage Navigate to the Time Edit Page after removing a time slot.
 * @param navigateToAreaSelectingPage Navigate to the Area Selecting Page after adding a time slot.
 * @param viewModel [EventViewModel] that provides and manages the UI state for creating an event.
 */
@Composable
fun TimeSlotsSelectingPage(
    onBack: () -> Unit,
    navigateToTimeEditPage: (Int) -> Unit,
    navigateToAreaSelectingPage: () -> Unit,
    viewModel: EventViewModel,
) {
    val uiState by viewModel.uiState.collectAsState()

    TimeSlotsSelectingPageContent(
        modifier = Modifier,
        uiState = uiState,
        onBack = onBack,
        onRemoveTimeSlot = viewModel::removeTimeSlot,
        navigateToTimeEditPage = navigateToTimeEditPage,
        navigateToAreaSelectingPage = navigateToAreaSelectingPage
    )
}

/**
 * UI content for the selecting time slots page.
 *
 * @param uiState Current UI state containing event title, host name, and other form values.
 * @param onBack Navigate back to the previous screen.
 * @param modifier Optional [Modifier] for layout adjustments.
 * @param onRemoveTimeSlot Callback to remove a selected time slot.
 * @param navigateToAreaSelectingPage Callback to navigate to the area selecting page.
 * @param navigateToTimeEditPage Callback to navigate to the time edit page.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeSlotsSelectingPageContent(
    modifier: Modifier = Modifier,
    uiState: EventUiState,
    onBack: () -> Unit,
    onRemoveTimeSlot: (TimeSlot) -> Unit,
    navigateToAreaSelectingPage: () -> Unit,
    navigateToTimeEditPage: (Int) -> Unit
) {
    Scaffold(
        topBar = {
            MeetingAppTopAppBar(
                title = "Select Time Slots",
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
            contentPadding = PaddingValues(
                start = 32.dp,
                end = 32.dp,
                top = 56.dp,
                bottom = 56.dp
            ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            item {
                Text(
                    text = "Time Slots",
                    modifier = Modifier.padding(16.dp),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            items(uiState.timeSlots.size) { index ->
                val timeSlot = uiState.timeSlots[index]

                Row(
                    modifier = Modifier
                        .padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    TimeSlotItem(
                        timeSlot = "${timeSlot.start} - ${timeSlot.end}",
                        onEditClick = { navigateToTimeEditPage(index) },
                        modifier = Modifier
                            .width(220.dp) // Fixed width so delete button fits
                    )

                    if (index != 0) {
                        Spacer(modifier = Modifier.padding(8.dp))
                        IconButton(
                            onClick = { onRemoveTimeSlot(timeSlot) },
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color.Red, shape = RoundedCornerShape(10.dp))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Remove,
                                contentDescription = "Remove",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.padding(8.dp))
                        Spacer(modifier = Modifier.size(40.dp))
                    }
                }
            }
            item {
                Spacer(modifier = Modifier.height(36.dp))
                OutlinedButton(
                    onClick = { navigateToTimeEditPage(-1) }, // <- editTimeSlot()
                    border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth(0.75f)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Add Time Slot",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
            item {
                Spacer(modifier = Modifier.height(36.dp))
                Button(
                    onClick = { navigateToAreaSelectingPage() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth(0.55f),
                ) {
                    Text(
                        text = "Next",
                        fontSize = 18.sp,
                        modifier = Modifier.padding(vertical = 6.dp)
                    )
                }
            }
        }
    }
}

/**
 * Time Slot Item
 * @param timeSlot Time slot to display
 * @param onEditClick Callback to edit the selected time slot
 * @param modifier Optional [Modifier] for layout adjustments
 */
@Composable
fun TimeSlotItem(
    timeSlot: String,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .clickable { onEditClick() },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(vertical = 12.dp, horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = timeSlot,
                modifier = Modifier
                    .padding(horizontal = 16.dp),
                textAlign = TextAlign.Center,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Edit Time Slot",
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/**
 * Preview for the [TimeSlotsSelectingPageContent] composable.
 */
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun TimeSlotsSelectingPagePreview() {
    val mockTimeSlots = listOf(
        TimeSlot(start = "09:00", end = "10:00"),
        TimeSlot(start = "11:30", end = "12:30"),
        TimeSlot(start = "14:00", end = "15:30")
    )

    val mockUiState = EventUiState(
        timeSlots = mockTimeSlots
    )

    MeetingAppTheme {
        TimeSlotsSelectingPageContent(
            uiState = mockUiState,
            onBack = {},
            onRemoveTimeSlot = {},
            navigateToAreaSelectingPage = {},
            navigateToTimeEditPage = {}
        )
    }
}