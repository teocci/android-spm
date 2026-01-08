package com.github.teocci.sudoku.presentation.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.github.teocci.sudoku.domain.model.Difficulty
import com.github.teocci.sudoku.presentation.daily.DailyChallengesScreen
import com.github.teocci.sudoku.presentation.game.GameScreen
import com.github.teocci.sudoku.presentation.main.MainScreen
import com.github.teocci.sudoku.presentation.profile.ProfileScreen
import com.github.teocci.sudoku.presentation.settings.SettingsScreen
import java.time.LocalDate

/**
 * Main navigation graph for the Sudoku app.
 */
@Composable
fun SudokuNavGraph(
    navController: NavHostController = rememberNavController(),
    onThemeChanged: (Boolean) -> Unit = {}
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Determine if bottom nav should be shown
    val showBottomNav = remember(currentRoute) {
        currentRoute in listOf(
            Screen.Main.route,
            Screen.DailyChallenges.route,
            Screen.Profile.route
        )
    }

    Scaffold(
        bottomBar = {
            if (showBottomNav) {
                BottomNavBar(
                    navController = navController
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            NavHost(
                navController = navController,
                startDestination = Screen.Main.route,
                enterTransition = {
                    fadeIn(animationSpec = tween(300))
                },
                exitTransition = {
                    fadeOut(animationSpec = tween(300))
                }
            ) {
                // Main/Home screen
                composable(route = Screen.Main.route) {
                    MainScreen(
                        onNavigateToGame = { difficulty ->
                            navController.navigate(Screen.Game.createRoute(difficulty))
                        },
                        onNavigateToSettings = {
                            navController.navigate(Screen.Settings.route)
                        }
                    )
                }

                // Game screen
                composable(
                    route = Screen.Game.route,
                    arguments = listOf(
                        navArgument(Screen.Game.ARG_DIFFICULTY) {
                            type = NavType.StringType
                            defaultValue = Difficulty.MEDIUM.name
                        },
                        navArgument(Screen.Game.ARG_IS_DAILY) {
                            type = NavType.BoolType
                            defaultValue = false
                        },
                        navArgument(Screen.Game.ARG_DATE_EPOCH) {
                            type = NavType.LongType
                            nullable = false
                            defaultValue = 0L
                        }
                    ),
                    enterTransition = {
                        slideIntoContainer(
                            towards = AnimatedContentTransitionScope.SlideDirection.Left,
                            animationSpec = tween(300)
                        )
                    },
                    exitTransition = {
                        slideOutOfContainer(
                            towards = AnimatedContentTransitionScope.SlideDirection.Right,
                            animationSpec = tween(300)
                        )
                    }
                ) { backStackEntry ->
                    val difficultyName = backStackEntry.arguments?.getString(Screen.Game.ARG_DIFFICULTY)
                        ?: Difficulty.MEDIUM.name
                    val difficulty = Difficulty.entries.find { it.name == difficultyName }
                        ?: Difficulty.MEDIUM
                    val isDaily = backStackEntry.arguments?.getBoolean(Screen.Game.ARG_IS_DAILY) ?: false
                    val dateEpoch = backStackEntry.arguments?.getLong(Screen.Game.ARG_DATE_EPOCH) ?: 0L
                    val date = if (dateEpoch > 0) LocalDate.ofEpochDay(dateEpoch) else null

                    GameScreen(
                        onNavigateBack = {
                            navController.popBackStack()
                        },
                        onNavigateToSettings = {
                            navController.navigate(Screen.Settings.route)
                        }
                    )
                }

                // Daily Challenges screen
                composable(route = Screen.DailyChallenges.route) {
                    DailyChallengesScreen(
                        onNavigateToGame = { date, difficulty ->
                            navController.navigate(
                                Screen.Game.createDailyRoute(difficulty, date.toEpochDay())
                            )
                        }
                    )
                }

                // Profile screen
                composable(route = Screen.Profile.route) {
                    ProfileScreen()
                }

                // Settings screen
                composable(
                    route = Screen.Settings.route,
                    enterTransition = {
                        slideIntoContainer(
                            towards = AnimatedContentTransitionScope.SlideDirection.Left,
                            animationSpec = tween(300)
                        )
                    },
                    exitTransition = {
                        slideOutOfContainer(
                            towards = AnimatedContentTransitionScope.SlideDirection.Right,
                            animationSpec = tween(300)
                        )
                    }
                ) {
                    SettingsScreen(
                        onNavigateBack = {
                            navController.popBackStack()
                        },
                        onThemeChanged = onThemeChanged
                    )
                }
            }
        }
    }
}

/**
 * Navigation actions helper.
 */
class SudokuNavigationActions(private val navController: NavHostController) {

    /**
     * Navigate to game with difficulty.
     */
    fun navigateToGame(difficulty: Difficulty) {
        navController.navigate(Screen.Game.createRoute(difficulty))
    }

    /**
     * Navigate to daily game.
     */
    fun navigateToDailyGame(date: LocalDate, difficulty: Difficulty) {
        navController.navigate(Screen.Game.createDailyRoute(difficulty, date.toEpochDay()))
    }

    /**
     * Navigate to settings.
     */
    fun navigateToSettings() {
        navController.navigate(Screen.Settings.route)
    }

    /**
     * Navigate to daily challenges.
     */
    fun navigateToDailyChallenges() {
        navController.navigate(Screen.DailyChallenges.route) {
            popUpTo(Screen.Main.route) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    /**
     * Navigate to profile.
     */
    fun navigateToProfile() {
        navController.navigate(Screen.Profile.route) {
            popUpTo(Screen.Main.route) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    /**
     * Navigate back.
     */
    fun navigateBack() {
        navController.popBackStack()
    }

    /**
     * Navigate to main.
     */
    fun navigateToMain() {
        navController.navigate(Screen.Main.route) {
            popUpTo(navController.graph.startDestinationId) {
                inclusive = true
            }
        }
    }
}
