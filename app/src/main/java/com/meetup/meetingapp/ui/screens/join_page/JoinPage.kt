package com.meetup.meetingapp.ui.screens.join_page

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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.meetup.meetingapp.MeetingAppTopAppBar
import com.meetup.meetingapp.R
import com.meetup.meetingapp.ui.AppViewModelProvider
import com.meetup.meetingapp.ui.navigation.NavigationDestination
import com.meetup.meetingapp.ui.theme.AppPadding
import com.meetup.meetingapp.ui.theme.AppSize
import com.meetup.meetingapp.ui.theme.AppSpacing
import com.meetup.meetingapp.ui.theme.MeetingAppTheme

/**
 * Navigation destination for the Create or Join screen.
 */
object JoinDestination : NavigationDestination {
    override val route = "join"
    override val titleRes = R.string.title_join_page
}

/**
 * Create or Join Page
 * @param onBack Navigate back
 * @param navigateToPastEventsPage Navigate to the past events page
 * @param navigateToParticipantPage Navigate to the participant page
 * @param viewModel [JoinViewModel] to retrieve all items in the Room database.
 */
@Composable
fun JoinPage(
    onBack: () -> Unit,
    navigateToPastEventsPage: () -> Unit,
    navigateToParticipantPage: (Pair<String, String>) -> Unit,
    viewModel: JoinViewModel = viewModel(
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

    JoinContent(
        code = viewModel.code,
        codeError = viewModel.codeError,
        onCodeChange = viewModel::updateCode,
        key = viewModel.key,
        keyError = viewModel.keyError,
        onKeyChange = viewModel::updateKey,
        onBack = onBack,
        onJoinEventClick = viewModel::joinEvent,
        onEventsClick = navigateToPastEventsPage,
    )
}

/**
 * Create or Join Page Content
 * @param code Code
 * @param codeError Code Error
 * @param onCodeChange Code Change
 * @param key Key
 * @param keyError Key Error
 * @param onKeyChange Key Change
 * @param onBack Navigate back
 * @param modifier Modifier
 * @param onJoinEventClick Join Event
 * @param onEventsClick Navigate to the past events page
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JoinContent(
    code: String,
    codeError: String?,
    onCodeChange: (String) -> Unit,
    key: String,
    keyError: String?,
    onKeyChange: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    onJoinEventClick: () -> Unit,
    onEventsClick: () -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            MeetingAppTopAppBar(
                title = stringResource(R.string.title_join_page),
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
            contentPadding = AppPadding.pagePadding, // Padding values for the entire screen
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            item {
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
                    modifier = Modifier
                        .fillMaxWidth(AppSize.lg)
                        .padding(bottom = AppSpacing.xxs),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Start
                )
                OutlinedTextField(
                    value = code,
                    onValueChange = {
                        onCodeChange(it)
                        // Clear error on type
                    },
                    label = { Text("Enter code") },
                    modifier = Modifier
                        .fillMaxWidth(AppSize.lg),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true,
                    // enabled = !isAlreadySubmitted, // Disable if already submitted
                    isError = codeError != null,
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
                if (codeError != null) {
                    Text(
                        text = codeError,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier
                            .fillMaxWidth(AppSize.lg)
                            .padding(start = 16.dp, top = 4.dp)
                    )
                }
                Spacer(modifier = Modifier.height(AppSpacing.md))
                Text(
                    text = "Event Key",
                    modifier = Modifier
                        .fillMaxWidth(AppSize.lg)
                        .padding(bottom = AppSpacing.xxs),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Start
                )
                OutlinedTextField(
                    value = key,
                    onValueChange = {
                        onKeyChange(it)
                        // Clear error on type
                    },
                    label = { Text("Enter key") },
                    modifier = Modifier
                        .fillMaxWidth(AppSize.lg),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyMedium,
                    isError = keyError != null,
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
                if (keyError != null) {
                    Text(
                        text = keyError,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier
                            .fillMaxWidth(AppSize.lg)
                            .padding(start = 16.dp, top = 4.dp)
                    )
                }
                Spacer(modifier = Modifier.height(AppSpacing.lg))
                Button(
                    onClick = { onJoinEventClick() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth(AppSize.lg),
                    contentPadding = PaddingValues(vertical = AppSpacing.md)
                ) {
                    Text(
                        text = "Join Event",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
                Spacer(modifier = Modifier.height(AppSpacing.xl))
                Button(
                    onClick = { onEventsClick() },
                    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.Transparent,
                        contentColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth(AppSize.lg),
                    contentPadding = PaddingValues(vertical = AppSpacing.md)
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
 * Preview for the [JoinContent] composable.
 */
@Preview(showBackground = true)
@Composable
fun JoinPagePreview() {
    MeetingAppTheme {
        JoinContent(
            code = "",
            codeError = null,
            onCodeChange = {},
            key = "",
            keyError = null,
            onKeyChange = {},
            onBack = {},
            onJoinEventClick = {},
            onEventsClick = {}
        )
    }
}