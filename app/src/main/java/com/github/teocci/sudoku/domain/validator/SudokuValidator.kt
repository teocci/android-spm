package com.github.teocci.sudoku.domain.validator

import com.github.teocci.sudoku.core.Constants.BOX_SIZE
import com.github.teocci.sudoku.core.Constants.GRID_SIZE
import com.github.teocci.sudoku.core.Constants.MAX_NUMBER
import com.github.teocci.sudoku.core.Constants.MIN_NUMBER
import com.github.teocci.sudoku.domain.model.Position

/**
 * Validator for Sudoku rules.
 * Checks that no number repeats in rows, columns, or 3x3 boxes.
 */
object SudokuValidator {

    /**
     * Check if a value is valid at the given position in a grid.
     * A value is valid if it doesn't already exist in the same row, column, or box.
     *
     * @param grid 2D array of values (0 for empty)
     * @param row Row index
     * @param col Column index
     * @param value The value to check (1-9)
     * @return true if the value is valid at this position
     */
    fun isValidPlacement(grid: Array<IntArray>, row: Int, col: Int, value: Int): Boolean {
        // Check if value is in valid range
        if (value !in MIN_NUMBER..MAX_NUMBER) return false

        // Check row
        if (isValueInRow(grid, row, value, excludeCol = col)) return false

        // Check column
        if (isValueInColumn(grid, col, value, excludeRow = row)) return false

        // Check box
        if (isValueInBox(grid, row, col, value, excludePosition = Position(row, col))) return false

        return true
    }

    /**
     * Check if a value exists in a row.
     *
     * @param grid 2D array of values
     * @param row Row index to check
     * @param value Value to look for
     * @param excludeCol Optional column to exclude from check
     * @return true if value exists in the row
     */
    fun isValueInRow(grid: Array<IntArray>, row: Int, value: Int, excludeCol: Int = -1): Boolean {
        for (col in 0 until GRID_SIZE) {
            if (col != excludeCol && grid[row][col] == value) {
                return true
            }
        }
        return false
    }

    /**
     * Check if a value exists in a column.
     *
     * @param grid 2D array of values
     * @param col Column index to check
     * @param value Value to look for
     * @param excludeRow Optional row to exclude from check
     * @return true if value exists in the column
     */
    fun isValueInColumn(grid: Array<IntArray>, col: Int, value: Int, excludeRow: Int = -1): Boolean {
        for (row in 0 until GRID_SIZE) {
            if (row != excludeRow && grid[row][col] == value) {
                return true
            }
        }
        return false
    }

    /**
     * Check if a value exists in a 3x3 box.
     *
     * @param grid 2D array of values
     * @param row Row index (any cell in the box)
     * @param col Column index (any cell in the box)
     * @param value Value to look for
     * @param excludePosition Optional position to exclude from check
     * @return true if value exists in the box
     */
    fun isValueInBox(
        grid: Array<IntArray>,
        row: Int,
        col: Int,
        value: Int,
        excludePosition: Position? = null
    ): Boolean {
        val boxStartRow = (row / BOX_SIZE) * BOX_SIZE
        val boxStartCol = (col / BOX_SIZE) * BOX_SIZE

        for (r in boxStartRow until boxStartRow + BOX_SIZE) {
            for (c in boxStartCol until boxStartCol + BOX_SIZE) {
                if (excludePosition != null && r == excludePosition.row && c == excludePosition.col) {
                    continue
                }
                if (grid[r][c] == value) {
                    return true
                }
            }
        }
        return false
    }

    /**
     * Check if a row is complete (all cells filled with valid values).
     *
     * @param grid 2D array of values
     * @param row Row index to check
     * @return true if the row is complete and valid
     */
    fun isRowComplete(grid: Array<IntArray>, row: Int): Boolean {
        val seen = BooleanArray(GRID_SIZE + 1)
        for (col in 0 until GRID_SIZE) {
            val value = grid[row][col]
            if (value == 0 || seen[value]) return false
            seen[value] = true
        }
        return true
    }

    /**
     * Check if a column is complete (all cells filled with valid values).
     *
     * @param grid 2D array of values
     * @param col Column index to check
     * @return true if the column is complete and valid
     */
    fun isColumnComplete(grid: Array<IntArray>, col: Int): Boolean {
        val seen = BooleanArray(GRID_SIZE + 1)
        for (row in 0 until GRID_SIZE) {
            val value = grid[row][col]
            if (value == 0 || seen[value]) return false
            seen[value] = true
        }
        return true
    }

    /**
     * Check if a box is complete (all cells filled with valid values).
     *
     * @param grid 2D array of values
     * @param boxIndex Box index (0-8)
     * @return true if the box is complete and valid
     */
    fun isBoxComplete(grid: Array<IntArray>, boxIndex: Int): Boolean {
        val boxStartRow = (boxIndex / BOX_SIZE) * BOX_SIZE
        val boxStartCol = (boxIndex % BOX_SIZE) * BOX_SIZE
        val seen = BooleanArray(GRID_SIZE + 1)

        for (r in boxStartRow until boxStartRow + BOX_SIZE) {
            for (c in boxStartCol until boxStartCol + BOX_SIZE) {
                val value = grid[r][c]
                if (value == 0 || seen[value]) return false
                seen[value] = true
            }
        }
        return true
    }

