package com.meetup.meetingapp.ui.screens.eventcreation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

/**
 * A customizable dialog wrapper for displaying a Material 3 time picker
 * or any other time‑selection UI.
 *
 * This dialog provides:
 * - A title section
 * - A slot for custom time‑picker content (`content`)
 * - An optional toggle button area (`toggle`) for switching between picker modes
 * - Standard Cancel / OK action buttons
 *
 * The dialog uses `usePlatformDefaultWidth = false` to allow the content
 * to size itself naturally instead of being constrained to the default dialog width.
 *
 * @param title The title displayed at the top of the dialog.
 * @param onDismiss Called when the dialog is dismissed or Cancel is pressed.
 * @param onConfirm Called when the OK button is pressed.
 * @param toggle Optional composable displayed next to the action buttons
 *               (e.g., a button to switch between dial and text input modes).
 * @param content The main body of the dialog, typically a `TimePicker`.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedTimePickerDialog(
    title: String = "Select Time",
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    toggle: @Composable () -> Unit = {},
    content: @Composable () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties =
            DialogProperties(
                usePlatformDefaultWidth = false,
            ),
    ) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier =
                Modifier
                    .width(IntrinsicSize.Min)
                    .height(IntrinsicSize.Min),
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
//                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(bottom = 20.dp),
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                )

                content()

                Row(
                    modifier =
                        Modifier
                            .height(40.dp)
                            .fillMaxWidth(),
                ) {
                    toggle()
                    Spacer(modifier = Modifier.weight(1f))
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    TextButton(onClick = onConfirm) { Text("OK") }
                }
            }
        }
    }
}

/**
 * Preview for [AdvancedTimePickerDialog].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun AdvancedTimePickerDialogPreview() {
    MaterialTheme {
        // We use a Box or Surface to provide a background for the Preview window
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            val state = rememberTimePickerState(initialHour = 11, initialMinute = 0)

            AdvancedTimePickerDialog(
                title = "Select Time",
                onDismiss = { },
                onConfirm = { },
                toggle = {
                    IconButton(onClick = { }) {
                        Icon(
                            imageVector = Icons.Default.EditCalendar,
                            contentDescription = "Switch to input",
                        )
                    }
                },
            ) {
                // Mocking the content that goes inside the dialog
                TimePicker(state = state)
            }
        }
    }
}
