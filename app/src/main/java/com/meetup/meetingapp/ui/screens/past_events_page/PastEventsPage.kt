package com.meetup.meetingapp.ui.screens.past_events_page

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.meetup.meetingapp.MeetingAppTopAppBar
import com.meetup.meetingapp.R
import com.meetup.meetingapp.ui.navigation.NavigationDestination

object PastEventsDestination : NavigationDestination {
    override val route = "past_events"
    override val titleRes = R.string.title_past_events_page
}

/**
 * Past Events Page
 * @param onBack Navigate back
 * @param modifier Modifier for the page
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PastEventsPage(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            MeetingAppTopAppBar(
                title = "Past Events",
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
                    text = "Your Events",
                    modifier = Modifier.padding(16.dp),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.padding(16.dp))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PastEventsPagePreview() {
    PastEventsPage(
        onBack = {},
        modifier = Modifier
    )
    PastEventsPage(onBack = {})
}

