package com.meetup.meetingapp.ui.screens.home

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.meetup.meetingapp.R
import com.meetup.meetingapp.ui.AppViewModelProvider
import com.meetup.meetingapp.ui.navigation.NavigationDestination
import com.meetup.meetingapp.ui.theme.AppPadding
import com.meetup.meetingapp.ui.theme.AppSize
import com.meetup.meetingapp.ui.theme.AppSpacing
import com.meetup.meetingapp.ui.theme.MeetingAppTheme

/**
 * This is the NavigationDestination for the Home screen
 */
object HomeDestination : NavigationDestination {
    override val route = "home"
    override val titleRes = R.string.app_name
}

/**
 * Home screen composable
 * @param onCreateEventClick Navigate to the second screen
 * @param onJoinEventClick Navigate to the second screen
 * @param onEventsClick Navigate to the past events screen
 * @param viewModel [HomeViewModel] to retrieve all items in the Room database.
 */
@Composable
fun HomeScreen(
    onCreateEventClick: () -> Unit,
    onJoinEventClick: () -> Unit,
    onEventsClick: () -> Unit,
    viewModel: HomeViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    /**
     * Sign in anonymously to the Firebase Realtime Database
     */
    LaunchedEffect(Unit) {
        viewModel.signInAnonymously()
        Log.d("HomeScreen", "Signed in anonymously")
    }

    HomeScreenContent(
        onCreateEventClick = onCreateEventClick,
        onJoinEventClick = onJoinEventClick,
        onEventsClick = onEventsClick,
    )
}

/**
 * Stateless version of the Home screen
 *
 * @param onCreateEventClick Navigate to the second screen
 * @param onJoinEventClick Navigate to the second screen
 * @param onEventsClick Navigate to the past events screen
 * @param modifier Modifier for the content
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenContent(
    onCreateEventClick: () -> Unit,
    onJoinEventClick: () -> Unit,
    onEventsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
    ) { paddingValues ->
        LazyColumn(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(paddingValues),
            contentPadding = AppPadding.pagePadding, // Padding values for the entire screen
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            item {
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_foreground),
                    contentDescription = "MeetUp Logo",
                    modifier =
                        Modifier
                            .size(120.dp)
                            .padding(bottom = AppSpacing.xl),
                )
            }
            item {
                Text(
                    text = "MeetUp",
                    style =
                        MaterialTheme.typography.displayLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary,
                            shadow =
                                Shadow(
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                    offset = Offset(0f, 6f),
                                    blurRadius = 7f,
                                ),
                        ),
                )
                Spacer(modifier = Modifier.height(AppSpacing.sm))

                Text(
                    text = "Make plans easy for everyone.",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                )

                Spacer(modifier = Modifier.height(AppSpacing.xl))

                Button(
                    onClick = onCreateEventClick,
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                        ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxSize(AppSize.lg),
                    contentPadding = PaddingValues(AppSpacing.md),
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Create Event",
                        style = MaterialTheme.typography.labelLarge,
                    )
                }

                Spacer(modifier = Modifier.height(AppSpacing.lg))

                Button(
                    onClick = onJoinEventClick,
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                        ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxSize(AppSize.lg),
                    contentPadding = PaddingValues(AppSpacing.md),
                ) {
                    Text(
                        text = "Join Event",
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
                Spacer(modifier = Modifier.height(AppSpacing.lg))

                Button(
                    onClick = onEventsClick,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                    colors =
                        ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary,
                        ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxSize(AppSize.lg),
                    contentPadding = PaddingValues(vertical = AppSpacing.md),
                ) {
                    Text(
                        text = "Events",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}

/**
 * Preview for the [HomeScreen] composable.
 */
@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    MeetingAppTheme {
        HomeScreenContent(
            onCreateEventClick = {},
            onJoinEventClick = {},
            onEventsClick = {},
        )
    }
}
