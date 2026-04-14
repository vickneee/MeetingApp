package com.meetup.meetingapp.ui.screens.components

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExposedDropdownMenuDefaults.TrailingIcon
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Composable function for displaying a multi-select dropdown menu.
 *
 * @param options List of options to choose from.
 * @param selected List of currently selected options.
 * @param onToggle Callback to handle option selection/deselection.
 * @param label Label for the dropdown menu.
 * @param instruction Instruction text for the dropdown menu.
 * @param toText Function to convert an option to a display text.
 */
@SuppressLint("ConfigurationScreenWidthHeight")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> AppMultiSelectDropdown(
    options: List<T>,
    selected: List<T>,
    onToggle: (T) -> Unit,
    label: String,
    instruction: String,
    toText: (T) -> String
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    var query by rememberSaveable { mutableStateOf("") }
    val scrollState = rememberScrollState()
    
    val configuration = LocalConfiguration.current
    val maxMenuHeight = (configuration.screenHeightDp * 0.8f).dp

    val filteredOptions = remember(options, query) {
        options.filter { toText(it).contains(query, ignoreCase = true) }
    }

    // Sort to show selected items at the top
    val sortedOptions = remember(filteredOptions, selected) {
        filteredOptions.sortedByDescending { it in selected }
    }

    Column {
        Text(
            text = label,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        
        // Display selected options on top in separate rows with a remove button
        if (selected.isNotEmpty()) {
            Column(modifier = Modifier.padding(bottom = 8.dp)) {
                selected.forEach { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "• ${toText(item)}",
                            fontSize = 14.sp,
                            color = Color(0xFF3B82F6),
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { onToggle(item) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Remove",
                                tint = Color.Gray,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            TextField(
                value = query,
                onValueChange = {
                    query = it
                    expanded = true
                },
                readOnly = false,
                placeholder = { Text(instruction) },
                trailingIcon = { TrailingIcon(expanded) },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedIndicatorColor = Color(0xFF3B82F6),
                    unfocusedIndicatorColor = Color.Gray,
                    focusedTrailingIconColor = Color(0xFF3B82F6),
                    unfocusedTrailingIconColor = Color.Gray,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                shape = RoundedCornerShape(8.dp),
                singleLine = true
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                scrollState = scrollState,
                modifier = Modifier.heightIn(max = maxMenuHeight)
            ) {
                sortedOptions.forEach { option ->
                    val isSelected = option in selected
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                AppCheckbox(checked = isSelected)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(toText(option))
                            }
                        },
                        onClick = { onToggle(option) },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                    )
                }
            }
        }
    }
}

/**
 * Composable function for displaying a checkbox.
 *
 * @param checked Whether the checkbox is currently checked.
 * @param onCheckedChange Callback to handle checkbox state changes.
 */
@Composable
fun AppCheckbox(checked: Boolean, onCheckedChange: ((Boolean) -> Unit)? = null) {
    Checkbox(
        checked = checked,
        onCheckedChange = onCheckedChange,
        colors = CheckboxDefaults.colors(
            checkedColor = Color(0xFF3B82F6),
            uncheckedColor = Color.Gray,
            checkmarkColor = Color.White
        )
    )
}