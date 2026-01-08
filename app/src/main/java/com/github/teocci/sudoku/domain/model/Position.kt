package com.github.teocci.sudoku.domain.model

import com.github.teocci.sudoku.core.Constants.BOX_SIZE
import com.github.teocci.sudoku.core.Constants.GRID_SIZE

/**
 * Represents a position on the Sudoku grid.
 *
 * @property row The row index (0-8)
 * @property col The column index (0-8)
 */
data class Position(
    val row: Int,
    val col: Int
) {
    init {
        require(row in 0 until GRID_SIZE) { "Row must be between 0 and ${GRID_SIZE - 1}" }
        require(col in 0 until GRID_SIZE) { "Column must be between 0 and ${GRID_SIZE - 1}" }
    }

    /**
     * Get the box index (0-8) for this position.
     * Boxes are numbered left-to-right, top-to-bottom.
     */
    val boxIndex: Int
        get() = (row / BOX_SIZE) * BOX_SIZE + (col / BOX_SIZE)

    /**
     * Get the top-left position of the box containing this position.
     */
    val boxStart: Position
        get() = Position(
            row = (row / BOX_SIZE) * BOX_SIZE,
            col = (col / BOX_SIZE) * BOX_SIZE
        )

    /**
     * Convert this position to a 1D index (0-80).
     */
    fun toIndex(): Int = row * GRID_SIZE + col

    /**
     * Check if this position is in the same row as another position.
     */
    fun isSameRow(other: Position): Boolean = row == other.row

    /**
     * Check if this position is in the same column as another position.
     */
    fun isSameColumn(other: Position): Boolean = col == other.col

    /**
     * Check if this position is in the same box as another position.
     */
    fun isSameBox(other: Position): Boolean = boxIndex == other.boxIndex

    /**
     * Check if this position is related to another position (same row, column, or box).
     */
    fun isRelatedTo(other: Position): Boolean {
        if (this == other) return false
        return isSameRow(other) || isSameColumn(other) || isSameBox(other)
    }

    /**
     * Get all positions in the same row.
     */
    fun getRowPositions(): List<Position> {
        return (0 until GRID_SIZE).map { c -> Position(row, c) }
    }

    /**
     * Get all positions in the same column.
     */
    fun getColumnPositions(): List<Position> {
        return (0 until GRID_SIZE).map { r -> Position(r, col) }
    }

    /**
     * Get all positions in the same box.
     */
    fun getBoxPositions(): List<Position> {
        val start = boxStart
        val positions = mutableListOf<Position>()
        for (r in start.row until start.row + BOX_SIZE) {
            for (c in start.col until start.col + BOX_SIZE) {
                positions.add(Position(r, c))
            }
        }
        return positions
    }

    /**
     * Get all related positions (same row, column, and box), excluding this position.
     */
    fun getRelatedPositions(): Set<Position> {
        val positions = mutableSetOf<Position>()
        positions.addAll(getRowPositions())
        positions.addAll(getColumnPositions())
        positions.addAll(getBoxPositions())
        positions.remove(this)
        return positions
    }

    override fun toString(): String = "($row, $col)"

    companion object {
        /**
         * Create a Position from a 1D index (0-80).
         */
        fun fromIndex(index: Int): Position {
            require(index in 0 until GRID_SIZE * GRID_SIZE) {
                "Index must be between 0 and ${GRID_SIZE * GRID_SIZE - 1}"
            }
            return Position(
                row = index / GRID_SIZE,
                col = index % GRID_SIZE
            )
        }

        /**
         * Get all positions on the grid.
         */
        fun allPositions(): List<Position> {
            return (0 until GRID_SIZE * GRID_SIZE).map { fromIndex(it) }
        }
    }
}
