package com.meetup.meetingapp.ui.screens.create_event_flow

import android.content.ClipData
import android.content.Intent
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.meetup.meetingapp.R
import com.meetup.meetingapp.ui.navigation.NavigationDestination
import com.meetup.meetingapp.ui.theme.AppPadding
import com.meetup.meetingapp.ui.theme.AppSize
import com.meetup.meetingapp.ui.theme.AppSpacing
import com.meetup.meetingapp.ui.theme.MeetingAppTheme
import kotlinx.coroutines.launch

/**
 * Navigation destination for the Event Created screen.
 */
object EventCreatedDestination : NavigationDestination {
    override val route = "event_created"
    override val titleRes = R.string.title_event_created
}

/**
 * Event Created Page
 * @param onNavigateToHome Navigate to the home screen
 * @param onNavigateToDashboard Navigate to the host dashboard
 * @param onNavigateToAvailability Navigate to the participant availability
 * @param viewModel [EventViewModel] to retrieve generated codes.
 * @param eventId Optional event ID to load an existing event.
 */
@Composable
fun EventCreatedPage(
    onNavigateToHome: () -> Unit,
    onNavigateToDashboard: (String) -> Unit,
    onNavigateToAvailability: (String, String) -> Unit,
    viewModel: EventViewModel,
    eventId: String? = null,
) {
    val eventState by viewModel.eventState.collectAsStateWithLifecycle()
    val hasHostSubmitted by viewModel.hasHostSubmitted.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val clipboard = LocalClipboard.current
    val coroutineScope = rememberCoroutineScope()

    // Load existing event data if an ID is provided
    LaunchedEffect(eventId) {
        if (!eventId.isNullOrEmpty()) {
            viewModel.loadExistingEvent(eventId)
        }
    }

    Crossfade(targetState = eventState, label = "event_created_loading") { state ->
        when (state) {
            is EventState.Loading -> LoadingScreen(modifier = Modifier.fillMaxSize())
            is EventState.Success -> {
                EventCreatedContent(
                    eventCode = state.eventCode,
                    eventKey = state.eventKey,
                    hasHostSubmitted = hasHostSubmitted,
                    onHomeClick = onNavigateToHome,
                    onNavigateToDashboard = { onNavigateToDashboard(state.eventId) },
                    onCopyCode = {
                        coroutineScope.launch {
                            clipboard.setClipEntry(
                                ClipEntry(
                                    ClipData.newPlainText(
                                        "Event Info",
                                        "Code: ${state.eventCode} Key: ${state.eventKey}",
                                    ),
                                ),
                            )
                        }
                    },
                    onShare = {
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(
                                Intent.EXTRA_TEXT,
                                "Join my event!\nCode: ${state.eventCode}\nKey: ${state.eventKey}",
                            )
                        }
                        context.startActivity(Intent.createChooser(intent, "Share event"))
                    },
                    onFillAvailability = {
                        onNavigateToAvailability(
                            state.eventCode,
                            state.eventKey
                        )
                    },
                )
            }

            is EventState.Error -> {
                ErrorScreen(
                    message = state.error.message ?: "Something went wrong",
                    onRetry = { viewModel.createEvent() },
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}

/**
 * Event Created Page Content
 * @param eventCode The generated event code
 * @param eventKey The generated event key
 * @param hasHostSubmitted Whether the host has already submitted availability
 * @param onHomeClick Navigate home
 * @param onCopyCode Copy code to clipboard
 * @param onShare Open share sheet
 * @param onFillAvailability Enter availability flow
 * @param onNavigateToDashboard Navigate to dashboard
 * @param modifier Modifier
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventCreatedContent(
    eventCode: String,
    eventKey: String,
    hasHostSubmitted: Boolean,
    onHomeClick: () -> Unit,
    onCopyCode: () -> Unit,
    onShare: () -> Unit,
    onFillAvailability: () -> Unit,
    onNavigateToDashboard: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.title_event_created),
                        fontWeight = FontWeight.Bold,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onHomeClick) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = "Home",
                        )
                    }
                },
                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface,
                        navigationIconContentColor = MaterialTheme.colorScheme.primary,
                    ),
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
                    "Your Event Code",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 22.sp,
                )
                Text(
                    text = eventCode,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp),
                )

                Text(
                    "Key",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 20.sp,
                )
                Text(
                    text = eventKey,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp),
                )

                Text(
                    text = "Share this code and key\nwith participants.",
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(vertical = AppSpacing.md),
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(AppSpacing.lg))

                OutlinedButton(
                    onClick = onCopyCode,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth(AppSize.lg),
                    contentPadding = PaddingValues(vertical = AppSpacing.md),
                ) {
                    Text(
                        "Copy Code",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelLarge,
                    )
                }

                Spacer(modifier = Modifier.height(AppSpacing.lg))

                OutlinedButton(
                    onClick = onShare,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth(AppSize.lg),
                    contentPadding = PaddingValues(vertical = AppSpacing.md),
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Share",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelLarge,
                    )
                }

                if (!hasHostSubmitted) {
                    Spacer(modifier = Modifier.height(AppSpacing.lg))

                    Button(
                        onClick = onFillAvailability,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth(AppSize.lg),
                        contentPadding = PaddingValues(vertical = AppSpacing.md),
                    ) {
                        Text(
                            "Fill in my availability",
                            color = MaterialTheme.colorScheme.onPrimary,
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(AppSpacing.lg))

                OutlinedButton(
                    onClick = onNavigateToDashboard,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth(AppSize.lg),
                    contentPadding = PaddingValues(vertical = AppSpacing.md),
                ) {
                    Text(
                        "Go to Dashboard",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        }
    }
}

/**
 * The screen displaying the loading message.
 *
 * @param modifier Optional modifier for layout adjustments.
 */
@Composable
fun LoadingScreen(modifier: Modifier = Modifier) {
    Image(
        modifier = modifier.size(200.dp),
        painter = painterResource(R.drawable.loading_img),
        contentDescription = "Loading",
    )
}

/**
 * The screen displaying error message with re-attempt button.
 *
 * @param message The error message to display.
 * @param onRetry Callback to retry the operation.
 * @param modifier Optional modifier for layout adjustments.
 */
@Composable
fun ErrorScreen(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_connection_error),
            contentDescription = "",
        )
        Text(text = message, modifier = Modifier.padding(16.dp))
        Button(onClick = onRetry) {
            Text("Retry")
        }
    }
}

/**
 * Preview for the [EventCreatedContent] composable.
 */
@Preview(showBackground = true)
@Composable
fun EventCreatedPagePreview() {
    MeetingAppTheme {
        EventCreatedContent(
            eventCode = "A7F9K2",
            eventKey = "83947",
            hasHostSubmitted = false,
            onHomeClick = {},
            onNavigateToDashboard = {},
            onCopyCode = {},
            onShare = {},
            onFillAvailability = {},
        )
    }
}
