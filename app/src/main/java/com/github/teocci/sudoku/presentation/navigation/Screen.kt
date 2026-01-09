package com.github.teocci.sudoku.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.ui.graphics.vector.ImageVector
import com.github.teocci.sudoku.domain.model.Difficulty

/**
 * Sealed class representing all navigation destinations in the app.
 */
sealed class Screen(
    val route: String,
    val title: String = ""
) {
    /**
     * Main/Home screen with difficulty selection.
     */
    data object Main : Screen(
        route = "main",
        title = "Sudoku"
    )

    /**
     * Game screen where the puzzle is played.
     * Arguments:
     * - difficulty: The difficulty level (easy, medium, hard, expert)
     * - isDaily: Whether this is a daily challenge
     * - dateEpoch: The date for daily challenges (epoch days)
     */
    data object Game : Screen(
        route = "game/{difficulty}?isDaily={isDaily}&dateEpoch={dateEpoch}",
        title = "Game"
    ) {
        const val ARG_DIFFICULTY = "difficulty"
        const val ARG_IS_DAILY = "isDaily"
        const val ARG_DATE_EPOCH = "dateEpoch"

        /**
         * Create a route for a regular game.
         */
        fun createRoute(difficulty: Difficulty): String {
            return "game/${difficulty.name.lowercase()}?isDaily=false&dateEpoch=0"
        }

        /**
         * Create a route for a daily challenge.
         */
        fun createDailyRoute(difficulty: Difficulty, dateEpochDay: Long): String {
            return "game/${difficulty.name.lowercase()}?isDaily=true&dateEpoch=$dateEpochDay"
        }

        /**
         * Parse difficulty from route argument.
         */
        fun parseDifficulty(difficultyArg: String?): Difficulty {
            return difficultyArg?.let { arg ->
                Difficulty.entries.find { it.name.equals(arg, ignoreCase = true) }
            } ?: Difficulty.MEDIUM
        }
    }

    /**
     * Daily challenges screen with calendar.
     * Arguments:
     * - autoSelectNext: Optional boolean to auto-select next unsolved challenge
     */
    data object DailyChallenges : Screen(
        route = "daily_challenges?autoSelectNext={autoSelectNext}",
        title = "Daily Challenges"
    ) {
        const val ARG_AUTO_SELECT_NEXT = "autoSelectNext"

        fun createRoute(autoSelectNext: Boolean = false): String {
            return "daily_challenges?autoSelectNext=$autoSelectNext"
        }
    }

    /**
     * Profile/Me screen with statistics.
     */
    data object Profile : Screen(
        route = "profile",
        title = "Me"
    )

    /**
     * Settings screen.
     */
    data object Settings : Screen(
        route = "settings",
        title = "Settings"
    )

    /**
     * Game completion screen.
     * Arguments:
     * - score: Final score
     * - time: Time taken in seconds
     * - difficulty: Difficulty level
     * - isDaily: Whether it was a daily challenge
     */
    data object GameComplete : Screen(
        route = "game_complete/{score}/{time}/{difficulty}/{isDaily}",
        title = "Complete!"
    ) {
        const val ARG_SCORE = "score"
        const val ARG_TIME = "time"
        const val ARG_DIFFICULTY = "difficulty"
        const val ARG_IS_DAILY = "isDaily"

        fun createRoute(
            score: Int,
            timeSeconds: Long,
            difficulty: Difficulty,
            isDaily: Boolean
        ): String {
            return "game_complete/$score/$timeSeconds/${difficulty.name.lowercase()}/$isDaily"
        }
    }

    /**
     * Game over screen (failed).
     */
    data object GameOver : Screen(
        route = "game_over/{score}/{difficulty}",
        title = "Game Over"
    ) {
        const val ARG_SCORE = "score"
        const val ARG_DIFFICULTY = "difficulty"

        fun createRoute(score: Int, difficulty: Difficulty): String {
            return "game_over/$score/${difficulty.name.lowercase()}"
        }
    }

    /**
     * Daily challenge completed screen.
     * Arguments:
     * - score: Final score
     * - time: Time taken in seconds
     * - difficulty: Difficulty level
     * - dateEpoch: The challenge date (epoch days)
     */
    data object DailyChallengeCompleted : Screen(
        route = "daily_challenge_completed/{score}/{time}/{difficulty}/{dateEpoch}",
        title = "Challenge Completed!"
    ) {
        const val ARG_SCORE = "score"
        const val ARG_TIME = "time"
        const val ARG_DIFFICULTY = "difficulty"
        const val ARG_DATE_EPOCH = "dateEpoch"

        fun createRoute(
            score: Int,
            timeSeconds: Long,
            difficulty: Difficulty,
            dateEpoch: Long
        ): String {
            return "daily_challenge_completed/$score/$timeSeconds/${difficulty.name.lowercase()}/$dateEpoch"
        }
    }

    companion object {
        /**
         * Get all bottom navigation screens.
         */
        fun bottomNavScreens(): List<Screen> = listOf(Main, DailyChallenges, Profile)

        /**
         * Get the start destination.
         */
        fun startDestination(): String = Main.route
    }
}

