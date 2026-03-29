package com.meetup.meetingapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.meetup.meetingapp.ui.AppViewModelProvider
import com.meetup.meetingapp.ui.screens.EventViewModel
import com.meetup.meetingapp.ui.screens.create_creating_event_page.CreateCreatingEventPage
import com.meetup.meetingapp.ui.screens.create_creating_event_page.CreateCreatingEventPageDestination
import com.meetup.meetingapp.ui.screens.home.HomeDestination
import com.meetup.meetingapp.ui.screens.home.HomeScreen
import com.meetup.meetingapp.ui.screens.create_or_join_page.CreateOrJoinPage
import com.meetup.meetingapp.ui.screens.create_or_join_page.CreateOrJoinDestination
import com.meetup.meetingapp.ui.screens.create_event_button_page.CreateEventButtonPage
import com.meetup.meetingapp.ui.screens.create_event_button_page.CreateEventButtonDestination
import com.meetup.meetingapp.ui.screens.event_created_page.EventCreatedDestination
import com.meetup.meetingapp.ui.screens.event_created_page.EventCreatedPage
import com.meetup.meetingapp.ui.screens.host_dashboard.HostDashboardDestination
import com.meetup.meetingapp.ui.screens.host_dashboard.HostDashboardPage
import com.meetup.meetingapp.ui.screens.past_events_page.PastEventsDestination
import com.meetup.meetingapp.ui.screens.past_events_page.PastEventsPage

/**
 * Main navigation graph for the MeetingApp.
 *
 * This NavHost defines all top‑level destinations and the nested navigation graph
 * used for the event‑creation flow.
 *
 * Structure:
 * - Home
 * - Create or Join
 * - Event Creation Flow (nested graph)
 *      - CreateCreatingEventPage
 *      - CreateEventButtonPage
 *      - EventCreatedPage
 * - Host Dashboard
 *
 * The event creation flow shares a single EventViewModel instance across
 * all three screens by scoping the ViewModel to the "event_creation_graph"
 * navigation graph. This ensures that user input is preserved across screens.
 *
 * @param navController The NavController used to navigate between screens.
 * @param modifier Optional modifier for layout adjustments.
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
                onMainClick = { navController.navigate(CreateOrJoinDestination.route) },
                onEventsClick = { navController.navigate(PastEventsDestination.route) }
            )
        }

        /**
         * Create or join destination
         */
        composable(route = CreateOrJoinDestination.route) {
            CreateOrJoinPage(
                onBack = { navController.popBackStack() },
                navigateToCreatingEventPage = {
                    navController.navigate("event_creation_graph")
                },
                navigateToPastEventsPage = {
                    navController.navigate(PastEventsDestination.route)
                }
            )
        }

        /**
         * Nested navigation graph for the event creation flow.
         * All screens inside this graph share the same EventViewModel instance.
         */
        navigation(
            startDestination = CreateCreatingEventPageDestination.route,
            route = "event_creation_graph"
        ) {
            /**
             * Create event destination
             */
            composable(CreateCreatingEventPageDestination.route) { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry("event_creation_graph")
                }
                val viewModel: EventViewModel = viewModel(
                    parentEntry,
                    factory = AppViewModelProvider.Factory
                )

                CreateCreatingEventPage(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    navigateToCreatingEventPage = {
                        navController.navigate(CreateEventButtonDestination.route)
                    }
                )
            }

            /**
             * Create event button destination (The Checkbox Page)
             */
            composable(CreateEventButtonDestination.route) { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry("event_creation_graph")
                }
                val viewModel: EventViewModel = viewModel(
                    parentEntry,
                    factory = AppViewModelProvider.Factory
                )

                CreateEventButtonPage(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onCreatedEvent = {
                        navController.navigate(EventCreatedDestination.route)
                    }
                )
            }

            /**
             * Event created destination
             */
            composable(EventCreatedDestination.route) { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry("event_creation_graph")
                }
                val viewModel: EventViewModel = viewModel(
                    parentEntry,
                    factory = AppViewModelProvider.Factory
                )

                EventCreatedPage(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onNavigateToDashboard = {
                        navController.navigate(HostDashboardDestination.route)
                    }
                )
            }
        }

        /**
         * Host Dashboard destination
         */
        composable(route = HostDashboardDestination.route) {
            HostDashboardPage(
                onBack = { navController.popBackStack() }
            )
        }

        /**
         * Past Events destination
         */
        composable(route = "past_events") {
            PastEventsPage(
                onBack = { navController.popBackStack() }
            )
        }
    }
}
