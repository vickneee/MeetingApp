package com.meetup.meetingapp.ui.screens.event_created_page

import android.content.ClipData
import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.meetup.meetingapp.MeetingAppTopAppBar
import com.meetup.meetingapp.R
import com.meetup.meetingapp.ui.navigation.NavigationDestination
import com.meetup.meetingapp.ui.screens.EventState
import com.meetup.meetingapp.ui.screens.EventViewModel
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
 * @param onBack Navigate back
 * @param onNavigateToDashboard Navigate to the host dashboard
 * @param viewModel [EventViewModel] to retrieve generated codes.
 */
@Composable
fun EventCreatedPage(
    onBack: () -> Unit,
    onNavigateToDashboard: (String) -> Unit,
    viewModel: EventViewModel
) {

    val eventState by viewModel.eventState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val clipboard = LocalClipboard.current
    val coroutineScope = rememberCoroutineScope()

    when (eventState) {
        is EventState.Loading -> LoadingScreen(modifier = Modifier.fillMaxSize())
        is EventState.Success -> {
            val state = eventState as EventState.Success
            EventCreatedContent(
                eventCode = state.eventCode,
                eventKey = state.eventKey,
                onBack = onBack,
                onNavigateToDashboard = { onNavigateToDashboard(state.eventId) },
                onCopyCode = {
                    coroutineScope.launch {
                        clipboard.setClipEntry(
                            ClipEntry(
                                ClipData.newPlainText(
                                    "Event Info",
                                    "Code: ${state.eventCode} Key: ${state.eventKey}"
                                )
                            )
                        )
                    }
                },
                onShare = {
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(
                            Intent.EXTRA_TEXT,
                            "Join my event!\nCode: ${state.eventCode}\nKey: ${state.eventKey}"
                        )
                    }
                    context.startActivity(Intent.createChooser(intent, "Share event"))
                },
                onFillAvailability = { /* Implementation */ }
            )
        }

        is EventState.Error -> {
            val state = eventState as EventState.Error
            ErrorScreen(
                message = state.error.message ?: "Something went wrong",
                onRetry = { viewModel.createEvent() },
                modifier = Modifier.fillMaxSize()
            )
        }
    }

}

/**
 * Event Created Page Content
 * @param eventCode The generated event code
 * @param eventKey The generated event key
 * @param onBack Navigate back
 * @param onNavigateToDashboard Navigate to dashboard
 * @param onCopyCode Copy code to clipboard
 * @param onShare Open share sheet
 * @param onFillAvailability Enter availability flow
 * @param modifier Modifier
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventCreatedContent(
    eventCode: String,
    eventKey: String,
    onBack: () -> Unit,
    onNavigateToDashboard: () -> Unit,
    onCopyCode: () -> Unit,
    onShare: () -> Unit,
    onFillAvailability: () -> Unit,
    modifier: Modifier = Modifier
) {
    val brandBlue = Color(0xFF3B82F6)

    Scaffold(
        topBar = {
            MeetingAppTopAppBar(
                title = "Event Created",
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
                Text("Your Event Code", fontSize = 22.sp)
                Text(
                    text = eventCode,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                Text("Key", fontSize = 20.sp)
                Text(
                    text = eventKey,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                Text(
                    text = "Share this code and key\nwith participants.",
                    fontSize = 20.sp,
                    modifier = Modifier.padding(vertical = 24.dp),
                    textAlign = TextAlign.Center
                )

                OutlinedButton(
                    onClick = onCopyCode,
                    border = BorderStroke(1.dp, brandBlue),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.width(180.dp)
                ) {
                    Text(
                        "Copy Code",
                        color = brandBlue,
                        fontSize = 18.sp,
                        modifier = Modifier.padding(4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = onShare,
                    border = BorderStroke(1.dp, brandBlue),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.width(180.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, tint = brandBlue)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Share",
                        color = brandBlue,
                        fontSize = 18.sp,
                        modifier = Modifier.padding(4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = onFillAvailability,
                    colors = ButtonDefaults.buttonColors(containerColor = brandBlue),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth(0.7f)
                ) {
                    Text(
                        "Fill in my availability",
                        fontSize = 18.sp,
                        modifier = Modifier.padding(8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = onNavigateToDashboard,
                    border = BorderStroke(1.dp, brandBlue),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth(0.7f)
                ) {
                    Text(
                        "Go to Dashboard",
                        color = brandBlue,
                        fontSize = 18.sp,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }
    }
}

/**
 * The screen displaying the loading message.
 */
@Composable
fun LoadingScreen(modifier: Modifier = Modifier) {
    Image(
        modifier = modifier.size(200.dp),
        painter = painterResource(R.drawable.loading_img),
        contentDescription = "Loading"
    )
}

/**
 * The screen displaying error message with re-attempt button.
 */
@Composable
fun ErrorScreen(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_connection_error), contentDescription = ""
        )
        Text(text = message, modifier = Modifier.padding(16.dp))
        Button(onClick = onRetry) {
            Text("Retry")
        }
    }
}


@Preview(showBackground = true)
@Composable
fun EventCreatedPagePreview() {
    EventCreatedContent(
        eventCode = "A7F9K2",
        eventKey = "83947",
        onBack = {},
        onNavigateToDashboard = {},
        onCopyCode = {},
        onShare = {},
        onFillAvailability = {}
    )
}