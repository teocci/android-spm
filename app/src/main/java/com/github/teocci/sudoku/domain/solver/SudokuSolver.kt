package com.github.teocci.sudoku.domain.solver

import com.github.teocci.sudoku.core.Constants.GRID_SIZE
import com.github.teocci.sudoku.core.Constants.MAX_NUMBER
import com.github.teocci.sudoku.core.Constants.MIN_NUMBER
import com.github.teocci.sudoku.domain.model.Position
import com.github.teocci.sudoku.domain.validator.SudokuValidator

/**
 * Sudoku solver using backtracking algorithm with optimizations.
 * Uses Minimum Remaining Values (MRV) heuristic for efficiency.
 */
object SudokuSolver {

    /**
     * Result of solving a Sudoku puzzle.
     */
    sealed class SolveResult {
        /**
         * Puzzle was solved successfully.
         * @property solution The complete solution grid
         */
        data class Solved(val solution: Array<IntArray>) : SolveResult() {
            override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (other !is Solved) return false
                return solution.contentDeepEquals(other.solution)
            }

            override fun hashCode(): Int = solution.contentDeepHashCode()
        }

        /**
         * Puzzle has no solution.
         */
        data object NoSolution : SolveResult()

        /**
         * Puzzle has multiple solutions (not a valid Sudoku).
         */
        data object MultipleSolutions : SolveResult()
    }

    /**
     * Solve a Sudoku puzzle.
     *
     * @param grid 2D array with initial values (0 for empty cells)
     * @return SolveResult indicating success or failure
     */
    fun solve(grid: Array<IntArray>): SolveResult {
        // Make a copy to avoid modifying the original
        val workingGrid = grid.map { it.copyOf() }.toTypedArray()

        return if (solveRecursive(workingGrid)) {
            SolveResult.Solved(workingGrid)
        } else {
            SolveResult.NoSolution
        }
    }

    /**
     * Check if a puzzle has exactly one solution.
     *
     * @param grid 2D array with initial values (0 for empty cells)
     * @return true if the puzzle has exactly one unique solution
     */
    fun hasUniqueSolution(grid: Array<IntArray>): Boolean {
        val workingGrid = grid.map { it.copyOf() }.toTypedArray()
        val solutionCount = countSolutions(workingGrid, maxCount = 2)
        return solutionCount == 1
    }

    /**
     * Count the number of solutions for a puzzle (up to maxCount).
     *
     * @param grid 2D array with initial values
     * @param maxCount Stop counting after finding this many solutions
     * @return Number of solutions found (up to maxCount)
     */
    fun countSolutions(grid: Array<IntArray>, maxCount: Int = 2): Int {
        val workingGrid = grid.map { it.copyOf() }.toTypedArray()
        var count = 0

        fun countRecursive(): Boolean {
            // Find the best empty cell using MRV heuristic
            val cellInfo = SudokuValidator.findBestEmptyCell(workingGrid)

            if (cellInfo == null) {
                // No empty cells - found a solution
                count++
                return count >= maxCount
            }

            val (position, candidates) = cellInfo

            if (candidates.isEmpty()) {
                // No valid candidates - dead end
                return false
            }

            for (num in candidates) {
                workingGrid[position.row][position.col] = num

                if (countRecursive()) {
                    // Stop if we've found enough solutions
                    workingGrid[position.row][position.col] = 0
                    return true
                }

                workingGrid[position.row][position.col] = 0
            }

            return false
        }

        countRecursive()
        return count
    }

    /**
     * Solve the puzzle using backtracking with MRV heuristic.
     *
     * @param grid The grid to solve (modified in place)
     * @return true if solved successfully
     */
    private fun solveRecursive(grid: Array<IntArray>): Boolean {
        // Find the best empty cell using MRV heuristic
        val cellInfo = SudokuValidator.findBestEmptyCell(grid)

        if (cellInfo == null) {
            // No empty cells - puzzle is solved
            return true
        }

        val (position, candidates) = cellInfo

        if (candidates.isEmpty()) {
            // No valid candidates - dead end
            return false
        }

        for (num in candidates) {
            grid[position.row][position.col] = num

            if (solveRecursive(grid)) {
                return true
            }

            // Backtrack
            grid[position.row][position.col] = 0
        }

        return false
    }

    /**
     * Solve using simple sequential cell selection (for comparison/testing).
     *
     * @param grid The grid to solve (modified in place)
     * @return true if solved successfully
     */
    fun solveSimple(grid: Array<IntArray>): Boolean {
        val emptyCell = SudokuValidator.findEmptyCell(grid) ?: return true

        for (num in MIN_NUMBER..MAX_NUMBER) {
            if (SudokuValidator.isValidPlacement(grid, emptyCell.row, emptyCell.col, num)) {
                grid[emptyCell.row][emptyCell.col] = num

                if (solveSimple(grid)) {
                    return true
                }

                grid[emptyCell.row][emptyCell.col] = 0
            }
        }

        return false
    }

    /**
     * Generate a complete, valid Sudoku solution.
     *
     * @param seed Optional seed for reproducible generation
     * @return A complete 9x9 grid with all cells filled
     */
    fun generateSolution(seed: Long? = null): Array<IntArray> {
        val grid = Array(GRID_SIZE) { IntArray(GRID_SIZE) }
        val random = if (seed != null) java.util.Random(seed) else java.util.Random()

        fun fillRecursive(): Boolean {
            val cellInfo = SudokuValidator.findBestEmptyCell(grid)

            if (cellInfo == null) {
                return true
            }

            val (position, candidates) = cellInfo

            if (candidates.isEmpty()) {
                return false
            }

            // Shuffle candidates for randomness
            val shuffledCandidates = candidates.shuffled(random)

            for (num in shuffledCandidates) {
                grid[position.row][position.col] = num

                if (fillRecursive()) {
                    return true
                }

                grid[position.row][position.col] = 0
            }

            return false
        }

        // Fill diagonal boxes first (they don't affect each other)
        fillDiagonalBoxes(grid, random)

        // Fill the rest
        fillRecursive()

        return grid
    }

    /**
     * Fill the three diagonal 3x3 boxes (0, 4, 8) with random valid values.
     * These boxes don't affect each other, so we can fill them independently.
     */
    private fun fillDiagonalBoxes(grid: Array<IntArray>, random: java.util.Random) {
        for (box in listOf(0, 4, 8)) {
            val boxStartRow = (box / 3) * 3
            val boxStartCol = (box % 3) * 3
            val numbers = (MIN_NUMBER..MAX_NUMBER).shuffled(random).toMutableList()

            var index = 0
            for (r in boxStartRow until boxStartRow + 3) {
                for (c in boxStartCol until boxStartCol + 3) {
                    grid[r][c] = numbers[index++]
                }
            }
        }
    }

    /**
     * Get a hint for the current position.
     * Returns the correct value that should be placed.
     *
     * @param grid Current grid state (0 for empty)
     * @param solution The complete solution
     * @param row Row index
     * @param col Column index
     * @return The correct value, or null if cell is already filled
     */
    fun getHint(grid: Array<IntArray>, solution: Array<IntArray>, row: Int, col: Int): Int? {
        if (grid[row][col] != 0) return null
        return solution[row][col]
    }

    /**
     * Find the best cell to give a hint for.
     * Selects the cell with the most constraints (hardest to figure out).
     *
     * @param grid Current grid state
     * @return Position of the best cell for a hint, or null if grid is full
     */
    fun findHintCell(grid: Array<IntArray>): Position? {
        var bestPosition: Position? = null
        var minCandidates = Int.MAX_VALUE

        for (row in 0 until GRID_SIZE) {
            for (col in 0 until GRID_SIZE) {
                if (grid[row][col] == 0) {
                    val candidates = SudokuValidator.getValidNumbers(grid, row, col)
                    // Prefer cells with fewer candidates (easier for player to verify)
                    if (candidates.size < minCandidates) {
                        minCandidates = candidates.size
                        bestPosition = Position(row, col)
                    }
                }
            }
        }

        return bestPosition
    }

    /**
     * Copy a grid.
     */
    fun copyGrid(grid: Array<IntArray>): Array<IntArray> {
        return grid.map { it.copyOf() }.toTypedArray()
    }

    /**
     * Create an empty grid.
     */
    fun emptyGrid(): Array<IntArray> {
        return Array(GRID_SIZE) { IntArray(GRID_SIZE) }
    }

    /**
     * Print a grid to string (for debugging).
     */
    fun gridToString(grid: Array<IntArray>): String {
        val sb = StringBuilder()
        for (row in 0 until GRID_SIZE) {
            if (row > 0 && row % 3 == 0) {
                sb.appendLine("------+-------+------")
            }
            for (col in 0 until GRID_SIZE) {
                if (col > 0 && col % 3 == 0) {
                    sb.append(" | ")
                } else if (col > 0) {
                    sb.append(" ")
                }
                val value = grid[row][col]
                sb.append(if (value == 0) "." else value.toString())
            }
            sb.appendLine()
        }
        return sb.toString()
    }
}
