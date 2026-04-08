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
import com.meetup.meetingapp.ui.screens.create_event_flow.AreaSelectingDestination
import com.meetup.meetingapp.ui.screens.create_event_flow.AreaSelectingPage
import com.meetup.meetingapp.ui.screens.create_event_flow.CreateCreatingEventPage
import com.meetup.meetingapp.ui.screens.create_event_flow.CreateCreatingEventPageDestination
import com.meetup.meetingapp.ui.screens.create_event_flow.CreateEventButtonDestination
import com.meetup.meetingapp.ui.screens.create_event_flow.CreateEventButtonPage
import com.meetup.meetingapp.ui.screens.create_event_flow.EditTimeSlotDestination
import com.meetup.meetingapp.ui.screens.create_event_flow.EditTimeSlotScreen
import com.meetup.meetingapp.ui.screens.create_event_flow.EventCreatedDestination
import com.meetup.meetingapp.ui.screens.create_event_flow.EventCreatedPage
import com.meetup.meetingapp.ui.screens.create_event_flow.EventViewModel
import com.meetup.meetingapp.ui.screens.create_event_flow.TimeSlotsSelectingPage
import com.meetup.meetingapp.ui.screens.create_event_flow.TimeSlotsSelectingPageDestination
import com.meetup.meetingapp.ui.screens.create_or_join_page.CreateOrJoinDestination
import com.meetup.meetingapp.ui.screens.create_or_join_page.CreateOrJoinPage
import com.meetup.meetingapp.ui.screens.home.HomeDestination
import com.meetup.meetingapp.ui.screens.home.HomeScreen
import com.meetup.meetingapp.ui.screens.host_dashboard.HostDashboardDestination
import com.meetup.meetingapp.ui.screens.host_dashboard.HostDashboardPage
import com.meetup.meetingapp.ui.screens.participant_input.AvailabilitySelectingPage
import com.meetup.meetingapp.ui.screens.participant_input.ParticipantMeetUpDetailDestination
import com.meetup.meetingapp.ui.screens.participant_input.ParticipantMeetUpDetailDestination.eventCodeArg
import com.meetup.meetingapp.ui.screens.participant_input.ParticipantMeetUpDetailPage
import com.meetup.meetingapp.ui.screens.participant_input.ParticipantPlaceTypeAndKeywordDestination
import com.meetup.meetingapp.ui.screens.participant_input.ParticipantPlaceTypeAndKeywordPage
import com.meetup.meetingapp.ui.screens.participant_input.ParticipantViewModel
import com.meetup.meetingapp.ui.screens.participant_input.SmallAreaSelectingDestination
import com.meetup.meetingapp.ui.screens.participant_input.SmallAreaSelectingPage
import com.meetup.meetingapp.ui.screens.participant_input.SubmissionCompleteDestination
import com.meetup.meetingapp.ui.screens.participant_input.SubmissionCompletePage
import com.meetup.meetingapp.ui.screens.participant_input.TimeAvailabilityDestination
import com.meetup.meetingapp.ui.screens.past_events_page.PastEventsDestination
import com.meetup.meetingapp.ui.screens.past_events_page.PastEventsPage
import com.meetup.meetingapp.ui.screens.vote_for_restaurant_flow.ParticipantDashboardWaitingDestination
import com.meetup.meetingapp.ui.screens.vote_for_restaurant_flow.ParticipantDashboardWaitingPage
import com.meetup.meetingapp.ui.screens.vote_for_restaurant_flow.RestaurantViewModel

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
                },
                navigateToParticipantPage = { (eventCode, eventKey) ->
                    navController.navigate("${ParticipantMeetUpDetailDestination.route}/$eventCode/$eventKey")
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
                        navController.navigate(TimeSlotsSelectingPageDestination.route)
                    }
                )
            }

            /**
             * Time slots selecting page
             */
            composable(TimeSlotsSelectingPageDestination.route) { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry("event_creation_graph")
                }
                val viewModel: EventViewModel = viewModel(
                    parentEntry,
                    factory = AppViewModelProvider.Factory
                )

                TimeSlotsSelectingPage(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    navigateToTimeEditPage = { index ->
                        navController.navigate(EditTimeSlotDestination.route + "/$index")
                        },
                    navigateToAreaSelectingPage = {
                        navController.navigate(AreaSelectingDestination.route)
                    }
                )
            }

            composable(EditTimeSlotDestination.route + "/{index}") { backStackEntry ->
                val index = backStackEntry.arguments?.getString("index")?.toInt() ?: -1
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry("event_creation_graph")
                }
                val viewModel: EventViewModel = viewModel(
                    parentEntry,
                    factory = AppViewModelProvider.Factory
                )

                EditTimeSlotScreen(
                    index = index,
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    navigateToTimeSlotsSelectingPage = {
                        navController.navigate(TimeSlotsSelectingPageDestination.route)
                    }
                )
            }

            /**
             * Area selecting destination
             */
            composable(AreaSelectingDestination.route) { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry("event_creation_graph")
                }
                val viewModel: EventViewModel = viewModel(
                    parentEntry,
                    factory = AppViewModelProvider.Factory
                )

                AreaSelectingPage(
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
                    onNavigateToDashboard = { eventId ->
                        navController.navigate("${HostDashboardDestination.route}/$eventId")

                    },
                    onNavigateToAvailability = { eventCode, eventKey ->
                        navController.navigate("${ParticipantMeetUpDetailDestination.route}/$eventCode/$eventKey")
                    }
                )
            }
        }

        /**
         * Nested navigation graph for the participant input flow.
         * All screens inside this graph share the same ParticipantViewModel instance.
         */
        navigation(
            startDestination = ParticipantMeetUpDetailDestination.routeWithArgs,
            route = "participant-input"
        ) {
            composable(ParticipantMeetUpDetailDestination.routeWithArgs) { backStackEntry ->
                // Parent entry for scoping the ViewModel to this navigation graph
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry("participant-input")
                }

                // The ViewModel reads the eventCode from SavedStateHandle automatically
                val participantViewModel: ParticipantViewModel = viewModel(
                    parentEntry,
                    factory = AppViewModelProvider.Factory
                )

                ParticipantMeetUpDetailPage(
                    onBack = { navController.popBackStack() },
                    viewModel = participantViewModel,
                    eventCode = eventCodeArg,
                    onNavigateToTimeAvailability = {
                        navController.navigate(TimeAvailabilityDestination.route)
                    }
                )
            }

            composable(TimeAvailabilityDestination.route) { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry("participant-input")
                }
                val participantViewModel: ParticipantViewModel = viewModel(
                    parentEntry,
                    factory = AppViewModelProvider.Factory
                )

                AvailabilitySelectingPage(
                    onBack = { navController.popBackStack() },
                    viewModel = participantViewModel,
                    navigateToNextStep = {
                        navController.navigate(SmallAreaSelectingDestination.route)
                    }
                )
            }

            /**
             * Screen for selecting one or more cities within the host's chosen cities.
             *
             */
            composable(SmallAreaSelectingDestination.route) { backStackEntry ->
                // Parent entry for scoping the ViewModel to this navigation graph
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry("participant-input")
                }

                // The ViewModel reads the eventCode from SavedStateHandle automatically
                val participantViewModel: ParticipantViewModel = viewModel(
                    parentEntry,
                    factory = AppViewModelProvider.Factory
                )

                SmallAreaSelectingPage(
                    onBack = { navController.popBackStack() },
                    viewModel = participantViewModel,
                    onNext = {
                        navController.navigate(ParticipantPlaceTypeAndKeywordDestination.route)
                    }
                )
            }

            /**
             * Screen for selecting preferred place types (e.g., café, restaurant) and food categories.
             *
             */
            composable(ParticipantPlaceTypeAndKeywordDestination.route) { backStackEntry ->
                // Parent entry for scoping the ViewModel to this navigation graph
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry("participant-input")
                }

                // The ViewModel reads the eventCode from SavedStateHandle automatically
                val participantViewModel: ParticipantViewModel = viewModel(
                    parentEntry,
                    factory = AppViewModelProvider.Factory
                )

                ParticipantPlaceTypeAndKeywordPage(
                    onBack = { navController.popBackStack() },
                    viewModel = participantViewModel,
                    onNavigateToSubmissionCompletePage = {
                        navController.navigate(SubmissionCompleteDestination.route)
                    }
                )
            }

            /**
             * Final screen in the participant input flow.
             *
             * Confirms that the participant’s preferences have been submitted.
             *
             */
            composable(SubmissionCompleteDestination.route) { backStackEntry ->
                // Parent entry for scoping the ViewModel to this navigation graph
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry("participant-input")
                }

                // The ViewModel reads the eventCode from SavedStateHandle automatically
                val participantViewModel: ParticipantViewModel = viewModel(
                    parentEntry,
                    factory = AppViewModelProvider.Factory
                )

                SubmissionCompletePage(
                    onBack = { navController.popBackStack() },
                    viewModel = participantViewModel,
                    onNavigateToDashboard = {
                        navController.navigate("${HostDashboardDestination.route}/$it")
                    }
                )
            }
        }

        /**
         * Host Dashboard destination
         */
        composable(route = HostDashboardDestination.routeWithArgs) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getString(HostDashboardDestination.eventIdArg) ?: ""
            HostDashboardPage(
                onBack = { navController.popBackStack() },
                onVoteForRestaurantClick = {
                    navController.navigate("${ParticipantDashboardWaitingDestination.route}/$eventId")
                }
            )
        }

        /**
         * Nested navigation graph for the participant input flow.
         * All screens inside this graph share the same ParticipantViewModel instance.
         */
        navigation(
            startDestination = ParticipantMeetUpDetailDestination.routeWithArgs,
            route = "vote-for-restaurant"
        ) {
            composable(ParticipantDashboardWaitingDestination.routeWithArgs) { backStackEntry ->
                // Parent entry for scoping the ViewModel to this navigation graph
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry("vote-for-restaurant")
                }
                val viewModel: RestaurantViewModel = viewModel(
                    parentEntry,
                    factory = AppViewModelProvider.Factory
                )

                ParticipantDashboardWaitingPage(
                    onBack = { navController.popBackStack() },
                    onNavigateToChooseDatePage = { /* */ },
                    onNavigateToHome = {
                        navController.navigate(HomeDestination.route)
                    },
                    viewModel = viewModel
                )
            }
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
