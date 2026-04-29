package com.meetup.meetingapp

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.meetup.meetingapp.ui.navigation.MeetingAppNavHost

/**
 * Root composable of the MeetingApp.
 *
 * This function initializes the app‑wide [NavHostController] and delegates
 * navigation graph setup to [MeetingAppNavHost]. It serves as the entry point
 * for all screens rendered in the Compose hierarchy.
 *
 * @param navController Optional controller for navigation. A new one is created
 *                      by default using [rememberNavController].
 */
@Composable
fun MeetingApp(navController: NavHostController = rememberNavController()) {
    MeetingAppNavHost(navController = navController)
}

/**
 * App bar used across the MeetingApp to display a centered title and,
 * optionally, a back navigation button.
 *
 * This top app bar:
 * - Shows a title in the center
 * - Displays a back arrow when [canNavigateBack] is true
 * - Supports scroll behaviors for collapsing toolbars
 * - Allows customizing window insets (default: status bars)
 *
 * @param title The text displayed in the center of the app bar.
 * @param canNavigateBack Whether the back button should be shown.
 * @param modifier Optional [Modifier] for styling or layout adjustments.
 * @param scrollBehavior Optional scroll behavior for Material 3 top app bars.
 * @param windowInsets Insets applied to the app bar (default: status bars).
 * @param navigateUp Callback invoked when the back button is pressed.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeetingAppTopAppBar(
    title: String,
    canNavigateBack: Boolean,
    modifier: Modifier = Modifier,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    windowInsets: WindowInsets = WindowInsets.statusBars,
    navigateUp: () -> Unit = {},
) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = title,
                fontWeight = FontWeight.SemiBold,
                fontSize = 20.sp,
            )
        },
        modifier = modifier,
        scrollBehavior = scrollBehavior,
        windowInsets = windowInsets,
        navigationIcon = {
            if (canNavigateBack) {
                IconButton(onClick = navigateUp) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back_button),
                    )
                }
            }
        },
    )
}
