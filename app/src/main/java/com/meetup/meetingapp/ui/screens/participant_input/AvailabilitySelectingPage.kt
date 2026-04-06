package com.meetup.meetingapp.ui.screens.participant_input

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import androidx.compose.ui.unit.sp
import com.meetup.meetingapp.MeetingAppTopAppBar

data class TimeSlot(val id: Int, val timeRange: String, val isSelected: Boolean)
data class DateAvailability(val date: String, val timeSlots: List<TimeSlot>)

@Composable
fun AvailabilitySelectingPage(
    onBack: () -> Unit,
    navigateToNextStep: () -> Unit, // muuta tämä oikeaan osoitteeseen
    viewModel: ParticipantViewModel
) {
    AvailabilitySelectingPageContent(
        onBack = onBack,
        onNext = navigateToNextStep,
        modifier = Modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AvailabilitySelectingPageContent(
    onBack: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Mock data
    val dates = remember {
        mutableStateListOf(
            DateAvailability("Mon, Apr 13", listOf(TimeSlot(1, "11:00 - 14:00", true), TimeSlot(2, "15:00 - 17:00", false))),
            DateAvailability("Tue, Apr 14", listOf(TimeSlot(3, "09:00 - 12:00", false))),
            DateAvailability("Wed, Apr 15", listOf(TimeSlot(4, "10:00 - 13:00", false))),
            DateAvailability("Thu, Apr 16", listOf(TimeSlot(5, "13:00 - 16:00", false))),
            DateAvailability("Fri, Apr 17", listOf(TimeSlot(6, "11:00 - 14:00", false)))
        )
    }
    Scaffold(
        topBar = {
            MeetingAppTopAppBar(
                title = "Select Your Availability",
                canNavigateBack = true,
                navigateUp = onBack
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .padding(horizontal = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            item {
                Spacer(modifier = Modifier.height(48.dp))

                Text(
                    text = "Choose all dates and time\nslots you can join",
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Medium,

                )

            }
            item {
                Spacer(modifier = Modifier.height(48.dp))
            }

            items(dates.size) { dateAvailability ->
                DateCard(availability = dates[dateAvailability])
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                Spacer(modifier = Modifier.height(48.dp))

                Button(
                    onClick = onNext,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .wrapContentWidth() // only as wide as text
                ) {
                    Text(
                        text = "Next",
                        fontSize = 18.sp,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun DateCard(availability: DateAvailability) {
    var expanded by remember { mutableStateOf(availability.date == "Mon, Apr 13") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
            .background(Color.White, RoundedCornerShape(8.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = availability.date,
                fontWeight = FontWeight.Bold,
//                fontSize = 18.sp,
            )
            Icon(
                imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                modifier = Modifier
//                    .background(Color(0xFF3B82F6))
//                    .border(1.dp, Color.Gray)
                    .size(24.dp)
            )
        }
    }
}

@Preview
@Composable
fun AvailabilitySelectingPageContentPreview() {
    MaterialTheme {
        AvailabilitySelectingPageContent(
            onBack = {},
            onNext = {},
            modifier = Modifier
        )
    }
}