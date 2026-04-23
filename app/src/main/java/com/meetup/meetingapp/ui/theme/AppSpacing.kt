package com.meetup.meetingapp.ui.theme
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.unit.dp

object AppSpacing {
    // very small space (inside components)
    val xxs = 4.dp

    // small spacing (between related items)
    val xs = 8.dp

    val xsm = 12.dp

    // default spacing
    val sm = 14.dp

    // default spacing (MOST USED)
    val md = 14.dp

    // section spacing
    val lg = 20.dp

    // big section gap
    val xl = 32.dp

    // huge section gap
    val xxl = 40.dp

    // huge section gap
    val xxxl = 56.dp
}

object AppPadding {
    val pagePadding =
        PaddingValues(
            start = AppSpacing.xl, // 32dp
            end = AppSpacing.xl, // 32dp
            top = AppSpacing.xxxl, // 56dp
            bottom = AppSpacing.xxxl, // 56dp
        )
}
