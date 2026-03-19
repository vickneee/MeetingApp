package com.example.meetingapp.ui.screens.secondscreen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.meetingapp.MeetingAppTopAppBar
import com.example.meetingapp.data.db.AppDatabase
import com.example.meetingapp.ui.viewmodel.MeetingAppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecondScreen(
    onBack: () -> Unit,
    viewModel: MeetingAppViewModel = viewModel(
        factory = MeetingAppViewModel.Factory(
            exampleRepository = AppDatabase.getDatabase(LocalContext.current).exampleDao()
        )
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