    /**
     * Check if the entire grid is complete and valid.
     *
     * @param grid 2D array of values
     * @return true if the grid is a valid, complete Sudoku solution
     */
    fun isGridComplete(grid: Array<IntArray>): Boolean {
        // Check all rows
        for (row in 0 until GRID_SIZE) {
            if (!isRowComplete(grid, row)) return false
        }

        // Check all columns
        for (col in 0 until GRID_SIZE) {
            if (!isColumnComplete(grid, col)) return false
        }

        // Check all boxes
        for (box in 0 until GRID_SIZE) {
            if (!isBoxComplete(grid, box)) return false
        }

        return true
    }

    /**
     * Check if a row is valid (no duplicates, ignoring empty cells).
     *
     * @param grid 2D array of values
     * @param row Row index to check
     * @return true if the row has no duplicate values
     */
    fun isRowValid(grid: Array<IntArray>, row: Int): Boolean {
        val seen = BooleanArray(GRID_SIZE + 1)
        for (col in 0 until GRID_SIZE) {
            val value = grid[row][col]
            if (value != 0) {
                if (seen[value]) return false
                seen[value] = true
            }
        }
        return true
    }

    /**
     * Check if a column is valid (no duplicates, ignoring empty cells).
     *
     * @param grid 2D array of values
     * @param col Column index to check
     * @return true if the column has no duplicate values
     */
    fun isColumnValid(grid: Array<IntArray>, col: Int): Boolean {
        val seen = BooleanArray(GRID_SIZE + 1)
        for (row in 0 until GRID_SIZE) {
            val value = grid[row][col]
            if (value != 0) {
                if (seen[value]) return false
                seen[value] = true
            }
        }
        return true
    }

    /**
     * Check if a box is valid (no duplicates, ignoring empty cells).
     *
     * @param grid 2D array of values
     * @param boxIndex Box index (0-8)
     * @return true if the box has no duplicate values
     */
    fun isBoxValid(grid: Array<IntArray>, boxIndex: Int): Boolean {
        val boxStartRow = (boxIndex / BOX_SIZE) * BOX_SIZE
        val boxStartCol = (boxIndex % BOX_SIZE) * BOX_SIZE
        val seen = BooleanArray(GRID_SIZE + 1)

        for (r in boxStartRow until boxStartRow + BOX_SIZE) {
            for (c in boxStartCol until boxStartCol + BOX_SIZE) {
                val value = grid[r][c]
                if (value != 0) {
                    if (seen[value]) return false
                    seen[value] = true
                }
            }
        }
        return true
    }

    /**
     * Check if the entire grid is valid (no duplicates in any row, column, or box).
     *
     * @param grid 2D array of values
     * @return true if the grid is valid (may not be complete)
     */
    fun isGridValid(grid: Array<IntArray>): Boolean {
        for (i in 0 until GRID_SIZE) {
            if (!isRowValid(grid, i)) return false
            if (!isColumnValid(grid, i)) return false
            if (!isBoxValid(grid, i)) return false
        }
        return true
    }

    /**
     * Get all valid numbers that can be placed at a position.
     *
     * @param grid 2D array of values
     * @param row Row index
     * @param col Column index
     * @return Set of valid numbers (1-9) that can be placed
     */
    fun getValidNumbers(grid: Array<IntArray>, row: Int, col: Int): Set<Int> {
        if (grid[row][col] != 0) return emptySet()

        val valid = mutableSetOf<Int>()
        for (num in MIN_NUMBER..MAX_NUMBER) {
            if (isValidPlacement(grid, row, col, num)) {
                valid.add(num)
            }
        }
        return valid
    }

    /**
     * Count the number of empty cells in a grid.
     *
     * @param grid 2D array of values
     * @return Number of cells with value 0
     */
    fun countEmptyCells(grid: Array<IntArray>): Int {
        var count = 0
        for (row in 0 until GRID_SIZE) {
            for (col in 0 until GRID_SIZE) {
                if (grid[row][col] == 0) count++
            }
        }
        return count
    }

    /**
     * Find the first empty cell in the grid.
     *
     * @param grid 2D array of values
     * @return Position of first empty cell, or null if grid is full
     */
    fun findEmptyCell(grid: Array<IntArray>): Position? {
        for (row in 0 until GRID_SIZE) {
            for (col in 0 until GRID_SIZE) {
                if (grid[row][col] == 0) {
                    return Position(row, col)
                }
            }
        }
        return null
    }

    /**
     * Find the empty cell with fewest valid candidates (MRV heuristic).
     * This is useful for optimizing the solver.
     *
     * @param grid 2D array of values
     * @return Pair of Position and valid candidates, or null if grid is full
     */
    fun findBestEmptyCell(grid: Array<IntArray>): Pair<Position, Set<Int>>? {
        var bestPosition: Position? = null
        var bestCandidates: Set<Int> = emptySet()
        var minCandidates = Int.MAX_VALUE

        for (row in 0 until GRID_SIZE) {
            for (col in 0 until GRID_SIZE) {
                if (grid[row][col] == 0) {
                    val candidates = getValidNumbers(grid, row, col)
                    if (candidates.isEmpty()) {
                        // No valid candidates - puzzle is unsolvable from this state
                        return Pair(Position(row, col), emptySet())
                    }
                    if (candidates.size < minCandidates) {
                        minCandidates = candidates.size
                        bestPosition = Position(row, col)
                        bestCandidates = candidates
                    }
                }
            }
        }

        return bestPosition?.let { Pair(it, bestCandidates) }
    }
}
