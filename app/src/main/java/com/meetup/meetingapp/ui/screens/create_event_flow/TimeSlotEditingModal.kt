package com.meetup.meetingapp.ui.screens.create_event_flow

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.rememberTimePickerState
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.common.math.LinearTransformation.horizontal
import com.meetup.meetingapp.MeetingAppTopAppBar
import com.meetup.meetingapp.R
import com.meetup.meetingapp.ui.navigation.NavigationDestination
import com.meetup.meetingapp.ui.theme.MeetingAppTheme

/**
 * Navigation destination for the Edit Time Slot screen.
 */
object EditTimeSlotDestination : NavigationDestination {
    override val route = "edit_time_slot"
    override val titleRes = R.string.edit_time_slot
}

private fun toMinutes(time: String): Int {
    val parts = time.split(":")
    return parts[0].toInt() * 60 + parts[1].toInt()
}

/**
 * Edit Time Slot Page
 *
 * This screen allows the user to edit the start and end time of an event.
 * @param index Index of the time slot to edit.
 * @param onBack Navigate back to the previous screen.
 * @param navigateToTimeSlotsSelectingPage Navigate to the Time Slots Selecting Page after saving the time slot.
 * @param viewModel [EventViewModel] that provides and manages the UI state for creating an event.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTimeSlotScreen(
    index: Int,
    onBack: () -> Unit,
    navigateToTimeSlotsSelectingPage: () -> Unit,
    viewModel: EventViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var selectedTime by remember { mutableStateOf("No time selected") }

    var startTime by remember {
        mutableStateOf(
            if (index >= 0) uiState.timeSlots[index].start else "12:00"
        )
    }
    var endTime by remember {
        mutableStateOf(
            if (index >= 0) uiState.timeSlots[index].end else "13:00"
        )
    }
    var showPickerType by remember { mutableStateOf<String?>(null) }

    EditTimeSlotContent(
        modifier = Modifier,
        uiState = uiState,
        startTime = startTime,
        endTime = endTime,
        onStartTimeClick = { showPickerType = "start" },
        onEndTimeClick = { showPickerType = "end" },
        showDialog = showDialog,
        selectedTime = selectedTime,
        showPickerType = showPickerType,
        onBack = onBack,
        onSaveTimeSlot = { start, end ->
            if (index >= 0) viewModel.updateTimeSlot(index, start, end)
            else viewModel.addTimeSlot(start, end)
        },
        navigateToTimeSlotsSelectingPage = navigateToTimeSlotsSelectingPage,
    )

    // Handle Time Picker Dialog
    if (showPickerType != null) {
        AdvancedTimePicker(
            onConfirm = { state ->
                val formattedTime = "%02d:%02d".format(state.hour, state.minute)
                if (showPickerType == "start") startTime = formattedTime
                else endTime = formattedTime
                showPickerType = null
            },
            onDismiss = { showPickerType = null }
        )
    }
}

/**
 * Advanced Time Picker
 * @param onConfirm Callback to handle the selected time.
 * @param onDismiss Callback to dismiss the dialog.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedTimePicker(
    onConfirm: (TimePickerState) -> Unit,
    onDismiss: () -> Unit
) {
    val state = rememberTimePickerState()
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { onConfirm(state) }) { Text("OK") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        text = { TimePicker(state = state) }
    )
}

/**
 * Edit Time Slot Content
 * @param uiState Current UI state containing event title, host name, and other form values.
 * @param startTime Start time of the event.
 * @param endTime End time of the event.
 * @param onStartTimeClick Callback to show the start time picker.
 * @param onEndTimeClick Callback to show the end time picker.
 * @param showDialog Whether to show the time picker dialog.
 * @param selectedTime Selected time.
 * @param showPickerType Type of the selected time (start or end).
 * @param onBack Navigate back to the previous screen.
 * @param onSaveTimeSlot Callback to save the time slot.
 * @param navigateToTimeSlotsSelectingPage Callback to navigate to the Time Slots Selecting Page.
 * @param modifier Optional [Modifier] for layout adjustments.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTimeSlotContent(
    uiState: EventUiState,
    startTime: String,
    endTime: String,
    onStartTimeClick: () -> Unit,
    onEndTimeClick: () -> Unit,
    showDialog: Boolean,
    selectedTime: String,
    showPickerType: String?,
    onBack: () -> Unit,
    onSaveTimeSlot: (String, String) -> Unit,
    navigateToTimeSlotsSelectingPage: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            MeetingAppTopAppBar(
                title = "Edit Time Slot",
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
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Start Time Section
            item {
                Text(
                    text = "Start Time",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(10.dp))
                TimeSelectorField(
                    label = "Start Time",
                    time = startTime,
                    onClick = { onStartTimeClick() } // showPickerType = "start"
                )
                Spacer(modifier = Modifier.height(110.dp))
            }

            // End Time Section
            item {
                Text(
                    text = "End Time",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(10.dp))
                TimeSelectorField(
                    label = "End Time",
                    time = endTime,
                    onClick = { onEndTimeClick() } // showPickerType = "end"
                )
                Spacer(modifier = Modifier.height(50.dp))
            }
            // Calculate minutes
            val startTotal = toMinutes(startTime)
            val endTotal = toMinutes(endTime)

            // Require at least 1 hour
            val isValid = endTotal - startTotal >= 60

            // Validation message
            if (!isValid) {
                item {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(8.dp),
                        tonalElevation = 2.dp,
                        modifier = Modifier
                            .fillMaxWidth().padding(horizontal = 48.dp)
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Warning",
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Start and end time must be \nat least 1 hour apart.",
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 14.sp,
                            )
                        }
                    }
                }
            }


            // Save Button
            item {
                Spacer(modifier = Modifier.height(40.dp))
                Button(
                    onClick = {
                        onSaveTimeSlot(startTime, endTime)
                        navigateToTimeSlotsSelectingPage()
                              },
                    enabled = isValid,
                    modifier = Modifier,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(text = "Save", fontSize = 18.sp,
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 6.dp))
                }
            }
        }
    }
}

/**
 * Time Selector Field
 * @param label Label for the field
 * @param time Time to display
 * @param onClick Callback to show the time picker
 */
@Composable
fun TimeSelectorField(
    label: String,
    time: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
        .padding(horizontal = 16.dp)
        .clickable { onClick() },
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
                text = time,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable(onClick = onClick)
            )
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Edit Time Slot",
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .padding(start = 8.dp)
                    .size(20.dp)
            )
        }
    }
}

@Preview(showBackground = true, name = "Default State")
@Composable
fun EditTimeSlotScreenPreview() {
    MeetingAppTheme {
        Surface {
            EditTimeSlotContent(
                uiState = EventUiState(),
                startTime = "00:00",
                endTime = "00:00",
                onStartTimeClick = {},
                onEndTimeClick = {},
                showDialog = false,
                selectedTime = "No time selected",
                showPickerType = null,
                onBack = {},
                onSaveTimeSlot = { start, end ->
                    // No-op for preview
                },
                navigateToTimeSlotsSelectingPage = {},
                modifier = Modifier
                )
        }
    }
}

