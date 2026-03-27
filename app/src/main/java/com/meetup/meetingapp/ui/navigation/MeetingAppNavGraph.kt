package com.meetup.meetingapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.meetup.meetingapp.ui.screens.create_creating_event_page.CreateCreatingEventPage
import com.meetup.meetingapp.ui.screens.create_creating_event_page.CreateCreatingEventPageDestination
import com.meetup.meetingapp.ui.screens.home.HomeDestination
import com.meetup.meetingapp.ui.screens.home.HomeScreen
import com.meetup.meetingapp.ui.screens.create_or_join_page.CreateOrJoinPage
import com.meetup.meetingapp.ui.screens.create_or_join_page.CreateOrJoinDestination
import com.meetup.meetingapp.ui.screens.create_event_button_page.CreateEventButtonPage
import com.meetup.meetingapp.ui.screens.create_event_button_page.CreateEventButtonDestination

/**
 * Provides Navigation graph for the application.
 */

@Composable
fun MeetingAppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController, startDestination = HomeDestination.route, modifier = modifier
    ) {
        /**
         * Home destination
         */
        composable(route = HomeDestination.route) {
            HomeScreen(
                onMainClick = { navController.navigate(CreateOrJoinDestination.route) }
            )
        }

        /**
         * Create or join destination
         */
        composable(route = CreateOrJoinDestination.route) {
            CreateOrJoinPage(
                onBack = { navController.popBackStack() },
                navigateToCreatingEventPage = {
                    navController.navigate(CreateCreatingEventPageDestination.route)
                }
            )
        }

        /**
         * Create event destination
         */
        composable(route = CreateCreatingEventPageDestination.route) {
            CreateCreatingEventPage(
                onBack = { navController.popBackStack() },
                navigateToCreatingEventPage = {
                    navController.navigate(CreateEventButtonDestination.route)
                }
            )
        }

        /**
         * Create event button destination (The Checkbox Page)
         */
        composable(route = CreateEventButtonDestination.route) {
            CreateEventButtonPage(
                onBack = { navController.popBackStack() }
            )
        }
    }
}
