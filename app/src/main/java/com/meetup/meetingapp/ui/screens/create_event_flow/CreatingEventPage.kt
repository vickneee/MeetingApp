package com.meetup.meetingapp.ui.screens.create_event_flow

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import com.meetup.meetingapp.MeetingAppTopAppBar
import com.meetup.meetingapp.R
import com.meetup.meetingapp.ui.navigation.NavigationDestination
import com.meetup.meetingapp.ui.theme.AppPadding
import com.meetup.meetingapp.ui.theme.AppSize
import com.meetup.meetingapp.ui.theme.AppSpacing
import com.meetup.meetingapp.ui.theme.MeetingAppTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Formats the start and end dates in the "dd.MM.yyyy" format.
 *
 * @param start The start date as a string.
 * @param end The end date as a string.
 * @return A formatted string representing the date range.
 */
fun formatDisplayDate(
    start: String,
    end: String,
): String =
    try {
        val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
        val startDate = LocalDate.parse(start)
        val endDate = LocalDate.parse(end)

        "${startDate.format(formatter)} - ${endDate.format(formatter)}"
    } catch (_: Exception) {
        "Invalid date"
    }

/**
 * Navigation destination for the Create Creating Event Page.
 */
object CreatingEventPageDestination : NavigationDestination {
    override val route = "creating_event_page"
    override val titleRes = R.string.title_creating_event_page
}

/**
 * Entry point composable for the event creation page.
 *
 * @param onBack Navigate back to the previous screen.
 * @param navigateToCreatingEventPage Navigate to the date range selection page.
 * @param viewModel [EventViewModel] that provides and manages the UI state for creating an event.
 */
@Composable
fun CreatingEventPage(
    onBack: () -> Unit,
    navigateToCreatingEventPage: () -> Unit,
    viewModel: EventViewModel,
) {
    /**
     * Collects the current UI state from the [EventViewModel].
     */
    val uiState by viewModel.uiState.collectAsState()

    /**
     * Controls the visibility of the date range picker modal.
     */
    var showModal by remember { mutableStateOf(false) }

    CreatingEventContent(
        uiState = uiState,
        onEventTitleChange = viewModel::updateTitle,
        onHostNameChange = viewModel::updateHostName,
        onBack = onBack,
        onOpenDatePicker = { showModal = true },
        canProceed = viewModel.canProceed,
        navigateToCreatingEventPage = navigateToCreatingEventPage, // real navigation
    )

    /**
     * Modal for selecting a date range.
     */
    if (showModal) {
        CustomDateRangePickerModal(
            onDismiss = { showModal = false },
            onSave = { range ->
                viewModel.updateDateRange(range.first, range.second)
                showModal = false
            },
        )
    }
}

/**
 * UI content for the event creation page.
 *
 * @param uiState Current UI state containing event title, host name, and other form values.
 * @param onEventTitleChange Callback when the event title is updated.
 * @param onHostNameChange Callback when the host name is updated.
 * @param onBack Navigate back to the previous screen.
 * @param onOpenDatePicker Callback to open the date range picker.
 * @param navigateToCreatingEventPage Navigate to the date range selection page.
 * @param canProceed Whether the "Next" button should be enabled or not.
 * @param modifier Optional [Modifier] for layout adjustments.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatingEventContent(
    uiState: EventUiState,
    onEventTitleChange: (String) -> Unit,
    onHostNameChange: (String) -> Unit,
    onBack: () -> Unit,
    onOpenDatePicker: () -> Unit,
    navigateToCreatingEventPage: () -> Unit,
    canProceed: Boolean,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            MeetingAppTopAppBar(
                title = "Create Event",
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
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            item {
                Text(
                    text = "Create an Event",
                    modifier = Modifier
                        .padding(bottom = AppSpacing.xl),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold,
                )

                Text(
                    text = "Event Title",
                    modifier =
                        Modifier
                            .fillMaxWidth(AppSize.xl)
                            .padding(bottom = AppSpacing.xxs),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Start,
                )
                OutlinedTextField(
                    value = uiState.eventTitle,
                    onValueChange = onEventTitleChange,
                    label = { Text("Enter title") },
                    modifier =
                        Modifier
                            .fillMaxWidth(AppSize.xl)
                            .padding(bottom = AppSpacing.sm),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true,
                    // enabled = !isAlreadySubmitted, // Disable if already submitted
                    colors =
                        OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            cursorColor = MaterialTheme.colorScheme.primary,
                            disabledTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            disabledBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                            disabledLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        ),
                )
                Spacer(modifier = Modifier.height(AppSpacing.xs))
                Text(
                    text = "Host Name",
                    modifier =
                        Modifier
                            .fillMaxWidth(AppSize.xl)
                            .padding(bottom = AppSpacing.xxs),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Start,
                )
                OutlinedTextField(
                    value = uiState.hostName,
                    onValueChange = onHostNameChange,
                    label = { Text("Enter host name") },
                    modifier =
                        Modifier
                            .fillMaxWidth(AppSize.xl)
                            .padding(bottom = AppSpacing.sm),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true,
                    // enabled = !isAlreadySubmitted, // Disable if already submitted
                    colors =
                        OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            cursorColor = MaterialTheme.colorScheme.primary,
                            disabledTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            disabledBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                            disabledLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        ),
                )

                Text(
                    text = "Date Range",
                    modifier = Modifier.padding(top = AppSpacing.md, bottom = AppSpacing.xs),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                )

                Spacer(modifier = Modifier.padding(bottom = AppSpacing.xs))

                Text(
                    text =
                        formatDisplayDate(
                            uiState.dateRange.start,
                            uiState.dateRange.end,
                        ),
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (uiState.hasSelectedDateRange) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = AppSpacing.md),
                )

                OutlinedButton(
                    onClick = { onOpenDatePicker() },
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                    colors =
                        ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.Transparent,
                            contentColor = MaterialTheme.colorScheme.primary,
                        ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth(AppSize.lg),
                    contentPadding = PaddingValues(vertical = AppSpacing.md),
                ) {
                    Text(
                        text = "Select New Date Range",
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
                Spacer(modifier = Modifier.height(AppSpacing.lg))
                Button(
                    onClick = { navigateToCreatingEventPage() },
                    enabled = canProceed,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth(AppSize.lg),
                    contentPadding = PaddingValues(vertical = AppSpacing.md),
                ) {
                    Text(
                        text = "Next",
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        }
    }
}

/**
 * Preview for the [CreatingEventContent] composable.
 */
@Preview(showBackground = true)
@Composable
fun CreatingEventPagePreview() {
    MeetingAppTheme {
        CreatingEventContent(
            uiState = EventUiState(eventTitle = "Sample Event", hostName = "host"),
            onEventTitleChange = {},
            onHostNameChange = {},
            onBack = {},
            onOpenDatePicker = {},
            navigateToCreatingEventPage = {},
            canProceed = true,
        )
    }
}
