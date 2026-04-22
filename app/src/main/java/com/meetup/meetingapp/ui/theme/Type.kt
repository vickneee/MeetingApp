package com.meetup.meetingapp.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val Typography = Typography(

    // BIG APP TITLE (Home screen "MeetUp")
    // use: app logo / main screen header
    displayLarge = TextStyle(
        fontSize = 70.sp,
        fontWeight = FontWeight.ExtraBold
    ),

    // PAGE TITLE (screen titles)
    // use: "Create Event", "Events List"
    titleLarge = TextStyle(
        fontSize = 22.sp,
        fontWeight = FontWeight.Bold
    ),

    // SECTION TITLE
    // use: "Event Title", "Host Name", "Date Range"
    titleMedium = TextStyle(
        fontSize = 20.sp,
        fontWeight = FontWeight.SemiBold
    ),

    // NORMAL TEXT
    // use: paragraphs, labels, descriptions
    bodyLarge = TextStyle(
        fontSize = 18.sp,
        fontWeight = FontWeight.Normal
    ),

    // SMALL TEXT
    // use: helper text, hints
    bodyMedium = TextStyle(
        fontSize = 16.sp
    ),

    // SMALL TEXT
    // use: helper text, hints
    bodySmall = TextStyle(
        fontSize = 14.sp
    ),

    // BUTTON TEXT
    // use: buttons like "Next", "Create Event"
    labelLarge = TextStyle(
        fontSize = 16.sp,
        fontWeight = FontWeight.SemiBold
    ),

    // BUTTON TEXT
    // use: buttons like "Next", "Create Event"
    labelMedium = TextStyle(
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold
    ),

    // SMALL LABELS
    // use: chips, tags, tiny UI text
    labelSmall = TextStyle(
        fontSize = 12.sp
    )
)