/**
 * Bottom navigation item with icon variants.
 */
sealed class BottomNavItem(
    val screen: Screen,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val label: String
) {
    data object Home : BottomNavItem(
        screen = Screen.Main,
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home,
        label = "Home"
    )

    data object Daily : BottomNavItem(
        screen = Screen.DailyChallenges,
        selectedIcon = Icons.Filled.CalendarMonth,
        unselectedIcon = Icons.Outlined.CalendarMonth,
        label = "Daily"
    )

    data object Me : BottomNavItem(
        screen = Screen.Profile,
        selectedIcon = Icons.Filled.Person,
        unselectedIcon = Icons.Outlined.Person,
        label = "Me"
    )

    companion object {
        /**
         * Get all bottom navigation items.
         */
        fun items(): List<BottomNavItem> = listOf(Home, Daily, Me)

        /**
         * Find the bottom nav item for a given route.
         */
        fun fromRoute(route: String?): BottomNavItem? {
            return items().find { it.screen.route == route }
        }
    }
}

/**
 * Navigation actions for common navigation patterns.
 */
object NavigationActions {
    /**
     * Navigate to a new game.
     */
    fun navigateToGame(
        navController: androidx.navigation.NavController,
        difficulty: Difficulty
    ) {
        navController.navigate(Screen.Game.createRoute(difficulty))
    }

    /**
     * Navigate to a daily challenge.
     */
    fun navigateToDailyChallenge(
        navController: androidx.navigation.NavController,
        difficulty: Difficulty,
        dateEpochDay: Long
    ) {
        navController.navigate(Screen.Game.createDailyRoute(difficulty, dateEpochDay))
    }

    /**
     * Navigate to game completion screen.
     */
    fun navigateToGameComplete(
        navController: androidx.navigation.NavController,
        score: Int,
        timeSeconds: Long,
        difficulty: Difficulty,
        isDaily: Boolean
    ) {
        navController.navigate(Screen.GameComplete.createRoute(score, timeSeconds, difficulty, isDaily)) {
            // Remove game screen from back stack
            popUpTo(Screen.Main.route) {
                inclusive = false
            }
        }
    }

    /**
     * Navigate to game over screen.
     */
    fun navigateToGameOver(
        navController: androidx.navigation.NavController,
        score: Int,
        difficulty: Difficulty
    ) {
        navController.navigate(Screen.GameOver.createRoute(score, difficulty)) {
            // Remove game screen from back stack
            popUpTo(Screen.Main.route) {
                inclusive = false
            }
        }
    }

    /**
     * Navigate to settings.
     */
    fun navigateToSettings(navController: androidx.navigation.NavController) {
        navController.navigate(Screen.Settings.route)
    }

    /**
     * Navigate back to main/home.
     */
    fun navigateToMain(navController: androidx.navigation.NavController) {
        navController.navigate(Screen.Main.route) {
            popUpTo(Screen.Main.route) {
                inclusive = true
            }
        }
    }

    /**
     * Pop back stack (go back).
     */
    fun navigateBack(navController: androidx.navigation.NavController) {
        navController.popBackStack()
    }
}

/**
 * Extension to check if current destination is a bottom nav destination.
 */
fun String?.isBottomNavRoute(): Boolean {
    return Screen.bottomNavScreens().any { it.route == this }
}

/**
 * Extension to check if current destination should show bottom nav.
 */
fun String?.shouldShowBottomNav(): Boolean {
    return this?.let { route ->
        // Show bottom nav on main screens, hide on game screens
        Screen.bottomNavScreens().any { it.route == route }
    } ?: false
}
