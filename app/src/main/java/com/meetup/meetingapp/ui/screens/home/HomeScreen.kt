package com.meetup.meetingapp.ui.screens.home

import android.util.Log
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.meetup.meetingapp.R
import com.meetup.meetingapp.ui.AppViewModelProvider
import com.meetup.meetingapp.ui.navigation.NavigationDestination

/**
 * This is the NavigationDestination for the Home screen
 */
object HomeDestination : NavigationDestination {
    override val route = "home"
    override val titleRes = R.string.app_name
}

/**
 * Home screen composable
 * @param onMainClick Navigate to the second screen
 * @param viewModel [HomeViewModel] to retrieve all items in the Room database.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onMainClick: () -> Unit,
    viewModel: HomeViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    /**
     * Sign in anonymously to the Firebase Realtime Database
     */
    LaunchedEffect(Unit) {
        viewModel.signInAnonymously()
        Log.d("HomeScreen", "Signed in anonymously")
    }

    val homeUiState by viewModel.homeUiState.collectAsState()

    Scaffold { innerPadding ->
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
            items(items = homeUiState.itemList, key = { it.id }) { item ->
                Text(text = "Item ID: ${item.id}, Name: ${item.name}")
            }
        }
    }
}
