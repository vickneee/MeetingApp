package com.example.meetingapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.meetingapp.ui.screens.home.HomeDestination
import com.example.meetingapp.ui.screens.home.HomeScreen
import com.example.meetingapp.ui.screens.secondscreen.SecondScreen
import com.example.meetingapp.ui.screens.secondscreen.SecondScreenDestination

@Composable
fun MeetingAppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController, startDestination = HomeDestination.route, modifier = modifier
    ) {
        composable(route = HomeDestination.route) {
            HomeScreen(
                onMainClick = { navController.navigate(SecondScreenDestination.route) }
            )
        }

        composable(route = SecondScreenDestination.route) {
            SecondScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}
