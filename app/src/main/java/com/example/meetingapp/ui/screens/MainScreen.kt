package com.example.meetingapp.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.meetingapp.ui.viewmodel.MeetingAppViewModel

@Composable
fun MainScreen(
    viewModel: MeetingAppViewModel,
    onMainClick: () -> Unit
) {
    Scaffold() { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            item {
                Text(text = "Main Screen")
                Text(text = "Hello!")
                Button(
                    onClick = onMainClick,
                    content = {
                        Text(text = "Second Screen")
                    })
            }
        }
    }
}