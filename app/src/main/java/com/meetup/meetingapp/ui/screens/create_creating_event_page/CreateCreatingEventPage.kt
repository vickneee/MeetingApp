package com.meetup.meetingapp.ui.screens.create_creating_event_page

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.meetup.meetingapp.ui.AppViewModelProvider
import com.meetup.meetingapp.ui.navigation.NavigationDestination
import com.meetup.meetingapp.ui.screens.EventUiState
import com.meetup.meetingapp.ui.screens.EventViewModel
import com.meetup.meetingapp.ui.screens.select_date_range.DateRangePickerScreen


object CreateCreatingEventPageDestination : NavigationDestination {
    override val route = "create_creating_event_page"
    override val titleRes = R.string.title_create_creating_event_page
}

/**
 * Entry point composable for the event creation page.
 *
 * @param onBack Navigate back to the previous screen.
 * @param navigateToCreatingEventPage Navigate to the date range selection page.
 * @param viewModel [EventViewModel] that provides and manages the UI state for creating an event.
 */

@Composable
fun CreateCreatingEventPage(
    onBack: () -> Unit,
    navigateToCreatingEventPage: () -> Unit,
    viewModel: EventViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    CreateCreatingEventPageContent(
        uiState = uiState,
        onEventTitleChange = viewModel::updateTitle,
        onHostNameChange = viewModel::updateHostName,
        onBack = onBack,
        navigateToCreatingEventPage = navigateToCreatingEventPage
    )
}

/**
 * UI content for the event creation page.
 *
 * @param uiState Current UI state containing event title, host name, and other form values.
 * @param onEventTitleChange Callback when the event title is updated.
 * @param onHostNameChange Callback when the host name is updated.
 * @param onBack Navigate back to the previous screen.
 * @param navigateToCreatingEventPage Navigate to the date range selection page.
 * @param modifier Optional [Modifier] for layout adjustments.
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateCreatingEventPageContent(
    uiState: EventUiState,
    onEventTitleChange: (String) -> Unit,
    onHostNameChange: (String) -> Unit,
    onBack: () -> Unit,
    navigateToCreatingEventPage: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            MeetingAppTopAppBar(
                title = "Create Event",
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
            item {
                Text(
                    text = "Event Title",
                    modifier = Modifier.padding(16.dp),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                OutlinedTextField(
                    value = uiState.eventTitle,
                    onValueChange = onEventTitleChange,
                    placeholder = { Text("Enter title") },
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 16.dp),
                    shape = RoundedCornerShape(8.dp)
                )

                Text(
                    text = "Host Name",
                    modifier = Modifier.padding(16.dp),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                OutlinedTextField(
                    value = uiState.hostName,
                    onValueChange = onHostNameChange,
                    placeholder = { Text("Enter host name") },
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 16.dp),
                    shape = RoundedCornerShape(8.dp)
                )

                Text(
                    text = "Date Range",
                    modifier = Modifier.padding(16.dp),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.padding(8.dp))
//                DateRangePickerScreen()

                Button(
                    onClick = { navigateToCreatingEventPage() },
                    border = BorderStroke(2.dp, Color(0xFF3B82F6)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.Transparent,
                        contentColor = Color(0xFF3B82F6)
                    ),
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Text(
                        text = "Select Date Range",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(
                            top = 6.dp,
                            bottom = 6.dp,
                            start = 36.dp,
                            end = 36.dp
                        )
                    )
                }

                Spacer(modifier = Modifier.padding(28.dp))

                Button(
                    onClick = { navigateToCreatingEventPage() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Text(
                        text = "Next",
                        fontSize = 18.sp,
                        modifier = Modifier.padding(4.dp)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CreateCreatingEventPagePreview(
    uiState: EventUiState = EventUiState(
        eventTitle = "Sample Event",
        hostName = "host",
    ),
    onEventTitleChange: (String) -> Unit = {},
    onHostNameChange: (String) -> Unit = {},
    onBack: () -> Unit = {}
) {
    CreateCreatingEventPageContent(
        uiState = uiState,
        onEventTitleChange = onEventTitleChange,
        onHostNameChange = onHostNameChange,
        onBack = onBack,
        navigateToCreatingEventPage = {}
    )
}
