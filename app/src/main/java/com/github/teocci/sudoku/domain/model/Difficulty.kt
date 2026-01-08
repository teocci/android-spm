package com.github.teocci.sudoku.domain.model

import com.github.teocci.sudoku.core.Constants

/**
 * Represents the difficulty level of a Sudoku puzzle.
 *
 * @property displayName Human-readable name for the difficulty
 * @property cellsToRemove Number of cells to remove from a solved board to create the puzzle
 * @property scoreMultiplier Multiplier applied to points earned at this difficulty
 */
enum class Difficulty(
    val displayName: String,
    val cellsToRemove: Int,
    val scoreMultiplier: Float
) {
    EASY(
        displayName = "Easy",
        cellsToRemove = Constants.DIFFICULTY_EASY_CELLS_TO_REMOVE,
        scoreMultiplier = 1.0f
    ),
    MEDIUM(
        displayName = "Medium",
        cellsToRemove = Constants.DIFFICULTY_MEDIUM_CELLS_TO_REMOVE,
        scoreMultiplier = 1.5f
    ),
    HARD(
        displayName = "Hard",
        cellsToRemove = Constants.DIFFICULTY_HARD_CELLS_TO_REMOVE,
        scoreMultiplier = 2.0f
    ),
    EXPERT(
        displayName = "Expert",
        cellsToRemove = Constants.DIFFICULTY_EXPERT_CELLS_TO_REMOVE,
        scoreMultiplier = 3.0f
    );

    /**
     * Number of cells that will be pre-filled (given) in the puzzle.
     */
    val givenCells: Int
        get() = Constants.TOTAL_CELLS - cellsToRemove

    /**
     * Apply the score multiplier to a base score.
     *
     * @param baseScore The base score to multiply
     * @return The score after applying the difficulty multiplier
     */
    fun applyMultiplier(baseScore: Int): Int {
        return (baseScore * scoreMultiplier).toInt()
    }

    /**
     * Get the next higher difficulty level.
     *
     * @return The next difficulty level, or null if this is EXPERT
     */
    fun nextDifficulty(): Difficulty? {
        return when (this) {
            EASY -> MEDIUM
            MEDIUM -> HARD
            HARD -> EXPERT
            EXPERT -> null
        }
    }

    /**
     * Get the previous lower difficulty level.
     *
     * @return The previous difficulty level, or null if this is EASY
     */
    fun previousDifficulty(): Difficulty? {
        return when (this) {
            EASY -> null
            MEDIUM -> EASY
            HARD -> MEDIUM
            EXPERT -> HARD
        }
    }

    companion object {
        /**
         * Get a difficulty by its display name (case-insensitive).
         *
         * @param name The display name to search for
         * @return The matching Difficulty, or null if not found
         */
        fun fromDisplayName(name: String): Difficulty? {
            return entries.find { it.displayName.equals(name, ignoreCase = true) }
        }

        /**
         * Get all difficulties in order from easiest to hardest.
         */
        fun allDifficulties(): List<Difficulty> = entries

        /**
         * Get the default difficulty for new games.
         */
        fun default(): Difficulty = MEDIUM
    }
}
