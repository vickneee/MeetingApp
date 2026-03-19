package com.example.meetingapp.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.meetingapp.R
import com.example.meetingapp.data.db.AppDatabase
import com.example.meetingapp.ui.screens.secondscreen.SecondScreen
import com.example.meetingapp.ui.screens.home.HomeScreen
import com.example.meetingapp.ui.viewmodel.MeetingAppViewModel

enum class Screen(@StringRes val title: Int) {
    HomeScreen(title = R.string.app_name),
    SecondScreen(title = R.string.second_screen)
}

@Composable
fun MeetingAppNavHost(
    navController: NavHostController = rememberNavController()
) {
    val context = LocalContext.current
    val database = AppDatabase.getDatabase(context)
    val viewModel: MeetingAppViewModel = viewModel(
        factory = MeetingAppViewModel.Factory(exampleRepository = database.exampleDao())
    )

    NavHost(
        navController = navController,
        startDestination = Screen.HomeScreen.name,
        modifier = Modifier.fillMaxSize()
    ) {
        composable(route = Screen.HomeScreen.name) {
            HomeScreen(
                viewModel = viewModel,
                onMainClick = {
                    navController.navigate(Screen.SecondScreen.name)
                }
            )
        }
        composable(route = Screen.SecondScreen.name) {
            SecondScreen(
                viewModel = viewModel,
                onBack = {
                    navController.navigate(Screen.HomeScreen.name)
                }
            )
        }
    }
}
