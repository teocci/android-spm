package com.github.teocci.sudoku.presentation.game

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewModelScope
import com.github.teocci.sudoku.core.Constants.GRID_SIZE
import com.github.teocci.sudoku.data.RepositoryProvider
import com.github.teocci.sudoku.data.local.DailyChallengeStore
import com.github.teocci.sudoku.data.repository.AchievementRepository
import com.github.teocci.sudoku.data.repository.GameHistoryEntry
import com.github.teocci.sudoku.data.repository.GameHistoryRepository
import com.github.teocci.sudoku.data.repository.GameRepository
import com.github.teocci.sudoku.data.repository.StatsRepository
import com.github.teocci.sudoku.domain.generator.SudokuGenerator
import com.github.teocci.sudoku.domain.model.Difficulty
import com.github.teocci.sudoku.domain.model.GameAction
import com.github.teocci.sudoku.domain.model.GameState
import com.github.teocci.sudoku.domain.model.GameStatus
import com.github.teocci.sudoku.domain.model.Position
import com.github.teocci.sudoku.domain.model.SudokuBoard
import com.github.teocci.sudoku.domain.scoring.ScoringEngine
import com.github.teocci.sudoku.presentation.components.FloatingScore
import com.github.teocci.sudoku.presentation.navigation.Screen
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * ViewModel for the game screen.
 * Manages all game logic, state, and user interactions.
 */
