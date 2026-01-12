package com.github.teocci.sudoku.domain.model

import com.github.teocci.sudoku.core.Constants.BOX_SIZE
import com.github.teocci.sudoku.core.Constants.GRID_SIZE

/**
 * Represents a 9x9 Sudoku board containing cells.
 *
 * @property cells 2D array of cells [row][col]
 */
data class SudokuBoard(
    private val cells: List<List<Cell>>
) {
    init {
        require(cells.size == GRID_SIZE) { "Board must have $GRID_SIZE rows" }
        cells.forEach { row ->
            require(row.size == GRID_SIZE) { "Each row must have $GRID_SIZE columns" }
        }
    }

    /**
     * Get a cell at the specified position.
     */
    fun getCell(position: Position): Cell = cells[position.row][position.col]

    /**
     * Get a cell at the specified row and column.
     */
    fun getCell(row: Int, col: Int): Cell = cells[row][col]

    /**
     * Update a cell at the specified position.
     *
     * @param position The position to update
     * @param cell The new cell value
     * @return A new SudokuBoard with the updated cell
     */
    fun updateCell(position: Position, cell: Cell): SudokuBoard {
        val newCells = cells.mapIndexed { rowIndex, row ->
            if (rowIndex == position.row) {
                row.mapIndexed { colIndex, existingCell ->
                    if (colIndex == position.col) cell else existingCell
                }
            } else {
                row
            }
        }
        return SudokuBoard(newCells)
    }

    /**
     * Update a cell at the specified row and column.
     */
    fun updateCell(row: Int, col: Int, cell: Cell): SudokuBoard {
        return updateCell(Position(row, col), cell)
    }

    /**
     * Set a value in a cell at the specified position.
     *
     * @param position The position to set
     * @param value The value to set (1-9)
     * @param checkError Whether to mark as error if incorrect
     * @return A new SudokuBoard with the value set
     */
    fun setValue(position: Position, value: Int, checkError: Boolean = true): SudokuBoard {
        val cell = getCell(position)
        if (cell.isFixed) return this
        return updateCell(position, cell.setValue(value, checkError))
    }

    /**
     * Clear a cell at the specified position.
     *
     * @param position The position to clear
     * @return A new SudokuBoard with the cell cleared
     */
    fun clearCell(position: Position): SudokuBoard {
        val cell = getCell(position)
        if (cell.isFixed) return this
        return updateCell(position, cell.clear())
    }

    /**
     * Toggle a note in a cell at the specified position.
     *
     * @param position The position to update
     * @param note The note to toggle (1-9)
     * @return A new SudokuBoard with the note toggled
     */
    fun toggleNote(position: Position, note: Int): SudokuBoard {
        val cell = getCell(position)
        if (cell.isFixed || cell.value != null) return this
        return updateCell(position, cell.toggleNote(note))
    }

    /**
     * Remove a note from all cells in a row, column, and box.
     * Called when a number is placed to auto-remove related notes.
     *
     * @param position The position where a number was placed
     * @param note The note to remove
     * @return A new SudokuBoard with the notes removed
     */
    fun removeNoteFromRelated(position: Position, note: Int): SudokuBoard {
        var board = this
        position.getRelatedPositions().forEach { relatedPos ->
            val cell = board.getCell(relatedPos)
            if (cell.notes.contains(note)) {
                board = board.updateCell(relatedPos, cell.removeNote(note))
            }
        }
        return board
    }

    /**
     * Reveal a cell using a hint.
     *
     * @param position The position to reveal
     * @return A new SudokuBoard with the cell revealed
     */
    fun revealCell(position: Position): SudokuBoard {
        val cell = getCell(position)
        if (cell.isFixed || cell.value != null) return this
        return updateCell(position, cell.reveal())
    }

    /**
     * Get all cells in a row.
     */
    fun getRow(row: Int): List<Cell> = cells[row]

    /**
     * Get all cells in a column.
     */
    fun getColumn(col: Int): List<Cell> = cells.map { it[col] }

    /**
     * Get all cells in a box.
     *
     * @param boxIndex The box index (0-8)
     */
    fun getBox(boxIndex: Int): List<Cell> {
        val startRow = (boxIndex / BOX_SIZE) * BOX_SIZE
        val startCol = (boxIndex % BOX_SIZE) * BOX_SIZE
        val boxCells = mutableListOf<Cell>()
        for (r in startRow until startRow + BOX_SIZE) {
            for (c in startCol until startCol + BOX_SIZE) {
                boxCells.add(cells[r][c])
            }
        }
        return boxCells
    }

    /**
     * Get the box containing a specific position.
     */
    fun getBoxAt(position: Position): List<Cell> = getBox(position.boxIndex)

    /**
     * Check if a row is complete (all cells filled correctly).
     */
    fun isRowComplete(row: Int): Boolean {
        return getRow(row).all { it.value != null && it.isCorrect }
    }

    /**
     * Check if a column is complete (all cells filled correctly).
     */
    fun isColumnComplete(col: Int): Boolean {
        return getColumn(col).all { it.value != null && it.isCorrect }
    }

    /**
     * Check if a box is complete (all cells filled correctly).
     */
    fun isBoxComplete(boxIndex: Int): Boolean {
        return getBox(boxIndex).all { it.value != null && it.isCorrect }
    }

    /**
     * Check if the entire board is complete (all cells filled correctly).
     */
    fun isComplete(): Boolean {
        return cells.flatten().all { it.value != null && it.isCorrect }
    }

    /**
     * Get all empty positions on the board.
     */
    fun getEmptyPositions(): List<Position> {
        val positions = mutableListOf<Position>()
        for (row in 0 until GRID_SIZE) {
            for (col in 0 until GRID_SIZE) {
                if (cells[row][col].isEmpty) {
                    positions.add(Position(row, col))
                }
            }
        }
        return positions
    }

    /**
     * Get all positions with a specific value.
     */
    fun getPositionsWithValue(value: Int): List<Position> {
        val positions = mutableListOf<Position>()
        for (row in 0 until GRID_SIZE) {
            for (col in 0 until GRID_SIZE) {
                if (cells[row][col].value == value) {
                    positions.add(Position(row, col))
                }
            }
        }
        return positions
    }

    /**
     * Count how many times a value appears on the board.
     */
    fun countValue(value: Int): Int {
        return cells.flatten().count { it.value == value }
    }

    /**
     * Check if a value is complete (appears 9 times on the board).
     */
    fun isValueComplete(value: Int): Boolean {
        return countValue(value) == GRID_SIZE
    }

    /**
     * Get the number of empty cells remaining.
     */
    fun getEmptyCellCount(): Int {
        return cells.flatten().count { it.isEmpty }
    }

    /**
     * Get the number of filled cells.
     */
    fun getFilledCellCount(): Int {
        return cells.flatten().count { !it.isEmpty }
    }

    /**
     * Get the number of errors on the board.
     */
    fun getErrorCount(): Int {
        return cells.flatten().count { it.isError }
    }

    /**
     * Iterate over all positions and cells.
     */
    fun forEachCell(action: (Position, Cell) -> Unit) {
        for (row in 0 until GRID_SIZE) {
            for (col in 0 until GRID_SIZE) {
                action(Position(row, col), cells[row][col])
            }
        }
    }

    /**
     * Get all cells as a flat list with their positions.
     */
    fun getAllCellsWithPositions(): List<Pair<Position, Cell>> {
        val result = mutableListOf<Pair<Position, Cell>>()
        forEachCell { position, cell ->
            result.add(Pair(position, cell))
        }
        return result
    }

    companion object {
        /**
         * Create an empty board (all cells empty with solution 0 - used for initialization).
         * Note: This creates an invalid board and should only be used temporarily.
         */
        fun empty(): SudokuBoard {
            val cells = List(GRID_SIZE) { row ->
                List(GRID_SIZE) { col ->
                    Cell(solution = 1) // Placeholder, will be replaced
                }
            }
            return SudokuBoard(cells)
        }

        /**
         * Create a board from a 2D array of solution values.
         * All cells start as fixed (pre-filled).
         *
         * @param solution 2D array of values (1-9)
         */
        fun fromSolution(solution: Array<IntArray>): SudokuBoard {
            val cells = List(GRID_SIZE) { row ->
                List(GRID_SIZE) { col ->
                    Cell.fixed(solution[row][col])
                }
            }
            return SudokuBoard(cells)
        }

        /**
         * Create a board from a solution with some cells hidden.
         *
         * @param solution 2D array of correct values (1-9)
         * @param puzzle 2D array where 0 indicates empty cells
         */
        fun fromPuzzle(solution: Array<IntArray>, puzzle: Array<IntArray>): SudokuBoard {
            val cells = List(GRID_SIZE) { row ->
                List(GRID_SIZE) { col ->
                    if (puzzle[row][col] != 0) {
                        Cell.fixed(puzzle[row][col])
                    } else {
                        Cell.empty(solution[row][col])
                    }
                }
            }
            return SudokuBoard(cells)
        }
    }
}
