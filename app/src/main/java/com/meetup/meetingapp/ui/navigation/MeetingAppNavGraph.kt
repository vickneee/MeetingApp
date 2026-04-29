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
import com.meetup.meetingapp.ui.screens.eventcreation.AddTimeSlotsPage
import com.meetup.meetingapp.ui.screens.eventcreation.AreaSelectingDestination
import com.meetup.meetingapp.ui.screens.eventcreation.AreaSelectingPage
import com.meetup.meetingapp.ui.screens.eventcreation.CreateEventDestination
import com.meetup.meetingapp.ui.screens.eventcreation.CreateEventPage
import com.meetup.meetingapp.ui.screens.eventcreation.CreatingEventPage
import com.meetup.meetingapp.ui.screens.eventcreation.CreatingEventPageDestination
import com.meetup.meetingapp.ui.screens.eventcreation.EditTimeSlotDestination
import com.meetup.meetingapp.ui.screens.eventcreation.EditTimeSlotScreen
import com.meetup.meetingapp.ui.screens.eventcreation.EventCreatedDestination
import com.meetup.meetingapp.ui.screens.eventcreation.EventCreatedPage
import com.meetup.meetingapp.ui.screens.eventcreation.EventViewModel
import com.meetup.meetingapp.ui.screens.eventcreation.TimeSlotsSelectingPageDestination
import com.meetup.meetingapp.ui.screens.eventlist.EventListDestination
import com.meetup.meetingapp.ui.screens.eventlist.EventsListPage
import com.meetup.meetingapp.ui.screens.home.HomeDestination
import com.meetup.meetingapp.ui.screens.home.HomeScreen
import com.meetup.meetingapp.ui.screens.home.HomeViewModel
import com.meetup.meetingapp.ui.screens.hostdashboard.HostDashboardDestination
import com.meetup.meetingapp.ui.screens.hostdashboard.HostDashboardPage
import com.meetup.meetingapp.ui.screens.hostdashboard.HostDashboardViewModel
import com.meetup.meetingapp.ui.screens.joinpage.JoinDestination
import com.meetup.meetingapp.ui.screens.joinpage.JoinPage
import com.meetup.meetingapp.ui.screens.participantdashboard.ParticipantDashboardDestination
import com.meetup.meetingapp.ui.screens.participantdashboard.ParticipantDashboardPage
import com.meetup.meetingapp.ui.screens.participantinput.AvailabilitySelectingPage
import com.meetup.meetingapp.ui.screens.participantinput.MeetUpDetailDestination
import com.meetup.meetingapp.ui.screens.participantinput.MeetUpDetailPage
import com.meetup.meetingapp.ui.screens.participantinput.ParticipantViewModel
import com.meetup.meetingapp.ui.screens.participantinput.PlaceTypeAndKeywordDestination
import com.meetup.meetingapp.ui.screens.participantinput.PlaceTypeAndKeywordPage
import com.meetup.meetingapp.ui.screens.participantinput.SmallAreaSelectingDestination
import com.meetup.meetingapp.ui.screens.participantinput.SmallAreaSelectingPage
import com.meetup.meetingapp.ui.screens.participantinput.SubmissionCompleteDestination
import com.meetup.meetingapp.ui.screens.participantinput.SubmissionCompletePage
import com.meetup.meetingapp.ui.screens.participantinput.TimeAvailabilityDestination
import com.meetup.meetingapp.ui.screens.placevote.DateAndAreaPageDestination
import com.meetup.meetingapp.ui.screens.placevote.DateAndAreaPageDestination.DateAndAreaPage
import com.meetup.meetingapp.ui.screens.placevote.PlaceDetailsDestination
import com.meetup.meetingapp.ui.screens.placevote.PlaceDetailsPage
import com.meetup.meetingapp.ui.screens.placevote.PlaceListPage
import com.meetup.meetingapp.ui.screens.placevote.PlaceListPageDestination
import com.meetup.meetingapp.ui.screens.placevote.PlaceViewModel

