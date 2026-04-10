package com.meetup.meetingapp.ui.screens.select_date_range

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import java.text.SimpleDateFormat
import java.util.*

/**
 * Custom date range picker modal dialog.
 *
 * @param onDismiss Callback to be invoked when the dialog is dismissed.
 * @param onSave Callback to be invoked when the user saves the selected date range
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomDateRangePickerModal(
    onDismiss: () -> Unit,
    onSave: (Pair<Long?, Long?>) -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            val dateRangePickerState = rememberDateRangePickerState()

            val formatter = SimpleDateFormat("MMM d", Locale.getDefault())
            val startDateText = dateRangePickerState.selectedStartDateMillis?.let { formatter.format(Date(it)) } ?: ""
            val endDateText = dateRangePickerState.selectedEndDateMillis?.let { formatter.format(Date(it)) } ?: ""
            val rangeDisplayText = if (startDateText.isNotEmpty()) "$startDateText – $endDateText" else "Select dates"

            val customCalendarColors = DatePickerDefaults.colors(
                containerColor = Color.White,
                dayInSelectionRangeContainerColor = Color(0xFF6200EE).copy(alpha = 0.12f),
                selectedDayContainerColor = Color(0xFF6200EE),
                selectedDayContentColor = Color.White
            )

            Scaffold(
                topBar = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF6200EE))
                            .padding(top = 16.dp, bottom = 24.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = onDismiss) {
                                Icon(Icons.Default.Close, "Close", tint = Color.White)
                            }
                            TextButton(onClick = {
                                onSave(
                                    Pair(
                                        dateRangePickerState.selectedStartDateMillis,
                                        dateRangePickerState.selectedEndDateMillis
                                    )
                                )
                                onDismiss()
                            }) {
                                Text("SAVE", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }

                        Column(modifier = Modifier.padding(start = 72.dp, end = 24.dp)) {
                            Text(
                                "SELECT DATE",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 12.sp
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = rangeDisplayText,
                                    color = Color.White,
                                    fontSize = 32.sp,
                                    modifier = Modifier.weight(1f)
                                )

                                IconButton(onClick = {
                                    dateRangePickerState.displayMode = if (dateRangePickerState.displayMode == DisplayMode.Picker) {
                                        DisplayMode.Input
                                    } else {
                                        DisplayMode.Picker
                                    }
                                }) {
                                    Icon(Icons.Default.Edit, "Edit", tint = Color.White)
                                }
                            }
                        }
                    }
                }
            ) { paddingValues ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                        .background(Color.White)
                ) {
                    HorizontalDivider(
                        modifier = Modifier
                            .padding(horizontal = 24.dp, vertical = 8.dp),
                        thickness = 1.dp,
                        color = Color.Gray.copy(alpha = 0.4f))
                }
                DateRangePicker(
                    state = dateRangePickerState,
                    colors = customCalendarColors,
                    title = null,
                    headline = null,
                    showModeToggle = false,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun CustomDateRangePickerModalPreview() {

    CustomDateRangePickerModal(
        onDismiss = {},
        onSave = { (start, end) ->
            println("Selected: $start to $end")
        }
    )
}
