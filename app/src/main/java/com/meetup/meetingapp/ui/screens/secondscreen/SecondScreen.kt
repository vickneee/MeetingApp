package com.meetup.meetingapp.ui.screens.secondscreen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.meetup.meetingapp.R
import com.meetup.meetingapp.MeetingAppTopAppBar
import com.meetup.meetingapp.ui.AppViewModelProvider
import com.meetup.meetingapp.ui.navigation.NavigationDestination


object SecondScreenDestination : NavigationDestination {
    override val route = "second_screen"
    override val titleRes = R.string.second_screen
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecondScreen(
    onBack: () -> Unit,
    viewModel: SecondViewModel = viewModel(
        factory = AppViewModelProvider.Factory
    )
) {
    Scaffold(
        topBar = {
            MeetingAppTopAppBar(
                title = "Second Screen",
                canNavigateBack = true,
                navigateUp = onBack
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            item {
                Text(text = "Second Screen")
                Text(text = "Hello!")
            }
        }
    }
}
