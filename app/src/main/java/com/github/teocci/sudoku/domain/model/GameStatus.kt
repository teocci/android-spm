package com.github.teocci.sudoku.domain.model

/**
 * Represents the current status of a Sudoku game.
 */
enum class GameStatus {
    /**
     * Game has not been started yet.
     * Initial state before puzzle is loaded.
     */
    NOT_STARTED,

    /**
     * Game is currently in progress.
     * Player is actively solving the puzzle.
     */
    IN_PROGRESS,

    /**
     * Game is paused.
     * Timer is stopped, board may be hidden.
     */
    PAUSED,

    /**
     * Game has been completed successfully.
     * All cells are filled correctly.
     */
    COMPLETED,

    /**
     * Game has failed.
     * Maximum mistakes reached (3/3).
     */
    FAILED;

    /**
     * Whether the game is currently active (can accept input).
     */
    val isActive: Boolean
        get() = this == IN_PROGRESS

    /**
     * Whether the game is in a terminal state (completed or failed).
     */
    val isTerminal: Boolean
        get() = this == COMPLETED || this == FAILED

    /**
     * Whether the game can be resumed.
     */
    val canResume: Boolean
        get() = this == PAUSED

    /**
     * Whether the game can be paused.
     */
    val canPause: Boolean
        get() = this == IN_PROGRESS

    /**
     * Whether the timer should be running.
     */
    val shouldTimerRun: Boolean
        get() = this == IN_PROGRESS

    /**
     * Whether the board should be visible/interactive.
     */
    val isBoardVisible: Boolean
        get() = this != PAUSED && this != NOT_STARTED

    /**
     * Get a human-readable display name for this status.
     */
    val displayName: String
        get() = when (this) {
            NOT_STARTED -> "Not Started"
            IN_PROGRESS -> "In Progress"
            PAUSED -> "Paused"
            COMPLETED -> "Completed"
            FAILED -> "Game Over"
        }

    companion object {
        /**
         * Get the initial status for a new game.
         */
        fun initial(): GameStatus = NOT_STARTED

        /**
         * Get all active (non-terminal) statuses.
         */
        fun activeStatuses(): List<GameStatus> = listOf(IN_PROGRESS, PAUSED)

        /**
         * Get all terminal statuses.
         */
        fun terminalStatuses(): List<GameStatus> = listOf(COMPLETED, FAILED)
    }
}
