package com.github.teocci.sudoku.presentation.main

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.teocci.sudoku.core.formatTime
import com.github.teocci.sudoku.domain.model.Difficulty
import com.github.teocci.sudoku.ui.theme.DifficultyEasy
import com.github.teocci.sudoku.ui.theme.DifficultyExpert
import com.github.teocci.sudoku.ui.theme.DifficultyHard
import com.github.teocci.sudoku.ui.theme.DifficultyMedium
import com.github.teocci.sudoku.ui.theme.SudokuPuzzleMasterTheme
import com.github.teocci.sudoku.ui.theme.SudokuTheme
import kotlinx.coroutines.flow.collectLatest

/**
 * Main/Home screen with difficulty selection.
 *
 * @param viewModel The main screen ViewModel
 * @param onNavigateToGame Called when starting a new game
 * @param onNavigateToSettings Called when settings is clicked
 */
@Composable
fun MainScreen(
    viewModel: MainViewModel = viewModel(),
    onNavigateToGame: (Difficulty) -> Unit = {},
    onNavigateToSettings: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    // Handle navigation events
    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collectLatest { event ->
            when (event) {
                is MainNavigationEvent.NavigateToGame -> onNavigateToGame(event.difficulty)
                is MainNavigationEvent.NavigateToSettings -> onNavigateToSettings()
                is MainNavigationEvent.NavigateToDailyChallenges -> { /* Handled by bottom nav */ }
                is MainNavigationEvent.NavigateToProfile -> { /* Handled by bottom nav */ }
            }
        }
    }

    MainScreenContent(
        uiState = uiState,
        onDifficultyClick = viewModel::startGame,
        onSettingsClick = viewModel::navigateToSettings,
        onContinueClick = viewModel::continueGame
    )
}

/**
 * Main screen content (stateless for preview).
 */
@Composable
private fun MainScreenContent(
    uiState: MainUiState,
    onDifficultyClick: (Difficulty) -> Unit,
    onSettingsClick: () -> Unit,
    onContinueClick: () -> Unit
) {
    val colors = SudokuTheme.colors
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header with settings
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.End
        ) {
            IconButton(onClick = onSettingsClick) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = colors.controlButtonIcon
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // App title
        Text(
            text = "Sudoku",
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Text(
            text = "Puzzle Master",
            fontSize = 20.sp,
            fontWeight = FontWeight.Light,
            color = colors.controlButtonIcon
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Continue game button (if available)
        if (uiState.hasSavedGame) {
            ContinueGameCard(
                difficulty = uiState.savedGameDifficulty ?: Difficulty.MEDIUM,
                progress = uiState.savedGameProgress,
                onClick = onContinueClick
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "or start a new game",
                fontSize = 14.sp,
                color = colors.controlButtonIcon
            )

            Spacer(modifier = Modifier.height(16.dp))
        } else {
            Text(
                text = "Select Difficulty",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = colors.controlButtonIcon
            )

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Difficulty buttons
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Difficulty.entries.forEach { difficulty ->
                DifficultyButton(
                    difficulty = difficulty,
                    onClick = { onDifficultyClick(difficulty) }
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Statistics card
        uiState.statistics?.let { stats ->
            StatisticsCard(statistics = stats)
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

/**
 * Continue game card for saved games.
 */
@Composable
private fun ContinueGameCard(
    difficulty: Difficulty,
    progress: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = SudokuTheme.colors
    val difficultyColor = getDifficultyColor(difficulty)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = colors.controlButtonBackground
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Play icon
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(difficultyColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Continue",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Continue Game",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.numberFixed
                )

                Text(
                    text = "${difficulty.displayName} • ${(progress * 100).toInt()}% complete",
                    fontSize = 14.sp,
                    color = colors.controlButtonIcon
                )
            }
        }
    }
}

/**
 * Difficulty selection button.
 */
@Composable
private fun DifficultyButton(
    difficulty: Difficulty,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = SudokuTheme.colors
    val difficultyColor = getDifficultyColor(difficulty)

    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = colors.controlButtonBackground,
            contentColor = colors.numberFixed
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Difficulty indicator
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(difficultyColor, CircleShape)
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Difficulty name
            Text(
                text = difficulty.displayName,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )

            // Cells to solve
            Text(
                text = "${difficulty.cellsToRemove} cells",
                fontSize = 14.sp,
                color = colors.controlButtonIcon
            )
        }
    }
}

/**
 * Statistics card showing user stats.
 */
@Composable
private fun StatisticsCard(
    statistics: UserStatistics,
    modifier: Modifier = Modifier
) {
    val colors = SudokuTheme.colors

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = colors.controlButtonBackground
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "Your Statistics",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.numberFixed
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    value = statistics.gamesPlayed.toString(),
                    label = "Played"
                )
                StatItem(
                    value = statistics.winRateFormatted,
                    label = "Win Rate"
                )
                StatItem(
                    value = statistics.currentStreak.toString(),
                    label = "Streak"
                )
                StatItem(
                    value = formatTime(statistics.bestTime),
                    label = "Best Time"
                )
            }
        }
    }
}

