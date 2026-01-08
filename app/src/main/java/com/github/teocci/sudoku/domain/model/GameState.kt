package com.github.teocci.sudoku.domain.model

import com.github.teocci.sudoku.core.Constants
import java.time.LocalDate

/**
 * Represents the complete state of a Sudoku game.
 *
 * @property board The current state of the Sudoku board
 * @property selectedCell The currently selected cell position (null if none)
 * @property activeNumber The currently active number for input (null if none)
 * @property isNumberLocked Whether the active number is locked (long-press mode)
 * @property notesMode Whether notes mode is enabled
 * @property mistakes Current number of mistakes made
 * @property maxMistakes Maximum allowed mistakes before game over
 * @property score Current score
 * @property elapsedTimeSeconds Elapsed game time in seconds
 * @property difficulty The difficulty level of the puzzle
 * @property gameStatus Current game status
 * @property hintsRemaining Number of hints remaining
 * @property actionHistory History of actions for undo (numbers only)
 * @property completedRows Set of row indices that have been completed
 * @property completedColumns Set of column indices that have been completed
 * @property completedBoxes Set of box indices that have been completed
 * @property gameDate The date of the game (for daily challenges)
 * @property isDailyChallenge Whether this is a daily challenge game
 */
data class GameState(
    val board: SudokuBoard,
    val selectedCell: Position? = null,
    val activeNumber: Int? = null,
    val isNumberLocked: Boolean = false,
    val notesMode: Boolean = false,
    val mistakes: Int = 0,
    val maxMistakes: Int = Constants.MAX_MISTAKES,
    val score: Int = 0,
    val elapsedTimeSeconds: Long = 0,
    val difficulty: Difficulty = Difficulty.MEDIUM,
    val gameStatus: GameStatus = GameStatus.NOT_STARTED,
    val hintsRemaining: Int = Constants.MAX_HINTS,
    val actionHistory: List<GameAction> = emptyList(),
    val completedRows: Set<Int> = emptySet(),
    val completedColumns: Set<Int> = emptySet(),
    val completedBoxes: Set<Int> = emptySet(),
    val gameDate: LocalDate = LocalDate.now(),
    val isDailyChallenge: Boolean = false
) {
    /**
     * Whether the game is currently active and accepting input.
     */
    val isActive: Boolean
        get() = gameStatus.isActive

    /**
     * Whether the game has ended (completed or failed).
     */
    val isGameOver: Boolean
        get() = gameStatus.isTerminal

    /**
     * Whether the player has won.
     */
    val isWon: Boolean
        get() = gameStatus == GameStatus.COMPLETED

    /**
     * Whether the player has lost (max mistakes reached).
     */
    val isLost: Boolean
        get() = gameStatus == GameStatus.FAILED

    /**
     * Whether the game is paused.
     */
    val isPaused: Boolean
        get() = gameStatus == GameStatus.PAUSED

    /**
     * Whether undo is available (has actions in history).
     */
    val canUndo: Boolean
        get() = actionHistory.isNotEmpty() && isActive

    /**
     * Whether hints are available.
     */
    val canUseHint: Boolean
        get() = hintsRemaining > 0 && isActive && board.getEmptyPositions().isNotEmpty()

    /**
     * Whether the selected cell can be edited.
     */
    val canEditSelectedCell: Boolean
        get() {
            val cell = selectedCell?.let { board.getCell(it) }
            return cell != null && cell.isEditable && isActive
        }

    /**
     * Get the currently selected cell.
     */
    val selectedCellData: Cell?
        get() = selectedCell?.let { board.getCell(it) }

    /**
     * Remaining mistakes before game over.
     */
    val mistakesRemaining: Int
        get() = maxMistakes - mistakes

    /**
     * Progress percentage (0.0 to 1.0).
     */
    val progress: Float
        get() {
            val total = Constants.TOTAL_CELLS
            val filled = board.getFilledCellCount()
            return filled.toFloat() / total
        }

    /**
     * Select a cell on the board.
     */
    fun selectCell(position: Position): GameState {
        if (!isActive) return this
        return copy(selectedCell = position)
    }

    /**
     * Clear the cell selection.
     */
    fun clearSelection(): GameState {
        return copy(selectedCell = null)
    }

    /**
     * Set the active number for input.
     */
    fun setActiveNumber(number: Int?, locked: Boolean = false): GameState {
        return copy(activeNumber = number, isNumberLocked = locked)
    }

    /**
     * Toggle notes mode.
     */
    fun toggleNotesMode(): GameState {
        return copy(notesMode = !notesMode)
    }

    /**
     * Start the game.
     */
    fun start(): GameState {
        return copy(gameStatus = GameStatus.IN_PROGRESS)
    }

    /**
     * Pause the game.
     */
    fun pause(): GameState {
        if (!gameStatus.canPause) return this
        return copy(gameStatus = GameStatus.PAUSED)
    }

    /**
     * Resume the game.
     */
    fun resume(): GameState {
        if (!gameStatus.canResume) return this
        return copy(gameStatus = GameStatus.IN_PROGRESS)
    }

    /**
     * Increment the elapsed time by one second.
     */
    fun tick(): GameState {
        if (!gameStatus.shouldTimerRun) return this
        return copy(elapsedTimeSeconds = elapsedTimeSeconds + 1)
    }

    /**
     * Add score points (applies difficulty multiplier).
     */
    fun addScore(basePoints: Int): GameState {
        val points = difficulty.applyMultiplier(basePoints)
        return copy(score = (score + points).coerceAtLeast(0))
    }

    /**
     * Record a mistake.
     */
    fun recordMistake(): GameState {
        val newMistakes = mistakes + 1
        val newStatus = if (newMistakes >= maxMistakes) GameStatus.FAILED else gameStatus
        return copy(
            mistakes = newMistakes,
            gameStatus = newStatus
        )
    }

    /**
     * Use a hint.
     */
    fun useHint(): GameState {
        if (!canUseHint) return this
        return copy(hintsRemaining = hintsRemaining - 1)
    }

    /**
     * Mark the game as completed.
     */
    fun complete(): GameState {
        return copy(gameStatus = GameStatus.COMPLETED)
    }

    /**
     * Add an action to the history (for undo).
     */
    fun addToHistory(action: GameAction): GameState {
        return copy(actionHistory = actionHistory + action)
    }

    /**
     * Remove the last action from history (after undo).
     */
    fun removeLastFromHistory(): GameState {
        if (actionHistory.isEmpty()) return this
        return copy(actionHistory = actionHistory.dropLast(1))
    }

    /**
     * Mark a row as completed.
     */
    fun markRowCompleted(row: Int): GameState {
        return copy(completedRows = completedRows + row)
    }

    /**
     * Mark a column as completed.
     */
    fun markColumnCompleted(col: Int): GameState {
        return copy(completedColumns = completedColumns + col)
    }

    /**
     * Mark a box as completed.
     */
    fun markBoxCompleted(boxIndex: Int): GameState {
        return copy(completedBoxes = completedBoxes + boxIndex)
    }

    /**
     * Update the board.
     */
    fun updateBoard(newBoard: SudokuBoard): GameState {
        return copy(board = newBoard)
    }

    companion object {
        /**
         * Create an initial game state with the given board and difficulty.
         */
        fun create(
            board: SudokuBoard,
            difficulty: Difficulty,
            isDailyChallenge: Boolean = false,
            gameDate: LocalDate = LocalDate.now()
        ): GameState {
            return GameState(
                board = board,
                difficulty = difficulty,
                isDailyChallenge = isDailyChallenge,
                gameDate = gameDate,
                gameStatus = GameStatus.IN_PROGRESS
            )
        }

        /**
         * Create an empty/placeholder game state.
         */
        fun empty(): GameState {
            return GameState(
                board = SudokuBoard.empty(),
                gameStatus = GameStatus.NOT_STARTED
            )
        }
    }
}
