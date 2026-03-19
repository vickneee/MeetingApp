package com.example.meetingapp.ui.screens.home

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.meetingapp.MeetingAppTopAppBar
import com.example.meetingapp.ui.viewmodel.MeetingAppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: MeetingAppViewModel,
    onMainClick: () -> Unit
) {
    Scaffold(
        topBar = {
            MeetingAppTopAppBar(title = "Home Screen", canNavigateBack = false)
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            item {
                Text(text = "Home Screen")
                Text(text = "Hello!")
                Button(
                    onClick = onMainClick,
                    content = {
                        Text(text = "Second Screen")
                    }
                )
            }
        }
    }
}
