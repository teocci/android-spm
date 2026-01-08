package com.github.teocci.sudoku.domain.generator

import com.github.teocci.sudoku.core.Constants.GRID_SIZE
import com.github.teocci.sudoku.core.Constants.TOTAL_CELLS
import com.github.teocci.sudoku.domain.model.Difficulty
import com.github.teocci.sudoku.domain.model.Position
import com.github.teocci.sudoku.domain.model.SudokuBoard
import com.github.teocci.sudoku.domain.solver.SudokuSolver
import java.time.LocalDate
import java.util.Random

/**
 * Generates Sudoku puzzles with varying difficulty levels.
 * Ensures each generated puzzle has exactly one unique solution.
 */
object SudokuGenerator {

    /**
     * Result of puzzle generation.
     */
    data class GeneratedPuzzle(
        val solution: Array<IntArray>,
        val puzzle: Array<IntArray>,
        val difficulty: Difficulty,
        val emptyCells: Int
    ) {
        /**
         * Create a SudokuBoard from this puzzle.
         */
        fun toBoard(): SudokuBoard {
            return SudokuBoard.fromPuzzle(solution, puzzle)
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is GeneratedPuzzle) return false
            return solution.contentDeepEquals(other.solution) &&
                    puzzle.contentDeepEquals(other.puzzle) &&
                    difficulty == other.difficulty &&
                    emptyCells == other.emptyCells
        }

