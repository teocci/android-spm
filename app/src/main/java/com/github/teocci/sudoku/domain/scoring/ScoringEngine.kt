package com.github.teocci.sudoku.domain.scoring

import com.github.teocci.sudoku.core.Constants
import com.github.teocci.sudoku.domain.model.Difficulty
import com.github.teocci.sudoku.domain.model.Position
import com.github.teocci.sudoku.domain.model.SudokuBoard
import com.github.teocci.sudoku.domain.validator.SudokuValidator
import kotlin.math.max

/**
 * Handles all scoring calculations for the Sudoku game.
 * Implements a point system based on difficulty, completions, time, and penalties.
 */
object ScoringEngine {

    /**
     * Result of a scoring calculation.
     */
    data class ScoreResult(
        val basePoints: Int,
        val multipliedPoints: Int,
        val breakdown: ScoreBreakdown
    ) {
        /**
         * Whether any row, column, or box was completed.
         */
        val hasCompletions: Boolean
            get() = breakdown.rowCompletion > 0 ||
                    breakdown.columnCompletion > 0 ||
                    breakdown.boxCompletion > 0

        /**
         * Total number of completions (rows + columns + boxes).
         */
        val totalCompletions: Int
            get() = listOf(
                breakdown.rowCompletion,
                breakdown.columnCompletion,
                breakdown.boxCompletion
            ).count { it > 0 }
    }

    /**
     * Detailed breakdown of score components.
     */
    data class ScoreBreakdown(
        val correctPlacement: Int = 0,
        val rowCompletion: Int = 0,
        val columnCompletion: Int = 0,
        val boxCompletion: Int = 0,
        val gameCompletion: Int = 0,
        val timeBonus: Int = 0,
        val hintPenalty: Int = 0,
        val mistakePenalty: Int = 0,
        val difficultyMultiplier: Float = 1.0f
    ) {
        /**
         * Total base points before multiplier.
         */
        val totalBase: Int
            get() = correctPlacement + rowCompletion + columnCompletion +
                    boxCompletion + gameCompletion + timeBonus +
                    hintPenalty + mistakePenalty

        /**
         * Total points after applying difficulty multiplier.
         */
        val totalMultiplied: Int
            get() = (totalBase * difficultyMultiplier).toInt()
    }

    /**
     * Cell complexity data for hint targeting.
     */
    data class CellComplexity(
        val position: Position,
        val candidateCount: Int,
        val relatedEmptyCells: Int,
        val complexityScore: Int
    )

    // ==================== POINT CALCULATIONS ====================

    /**
     * Calculate points for placing a correct number.
     *
     * @param difficulty The current difficulty level
     * @return Points earned
     */
    fun calculateCorrectPlacement(difficulty: Difficulty): Int {
        return difficulty.applyMultiplier(Constants.POINTS_CORRECT_NUMBER)
    }

    /**
     * Calculate penalty for placing an incorrect number.
     *
     * @param difficulty The current difficulty level
     * @return Points lost (negative value)
     */
    fun calculateMistakePenalty(difficulty: Difficulty): Int {
        // Mistakes don't get multiplied - same penalty regardless of difficulty
        return Constants.POINTS_MISTAKE_PENALTY
    }

    /**
     * Calculate penalty for using a hint.
     *
     * @param difficulty The current difficulty level
     * @return Points lost (negative value)
     */
    fun calculateHintPenalty(difficulty: Difficulty): Int {
        // Hints don't get multiplied - same penalty regardless of difficulty
        return Constants.POINTS_HINT_PENALTY
    }

    /**
     * Calculate bonus for completing a row.
     *
     * @param difficulty The current difficulty level
     * @return Points earned
     */
    fun calculateRowCompletion(difficulty: Difficulty): Int {
        return difficulty.applyMultiplier(Constants.POINTS_ROW_COMPLETION)
    }

