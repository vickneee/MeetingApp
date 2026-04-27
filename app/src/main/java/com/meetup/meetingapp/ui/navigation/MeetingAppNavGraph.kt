package com.meetup.meetingapp.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.meetup.meetingapp.ui.AppViewModelProvider
import com.meetup.meetingapp.ui.screens.create_event_flow.AddTimeSlotsPage
import com.meetup.meetingapp.ui.screens.create_event_flow.AreaSelectingDestination
import com.meetup.meetingapp.ui.screens.create_event_flow.AreaSelectingPage
import com.meetup.meetingapp.ui.screens.create_event_flow.CreateEventDestination
import com.meetup.meetingapp.ui.screens.create_event_flow.CreateEventPage
import com.meetup.meetingapp.ui.screens.create_event_flow.CreatingEventPage
import com.meetup.meetingapp.ui.screens.create_event_flow.CreatingEventPageDestination
import com.meetup.meetingapp.ui.screens.create_event_flow.EditTimeSlotDestination
import com.meetup.meetingapp.ui.screens.create_event_flow.EditTimeSlotScreen
import com.meetup.meetingapp.ui.screens.create_event_flow.EventCreatedDestination
import com.meetup.meetingapp.ui.screens.create_event_flow.EventCreatedPage
import com.meetup.meetingapp.ui.screens.create_event_flow.EventViewModel
import com.meetup.meetingapp.ui.screens.create_event_flow.TimeSlotsSelectingPageDestination
import com.meetup.meetingapp.ui.screens.event_list_page.EventListDestination
import com.meetup.meetingapp.ui.screens.event_list_page.EventsListPage
import com.meetup.meetingapp.ui.screens.home.HomeDestination
import com.meetup.meetingapp.ui.screens.home.HomeScreen
import com.meetup.meetingapp.ui.screens.home.HomeViewModel
import com.meetup.meetingapp.ui.screens.host_dashboard.HostDashboardDestination
import com.meetup.meetingapp.ui.screens.host_dashboard.HostDashboardPage
import com.meetup.meetingapp.ui.screens.host_dashboard.HostDashboardViewModel
import com.meetup.meetingapp.ui.screens.join_page.JoinDestination
import com.meetup.meetingapp.ui.screens.join_page.JoinPage
import com.meetup.meetingapp.ui.screens.participant_dashboard.ParticipantDashboardDestination
import com.meetup.meetingapp.ui.screens.participant_dashboard.ParticipantDashboardPage
import com.meetup.meetingapp.ui.screens.participant_input_flow.AvailabilitySelectingPage
import com.meetup.meetingapp.ui.screens.participant_input_flow.MeetUpDetailDestination
import com.meetup.meetingapp.ui.screens.participant_input_flow.MeetUpDetailPage
import com.meetup.meetingapp.ui.screens.participant_input_flow.ParticipantViewModel
import com.meetup.meetingapp.ui.screens.participant_input_flow.PlaceTypeAndKeywordDestination
import com.meetup.meetingapp.ui.screens.participant_input_flow.PlaceTypeAndKeywordPage
import com.meetup.meetingapp.ui.screens.participant_input_flow.SmallAreaSelectingDestination
import com.meetup.meetingapp.ui.screens.participant_input_flow.SmallAreaSelectingPage
import com.meetup.meetingapp.ui.screens.participant_input_flow.SubmissionCompleteDestination
import com.meetup.meetingapp.ui.screens.participant_input_flow.SubmissionCompletePage
import com.meetup.meetingapp.ui.screens.participant_input_flow.TimeAvailabilityDestination
import com.meetup.meetingapp.ui.screens.vote_for_place_flow.DateAndAreaPage
import com.meetup.meetingapp.ui.screens.vote_for_place_flow.DateAndAreaPageDestination
import com.meetup.meetingapp.ui.screens.vote_for_place_flow.PlaceDetailsDestination
import com.meetup.meetingapp.ui.screens.vote_for_place_flow.PlaceDetailsPage
import com.meetup.meetingapp.ui.screens.vote_for_place_flow.PlaceListPage
import com.meetup.meetingapp.ui.screens.vote_for_place_flow.PlaceListPageDestination
import com.meetup.meetingapp.ui.screens.vote_for_place_flow.PlaceViewModel

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
 * all three screens by scoping the ViewModel to the \"event_creation_graph\"
 * navigation graph. This ensures that user input is preserved across screens.
 *
 * @param navController The NavController used to navigate between screens.
 * @param modifier Optional modifier for layout adjustments.
 */
