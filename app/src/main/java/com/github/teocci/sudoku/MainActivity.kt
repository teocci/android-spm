package com.github.teocci.sudoku

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.compose.rememberNavController
import com.github.teocci.sudoku.data.RepositoryProvider
import com.github.teocci.sudoku.data.local.GamePreferences
import com.github.teocci.sudoku.presentation.navigation.SudokuNavGraph
import com.github.teocci.sudoku.ui.theme.SudokuPuzzleMasterTheme

/**
 * Main entry point for the Sudoku Puzzle Master app.
 *
 * Features:
 * - Jetpack Compose UI with Material3
 * - Edge-to-edge display
 * - Dark/Light theme support
 * - Navigation with bottom bar
 */
class MainActivity : ComponentActivity() {

    private lateinit var gamePreferences: GamePreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize preferences
        gamePreferences = GamePreferences.getInstance(this)

        // Initialize repositories
        RepositoryProvider.initialize(this)

        enableEdgeToEdge()

        setContent {
            // Remember theme state
            var darkTheme by remember { mutableStateOf(gamePreferences.darkTheme) }

            // Navigation controller
            val navController = rememberNavController()

            SudokuPuzzleMasterTheme(darkTheme = darkTheme) {
                SudokuNavGraph(
                    navController = navController,
                    onThemeChanged = { isDark ->
                        darkTheme = isDark
                        gamePreferences.darkTheme = isDark
                    }
                )
            }
        }
    }
}