    /**
     * Calculate bonus for completing a column.
     *
     * @param difficulty The current difficulty level
     * @return Points earned
     */
    fun calculateColumnCompletion(difficulty: Difficulty): Int {
        return difficulty.applyMultiplier(Constants.POINTS_COLUMN_COMPLETION)
    }

    /**
     * Calculate bonus for completing a 3x3 box.
     *
     * @param difficulty The current difficulty level
     * @return Points earned
     */
    fun calculateBoxCompletion(difficulty: Difficulty): Int {
        return difficulty.applyMultiplier(Constants.POINTS_BOX_COMPLETION)
    }

    /**
     * Calculate bonus for completing the entire game.
     *
     * @param difficulty The current difficulty level
     * @return Points earned
     */
    fun calculateGameCompletion(difficulty: Difficulty): Int {
        return difficulty.applyMultiplier(Constants.POINTS_GAME_COMPLETION)
    }

    /**
     * Calculate time bonus based on elapsed time.
     * Faster completion = more bonus points.
     *
     * @param elapsedSeconds Time taken to complete the game
     * @param difficulty The difficulty level
     * @return Time bonus points
     */
    fun calculateTimeBonus(elapsedSeconds: Long, difficulty: Difficulty): Int {
        // Base time targets (in seconds) for each difficulty
        val targetTime = when (difficulty) {
            Difficulty.EASY -> 300L      // 5 minutes
            Difficulty.MEDIUM -> 600L    // 10 minutes
            Difficulty.HARD -> 900L      // 15 minutes
            Difficulty.EXPERT -> 1200L   // 20 minutes
        }

        // No bonus if over target time
        if (elapsedSeconds >= targetTime) return 0

        // Calculate bonus as percentage of target time remaining
        val timeRemaining = targetTime - elapsedSeconds
        val bonusPercentage = timeRemaining.toFloat() / targetTime

        // Max bonus is 500 points, scaled by percentage and difficulty
        val maxBonus = Constants.POINTS_TIME_BONUS_MAX
        val baseBonus = (maxBonus * bonusPercentage).toInt()

        return difficulty.applyMultiplier(baseBonus)
    }

    // ==================== COMPREHENSIVE SCORING ====================

    /**
     * Calculate total score for placing a number, including any completions.
     *
     * @param board The current board state (after placement)
     * @param position Where the number was placed
     * @param isCorrect Whether the placement was correct
     * @param difficulty The current difficulty
     * @param previouslyCompletedRows Rows that were already completed
     * @param previouslyCompletedCols Columns that were already completed
     * @param previouslyCompletedBoxes Boxes that were already completed
     * @return ScoreResult with points and breakdown
     */
    fun calculatePlacementScore(
        board: SudokuBoard,
        position: Position,
        isCorrect: Boolean,
        difficulty: Difficulty,
        previouslyCompletedRows: Set<Int> = emptySet(),
        previouslyCompletedCols: Set<Int> = emptySet(),
        previouslyCompletedBoxes: Set<Int> = emptySet()
    ): ScoreResult {
        var breakdown = ScoreBreakdown(difficultyMultiplier = difficulty.scoreMultiplier)

        if (!isCorrect) {
            breakdown = breakdown.copy(mistakePenalty = Constants.POINTS_MISTAKE_PENALTY)
            return ScoreResult(
                basePoints = breakdown.totalBase,
                multipliedPoints = breakdown.mistakePenalty, // Penalties not multiplied
                breakdown = breakdown
            )
        }

        // Points for correct placement
        breakdown = breakdown.copy(correctPlacement = Constants.POINTS_CORRECT_NUMBER)

        // Check for row completion
        if (board.isRowComplete(position.row) && position.row !in previouslyCompletedRows) {
            breakdown = breakdown.copy(rowCompletion = Constants.POINTS_ROW_COMPLETION)
        }

        // Check for column completion
        if (board.isColumnComplete(position.col) && position.col !in previouslyCompletedCols) {
            breakdown = breakdown.copy(columnCompletion = Constants.POINTS_COLUMN_COMPLETION)
        }

        // Check for box completion
        if (board.isBoxComplete(position.boxIndex) && position.boxIndex !in previouslyCompletedBoxes) {
            breakdown = breakdown.copy(boxCompletion = Constants.POINTS_BOX_COMPLETION)
        }

        // Check for game completion
        if (board.isComplete()) {
            breakdown = breakdown.copy(gameCompletion = Constants.POINTS_GAME_COMPLETION)
        }

        return ScoreResult(
            basePoints = breakdown.totalBase,
            multipliedPoints = breakdown.totalMultiplied,
            breakdown = breakdown
        )
    }

