package com.meetup.meetingapp.ui.screens.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Composable that displays a participant's name and an icon in a row.
 * Used in dashboards to list attendees.
 *
 * @param name The name of the participant to display.
 * @param modifier Optional modifier for layout customization.
 */
@Composable
fun ParticipantItemRow(
    name: String,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier =
            modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
    ) {
        Icon(
            imageVector = Icons.Default.Person,
            contentDescription = null,
            modifier =
                Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray.copy(alpha = 0.3f))
                    .padding(4.dp),
            tint = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = name,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}