/**
 * Defines the full navigation graph for the MeetingApp.
 *
 * This file configures:
 *
 * 1. **Top‑level destinations**
 *    - Home
 *    - Join Event
 *    - Event List
 *    - Host Dashboard
 *    - Participant Dashboard
 *
 * 2. **Nested navigation graphs**
 *    - `event_creation_graph`
 *         → All event‑creation screens share a single [EventViewModel]
 *    - `participant-input`
 *         → All participant‑input screens share a single [ParticipantViewModel]
 *    - `vote-for-place`
 *         → All place‑voting screens share a single [PlaceViewModel]
 *
 * 3. **Shared ViewModel scoping**
 *    ViewModels are scoped to their respective navigation graphs using:
 *
 *        navController.getBackStackEntry(graphRoute)
 *
 *    This ensures that:
 *    - User input persists across multiple screens
 *    - State is not lost when navigating forward/backward
 *    - Each flow (event creation, participant input, voting) maintains its own state
 *
 * 4. **Animated transitions**
 *    All navigation transitions use fade‑in / fade‑out animations for a smoother UX.
 *
 * 5. **Deep‑link‑like argument passing**
 *    Several destinations accept dynamic arguments such as:
 *    - `eventId`
 *    - `eventCode`
 *    - `eventKey`
 *    - `placeId`
 *
 * Overall, this file acts as the central routing map for the entire application,
 * defining how users move between screens and how shared state is preserved.
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
         * Nested navigation graph for the event‑creation flow.
         *
         * All screens inside this graph share a single [EventViewModel] instance.
         * The ViewModel is scoped to this graph using:
         *
         *     navController.getBackStackEntry("event_creation_graph")
         *
         * This ensures that:
         * - User input (time slots, area, event settings) persists across screens
         * - State is not lost when navigating forward/backward
         * - The event creation process behaves as a single multi‑step form
         *
         * Screens included:
         * - CreatingEventPage
         * - AddTimeSlotsPage
         * - EditTimeSlotScreen
         * - AreaSelectingPage
         * - CreateEventPage
         * - EventCreatedPage
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
                    eventId = eventId,
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
         *
         * All screens inside this graph share a single [ParticipantViewModel] instance.
         * The ViewModel is scoped to this graph using:
         *
         *     navController.getBackStackEntry("participant-input")
         *
         * This allows the participant’s selections (availability, cities, place types)
         * to persist across multiple steps without being lost.
         *
         * Screens included:
         * - MeetUpDetailPage
         * - AvailabilitySelectingPage
         * - SmallAreaSelectingPage
         * - PlaceTypeAndKeywordPage
         * - SubmissionCompletePage
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
                    },
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
                backStackEntry.arguments?.getString(HostDashboardDestination.EVENT_ID_ARG) ?: ""
            HostDashboardPage(
                onBack = { navController.popBackStack() },
                onVoteForRestaurantClick = { navController.navigate("${DateAndAreaPageDestination.route}/$eventId") },
                onFinalPlanClick = { placeId ->
                    navController.navigate("${PlaceDetailsDestination.route}/$eventId/$placeId")
                },
                onFillAvailability = { eventCode, eventKey ->
                    navController.navigate("${MeetUpDetailDestination.route}/$eventCode/$eventKey")
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
                    ?.getString(ParticipantDashboardDestination.EVENT_ID_ARG) ?: ""
            ParticipantDashboardPage(
                onBack = { navController.popBackStack() },
                onVoteForRestaurantClick = { navController.navigate("${DateAndAreaPageDestination.route}/$eventId") },
                onFinalPlanClick = { placeId ->
                    navController.navigate("${PlaceDetailsDestination.route}/$eventId/$placeId")
                },
                onFillAvailability = { eventCode, eventKey ->
                    navController.navigate("${MeetUpDetailDestination.route}/$eventCode/$eventKey")
                },
                onNavigateToHome = {
                    navController.navigate(HomeDestination.route)
                },
            )
        }

        /**
         * Nested navigation graph for the place‑voting flow.
         *
         * All screens inside this graph share a single [PlaceViewModel] instance.
         * The ViewModel is scoped to this graph using:
         *
         *     navController.getBackStackEntry("vote-for-place")
         *
         * This ensures that:
         * - The selected date/area persists across screens
         * - The restaurant list and selected place remain consistent
         * - The voting flow behaves as a unified multi‑step process
         *
         * Screens included:
         * - DateAndAreaPage
         * - PlaceListPage
         * - PlaceDetailsPage
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
                    onEditSelection = {
                        val event = viewModel.event.value
                        if (event != null) {
                            navController.navigate(
                                "${MeetUpDetailDestination.route}/${event.eventCode}/${event.eventKey}",
                            )
                        }
                    },
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
                    onEditSelection = {
                        val event = viewModel.event.value
                        if (event != null) {
                            navController.navigate(
                                "${MeetUpDetailDestination.route}/${event.eventCode}/${event.eventKey}",
                            )
                        }
                    },
                    onNavigateToPlaceDetails = { placeId ->
                        val eventId = parentEntry.arguments?.getString(DateAndAreaPageDestination.EVENT_ID_ARG) ?: ""
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
                    backStackEntry.arguments?.getString(PlaceDetailsDestination.PLACE_ID_ARG)
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