    /**
     * Calculate final score for a completed game.
     *
     * @param baseScore Score accumulated during gameplay
     * @param elapsedSeconds Total time taken
     * @param hintsUsed Number of hints used
     * @param mistakes Number of mistakes made
     * @param difficulty The difficulty level
     * @return Final score with time bonus applied
     */
    fun calculateFinalScore(
        baseScore: Int,
        elapsedSeconds: Long,
        hintsUsed: Int,
        mistakes: Int,
        difficulty: Difficulty
    ): ScoreResult {
        val timeBonus = calculateTimeBonus(elapsedSeconds, difficulty)
        val hintPenalty = hintsUsed * Constants.POINTS_HINT_PENALTY
        val mistakePenalty = mistakes * Constants.POINTS_MISTAKE_PENALTY

        val breakdown = ScoreBreakdown(
            timeBonus = timeBonus,
            hintPenalty = hintPenalty,
            mistakePenalty = mistakePenalty,
            difficultyMultiplier = difficulty.scoreMultiplier
        )

        val finalScore = max(0, baseScore + timeBonus + hintPenalty + mistakePenalty)

        return ScoreResult(
            basePoints = baseScore + breakdown.totalBase,
            multipliedPoints = finalScore,
            breakdown = breakdown
        )
    }

    // ==================== HINT TARGETING ====================

    /**
     * Calculate complexity score for a cell.
     * Lower complexity = easier cell = better hint target.
     *
     * @param grid The current grid state
     * @param position The cell position
     * @return CellComplexity with score details
     */
    fun calculateCellComplexity(grid: Array<IntArray>, position: Position): CellComplexity {
        // Get valid candidates for this cell
        val candidates = SudokuValidator.getValidNumbers(grid, position.row, position.col)

        // Count empty cells in related positions
        var relatedEmpty = 0
        position.getRelatedPositions().forEach { relatedPos ->
            if (grid[relatedPos.row][relatedPos.col] == 0) {
                relatedEmpty++
            }
        }

        // Complexity score: fewer candidates and fewer related empty cells = easier
        // We want to hint at easier cells (lower score)
        val complexityScore = candidates.size * 10 + relatedEmpty

        return CellComplexity(
            position = position,
            candidateCount = candidates.size,
            relatedEmptyCells = relatedEmpty,
            complexityScore = complexityScore
        )
    }

    /**
     * Find the best cell to give a hint for.
     * Targets the easiest cell (lowest complexity score).
     *
     * @param grid The current grid state
     * @return Position of the best hint cell, or null if no empty cells
     */
    fun findBestHintCell(grid: Array<IntArray>): Position? {
        val emptyCells = mutableListOf<CellComplexity>()

        for (row in 0 until Constants.GRID_SIZE) {
            for (col in 0 until Constants.GRID_SIZE) {
                if (grid[row][col] == 0) {
                    emptyCells.add(calculateCellComplexity(grid, Position(row, col)))
                }
            }
        }

        if (emptyCells.isEmpty()) return null

        // Sort by complexity (ascending) and pick the easiest
        emptyCells.sortBy { it.complexityScore }

        // Among cells with same complexity, pick randomly for variety
        val lowestComplexity = emptyCells.first().complexityScore
        val easiestCells = emptyCells.filter { it.complexityScore == lowestComplexity }

        return easiestCells.random().position
    }

