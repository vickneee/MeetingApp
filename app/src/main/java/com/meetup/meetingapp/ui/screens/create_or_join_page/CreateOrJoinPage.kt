package com.meetup.meetingapp.ui.screens.create_or_join_page

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.meetup.meetingapp.MeetingAppTopAppBar
import com.meetup.meetingapp.R
import com.meetup.meetingapp.ui.AppViewModelProvider
import com.meetup.meetingapp.ui.navigation.NavigationDestination
import com.meetup.meetingapp.ui.theme.AppSize
import com.meetup.meetingapp.ui.theme.AppSpacing
import com.meetup.meetingapp.ui.theme.MeetingAppTheme

/**
 * Navigation destination for the Create or Join screen.
 */
object CreateOrJoinDestination : NavigationDestination {
    override val route = "create_or_join"
    override val titleRes = R.string.title_create_or_join_page
}

/**
 * Create or Join Page
 * @param onBack Navigate back
 * @param navigateToCreatingEventPage Navigate to the next page
 * @param navigateToPastEventsPage Navigate to the past events page
 * @param navigateToParticipantPage Navigate to the participant page
 * @param viewModel [CreateOrJoinViewModel] to retrieve all items in the Room database.
 */
@Composable
fun CreateOrJoinPage(
    onBack: () -> Unit,
    navigateToCreatingEventPage: () -> Unit,
    navigateToPastEventsPage: () -> Unit,
    navigateToParticipantPage: (Pair<String, String>) -> Unit,
    viewModel: CreateOrJoinViewModel = viewModel(
        factory = AppViewModelProvider.Factory
    )
) {
    // Navigate when join succeeds
    LaunchedEffect(viewModel.navigateToEventsListPage) {
        if (viewModel.navigateToEventsListPage) {
            viewModel.onNavigatedToPastEvents()
            navigateToPastEventsPage()
        }
    }

    LaunchedEffect(viewModel.navigateToParticipantPage) {
        viewModel.navigateToParticipantPage?.let { (eventCode, eventKey) ->
            viewModel.onNavigatedToParticipantPage()
            navigateToParticipantPage(eventCode to eventKey)
        }
    }

    CreateOrJoinContent(
        code = viewModel.code,
        onCodeChange = viewModel::updateCode,
        key = viewModel.key,
        onKeyChange = viewModel::updateKey,
        onBack = onBack,
        onCreateEventClick = navigateToCreatingEventPage,
        onJoinEventClick = viewModel::joinEvent,
        onEventsClick = navigateToPastEventsPage
    )
}

/**
 * Create or Join Page Content
 * @param code Code
 * @param onCodeChange Code Change
 * @param key Key
 * @param onKeyChange Key Change
 * @param onBack Navigate back
 * @param onCreateEventClick Navigate to the next page
 * @param modifier Modifier
 * @param onJoinEventClick Join Event
 * @param onEventsClick Navigate to the past events page
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateOrJoinContent(
    code: String,
    onCodeChange: (String) -> Unit,
    key: String,
    onKeyChange: (String) -> Unit,
    onBack: () -> Unit,
    onCreateEventClick: () -> Unit,
    modifier: Modifier = Modifier,
    onJoinEventClick: () -> Unit,
    onEventsClick: () -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            MeetingAppTopAppBar(
                title = "MeetUp Planner",
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
            contentPadding = PaddingValues(
                start = 32.dp,
                end = 32.dp,
                top = 56.dp,
                bottom = 56.dp
            ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            item {
                Text(
                    text = "Make plans easy for everyone.",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(AppSpacing.xl))

                Button(
                    onClick = onCreateEventClick,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxSize(AppSize.sm),
                    contentPadding = PaddingValues(vertical = AppSpacing.sm)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Create Event",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
                Spacer(modifier = Modifier.height(AppSpacing.sm))

                Text(
                    text = "Join an Event",
                    modifier = Modifier.padding(AppSpacing.md),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Event Code",
                    modifier = Modifier.fillMaxWidth(AppSize.xl),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Start
                )
                OutlinedTextField(
                    value = code,
                    onValueChange = onCodeChange,
                    label = { Text("Enter code") },
                    modifier = Modifier
                        .fillMaxWidth(AppSize.xl),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true,
                    // enabled = !isAlreadySubmitted, // Disable if already submitted
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        cursorColor = MaterialTheme.colorScheme.primary,
                        disabledTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        disabledBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                        disabledLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                )
                Spacer(modifier = Modifier.height(AppSpacing.md))
                Text(
                    text = "Event Key",
                    modifier = Modifier.fillMaxWidth(AppSize.xl),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Start
                )
                Spacer(modifier = Modifier.height(AppSpacing.xxs))
                OutlinedTextField(
                    value = key,
                    onValueChange = onKeyChange,
                    label = { Text("Enter key") },
                    modifier = Modifier
                        .fillMaxWidth(AppSize.xl),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyMedium,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        cursorColor = MaterialTheme.colorScheme.primary,
                        disabledTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        disabledBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                        disabledLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                )
                Spacer(modifier = Modifier.height(AppSpacing.lg))
                Button(
                    onClick = { onJoinEventClick() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth(AppSize.sm),
                    contentPadding = PaddingValues(vertical = AppSpacing.sm)
                ) {
                    Text(
                        text = "Join Event",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
                Spacer(modifier = Modifier.height(AppSpacing.lg))
                Button(
                    onClick = { onEventsClick() },
                    border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.Transparent,
                        contentColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth(AppSize.sm),
                    contentPadding = PaddingValues(vertical = AppSpacing.sm)
                ) {
                    Text(
                        text = "Events",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

/**
 * Preview for the [CreateOrJoinContent] composable.
 */
@Preview(showBackground = true)
@Composable
fun CreateOrJoinPagePreview() {
    MeetingAppTheme {
        CreateOrJoinContent(
            code = "",
            onCodeChange = {},
            key = "",
            onKeyChange = {},
            onBack = {},
            onCreateEventClick = {},
            onJoinEventClick = {},
            onEventsClick = {}
        )
    }
}