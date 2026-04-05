package com.meetup.meetingapp.ui.screens.create_event_flow

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.meetup.meetingapp.MeetingAppTopAppBar
import com.meetup.meetingapp.R
import com.meetup.meetingapp.ui.navigation.NavigationDestination

/**
 * Navigation destination for the Edit Time Slot screen.
 */
object EditTimeSlotDestination : NavigationDestination {
    override val route = "edit_time_slot"
    override val titleRes = R.string.edit_time_slot
}

/**
 * Edit Time Slot Page
 *
 * This screen allows the user to edit the start and end time of an event.
 *
 * @param onBack Navigate back to the previous screen.
 * @param navigateToTimeSlotsSelectingPage Navigate to the Time Slots Selecting Page after saving the time slot.
 * @param viewModel [EventViewModel] that provides and manages the UI state for creating an event.
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTimeSlotScreen(
    onBack: () -> Unit,
    navigateToTimeSlotsSelectingPage: () -> Unit,
    viewModel: EventViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    var startTime by remember { mutableStateOf("00:00") }
    var endTime by remember { mutableStateOf("00:00") }
    var showDialog by remember { mutableStateOf(false) }
    var selectedTime by remember { mutableStateOf("No time selected") }
    // Logic to track which picker is open
    var showPickerType by remember { mutableStateOf<String?>(null) } // "start" or "end"

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
            println("Start: $start, End: $end")
        },
        navigateToTimeSlotsSelectingPage = navigateToTimeSlotsSelectingPage, // real navigation


    )
     // Handle Time Picker Dialog
    if (showPickerType != null) {
        AdvancedTimePicker(
            onConfirm = { state ->
                val formattedTime = String.format("%02d:%02d", state.hour, state.minute)
                if (showPickerType == "start") startTime = formattedTime
                else endTime = formattedTime
                showPickerType = null
            },
            onDismiss = { showPickerType = null }
        )
    }
}

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
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
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
                Spacer(modifier = Modifier.height(40.dp))
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
            }

            // Save Button
            item {
                Spacer(modifier = Modifier.height(40.dp))
                Button(
                    onClick = { navigateToTimeSlotsSelectingPage() }, // onSave(startTime, endTime)
                    modifier = Modifier
                        .width(140.dp)
                        .height(48.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6))
                ) {
                    Text(text = "Save", fontSize = 18.sp, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun TimeSelectorField(
    label: String,
    time: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
        .padding(horizontal = 16.dp)
        .clickable { onClick() }, // onEditClick()
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
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable(onClick = onClick)
            )
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Edit Time Slot",
                tint = Color.Gray,
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
    MaterialTheme {
        Surface {
            EditTimeSlotScreen(
                onBack = { /* No-op for preview */ },
                navigateToTimeSlotsSelectingPage = { /* No-op for preview */ },
                viewModel = viewModel()
            )
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
                onSaveTimeSlot = {start, end ->
                    println("Start: $start, End: $end")
                },
                navigateToTimeSlotsSelectingPage = {},
                modifier = Modifier
                )
        }
    }
}

