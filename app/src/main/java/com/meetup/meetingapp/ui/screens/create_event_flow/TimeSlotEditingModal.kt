package com.meetup.meetingapp.ui.screens.create_event_flow

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.meetup.meetingapp.MeetingAppTopAppBar
import com.meetup.meetingapp.R
import com.meetup.meetingapp.ui.navigation.NavigationDestination
import com.meetup.meetingapp.ui.theme.AppPadding
import com.meetup.meetingapp.ui.theme.AppSize
import com.meetup.meetingapp.ui.theme.AppSpacing
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
    viewModel: EventViewModel,
) {
    val uiState by viewModel.uiState.collectAsState()

    var startTime by remember {
        mutableStateOf(
            if (index >= 0) uiState.timeSlots[index].start else "13:00",
        )
    }
    var endTime by remember {
        mutableStateOf(
            if (index >= 0) uiState.timeSlots[index].end else "16:00",
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
        showDialog = false,
        selectedTime = "",
        showPickerType = showPickerType,
        onBack = onBack,
        onSaveTimeSlot = { start, end ->
            if (index >= 0) {
                viewModel.updateTimeSlot(index, start, end)
            } else {
                viewModel.addTimeSlot(start, end)
            }
        },
        navigateToTimeSlotsSelectingPage = navigateToTimeSlotsSelectingPage,
    )

    // Handle Time Picker Dialog
    if (showPickerType != null) {
        AdvancedTimePicker(
            initialHour = if (showPickerType == "start") startTime.split(":")[0].toInt() else endTime.split(":")[0].toInt(),
            initialMinute = if (showPickerType == "start") startTime.split(":")[1].toInt() else endTime.split(":")[1].toInt(),
            onConfirm = { state ->
                val formattedTime = "%02d:%02d".format(state.hour, state.minute)
                if (showPickerType == "start") {
                    startTime = formattedTime
                } else if (showPickerType == "end") {
                    endTime = formattedTime
                }
                showPickerType = null
            },
            onDismiss = { showPickerType = null },
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
    initialHour: Int = 0,
    initialMinute: Int = 0,
    onConfirm: (TimePickerState) -> Unit,
    onDismiss: () -> Unit,
) {
    val state = rememberTimePickerState(initialHour = initialHour, initialMinute = initialMinute)
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { onConfirm(state) }) { Text("OK") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        text = { TimePicker(state = state) },
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
                title = stringResource(R.string.edit_time_slot),
                canNavigateBack = true,
                navigateUp = onBack,
            )
        },
    ) { paddingValues ->
        LazyColumn(
            modifier =
                modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(paddingValues),
            contentPadding = AppPadding.pagePadding, // Padding values for the entire screen
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            item {
                Text(
                    text = "Edit Time Slot",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
            // Start Time Section
            item {
                Text(
                    text = "Start Time",
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                    style = MaterialTheme.typography.labelLarge,
                    textAlign = TextAlign.Start,
                )
                TimeSelectorField(
                    label = "Start Time",
                    time = startTime,
                    onClick = { onStartTimeClick() },
                    textAlign = TextAlign.Start,
                )
                Spacer(modifier = Modifier.height(AppSpacing.xxs))
            }
            // End Time Section
            item {
                Text(
                    text = "End Time",
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                    style = MaterialTheme.typography.labelLarge,
                    textAlign = TextAlign.Start,
                )
                TimeSelectorField(
                    label = "End Time",
                    time = endTime,
                    onClick = { onEndTimeClick() },
                    textAlign = TextAlign.Start,
                )
            }
            // Calculate minutes
            val startTotal = toMinutes(startTime)
            val endTotal = toMinutes(endTime)

            // Require at least 1 hour
            val isValid = endTotal - startTotal >= 60

            // Validation message
            if (!isValid) {
                item {
                    Spacer(modifier = Modifier.height(AppSpacing.xl))
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(8.dp),
                        tonalElevation = 2.dp,
                        modifier =
                            Modifier
                                .fillMaxWidth(AppSize.lg),
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Warning",
                                tint = MaterialTheme.colorScheme.error,
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

            item {
                Spacer(modifier = Modifier.height(AppSpacing.xl))
                Button(
                    onClick = {
                        onSaveTimeSlot(startTime, endTime)
                        navigateToTimeSlotsSelectingPage()
                    },
                    enabled = isValid,
                    shape = RoundedCornerShape(8.dp),
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor =
                                MaterialTheme
                                    .colorScheme.primary,
                        ),
                    modifier = Modifier.fillMaxWidth(AppSize.lg),
                    contentPadding = PaddingValues(vertical = AppSpacing.md),
                ) {
                    Text(
                        text = "Save",
                        style = MaterialTheme.typography.labelLarge,
                    )
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
 * @param textAlign Text alignment for the time field
 */
@Composable
fun TimeSelectorField(
    label: String,
    time: String,
    onClick: () -> Unit,
    textAlign: TextAlign,
) {
    Row(
        modifier = Modifier.fillMaxWidth(AppSize.lg),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Card(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(8.dp),
            colors =
                CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        ) {
            Row(
                modifier =
                    Modifier
                        .padding(vertical = 14.dp, horizontal = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = time,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    modifier =
                        Modifier
                            .weight(1f)
                            .padding(horizontal = 16.dp, vertical = 2.dp),
                    textAlign = textAlign,
                )
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Select $label",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
        Spacer(modifier = Modifier.width(10.dp))
        IconButton(
            onClick = onClick,
            modifier =
                Modifier
                    .size(40.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(8.dp),
                    ),
        ) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Edit $label",
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(24.dp),
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
                onSaveTimeSlot = { _, _ -> },
                navigateToTimeSlotsSelectingPage = {},
            )
        }
    }
}
