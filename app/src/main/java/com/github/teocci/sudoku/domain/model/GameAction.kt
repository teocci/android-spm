package com.github.teocci.sudoku.domain.model

/**
 * Sealed class representing all possible game actions.
 * Used for the Command pattern to enable undo functionality.
 *
 * Note: Only PlaceNumber actions are stored in history for undo (per user decision).
 */
sealed class GameAction {

    /**
     * Place a number in a cell.
     * This action is stored in history and can be undone.
     *
     * @property position The position where the number was placed
     * @property number The number that was placed (1-9)
     * @property previousValue The value that was in the cell before (null if empty)
     * @property wasCorrect Whether the placement was correct
     */
    data class PlaceNumber(
        val position: Position,
        val number: Int,
        val previousValue: Int?,
        val wasCorrect: Boolean = true
    ) : GameAction() {
        /**
         * Whether this action can be undone.
         */
        val isUndoable: Boolean = true
    }

    /**
     * Toggle a note in a cell.
     * Note changes are NOT undoable (per user decision).
     *
     * @property position The position where the note was toggled
     * @property note The note number that was toggled (1-9)
     * @property wasAdded Whether the note was added (true) or removed (false)
     */
    data class ToggleNote(
        val position: Position,
        val note: Int,
        val wasAdded: Boolean
    ) : GameAction()

    /**
     * Erase a cell (clear value and notes).
     * This action is stored in history and can be undone.
     *
     * @property position The position that was erased
     * @property previousValue The value that was in the cell before (null if empty)
     * @property previousNotes The notes that were in the cell before
     */
    data class EraseCell(
        val position: Position,
        val previousValue: Int?,
        val previousNotes: Set<Int>
    ) : GameAction() {
        /**
         * Whether this action can be undone (only if there was a value).
         */
        val isUndoable: Boolean = previousValue != null
    }

    /**
     * Use a hint to reveal a cell.
     *
     * @property position The position that was revealed
     * @property revealedNumber The number that was revealed
     */
    data class UseHint(
        val position: Position,
        val revealedNumber: Int
    ) : GameAction()

    /**
     * Select a cell on the board.
     *
     * @property position The position that was selected (null to clear selection)
     */
    data class SelectCell(
        val position: Position?
    ) : GameAction()

    /**
     * Set the active number for input.
     *
     * @property number The number to set as active (null to clear)
     * @property isLocked Whether the number is locked (long-press mode)
     */
    data class SetActiveNumber(
        val number: Int?,
        val isLocked: Boolean = false
    ) : GameAction()

    /**
     * Toggle notes mode on/off.
     */
    data object ToggleNotesMode : GameAction()

    /**
     * Pause the game.
     */
    data object Pause : GameAction()

    /**
     * Resume the game.
     */
    data object Resume : GameAction()

    /**
     * Undo the last undoable action.
     */
    data object Undo : GameAction()

    /**
     * Start a new game.
     *
     * @property difficulty The difficulty level for the new game
     */
    data class NewGame(
        val difficulty: Difficulty
    ) : GameAction()

    /**
     * Restart the current game.
     */
    data object RestartGame : GameAction()

    /**
     * Timer tick (increment elapsed time by 1 second).
     */
    data object TimerTick : GameAction()

    companion object {
        /**
         * Check if an action should be stored in history for undo.
         */
        fun isHistoryAction(action: GameAction): Boolean {
            return when (action) {
                is PlaceNumber -> action.isUndoable
                is EraseCell -> action.isUndoable
                else -> false
            }
        }

        /**
         * Get a description of the action for debugging/logging.
         */
        fun getDescription(action: GameAction): String {
            return when (action) {
                is PlaceNumber -> "Place ${action.number} at ${action.position}"
                is ToggleNote -> "Toggle note ${action.note} at ${action.position}"
                is EraseCell -> "Erase cell at ${action.position}"
                is UseHint -> "Use hint at ${action.position}"
                is SelectCell -> "Select cell ${action.position ?: "none"}"
                is SetActiveNumber -> "Set active number ${action.number ?: "none"}"
                is ToggleNotesMode -> "Toggle notes mode"
                is Pause -> "Pause game"
                is Resume -> "Resume game"
                is Undo -> "Undo last action"
                is NewGame -> "New game (${action.difficulty.displayName})"
                is RestartGame -> "Restart game"
                is TimerTick -> "Timer tick"
            }
        }
    }
}

/**
 * Represents the result of applying an action to the game state.
 *
 * @property newState The new game state after the action
 * @property completedRows Rows that were completed by this action
 * @property completedColumns Columns that were completed by this action
 * @property completedBoxes Boxes that were completed by this action
 * @property pointsEarned Points earned/lost from this action
 * @property wasError Whether the action resulted in an error (wrong number)
 */
data class ActionResult(
    val newState: GameState,
    val completedRows: Set<Int> = emptySet(),
    val completedColumns: Set<Int> = emptySet(),
    val completedBoxes: Set<Int> = emptySet(),
    val pointsEarned: Int = 0,
    val wasError: Boolean = false
) {
    /**
     * Whether any row, column, or box was completed.
     */
    val hasCompletions: Boolean
        get() = completedRows.isNotEmpty() ||
                completedColumns.isNotEmpty() ||
                completedBoxes.isNotEmpty()

    /**
     * Total number of completions (rows + columns + boxes).
     */
    val totalCompletions: Int
        get() = completedRows.size + completedColumns.size + completedBoxes.size

    /**
     * Whether the game was completed by this action.
     */
    val isGameComplete: Boolean
        get() = newState.gameStatus == GameStatus.COMPLETED

    /**
     * Whether the game was lost by this action.
     */
    val isGameOver: Boolean
        get() = newState.gameStatus == GameStatus.FAILED
}
