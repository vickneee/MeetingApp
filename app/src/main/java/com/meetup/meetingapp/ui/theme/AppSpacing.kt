package com.meetup.meetingapp.ui.theme
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.unit.dp

object AppSpacing {

    // very small space (inside components)
    val xxs = 4.dp

    // small spacing (between related items)
    val xs = 8.dp

    val xsm = 10.dp

    // default spacing (MOST USED)
    // use: between fields, inputs, texts
    val sm = 14.dp

    // default spacing (MOST USED)
    // use: between fields, inputs, texts
    val md = 16.dp

    // section spacing
    // use: between groups (form sections)
    val lg = 24.dp

    // big section gap
    // use: screen sections separation
    val xl = 32.dp

    // huge section gap
    val xxl = 48.dp

    // huge section gap
    val xxxl = 56.dp

    val xxxxl = 72.dp
}

object AppPadding {
    val pagePadding = PaddingValues(
        start = AppSpacing.xl,    // 32dp
        end = AppSpacing.xl,      // 32dp
        top = AppSpacing.xxxl,     // 56dp
        bottom = AppSpacing.xxxl   // 56dp
    )
}