        override fun hashCode(): Int {
            var result = solution.contentDeepHashCode()
            result = 31 * result + puzzle.contentDeepHashCode()
            result = 31 * result + difficulty.hashCode()
            result = 31 * result + emptyCells
            return result
        }
    }

    /**
     * Generate a new puzzle with the specified difficulty.
     *
     * @param difficulty The desired difficulty level
     * @param seed Optional seed for reproducible generation
     * @return A GeneratedPuzzle containing both solution and puzzle
     */
    fun generate(difficulty: Difficulty, seed: Long? = null): GeneratedPuzzle {
        val random = if (seed != null) Random(seed) else Random()

        // Generate a complete solution
        val solution = SudokuSolver.generateSolution(seed)

        // Create puzzle by removing cells
        val puzzle = createPuzzle(solution, difficulty, random)

        // Count empty cells
        val emptyCells = puzzle.sumOf { row -> row.count { it == 0 } }

        return GeneratedPuzzle(
            solution = solution,
            puzzle = puzzle,
            difficulty = difficulty,
            emptyCells = emptyCells
        )
    }

    /**
     * Generate a daily challenge puzzle for a specific date.
     * The same date always produces the same puzzle.
     *
     * @param date The date for the daily challenge
     * @param difficulty The difficulty level
     * @return A GeneratedPuzzle for the daily challenge
     */
    fun generateDailyChallenge(
        date: LocalDate = LocalDate.now(),
        difficulty: Difficulty = Difficulty.MEDIUM
    ): GeneratedPuzzle {
        // Create a deterministic seed from the date
        val seed = date.toEpochDay() * 31 + difficulty.ordinal
        return generate(difficulty, seed)
    }

    /**
     * Create a puzzle from a solution by removing cells.
     * Ensures the resulting puzzle has exactly one solution.
     *
     * @param solution The complete solution grid
     * @param difficulty The desired difficulty
     * @param random Random instance for cell selection
     * @return The puzzle grid with some cells set to 0
     */
    private fun createPuzzle(
        solution: Array<IntArray>,
        difficulty: Difficulty,
        random: Random
    ): Array<IntArray> {
        val puzzle = solution.map { it.copyOf() }.toTypedArray()
        val cellsToRemove = difficulty.cellsToRemove

        // Get all positions and shuffle them
        val positions = mutableListOf<Position>()
        for (row in 0 until GRID_SIZE) {
            for (col in 0 until GRID_SIZE) {
                positions.add(Position(row, col))
            }
        }
        positions.shuffle(random)

        var removed = 0
        var attempts = 0
        val maxAttempts = TOTAL_CELLS * 2 // Prevent infinite loops

        for (position in positions) {
            if (removed >= cellsToRemove || attempts >= maxAttempts) break

            val row = position.row
            val col = position.col

            if (puzzle[row][col] == 0) continue

            // Try removing this cell
            val backup = puzzle[row][col]
            puzzle[row][col] = 0
            attempts++

            // Check if puzzle still has unique solution
            if (SudokuSolver.hasUniqueSolution(puzzle)) {
                removed++
            } else {
                // Restore the cell - removing it would create multiple solutions
                puzzle[row][col] = backup
            }
        }

        // If we couldn't remove enough cells, try a different strategy
        if (removed < cellsToRemove - 5) {
            return createPuzzleSymmetric(solution, difficulty, random)
        }

        return puzzle
    }

    /**
     * Create a puzzle using symmetric removal pattern.
     * Often produces more aesthetically pleasing puzzles.
     *
     * @param solution The complete solution grid
     * @param difficulty The desired difficulty
     * @param random Random instance for cell selection
     * @return The puzzle grid with some cells set to 0
     */
    private fun createPuzzleSymmetric(
        solution: Array<IntArray>,
        difficulty: Difficulty,
        random: Random
    ): Array<IntArray> {
        val puzzle = solution.map { it.copyOf() }.toTypedArray()
        val cellsToRemove = difficulty.cellsToRemove

        // Generate pairs of symmetric positions
        val pairs = mutableListOf<Pair<Position, Position>>()
        for (row in 0 until GRID_SIZE) {
            for (col in 0 until GRID_SIZE) {
                val symRow = GRID_SIZE - 1 - row
                val symCol = GRID_SIZE - 1 - col
                if (row < symRow || (row == symRow && col < symCol)) {
                    pairs.add(Pair(Position(row, col), Position(symRow, symCol)))
                } else if (row == symRow && col == symCol) {
                    // Center cell (for odd-sized grids)
                    pairs.add(Pair(Position(row, col), Position(row, col)))
                }
            }
        }
        pairs.shuffle(random)

        var removed = 0
        var attempts = 0
        val maxAttempts = pairs.size * 2

        for ((pos1, pos2) in pairs) {
            if (removed >= cellsToRemove || attempts >= maxAttempts) break

            val isSameCell = pos1 == pos2
            val cellsInPair = if (isSameCell) 1 else 2

            if (puzzle[pos1.row][pos1.col] == 0) continue

            // Try removing this pair
            val backup1 = puzzle[pos1.row][pos1.col]
            val backup2 = if (!isSameCell) puzzle[pos2.row][pos2.col] else 0

            puzzle[pos1.row][pos1.col] = 0
            if (!isSameCell) {
                puzzle[pos2.row][pos2.col] = 0
            }
            attempts++

            // Check if puzzle still has unique solution
            if (SudokuSolver.hasUniqueSolution(puzzle)) {
                removed += cellsInPair
            } else {
                // Restore the cells
                puzzle[pos1.row][pos1.col] = backup1
                if (!isSameCell) {
                    puzzle[pos2.row][pos2.col] = backup2
                }
            }
        }

        return puzzle
    }

    /**
     * Estimate the difficulty of an existing puzzle.
     * Based on number of empty cells and solving complexity.
     *
     * @param puzzle The puzzle grid (0 for empty cells)
     * @return Estimated difficulty level
     */
    fun estimateDifficulty(puzzle: Array<IntArray>): Difficulty {
        val emptyCells = puzzle.sumOf { row -> row.count { it == 0 } }

        return when {
            emptyCells <= 35 -> Difficulty.EASY
            emptyCells <= 45 -> Difficulty.MEDIUM
            emptyCells <= 52 -> Difficulty.HARD
            else -> Difficulty.EXPERT
        }
    }

    /**
     * Validate that a puzzle is solvable and has a unique solution.
     *
     * @param puzzle The puzzle grid to validate
     * @return true if the puzzle is valid and has exactly one solution
     */
    fun isValidPuzzle(puzzle: Array<IntArray>): Boolean {
        return SudokuSolver.hasUniqueSolution(puzzle)
    }

    /**
     * Get the solution for a puzzle.
     *
     * @param puzzle The puzzle grid
     * @return The solution, or null if unsolvable
     */
    fun getSolution(puzzle: Array<IntArray>): Array<IntArray>? {
        return when (val result = SudokuSolver.solve(puzzle)) {
            is SudokuSolver.SolveResult.Solved -> result.solution
            else -> null
        }
    }

    /**
     * Generate multiple puzzles of the same difficulty.
     * Useful for pre-generating puzzles for offline play.
     *
     * @param difficulty The difficulty level
     * @param count Number of puzzles to generate
     * @return List of generated puzzles
     */
    fun generateBatch(difficulty: Difficulty, count: Int): List<GeneratedPuzzle> {
        return (0 until count).map { generate(difficulty) }
    }

    /**
     * Generate puzzles for all difficulty levels.
     *
     * @param countPerDifficulty Number of puzzles per difficulty
     * @return Map of difficulty to list of puzzles
     */
    fun generateAllDifficulties(countPerDifficulty: Int = 1): Map<Difficulty, List<GeneratedPuzzle>> {
        return Difficulty.entries.associateWith { difficulty ->
            generateBatch(difficulty, countPerDifficulty)
        }
    }

    /**
     * Get the number of clues (filled cells) in a puzzle.
     *
     * @param puzzle The puzzle grid
     * @return Number of non-zero cells
     */
    fun getClueCount(puzzle: Array<IntArray>): Int {
        return puzzle.sumOf { row -> row.count { it != 0 } }
    }

    /**
     * Get positions of all clues (pre-filled cells) in a puzzle.
     *
     * @param puzzle The puzzle grid
     * @return Set of positions with clues
     */
    fun getCluePositions(puzzle: Array<IntArray>): Set<Position> {
        val positions = mutableSetOf<Position>()
        for (row in 0 until GRID_SIZE) {
            for (col in 0 until GRID_SIZE) {
                if (puzzle[row][col] != 0) {
                    positions.add(Position(row, col))
                }
            }
        }
        return positions
    }

    /**
     * Get positions of all empty cells in a puzzle.
     *
     * @param puzzle The puzzle grid
     * @return Set of empty positions
     */
    fun getEmptyPositions(puzzle: Array<IntArray>): Set<Position> {
        val positions = mutableSetOf<Position>()
        for (row in 0 until GRID_SIZE) {
            for (col in 0 until GRID_SIZE) {
                if (puzzle[row][col] == 0) {
                    positions.add(Position(row, col))
                }
            }
        }
        return positions
    }

    /**
     * Create a puzzle string representation (for debugging/sharing).
     *
     * @param puzzle The puzzle grid
     * @return String representation with dots for empty cells
     */
    fun puzzleToString(puzzle: Array<IntArray>): String {
        return puzzle.joinToString("\n") { row ->
            row.joinToString(" ") { if (it == 0) "." else it.toString() }
        }
    }

    /**
     * Create a compact puzzle string (81 characters, 0 for empty).
     *
     * @param puzzle The puzzle grid
     * @return Compact string representation
     */
    fun puzzleToCompactString(puzzle: Array<IntArray>): String {
        return puzzle.joinToString("") { row ->
            row.joinToString("") { it.toString() }
        }
    }

    /**
     * Parse a compact puzzle string back to a grid.
     *
     * @param compact 81-character string
     * @return Puzzle grid, or null if invalid
     */
    fun parsePuzzle(compact: String): Array<IntArray>? {
        if (compact.length != TOTAL_CELLS) return null

        return try {
            Array(GRID_SIZE) { row ->
                IntArray(GRID_SIZE) { col ->
                    compact[row * GRID_SIZE + col].digitToInt()
                }
            }
        } catch (e: Exception) {
            null
        }
    }
}