    /**
     * Calculate potential points for completing a cell.
     * Used for hint targeting (hint the lowest value cells).
     *
     * @param grid The current grid state
     * @param position The cell position
     * @param difficulty The current difficulty
     * @return Potential points if this cell is filled correctly
     */
    fun calculatePotentialPoints(
        grid: Array<IntArray>,
        position: Position,
        difficulty: Difficulty
    ): Int {
        var points = Constants.POINTS_CORRECT_NUMBER

        // Check if filling this would complete a row
        val rowValues = (0 until Constants.GRID_SIZE)
            .map { col -> grid[position.row][col] }
            .filter { it != 0 }
        if (rowValues.size == Constants.GRID_SIZE - 1) {
            points += Constants.POINTS_ROW_COMPLETION
        }

        // Check if filling this would complete a column
        val colValues = (0 until Constants.GRID_SIZE)
            .map { row -> grid[row][position.col] }
            .filter { it != 0 }
        if (colValues.size == Constants.GRID_SIZE - 1) {
            points += Constants.POINTS_COLUMN_COMPLETION
        }

        // Check if filling this would complete a box
        val boxStartRow = (position.row / Constants.BOX_SIZE) * Constants.BOX_SIZE
        val boxStartCol = (position.col / Constants.BOX_SIZE) * Constants.BOX_SIZE
        var boxFilled = 0
        for (r in boxStartRow until boxStartRow + Constants.BOX_SIZE) {
            for (c in boxStartCol until boxStartCol + Constants.BOX_SIZE) {
                if (grid[r][c] != 0) boxFilled++
            }
        }
        if (boxFilled == Constants.BOX_SIZE * Constants.BOX_SIZE - 1) {
            points += Constants.POINTS_BOX_COMPLETION
        }

        return difficulty.applyMultiplier(points)
    }

    // ==================== STATISTICS ====================

    /**
     * Calculate accuracy percentage.
     *
     * @param correctPlacements Number of correct placements
     * @param totalPlacements Total number of placements (including mistakes)
     * @return Accuracy as percentage (0-100)
     */
    fun calculateAccuracy(correctPlacements: Int, totalPlacements: Int): Float {
        if (totalPlacements == 0) return 100f
        return (correctPlacements.toFloat() / totalPlacements) * 100
    }

    /**
     * Calculate average time per cell.
     *
     * @param elapsedSeconds Total time taken
     * @param cellsFilled Number of cells filled by player
     * @return Average seconds per cell
     */
    fun calculateAverageTimePerCell(elapsedSeconds: Long, cellsFilled: Int): Float {
        if (cellsFilled == 0) return 0f
        return elapsedSeconds.toFloat() / cellsFilled
    }

    /**
     * Get a performance rating based on score.
     *
     * @param score The final score
     * @param difficulty The difficulty level
     * @return Rating from 1 to 5 stars
     */
    fun calculateStarRating(score: Int, difficulty: Difficulty): Int {
        // Thresholds vary by difficulty
        val thresholds = when (difficulty) {
            Difficulty.EASY -> listOf(500, 750, 1000, 1250)
            Difficulty.MEDIUM -> listOf(750, 1125, 1500, 1875)
            Difficulty.HARD -> listOf(1000, 1500, 2000, 2500)
            Difficulty.EXPERT -> listOf(1500, 2250, 3000, 3750)
        }

        return when {
            score >= thresholds[3] -> 5
            score >= thresholds[2] -> 4
            score >= thresholds[1] -> 3
            score >= thresholds[0] -> 2
            else -> 1
        }
    }

    /**
     * Format score for display with thousands separator.
     *
     * @param score The score to format
     * @return Formatted score string
     */
    fun formatScore(score: Int): String {
        return String.format("%,d", score)
    }
}
