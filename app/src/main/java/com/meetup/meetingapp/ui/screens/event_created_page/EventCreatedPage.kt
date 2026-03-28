package com.meetup.meetingapp.ui.screens.event_created_page

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.meetup.meetingapp.MeetingAppTopAppBar
import com.meetup.meetingapp.R
import com.meetup.meetingapp.ui.AppViewModelProvider
import com.meetup.meetingapp.ui.navigation.NavigationDestination

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
 * @param viewModel [EventCreatedViewModel] to retrieve generated codes.
 */
@Composable
fun EventCreatedPage(
    onBack: () -> Unit,
    onNavigateToDashboard: () -> Unit,
    viewModel: EventCreatedViewModel = viewModel(
        factory = AppViewModelProvider.Factory
    )
) {
    EventCreatedContent(
        eventCode = viewModel.eventCode,
        eventKey = viewModel.eventKey,
        onBack = onBack,
        onNavigateToDashboard = onNavigateToDashboard,
        onCopyCode = { /* Implementation */ },
        onShare = { /* Implementation */ },
        onFillAvailability = { /* Implementation */ }
    )
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
                    Text("Copy Code", color = brandBlue, fontSize = 18.sp)
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
                    Text("Share", color = brandBlue, fontSize = 18.sp)
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = onFillAvailability,
                    colors = ButtonDefaults.buttonColors(containerColor = brandBlue),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth(0.7f)
                ) {
                    Text("Fill in my availability", fontSize = 18.sp, modifier = Modifier.padding(8.dp))
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = onNavigateToDashboard,
                    border = BorderStroke(1.dp, brandBlue),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth(0.7f)
                ) {
                    Text("Go to Dashboard", color = brandBlue, fontSize = 18.sp, modifier = Modifier.padding(8.dp))
                }
            }
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