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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExposedDropdownMenuDefaults.TrailingIcon
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.meetup.meetingapp.ui.theme.AppSize
import com.meetup.meetingapp.ui.theme.AppSpacing

/**
 * Composable function for displaying a multi-select dropdown menu.
 *
 * @param options List of options to choose from.
 * @param selected List of currently selected options.
 * @param onToggle Callback to handle option selection/deselection.
 * @param label Label for the dropdown menu.
 * @param instruction Instruction text for the dropdown menu.
 * @param toText Function to convert an option to a display text.
 * @param enabled Whether the dropdown menu is enabled.
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
    toText: (T) -> String,
    enabled: Boolean = true,
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    var query by rememberSaveable { mutableStateOf("") }
    val scrollState = rememberScrollState()
    val focusRequester = remember { FocusRequester() }

    val configuration = LocalConfiguration.current
    val maxMenuHeight =
        remember(configuration.screenHeightDp) {
            (configuration.screenHeightDp * 0.6f).dp
        }

    // Optimization: Cache text representations to avoid expensive 'toText' calls during filtering/sorting
    val optionsWithText =
        remember(options) {
            options.map { it to toText(it) }
        }

    // Optimization: Convert selected to a set for O(1) lookup
    val selectedSet = remember(selected) { selected.toSet() }

    val filteredOptions =
        remember(optionsWithText, query) {
            val q = query.trim()
            if (q.isEmpty()) {
                optionsWithText
            } else {
                optionsWithText.filter { it.second.contains(q, ignoreCase = true) }
            }
        }

    // Sort to show selected items at the top and limit to 100 results for high performance.
    // Rendering thousands of items in a single frame causes latency; 100 is optimal for search.
    val sortedOptions =
        remember(filteredOptions, selectedSet) {
            filteredOptions.sortedByDescending { it.first in selectedSet }.take(100)
        }

    // Automatically request focus when expanded to ensure keyboard activity
    LaunchedEffect(expanded) {
        if (expanded) {
            focusRequester.requestFocus()
        }
    }

    Column {
        Text(
            text = label,
            fontWeight = FontWeight.Medium,
            style = MaterialTheme.typography.labelLarge,
            modifier =
                Modifier
                    .fillMaxWidth(AppSize.lg)
                    .padding(bottom = AppSpacing.xs),
            color = MaterialTheme.colorScheme.onBackground,
        )

        // Display selected options on top in separate rows with a remove button
        if (selected.isNotEmpty()) {
            Column(modifier = Modifier.padding(bottom = 8.dp)) {
                selected.forEach { item ->
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth(AppSize.xl)
                                .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = "• ${toText(item)}",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f),
                        )
                        IconButton(
                            onClick = { onToggle(item) },
                            modifier = Modifier.size(24.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Remove",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp),
                            )
                        }
                    }
                }
            }
        }

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { if (enabled) expanded = it },
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = {
                    if (enabled) {
                        query = it
                        expanded = true
                    }
                },
                readOnly = !enabled,
                placeholder = { Text(instruction) },
                trailingIcon = { TrailingIcon(expanded = expanded && enabled) },
                colors =
                    OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = if (enabled) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedTrailingIconColor = MaterialTheme.colorScheme.primary,
                        unfocusedTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                        disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                    ),
                enabled = enabled,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                modifier = Modifier
                        .fillMaxWidth(AppSize.xl)
                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled = enabled)
                        .focusRequester(focusRequester),
                shape = RoundedCornerShape(8.dp),
                singleLine = true,
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                scrollState = scrollState,
                modifier = Modifier.heightIn(max = maxMenuHeight),
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 3.dp,
            ) {
                sortedOptions.forEach { pair ->
                    val option = pair.first
                    val text = pair.second
                    val isSelected = option in selectedSet
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                AppCheckbox(checked = isSelected)
                                Spacer(
                                    modifier =
                                        Modifier
                                            .width(AppSpacing.xxs),
                                )
                                Text(text)
                            }
                        },
                        onClick = { onToggle(option) },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
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
fun AppCheckbox(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)? = null,
) {
    Checkbox(
        checked = checked,
        onCheckedChange = onCheckedChange,
        colors =
            CheckboxDefaults.colors(
                checkedColor = MaterialTheme.colorScheme.primary,
                uncheckedColor = MaterialTheme.colorScheme.outline,
                checkmarkColor = Color.White,
            ),
    )
}