class GameViewModel(
    savedStateHandle: SavedStateHandle,
    private val gameRepository: GameRepository = RepositoryProvider.getGameRepository(),
    private val statsRepository: StatsRepository = RepositoryProvider.getStatsRepository(),
    private val dailyChallengeStore: DailyChallengeStore = RepositoryProvider.getDailyChallengeStore(),
    private val achievementRepository: AchievementRepository = RepositoryProvider.getAchievementRepository(),
    private val gameHistoryRepository: GameHistoryRepository = RepositoryProvider.getGameHistoryRepository()
) : ViewModel() {

    // Parse navigation arguments
    private val difficultyArg: String? = savedStateHandle[Screen.Game.ARG_DIFFICULTY]
    private val isDailyArg: Boolean = savedStateHandle[Screen.Game.ARG_IS_DAILY] ?: false
    private val dateEpochArg: Long = savedStateHandle[Screen.Game.ARG_DATE_EPOCH] ?: 0L

    private val difficulty: Difficulty = Screen.Game.parseDifficulty(difficultyArg)
    private val isDaily: Boolean = isDailyArg
    private val gameDate: LocalDate = if (dateEpochArg > 0) {
        LocalDate.ofEpochDay(dateEpochArg)
    } else {
        LocalDate.now()
    }

    // Game state
    private val _gameState = MutableStateFlow(GameState.empty())
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    // UI state for animations
    private val _floatingScores = MutableStateFlow<List<FloatingScore>>(emptyList())
    val floatingScores: StateFlow<List<FloatingScore>> = _floatingScores.asStateFlow()

    private val _completingCells = MutableStateFlow<Set<Position>>(emptySet())
    val completingCells: StateFlow<Set<Position>> = _completingCells.asStateFlow()

    private val _hintCell = MutableStateFlow<Position?>(null)
    val hintCell: StateFlow<Position?> = _hintCell.asStateFlow()

    private val _showCelebration = MutableStateFlow(false)
    val showCelebration: StateFlow<Boolean> = _showCelebration.asStateFlow()

    // Number counts for the number pad
    private val _numberCounts = MutableStateFlow<Map<Int, Int>>(emptyMap())
    val numberCounts: StateFlow<Map<Int, Int>> = _numberCounts.asStateFlow()

    // Events
    private val _events = MutableSharedFlow<GameEvent>()
    val events = _events.asSharedFlow()

    // Timer job
    private var timerJob: Job? = null

    init {
        // Check for saved game if this is a daily challenge
        if (isDaily) {
            viewModelScope.launch {
                val savedGame = gameRepository.loadGameState()
                if (savedGame != null &&
                    savedGame.isDailyChallenge &&
                    savedGame.gameDate == gameDate) {
                    // Has in-progress daily challenge - emit event for resume dialog
                    _events.emit(GameEvent.ResumeDailyChallenge(savedGame))
                    return@launch
                }
                // No saved game, start new
                startNewGame()
            }
        } else {
            // Regular game, always start new
            startNewGame()
        }
    }

    /**
     * Start a new game with the configured difficulty.
     */
    fun startNewGame() {
        viewModelScope.launch {
            // Generate puzzle
            val puzzle = if (isDaily) {
                SudokuGenerator.generateDailyChallenge(gameDate, difficulty)
            } else {
                SudokuGenerator.generate(difficulty)
            }

            // Create board and initial state
            val board = puzzle.toBoard()
            val initialState = GameState.create(
                board = board,
                difficulty = difficulty,
                isDailyChallenge = isDaily,
                gameDate = gameDate
            )

            _gameState.value = initialState
            updateNumberCounts()
            startTimer()
        }
    }

    /**
     * Restart the current game (same puzzle).
     */
    fun restartGame() {
        viewModelScope.launch {
            val currentState = _gameState.value

            // Regenerate the same puzzle for daily challenges
            val puzzle = if (isDaily) {
                SudokuGenerator.generateDailyChallenge(gameDate, difficulty)
            } else {
                SudokuGenerator.generate(difficulty)
            }

            val board = puzzle.toBoard()
            _gameState.value = GameState.create(
                board = board,
                difficulty = difficulty,
                isDailyChallenge = isDaily,
                gameDate = gameDate
            )

            _floatingScores.value = emptyList()
            _completingCells.value = emptySet()
            _hintCell.value = null
            _showCelebration.value = false

            updateNumberCounts()
            startTimer()
        }
    }

    /**
     * Handle cell selection.
     */
    fun selectCell(position: Position) {
        if (!_gameState.value.isActive) return

        val state = _gameState.value

        // Early exit for lock notes mode - toggle note without selecting
        if (state.notesMode && state.isNumberLocked && state.activeNumber != null) {
            toggleNoteAt(position, state.activeNumber)
            return
        }

        // If a number is locked (normal mode), place it at clicked position
        if (state.isNumberLocked && state.activeNumber != null) {
            placeNumber(state.activeNumber, targetPosition = position)
            return
        }

        _gameState.update { state ->
            // If same cell is selected, deselect it
            if (state.selectedCell == position) {
                state.clearSelection()
            } else {
                state.selectCell(position)
            }
        }
    }

    /**
     * Handle cell drag (for notes mode).
     */
    fun onCellDrag(position: Position) {
        val state = _gameState.value
        if (!state.isActive || !state.notesMode || state.activeNumber == null) return

        val cell = state.board.getCell(position)
        if (cell.isFixed || cell.value != null) return

        // Toggle note in the dragged cell
        toggleNoteAt(position, state.activeNumber)
    }

    /**
     * Handle number input from number pad.
     */
    fun onNumberClick(number: Int) {
        val state = _gameState.value
        if (!state.isActive) return

        if (state.notesMode) {
            // In notes mode, set active number for painting
            _gameState.update { it.setActiveNumber(number, locked = false) }

            // If a cell is selected, toggle note there
            state.selectedCell?.let { position ->
                toggleNoteAt(position, number)
            }
        } else {
            // In normal mode, place number if cell is selected
            if (state.selectedCell != null) {
                placeNumber(number)
            } else {
                // Set active number for highlighting
                val newActiveNumber = if (state.activeNumber == number) null else number
                _gameState.update { it.setActiveNumber(newActiveNumber, locked = false) }
            }
        }
    }

    /**
     * Handle long press on number (lock number for rapid entry).
     */
    fun onNumberLongClick(number: Int) {
        val state = _gameState.value
        if (!state.isActive) return

        // Toggle lock state
        val isCurrentlyLocked = state.isNumberLocked && state.activeNumber == number

        _gameState.update { state ->
            state.clearSelection()
        }

        _gameState.update {
            it.setActiveNumber(
                number = if (isCurrentlyLocked) null else number,
                locked = !isCurrentlyLocked
            )
        }
    }

    /**
     * Place a number in the selected cell or at a specific position.
     */
    private fun placeNumber(number: Int, targetPosition: Position? = null) {
        val state = _gameState.value
        val position = targetPosition ?: state.selectedCell ?: return
        val cell = state.board.getCell(position)

        if (cell.isFixed) return

        // Store previous value for undo
        val previousValue = cell.value

        // Check if correct
        val isCorrect = cell.solution == number

        // Update board
        var newBoard = state.board.setValue(position, number, checkError = true)

        // Auto-remove notes from related cells
        if (isCorrect) {
            newBoard = newBoard.removeNoteFromRelated(position, number)
        }

        // Create action for history
        val action = GameAction.PlaceNumber(
            position = position,
            number = number,
            previousValue = previousValue,
            wasCorrect = isCorrect
        )

        // Calculate score
        val scoreResult = ScoringEngine.calculatePlacementScore(
            board = newBoard,
            position = position,
            isCorrect = isCorrect,
            difficulty = state.difficulty,
            previouslyCompletedRows = state.completedRows,
            previouslyCompletedCols = state.completedColumns,
            previouslyCompletedBoxes = state.completedBoxes
        )

        // Update state
        _gameState.update { currentState ->
            var newState = currentState
                .updateBoard(newBoard)
                .addScore(scoreResult.multipliedPoints)

            // Add to history if undoable
            if (GameAction.isHistoryAction(action)) {
                newState = newState.addToHistory(action)
            }

            // Handle mistake
            if (!isCorrect) {
                newState = newState.recordMistake()
            }

            // Mark completions
            if (scoreResult.breakdown.rowCompletion > 0) {
                newState = newState.markRowCompleted(position.row)
            }
            if (scoreResult.breakdown.columnCompletion > 0) {
                newState = newState.markColumnCompleted(position.col)
            }
            if (scoreResult.breakdown.boxCompletion > 0) {
                newState = newState.markBoxCompleted(position.boxIndex)
            }

            // Check for game completion
            if (newBoard.isComplete()) {
                newState = newState.complete()
            }

            // Clear locked number if used
            if (!currentState.isNumberLocked) {
                newState = newState.setActiveNumber(null, locked = false)
            }

            newState
        }

        // Update number counts
        updateNumberCounts()

        // Show floating score
        if (scoreResult.multipliedPoints != 0) {
            showFloatingScore(scoreResult.multipliedPoints, position)
        }

        // Handle completions animation
        if (scoreResult.hasCompletions) {
            handleCompletionAnimation(scoreResult, position)
        }

        // Emit events
        viewModelScope.launch {
            if (!isCorrect) {
                _events.emit(GameEvent.Mistake)
            }
            if (scoreResult.hasCompletions) {
                _events.emit(GameEvent.Completion(scoreResult.totalCompletions))
            }
            if (_gameState.value.isWon) {
                handleGameWon()
            }
            if (_gameState.value.isLost) {
                handleGameLost()
            }
        }
    }

    /**
     * Toggle a note at a specific position.
     */
    private fun toggleNoteAt(position: Position, note: Int) {
        val state = _gameState.value
        val cell = state.board.getCell(position)

        if (cell.isFixed || cell.value != null) return

        val wasAdded = note !in cell.notes
        val newBoard = state.board.toggleNote(position, note)

        _gameState.update { it.updateBoard(newBoard) }
    }

    /**
     * Toggle notes mode.
     */
    fun toggleNotesMode() {
        _gameState.update { it.toggleNotesMode() }
    }

    /**
     * Erase the selected cell.
     */
    fun eraseCell() {
        val state = _gameState.value
        val position = state.selectedCell ?: return
        val cell = state.board.getCell(position)

        if (cell.isFixed) return

        // Store previous state for undo
        val previousValue = cell.value
        val previousNotes = cell.notes

        // Create action
        val action = GameAction.EraseCell(
            position = position,
            previousValue = previousValue,
            previousNotes = previousNotes
        )

        // Clear cell
        val newBoard = state.board.clearCell(position)

        _gameState.update { currentState ->
            var newState = currentState.updateBoard(newBoard)

            // Add to history if had a value
            if (GameAction.isHistoryAction(action)) {
                newState = newState.addToHistory(action)
            }

            newState
        }

        updateNumberCounts()
    }

    /**
     * Undo the last action.
     */
    fun undo() {
        val state = _gameState.value
        if (!state.canUndo) return

        val lastAction = state.actionHistory.lastOrNull() ?: return

        when (lastAction) {
            is GameAction.PlaceNumber -> {
                // Restore previous value
                val position = lastAction.position
                val newBoard = if (lastAction.previousValue != null) {
                    state.board.setValue(position, lastAction.previousValue, checkError = false)
                } else {
                    state.board.clearCell(position)
                }

                _gameState.update {
                    it.updateBoard(newBoard).removeLastFromHistory()
                }
            }

            is GameAction.EraseCell -> {
                // Restore previous value and notes
                val position = lastAction.position
                var cell = state.board.getCell(position)

                if (lastAction.previousValue != null) {
                    cell = cell.setValue(lastAction.previousValue, checkError = false)
                }
                lastAction.previousNotes.forEach { note ->
                    cell = cell.toggleNote(note)
                }

                val newBoard = state.board.updateCell(position, cell)
                _gameState.update {
                    it.updateBoard(newBoard).removeLastFromHistory()
                }
            }

            else -> {
                // Other actions not undoable
            }
        }

        updateNumberCounts()
    }

    /**
     * Use a hint.
     */
    fun useHint() {
        val state = _gameState.value
        if (!state.canUseHint) return

        // Find best cell for hint
        val grid = boardToGrid(state.board)
        val hintPosition = ScoringEngine.findBestHintCell(grid) ?: return
        val cell = state.board.getCell(hintPosition)

        // Reveal the cell
        val newBoard = state.board.revealCell(hintPosition)

        // Apply hint penalty
        val hintPenalty = ScoringEngine.calculateHintPenalty(state.difficulty)

        _gameState.update { currentState ->
            currentState
                .updateBoard(newBoard)
                .useHint()
                .addScore(hintPenalty)
        }

        // Show hint highlight
        _hintCell.value = hintPosition
        viewModelScope.launch {
            delay(1500)
            _hintCell.value = null
        }

        // Show floating score
        showFloatingScore(hintPenalty, hintPosition)

        updateNumberCounts()

        // Check for completion
        if (_gameState.value.board.isComplete()) {
            _gameState.update { it.complete() }
            viewModelScope.launch { handleGameWon() }
        }
    }

    /**
     * Pause the game.
     */
    fun pauseGame() {
        timerJob?.cancel()
        _gameState.update { it.pause() }

        // Auto-save daily challenges
        if (isDaily) {
            saveGameState()
        }
    }

    /**
     * Save current game state.
     */
    private fun saveGameState() {
        viewModelScope.launch {
            val state = _gameState.value
            gameRepository.saveGameState(state)
        }
    }

    /**
     * Resume the game.
     */
    fun resumeGame() {
        _gameState.update { it.resume() }
        startTimer()
    }

    /**
     * Toggle pause state.
     */
    fun togglePause() {
        if (_gameState.value.isPaused) {
            resumeGame()
        } else {
            pauseGame()
        }
    }

    // ==================== PRIVATE HELPERS ====================

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                if (_gameState.value.gameStatus == GameStatus.IN_PROGRESS) {
                    _gameState.update { it.tick() }
                }
            }
        }
    }

    private fun updateNumberCounts() {
        val board = _gameState.value.board
        val counts = mutableMapOf<Int, Int>()
        for (num in 1..GRID_SIZE) {
            counts[num] = board.countValue(num)
        }
        _numberCounts.value = counts
    }

    private fun showFloatingScore(points: Int, position: Position) {
        val score = FloatingScore(
            points = points,
            offsetX = (position.col - 4) * 20f,
            offsetY = (position.row - 4) * 20f
        )
        _floatingScores.update { it + score }
    }

    fun removeFloatingScore(id: String) {
        _floatingScores.update { scores -> scores.filter { it.id != id } }
    }

    private fun handleCompletionAnimation(result: ScoringEngine.ScoreResult, position: Position) {
        viewModelScope.launch {
            val completingPositions = mutableSetOf<Position>()

            // Add row positions
            if (result.breakdown.rowCompletion > 0) {
                for (col in 0 until GRID_SIZE) {
                    completingPositions.add(Position(position.row, col))
                }
            }

            // Add column positions
            if (result.breakdown.columnCompletion > 0) {
                for (row in 0 until GRID_SIZE) {
                    completingPositions.add(Position(row, position.col))
                }
            }

            // Add box positions
            if (result.breakdown.boxCompletion > 0) {
                val boxStartRow = (position.row / 3) * 3
                val boxStartCol = (position.col / 3) * 3
                for (r in boxStartRow until boxStartRow + 3) {
                    for (c in boxStartCol until boxStartCol + 3) {
                        completingPositions.add(Position(r, c))
                    }
                }
            }

            _completingCells.value = completingPositions
            delay(500)
            _completingCells.value = emptySet()
        }
    }

    private suspend fun handleGameWon() {
        timerJob?.cancel()
        _showCelebration.value = true

        // Calculate final score with time bonus
        val state = _gameState.value
        val finalScore = ScoringEngine.calculateFinalScore(
            baseScore = state.score,
            elapsedSeconds = state.elapsedTimeSeconds,
            hintsUsed = state.hintsRemaining,
            mistakes = state.mistakes,
            difficulty = state.difficulty
        )

        // Record statistics
        val hintsUsed = 3 - state.hintsRemaining
        val isPerfect = state.mistakes == 0 && hintsUsed == 0

        // 1. Record in stats
        statsRepository.recordGameCompleted(
            difficulty = state.difficulty,
            time = state.elapsedTimeSeconds,
            score = finalScore.multipliedPoints,
            mistakes = state.mistakes,
            hintsUsed = hintsUsed
        )

        // 2. Record in game history
        gameHistoryRepository.recordGame(
            GameHistoryEntry(
                timestamp = System.currentTimeMillis(),
                difficulty = state.difficulty,
                timeSeconds = state.elapsedTimeSeconds,
                score = finalScore.multipliedPoints,
                isCompleted = true,
                mistakes = state.mistakes,
                hintsUsed = hintsUsed,
                isDailyChallenge = isDaily
            )
        )

        // 3. Check achievements
        achievementRepository.checkAndUpdateAchievements()

        // 4. Handle daily challenge completion
        if (isDaily) {
            dailyChallengeStore.markDayCompleted(
                date = gameDate,
                time = state.elapsedTimeSeconds,
                score = finalScore.multipliedPoints,
                mistakes = state.mistakes
            )

            // Clear saved game state for this daily challenge
            gameRepository.deleteSavedGame()
        }

        delay(2000)
        _events.emit(
            GameEvent.GameWon(
                score = finalScore.multipliedPoints,
                time = state.elapsedTimeSeconds,
                difficulty = state.difficulty,
                isDaily = isDaily
            )
        )
    }

    private suspend fun handleGameLost() {
        timerJob?.cancel()
        val state = _gameState.value

        // Record failed game
        val hintsUsed = 3 - state.hintsRemaining

        // 1. Record in stats
        statsRepository.recordGameFailed(
            difficulty = state.difficulty,
            time = state.elapsedTimeSeconds,
            score = state.score,
            hintsUsed = hintsUsed
        )

        // 2. Record in game history
        gameHistoryRepository.recordGame(
            GameHistoryEntry(
                timestamp = System.currentTimeMillis(),
                difficulty = state.difficulty,
                timeSeconds = state.elapsedTimeSeconds,
                score = state.score,
                isCompleted = false,
                mistakes = state.mistakes,
                hintsUsed = hintsUsed,
                isDailyChallenge = isDaily
            )
        )

        // Note: Don't check achievements on loss

        // Clear saved daily challenge if applicable
        if (isDaily) {
            gameRepository.deleteSavedGame()
        }

        _events.emit(
            GameEvent.GameLost(
                score = state.score,
                difficulty = state.difficulty
            )
        )
    }

    /**
     * Resume a saved game.
     */
    fun resumeSavedGame(savedState: GameState) {
        _gameState.value = savedState
        updateNumberCounts()
        if (!savedState.isPaused) {
            startTimer()
        }
    }

    /**
     * Clear saved game and start fresh.
     */
    fun clearSavedGame() {
        viewModelScope.launch {
            gameRepository.deleteSavedGame()
        }
    }

    private fun boardToGrid(board: SudokuBoard): Array<IntArray> {
        return Array(GRID_SIZE) { row ->
            IntArray(GRID_SIZE) { col ->
                board.getCell(row, col).value ?: 0
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: Class<T>,
                extras: CreationExtras
            ): T {
                val savedStateHandle = extras.createSavedStateHandle()
                val gameRepository = RepositoryProvider.getGameRepository()
                val statsRepository = RepositoryProvider.getStatsRepository()
                val dailyChallengeStore = RepositoryProvider.getDailyChallengeStore()
                val achievementRepository = RepositoryProvider.getAchievementRepository()
                val gameHistoryRepository = RepositoryProvider.getGameHistoryRepository()

                return GameViewModel(
                    savedStateHandle = savedStateHandle,
                    gameRepository = gameRepository,
                    statsRepository = statsRepository,
                    dailyChallengeStore = dailyChallengeStore,
                    achievementRepository = achievementRepository,
                    gameHistoryRepository = gameHistoryRepository
                ) as T
            }
        }
    }
}

/**
 * Game events for UI to handle.
 */
sealed class GameEvent {
    data object Mistake : GameEvent()
    data class Completion(val count: Int) : GameEvent()
    data class GameWon(
        val score: Int,
        val time: Long,
        val difficulty: Difficulty,
        val isDaily: Boolean
    ) : GameEvent()

    data class GameLost(
        val score: Int,
        val difficulty: Difficulty
    ) : GameEvent()

    data class ResumeDailyChallenge(val savedState: GameState) : GameEvent()
}
