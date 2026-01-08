package com.github.teocci.sudoku.domain.model

import com.github.teocci.sudoku.core.Constants.MAX_NUMBER
import com.github.teocci.sudoku.core.Constants.MIN_NUMBER

/**
 * Represents a single cell in the Sudoku grid.
 *
 * @property value The current value of the cell (null if empty)
 * @property solution The correct solution value for this cell
 * @property notes Set of candidate numbers noted by the player (1-9)
 * @property isFixed Whether this cell was part of the initial puzzle (cannot be modified)
 * @property isError Whether this cell contains an incorrect value
 * @property isRevealed Whether this cell was revealed by a hint
 */
data class Cell(
    val value: Int? = null,
    val solution: Int,
    val notes: Set<Int> = emptySet(),
    val isFixed: Boolean = false,
    val isError: Boolean = false,
    val isRevealed: Boolean = false
) {
    init {
        require(solution in MIN_NUMBER..MAX_NUMBER) {
            "Solution must be between $MIN_NUMBER and $MAX_NUMBER"
        }
        value?.let {
            require(it in MIN_NUMBER..MAX_NUMBER) {
                "Value must be between $MIN_NUMBER and $MAX_NUMBER"
            }
        }
        notes.forEach { note ->
            require(note in MIN_NUMBER..MAX_NUMBER) {
                "Note must be between $MIN_NUMBER and $MAX_NUMBER"
            }
        }
    }

    /**
     * Whether this cell is empty (no value set).
     */
    val isEmpty: Boolean
        get() = value == null

    /**
     * Whether this cell has the correct value.
     */
    val isCorrect: Boolean
        get() = value == solution

    /**
     * Whether this cell can be modified by the player.
     */
    val isEditable: Boolean
        get() = !isFixed

    /**
     * Whether this cell has any notes.
     */
    val hasNotes: Boolean
        get() = notes.isNotEmpty()

    /**
     * Set a value in this cell.
     * Clears notes when a value is set.
     *
     * @param newValue The value to set (1-9)
     * @param checkError Whether to mark as error if incorrect
     * @return A new Cell with the updated value
     */
    fun setValue(newValue: Int, checkError: Boolean = true): Cell {
        require(newValue in MIN_NUMBER..MAX_NUMBER) {
            "Value must be between $MIN_NUMBER and $MAX_NUMBER"
        }
        return copy(
            value = newValue,
            notes = emptySet(),
            isError = if (checkError) newValue != solution else false
        )
    }

    /**
     * Clear the value from this cell.
     *
     * @return A new Cell with no value
     */
    fun clearValue(): Cell {
        return copy(
            value = null,
            isError = false
        )
    }

    /**
     * Toggle a note in this cell.
     * Only works if the cell is empty.
     *
     * @param note The note to toggle (1-9)
     * @return A new Cell with the updated notes
     */
    fun toggleNote(note: Int): Cell {
        require(note in MIN_NUMBER..MAX_NUMBER) {
            "Note must be between $MIN_NUMBER and $MAX_NUMBER"
        }
        if (value != null) return this

        val newNotes = if (notes.contains(note)) {
            notes - note
        } else {
            notes + note
        }
        return copy(notes = newNotes)
    }

    /**
     * Add a note to this cell.
     * Only works if the cell is empty.
     *
     * @param note The note to add (1-9)
     * @return A new Cell with the added note
     */
    fun addNote(note: Int): Cell {
        require(note in MIN_NUMBER..MAX_NUMBER) {
            "Note must be between $MIN_NUMBER and $MAX_NUMBER"
        }
        if (value != null) return this
        return copy(notes = notes + note)
    }

    /**
     * Remove a note from this cell.
     *
     * @param note The note to remove (1-9)
     * @return A new Cell with the note removed
     */
    fun removeNote(note: Int): Cell {
        return copy(notes = notes - note)
    }

    /**
     * Clear all notes from this cell.
     *
     * @return A new Cell with no notes
     */
    fun clearNotes(): Cell {
        return copy(notes = emptySet())
    }

    /**
     * Clear both value and notes from this cell.
     *
     * @return A new Cell with no value and no notes
     */
    fun clear(): Cell {
        return copy(
            value = null,
            notes = emptySet(),
            isError = false
        )
    }

    /**
     * Mark this cell as revealed by a hint.
     *
     * @return A new Cell with the correct value and revealed flag
     */
    fun reveal(): Cell {
        return copy(
            value = solution,
            notes = emptySet(),
            isError = false,
            isRevealed = true
        )
    }

    /**
     * Clear the error state of this cell.
     *
     * @return A new Cell with isError = false
     */
    fun clearError(): Cell {
        return copy(isError = false)
    }

    companion object {
        /**
         * Create an empty cell with a solution.
         */
        fun empty(solution: Int): Cell {
            return Cell(solution = solution)
        }

        /**
         * Create a fixed cell (part of the initial puzzle).
         */
        fun fixed(value: Int): Cell {
            return Cell(
                value = value,
                solution = value,
                isFixed = true
            )
        }
    }
}
