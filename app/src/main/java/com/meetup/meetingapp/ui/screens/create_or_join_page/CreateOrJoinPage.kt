package com.meetup.meetingapp.ui.screens.create_or_join_page

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.meetup.meetingapp.MeetingAppTopAppBar
import com.meetup.meetingapp.R
import com.meetup.meetingapp.ui.AppViewModelProvider
import com.meetup.meetingapp.ui.navigation.NavigationDestination

object CreateOrJoinDestination : NavigationDestination {
    override val route = "create_or_join"
    override val titleRes = R.string.title_create_or_join_page
}

/**
 * Create or Join Page
 * @param onBack Navigate back
 * @param navigateToCreatingEventPage Navigate to the next page
 * @param viewModel [CreateOrJoinViewModel] to retrieve all items in the Room database.
 */
@Composable
fun CreateOrJoinPage(
    onBack: () -> Unit,
    navigateToCreatingEventPage: () -> Unit,
    viewModel: CreateOrJoinViewModel = viewModel(
        factory = AppViewModelProvider.Factory
    )
) {
    CreateOrJoinContent(
        code = viewModel.code,
        onCodeChange = viewModel::updateCode,
        key = viewModel.key,
        onKeyChange = viewModel::updateKey,
        onBack = onBack,
        onCreateEventClick = navigateToCreatingEventPage,
        onJoinEventClick = viewModel::joinEvent
    )
}

/**
 * Create or Join Page Content
 * @param code Code
 * @param onCodeChange Code Change
 * @param key Key
 * @param onKeyChange Key Change
 * @param onBack Navigate back
 * @param onCreateEventClick Navigate to the next page
 * @param modifier Modifier
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateOrJoinContent(
    code: String,
    onCodeChange: (String) -> Unit,
    key: String,
    onKeyChange: (String) -> Unit,
    onBack: () -> Unit,
    onCreateEventClick: () -> Unit,
    modifier: Modifier = Modifier,
    onJoinEventClick: () -> Unit
) {
    Scaffold(
        topBar = {
            MeetingAppTopAppBar(
                title = "MeetUp Planner",
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
                    text = "Make plans easy for everyone.",
                    modifier = Modifier.padding(16.dp),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.padding(16.dp))

                Button(
                    onClick = onCreateEventClick,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.padding(4.dp))
                    Text(
                        text = "Create Event",
                        fontSize = 18.sp,
                        modifier = Modifier.padding(4.dp)
                    )
                }
                Log.d("CreateOrJoinPage", "Code: $code, Key: $key")

                Spacer(modifier = Modifier.padding(16.dp))

                Text(
                    text = "Join an Event",
                    modifier = Modifier.padding(16.dp),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                OutlinedTextField(
                    value = code,
                    onValueChange = onCodeChange,
                    placeholder = { Text(text = "Enter code") },
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 16.dp),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = key,
                    onValueChange = onKeyChange,
                    placeholder = { Text("Enter key") },
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 24.dp),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true
                )

                Button(
                    onClick = { onJoinEventClick() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Text(
                        text = "Join Event",
                        fontSize = 18.sp,
                           modifier = Modifier.padding(4.dp)
                    )
                }

                Spacer(modifier = Modifier.padding(28.dp))

                Button(
                    onClick = { },
                    border = BorderStroke(2.dp, Color(0xFF3B82F6)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.Transparent,
                        contentColor = Color(0xFF3B82F6)
                    ),
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Text(
                        text = "Events",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(
                            top = 4.dp,
                            bottom = 4.dp,
                            start = 36.dp,
                            end = 36.dp
                        )
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CreateOrJoinPagePreview() {
    CreateOrJoinContent(
        code = "",
        onCodeChange = {},
        key = "",
        onKeyChange = {},
        onBack = {},
        onCreateEventClick = {}
    ) {}
}