@Composable
fun MeetingAppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    val homeViewModel: HomeViewModel = viewModel(factory = AppViewModelProvider.Factory)
    val currentUserId by homeViewModel.currentUserId.collectAsStateWithLifecycle()

    NavHost(
        navController = navController,
        startDestination = HomeDestination.route,
        enterTransition = {
            fadeIn(animationSpec = tween(500))
        },
        exitTransition = {
            fadeOut(animationSpec = tween(500))
        },
        popEnterTransition = {
            fadeIn(animationSpec = tween(500))
        },
        popExitTransition = {
            fadeOut(animationSpec = tween(500))
        },
        modifier = modifier,
    ) {
        /**
         * Home destination
         */
        composable(route = HomeDestination.route) {
            HomeScreen(
                onCreateEventClick = { navController.navigate(CreatingEventPageDestination.route) },
                onJoinEventClick = { navController.navigate(JoinDestination.route) },
                onEventsClick = { navController.navigate(EventListDestination.route) },
            )
        }

        /**
         * Join destination
         */
        composable(route = JoinDestination.route) {
            JoinPage(
                onBack = { navController.popBackStack() },
                navigateToPastEventsPage = {
                    navController.navigate(EventListDestination.route)
                },
                navigateToParticipantPage = { (eventCode, eventKey) ->
                    navController.navigate("${MeetUpDetailDestination.route}/$eventCode/$eventKey")
                },
            )
        }

        /**
         * Nested navigation graph for the event creation flow.
         * All screens inside this graph share the same EventViewModel instance.
         */
        navigation(
            startDestination = CreatingEventPageDestination.route,
            route = "event_creation_graph",
        ) {
            /**
             * Create event destination
             */
            composable(CreatingEventPageDestination.route) { backStackEntry ->
                // Parent entry for scoping the ViewModel to this navigation graph
                val parentEntry =
                    remember(backStackEntry) {
                        navController.getBackStackEntry("event_creation_graph")
                    }
                val viewModel: EventViewModel =
                    viewModel(
                        parentEntry,
                        factory = AppViewModelProvider.Factory,
                    )

                CreatingEventPage(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    navigateToCreatingEventPage = {
                        navController.navigate(TimeSlotsSelectingPageDestination.route)
                    },
                )
            }

            /**
             * Time slots selecting page
             */
            composable(TimeSlotsSelectingPageDestination.route) { backStackEntry ->
                // Parent entry for scoping the ViewModel to this navigation graph
                val parentEntry =
                    remember(backStackEntry) {
                        navController.getBackStackEntry("event_creation_graph")
                    }
                val viewModel: EventViewModel =
                    viewModel(
                        parentEntry,
                        factory = AppViewModelProvider.Factory,
                    )

                AddTimeSlotsPage(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    navigateToTimeEditPage = { index ->
                        navController.navigate(EditTimeSlotDestination.route + "/$index")
                    },
                    navigateToAreaSelectingPage = {
                        navController.navigate(AreaSelectingDestination.route)
                    },
                )
            }

            composable(EditTimeSlotDestination.route + "/{index}") { backStackEntry ->
                val index = backStackEntry.arguments?.getString("index")?.toInt() ?: -1
                // Parent entry for scoping the ViewModel to this navigation graph
                val parentEntry =
                    remember(backStackEntry) {
                        navController.getBackStackEntry("event_creation_graph")
                    }
                val viewModel: EventViewModel =
                    viewModel(
                        parentEntry,
                        factory = AppViewModelProvider.Factory,
                    )

                EditTimeSlotScreen(
                    index = index,
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    navigateToTimeSlotsSelectingPage = {
                        navController.navigate(TimeSlotsSelectingPageDestination.route)
                    },
                )
            }

            /**
             * Area selecting destination
             */
            composable(AreaSelectingDestination.route) { backStackEntry ->
                // Parent entry for scoping the ViewModel to this navigation graph
                val parentEntry =
                    remember(backStackEntry) {
                        navController.getBackStackEntry("event_creation_graph")
                    }
                val viewModel: EventViewModel =
                    viewModel(
                        parentEntry,
                        factory = AppViewModelProvider.Factory,
                    )

                AreaSelectingPage(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    navigateToCreatingEventPage = {
                        navController.navigate(CreateEventDestination.route)
                    },
                )
            }

            /**
             * Create event button destination (The Checkbox Page)
             */
            composable(CreateEventDestination.route) { backStackEntry ->
                // Parent entry for scoping the ViewModel to this navigation graph
                val parentEntry =
                    remember(backStackEntry) {
                        navController.getBackStackEntry("event_creation_graph")
                    }
                val viewModel: EventViewModel =
                    viewModel(
                        parentEntry,
                        factory = AppViewModelProvider.Factory,
                    )

                CreateEventPage(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onCreatedEvent = {
                        navController.navigate(EventCreatedDestination.route)
                    },
                )
            }

            /**
             * Event created destination
             */
            composable(EventCreatedDestination.route + "/{eventId}") { backStackEntry ->
                val eventId = backStackEntry.arguments?.getString("eventId") ?: ""
                // Parent entry for scoping the ViewModel to this navigation graph
                val parentEntry =
                    remember(backStackEntry) {
                        navController.getBackStackEntry("event_creation_graph")
                    }
                val viewModel: EventViewModel =
                    viewModel(
                        parentEntry,
                        factory = AppViewModelProvider.Factory,
                    )

                // Load the existing event data into the shared ViewModel
                LaunchedEffect(eventId) {
                    viewModel.loadExistingEvent(eventId)
                }

                EventCreatedPage(
                    viewModel = viewModel,
                    onNavigateToHome = { navController.navigate(HomeDestination.route) },
                    onNavigateToDashboard = { eventId ->
                        navController.navigate("${HostDashboardDestination.route}/$eventId")
                    },
                    onNavigateToAvailability = { eventCode, eventKey ->
                        navController.navigate("${MeetUpDetailDestination.route}/$eventCode/$eventKey")
                    },
                    eventId = eventId
                )
            }
            
            // Fallback for direct navigation from CreateEventPage
            composable(EventCreatedDestination.route) { backStackEntry ->
                val parentEntry =
                    remember(backStackEntry) {
                        navController.getBackStackEntry("event_creation_graph")
                    }
                val viewModel: EventViewModel =
                    viewModel(
                        parentEntry,
                        factory = AppViewModelProvider.Factory,
                    )

                EventCreatedPage(
                    viewModel = viewModel,
                    onNavigateToHome = { navController.navigate(HomeDestination.route) },
                    onNavigateToDashboard = { eventId ->
                        navController.navigate("${HostDashboardDestination.route}/$eventId")
                    },
                    onNavigateToAvailability = { eventCode, eventKey ->
                        navController.navigate("${MeetUpDetailDestination.route}/$eventCode/$eventKey")
                    },
                )
            }
        }

        /**
         * Nested navigation graph for the participant input flow.
         * All screens inside this graph share the same ParticipantViewModel instance.
         */
        navigation(
            startDestination = MeetUpDetailDestination.routeWithArgs,
            route = "participant-input",
        ) {
            composable(MeetUpDetailDestination.routeWithArgs) { backStackEntry ->
                // Parent entry for scoping the ViewModel to this navigation graph
                val parentEntry =
                    remember(backStackEntry) {
                        navController.getBackStackEntry("participant-input")
                    }

                // The ViewModel reads the eventCode from SavedStateHandle automatically
                val participantViewModel: ParticipantViewModel =
                    viewModel(
                        parentEntry,
                        factory = AppViewModelProvider.Factory,
                    )

                MeetUpDetailPage(
                    onBack = { navController.popBackStack() },
                    viewModel = participantViewModel,
                    onNavigateToTimeAvailability = {
                        navController.navigate(TimeAvailabilityDestination.route)
                    },
                    onNavigateToHome = {
                        navController.navigate(HomeDestination.route)
                    }
                )
            }

            /**
             * Screen for selecting time availability.
             */
            composable(TimeAvailabilityDestination.route) { backStackEntry ->
                // Parent entry for scoping the ViewModel to this navigation graph
                val parentEntry =
                    remember(backStackEntry) {
                        navController.getBackStackEntry("participant-input")
                    }
                val participantViewModel: ParticipantViewModel =
                    viewModel(
                        parentEntry,
                        factory = AppViewModelProvider.Factory,
                    )

                AvailabilitySelectingPage(
                    onBack = { navController.popBackStack() },
                    viewModel = participantViewModel,
                    navigateToNextStep = {
                        navController.navigate(SmallAreaSelectingDestination.route)
                    },
                )
            }

            /**
             * Screen for selecting one or more cities within the host's chosen cities.
             */
            composable(SmallAreaSelectingDestination.route) { backStackEntry ->
                // Parent entry for scoping the ViewModel to this navigation graph
                val parentEntry =
                    remember(backStackEntry) {
                        navController.getBackStackEntry("participant-input")
                    }

                // The ViewModel reads the eventCode from SavedStateHandle automatically
                val participantViewModel: ParticipantViewModel =
                    viewModel(
                        parentEntry,
                        factory = AppViewModelProvider.Factory,
                    )

                SmallAreaSelectingPage(
                    onBack = { navController.popBackStack() },
                    viewModel = participantViewModel,
                    onNext = {
                        navController.navigate(PlaceTypeAndKeywordDestination.route)
                    },
                )
            }

            /**
             * Screen for selecting preferred place types (e.g., café, restaurant) and food categories.
             */
            composable(PlaceTypeAndKeywordDestination.route) { backStackEntry ->
                // Parent entry for scoping the ViewModel to this navigation graph
                val parentEntry =
                    remember(backStackEntry) {
                        navController.getBackStackEntry("participant-input")
                    }

                // The ViewModel reads the eventCode from SavedStateHandle automatically
                val participantViewModel: ParticipantViewModel =
                    viewModel(
                        parentEntry,
                        factory = AppViewModelProvider.Factory,
                    )

                PlaceTypeAndKeywordPage(
                    onBack = { navController.popBackStack() },
                    viewModel = participantViewModel,
                    onNavigateToSubmissionCompletePage = {
                        navController.navigate(SubmissionCompleteDestination.route)
                    },
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
                val parentEntry =
                    remember(backStackEntry) {
                        navController.getBackStackEntry("participant-input")
                    }

                // The ViewModel reads the eventCode from SavedStateHandle automatically
                val participantViewModel: ParticipantViewModel =
                    viewModel(
                        parentEntry,
                        factory = AppViewModelProvider.Factory,
                    )

                SubmissionCompletePage(
                    viewModel = participantViewModel,
                    onHomeClick = {
                        navController.navigate(HomeDestination.route)
                    },
                    onNavigateToHostDashboard = { eventId ->
                        navController.navigate("${HostDashboardDestination.route}/$eventId")
                    },
                    onNavigateToParticipantDashboard = { eventId ->
                        navController.navigate("${ParticipantDashboardDestination.route}/$eventId")
                    },
                )
            }
        }

        /**
         * Host Dashboard destination
         */
        composable(route = HostDashboardDestination.routeWithArgs) { backStackEntry ->
            val viewModel: HostDashboardViewModel =
                viewModel(
                    backStackEntry,
                    factory = AppViewModelProvider.Factory,
                )
            val eventId =
                backStackEntry.arguments?.getString(HostDashboardDestination.EVENTIDARG) ?: ""
            HostDashboardPage(
                onBack = { navController.popBackStack() },
                onVoteForRestaurantClick = { navController.navigate("${DateAndAreaPageDestination.route}/$eventId") },
                onFinalPlanClick = { placeId ->
                    navController.navigate("${PlaceDetailsDestination.route}/$eventId/$placeId")
                },
                onFillAvailability = { eventId, eventKey ->
                    navController.navigate("${MeetUpDetailDestination.route}/$eventId/$eventKey")
                },
                onNavigateToHome = {
                    navController.navigate(HomeDestination.route)
                },
                onShowEventCodes = {
                    navController.navigate(EventCreatedDestination.route + "/$eventId")
                },
                viewModel = viewModel,
            )
        }

        /**
         * Participant Dashboard destination
         */
        composable(ParticipantDashboardDestination.routeWithArgs) { backStackEntry ->
            val eventId =
                backStackEntry.arguments
                    ?.getString(ParticipantDashboardDestination.EVENTIDARG) ?: ""
            ParticipantDashboardPage(
                onBack = { navController.popBackStack() },
                onVoteForRestaurantClick = { navController.navigate("${DateAndAreaPageDestination.route}/$eventId") },
                onFinalPlanClick = { placeId ->
                    navController.navigate("${PlaceDetailsDestination.route}/$eventId/$placeId")
                },
                onFillAvailability = { eventId, eventKey ->
                    navController.navigate("${MeetUpDetailDestination.route}/$eventId/$eventKey")
                },
                onNavigateToHome = {
                    navController.navigate(HomeDestination.route)
                },
            )
        }

        /**
         * Nested navigation graph for the participant input flow.
         * All screens inside this graph share the same PlaceViewModel instance.
         */
        navigation(
            startDestination = DateAndAreaPageDestination.routeWithArgs,
            route = "vote-for-place",
        ) {
            /**
             * Date and area destination
             */
            composable(DateAndAreaPageDestination.routeWithArgs) { backStackEntry ->
                val parentEntry =
                    remember(backStackEntry) {
                        navController.getBackStackEntry("vote-for-place")
                    }
                val viewModel: PlaceViewModel =
                    viewModel(
                        parentEntry,
                        factory = AppViewModelProvider.Factory,
                    )
                DateAndAreaPage(
                    onBack = { navController.popBackStack() },
                    onNavigateToRestaurantListPage = {
                        navController.navigate(PlaceListPageDestination.route)
                    },
                    viewModel = viewModel,
                )
            }

            /**
             * Place list destination
             */
            composable(PlaceListPageDestination.route) { backStackEntry ->
                val parentEntry =
                    remember(backStackEntry) {
                        navController.getBackStackEntry("vote-for-place")
                    }
                val viewModel: PlaceViewModel =
                    viewModel(
                        parentEntry,
                        factory = AppViewModelProvider.Factory,
                    )

                PlaceListPage(
                    onBack = { navController.popBackStack() },
                    onEditSelection = { navController.navigate(MeetUpDetailDestination.route) },
                    onNavigateToPlaceDetails = { placeId ->
                        val eventId = parentEntry.arguments?.getString(DateAndAreaPageDestination.EVENTIDARG) ?: ""
                        navController.navigate("${PlaceDetailsDestination.route}/$eventId/$placeId")
                    },
                    viewModel = viewModel,
                )
            }

            /**
             * Place detail destination
             */
            composable(PlaceDetailsDestination.routeWithArgs) { backStackEntry ->

                val placeId =
                    backStackEntry.arguments?.getString(PlaceDetailsDestination.placeIdArg)
                        ?: ""

                val parentEntry =
                    remember(backStackEntry) {
                        navController.getBackStackEntry("vote-for-place")
                    }
                val viewModel: PlaceViewModel =
                    viewModel(
                        parentEntry,
                        factory = AppViewModelProvider.Factory,
                    )

                PlaceDetailsPage(
                    onBack = { navController.popBackStack() },
                    onNavigateToHostDashboard = { eventId ->
                        navController.navigate("${HostDashboardDestination.route}/$eventId")
                    },
                    onNavigateToParticipantDashboard = { eventId ->
                        navController.navigate("${ParticipantDashboardDestination.route}/$eventId")
                    },
                    onNavigateToHome = {
                        navController.navigate(HomeDestination.route)
                    },
                    viewModel = viewModel,
                    placeId = placeId,
                )
            }
        }

        /**
         * Events List destination
         */
        composable(route = EventListDestination.route) {
            EventsListPage(
                onBack = { navController.popBackStack() },
                currentUserId = currentUserId,
                onNavigateToHostDashboard = { eventId ->
                    navController.navigate("${HostDashboardDestination.route}/$eventId")
                },
                onNavigateToParticipantDashboard = { eventId ->
                    navController.navigate("${ParticipantDashboardDestination.route}/$eventId")
                },
            )
        }
    }
}
