package com.github.teocci.sudoku.presentation.game

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.teocci.sudoku.domain.model.Difficulty
import com.github.teocci.sudoku.domain.model.GameState
import com.github.teocci.sudoku.domain.model.Position
import com.github.teocci.sudoku.domain.model.SudokuBoard
import com.github.teocci.sudoku.presentation.components.FloatingScore
import com.github.teocci.sudoku.presentation.components.FloatingScoreContainer
import com.github.teocci.sudoku.presentation.components.GameCompletionCelebration
import com.github.teocci.sudoku.presentation.components.GameControls
import com.github.teocci.sudoku.presentation.components.GameHeader
import com.github.teocci.sudoku.presentation.components.NumberPad
import com.github.teocci.sudoku.presentation.components.SudokuGrid
import com.github.teocci.sudoku.presentation.components.SudokuGridPaused
import com.github.teocci.sudoku.presentation.daily.components.ResumeDailyChallengeDialog
import com.github.teocci.sudoku.ui.theme.SudokuPuzzleMasterTheme
import com.github.teocci.sudoku.ui.theme.SudokuTheme
import kotlinx.coroutines.flow.collectLatest

/**
 * Main game screen composable.
 *
 * @param viewModel The game ViewModel
 * @param onNavigateBack Called when back button is pressed
 * @param onNavigateToSettings Called when settings button is pressed
 * @param onGameComplete Called when game is won
 * @param onGameOver Called when game is lost
 */
@Composable
fun GameScreen(
    viewModel: GameViewModel = viewModel(factory = GameViewModel.Factory),
    onNavigateBack: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onGameComplete: (score: Int, time: Long, difficulty: Difficulty, isDaily: Boolean) -> Unit = { _, _, _, _ -> },
    onGameOver: (score: Int, difficulty: Difficulty) -> Unit = { _, _ -> }
) {
    val gameState by viewModel.gameState.collectAsState()
    val floatingScores by viewModel.floatingScores.collectAsState()
    val completingCells by viewModel.completingCells.collectAsState()
    val hintCell by viewModel.hintCell.collectAsState()
    val showCelebration by viewModel.showCelebration.collectAsState()
    val numberCounts by viewModel.numberCounts.collectAsState()

    val haptic = LocalHapticFeedback.current

    // State for resume dialog
    var showResumeDialog by remember { mutableStateOf(false) }
    var savedGameState by remember { mutableStateOf<GameState?>(null) }

    // Handle events
    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is GameEvent.Mistake -> {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                }
                is GameEvent.Completion -> {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                }
                is GameEvent.GameWon -> {
                    onGameComplete(event.score, event.time, event.difficulty, event.isDaily)
                }
                is GameEvent.GameLost -> {
                    onGameOver(event.score, event.difficulty)
                }
                is GameEvent.ResumeDailyChallenge -> {
                    savedGameState = event.savedState
                    showResumeDialog = true
                }
            }
        }
    }

    GameScreenContent(
        gameState = gameState,
        floatingScores = floatingScores,
        completingCells = completingCells,
        hintCell = hintCell,
        showCelebration = showCelebration,
        numberCounts = numberCounts,
        onBackClick = onNavigateBack,
        onSettingsClick = onNavigateToSettings,
        onPauseClick = viewModel::togglePause,
        onCellClick = viewModel::selectCell,
        onCellDrag = viewModel::onCellDrag,
        onNumberClick = viewModel::onNumberClick,
        onNumberLongClick = viewModel::onNumberLongClick,
        onUndoClick = viewModel::undo,
        onEraseClick = viewModel::eraseCell,
        onNotesClick = viewModel::toggleNotesMode,
        onHintClick = viewModel::useHint,
        onFloatingScoreComplete = viewModel::removeFloatingScore,
        onResumeClick = viewModel::resumeGame
    )

    // Resume dialog overlay
    if (showResumeDialog && savedGameState != null) {
        ResumeDailyChallengeDialog(
            savedGame = savedGameState!!,
            onContinue = {
                viewModel.resumeSavedGame(savedGameState!!)
                showResumeDialog = false
            },
            onRestart = {
                viewModel.clearSavedGame()
                viewModel.restartGame()
                showResumeDialog = false
            },
            onCancel = {
                showResumeDialog = false
                onNavigateBack()
            }
        )
    }
}

/**
 * Game screen content (stateless for preview).
 */