/**
 * Individual statistic item.
 */
@Composable
private fun StatItem(
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    val colors = SudokuTheme.colors

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = colors.scoreText
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = label,
            fontSize = 12.sp,
            color = colors.controlButtonIcon
        )
    }
}

/**
 * Get the color associated with a difficulty level.
 */
private fun getDifficultyColor(difficulty: Difficulty): Color {
    return when (difficulty) {
        Difficulty.EASY -> DifficultyEasy
        Difficulty.MEDIUM -> DifficultyMedium
        Difficulty.HARD -> DifficultyHard
        Difficulty.EXPERT -> DifficultyExpert
    }
}

// ==================== PREVIEWS ====================

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun PreviewMainScreen() {
    SudokuPuzzleMasterTheme(darkTheme = true) {
        MainScreenContent(
            uiState = MainUiState(
                statistics = UserStatistics(
                    gamesPlayed = 42,
                    gamesWon = 38,
                    currentStreak = 5,
                    bestStreak = 12,
                    averageTime = 480L,
                    bestTime = 245L
                )
            ),
            onDifficultyClick = {},
            onSettingsClick = {},
            onContinueClick = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun PreviewMainScreen_WithSavedGame() {
    SudokuPuzzleMasterTheme(darkTheme = true) {
        MainScreenContent(
            uiState = MainUiState(
                hasSavedGame = true,
                savedGameDifficulty = Difficulty.MEDIUM,
                savedGameProgress = 0.65f,
                statistics = UserStatistics(
                    gamesPlayed = 42,
                    gamesWon = 38,
                    currentStreak = 5,
                    bestStreak = 12,
                    averageTime = 480L,
                    bestTime = 245L
                )
            ),
            onDifficultyClick = {},
            onSettingsClick = {},
            onContinueClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewDifficultyButton() {
    SudokuPuzzleMasterTheme(darkTheme = true) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DifficultyButton(difficulty = Difficulty.EASY, onClick = {})
            DifficultyButton(difficulty = Difficulty.MEDIUM, onClick = {})
            DifficultyButton(difficulty = Difficulty.HARD, onClick = {})
            DifficultyButton(difficulty = Difficulty.EXPERT, onClick = {})
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewStatisticsCard() {
    SudokuPuzzleMasterTheme(darkTheme = true) {
        Box(modifier = Modifier.padding(16.dp)) {
            StatisticsCard(
                statistics = UserStatistics(
                    gamesPlayed = 42,
                    gamesWon = 38,
                    currentStreak = 5,
                    bestStreak = 12,
                    averageTime = 480L,
                    bestTime = 245L
                )
            )
        }
    }
}
