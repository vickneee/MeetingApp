package com.meetup.meetingapp.ui.screens.select_date_range

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DateRangePickerScreen() {
    var showModal by remember { mutableStateOf(false) }
    var selectedDateText by remember { mutableStateOf( "") }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = selectedDateText, style = MaterialTheme.typography.bodyLarge)

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { showModal = true }) {
            Text("Open Date Range")
        }
    }

    if (showModal) {
        CustomDateRangePickerModal(
            onDismiss = { showModal = false },
            onSave = { range ->
                selectedDateText = formatDisplayDate(range.first, range.second)
            }
        )
    }
}

fun formatDisplayDate(start: Long?, end: Long?): String {
    if (start == null || end == null) return "Incomplete range selected"
    val formatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    return "${formatter.format(Date(start))} - ${formatter.format(Date(end))}"
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    DateRangePickerScreen()
}