@Composable
private fun GameScreenContent(
    gameState: GameState,
    floatingScores: List<FloatingScore>,
    completingCells: Set<Position>,
    hintCell: Position?,
    showCelebration: Boolean,
    numberCounts: Map<Int, Int>,
    onBackClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onPauseClick: () -> Unit,
    onCellClick: (Position) -> Unit,
    onCellDrag: (Position) -> Unit,
    onNumberClick: (Int) -> Unit,
    onNumberLongClick: (Int) -> Unit,
    onUndoClick: () -> Unit,
    onEraseClick: () -> Unit,
    onNotesClick: () -> Unit,
    onHintClick: () -> Unit,
    onFloatingScoreComplete: (String) -> Unit,
    onResumeClick: () -> Unit
) {
    val colors = SudokuTheme.colors

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            GameHeader(
                date = gameState.gameDate,
                difficulty = gameState.difficulty,
                mistakes = gameState.mistakes,
                maxMistakes = gameState.maxMistakes,
                score = gameState.score,
                elapsedSeconds = gameState.elapsedTimeSeconds,
                isPaused = gameState.isPaused,
                onBackClick = onBackClick,
                onSettingsClick = onSettingsClick,
                onPauseClick = onPauseClick
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Grid area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                if (gameState.isPaused) {
                    // Show paused state
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        SudokuGridPaused(
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = "Game Paused",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.timerPaused
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Tap the timer to resume",
                            fontSize = 14.sp,
                            color = colors.controlButtonIcon
                        )
                    }
                } else {
                    // Show game grid
                    Box {
                        SudokuGrid(
                            board = gameState.board,
                            selectedCell = gameState.selectedCell,
                            activeNumber = gameState.activeNumber,
                            completingCells = completingCells,
                            hintCell = hintCell,
                            onCellClick = onCellClick,
                            onCellDrag = onCellDrag,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Floating scores overlay
                        FloatingScoreContainer(
                            scores = floatingScores,
                            onScoreAnimationComplete = onFloatingScoreComplete
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Controls
            GameControls(
                notesMode = gameState.notesMode,
                canUndo = gameState.canUndo,
                canErase = gameState.canEditSelectedCell && gameState.selectedCellData?.value != null,
                hintsRemaining = gameState.hintsRemaining,
                onUndoClick = onUndoClick,
                onEraseClick = onEraseClick,
                onNotesClick = onNotesClick,
                onHintClick = onHintClick,
                enabled = gameState.isActive
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Number pad
            NumberPad(
                activeNumber = gameState.activeNumber,
                isNumberLocked = gameState.isNumberLocked,
                numberCounts = numberCounts,
                onNumberClick = onNumberClick,
                onNumberLongClick = onNumberLongClick,
                enabled = gameState.isActive
            )

            Spacer(modifier = Modifier.height(72.dp))
        }

        // Celebration overlay
        GameCompletionCelebration(
            isVisible = showCelebration,
            onAnimationComplete = {},
            modifier = Modifier.fillMaxSize()
        )

        // Paused overlay tap area
        if (gameState.isPaused) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background.copy(alpha = 0.3f))
            )
        }
    }
}

/**
 * Game over overlay.
 */
@Composable
fun GameOverOverlay(
    score: Int,
    onNewGame: () -> Unit,
    onMainMenu: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = SudokuTheme.colors

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background.copy(alpha = 0.9f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Game Over",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = colors.error
            )

            Text(
                text = "Too many mistakes!",
                fontSize = 16.sp,
                color = colors.controlButtonIcon
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Final Score",
                fontSize = 14.sp,
                color = colors.controlButtonIcon
            )

            Text(
                text = String.format("%,d", score),
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = colors.scoreText
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Buttons would go here - using text for simplicity
            Text(
                text = "Tap to continue",
                fontSize = 14.sp,
                color = colors.controlButtonIcon,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ==================== PREVIEWS ====================

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun PreviewGameScreen() {
    SudokuPuzzleMasterTheme(darkTheme = true) {
        GameScreenContent(
            gameState = createPreviewGameState(),
            floatingScores = emptyList(),
            completingCells = emptySet(),
            hintCell = null,
            showCelebration = false,
            numberCounts = mapOf(
                1 to 3, 2 to 5, 3 to 9, 4 to 2,
                5 to 7, 6 to 4, 7 to 6, 8 to 1, 9 to 8
            ),
            onBackClick = {},
            onSettingsClick = {},
            onPauseClick = {},
            onCellClick = {},
            onCellDrag = {},
            onNumberClick = {},
            onNumberLongClick = {},
            onUndoClick = {},
            onEraseClick = {},
            onNotesClick = {},
            onHintClick = {},
            onFloatingScoreComplete = {},
            onResumeClick = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun PreviewGameScreen_Paused() {
    SudokuPuzzleMasterTheme(darkTheme = true) {
        GameScreenContent(
            gameState = createPreviewGameState().pause(),
            floatingScores = emptyList(),
            completingCells = emptySet(),
            hintCell = null,
            showCelebration = false,
            numberCounts = emptyMap(),
            onBackClick = {},
            onSettingsClick = {},
            onPauseClick = {},
            onCellClick = {},
            onCellDrag = {},
            onNumberClick = {},
            onNumberLongClick = {},
            onUndoClick = {},
            onEraseClick = {},
            onNotesClick = {},
            onHintClick = {},
            onFloatingScoreComplete = {},
            onResumeClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewGameOverOverlay() {
    SudokuPuzzleMasterTheme(darkTheme = true) {
        GameOverOverlay(
            score = 1250,
            onNewGame = {},
            onMainMenu = {}
        )
    }
}

/**
 * Create a preview game state with sample data.
 */
private fun createPreviewGameState(): GameState {
    return GameState(
        board = SudokuBoard.empty(),
        selectedCell = Position(4, 4),
        activeNumber = 5,
        notesMode = false,
        mistakes = 1,
        score = 1250,
        elapsedTimeSeconds = 325,
        difficulty = Difficulty.MEDIUM,
        gameStatus = com.github.teocci.sudoku.domain.model.GameStatus.IN_PROGRESS,
        hintsRemaining = 2
    )
}
