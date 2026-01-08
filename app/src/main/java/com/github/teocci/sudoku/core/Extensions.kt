package com.github.teocci.sudoku.core

import com.github.teocci.sudoku.core.Constants.BOX_SIZE
import com.github.teocci.sudoku.core.Constants.GRID_SIZE
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Get the box index (0-8) for a given row and column.
 */
fun getBoxIndex(row: Int, col: Int): Int {
    return (row / BOX_SIZE) * BOX_SIZE + (col / BOX_SIZE)
}

/**
 * Get the top-left position of the box containing the given row and column.
 */
fun getBoxStartPosition(row: Int, col: Int): Pair<Int, Int> {
    val startRow = (row / BOX_SIZE) * BOX_SIZE
    val startCol = (col / BOX_SIZE) * BOX_SIZE
    return Pair(startRow, startCol)
}

/**
 * Check if two positions are in the same row, column, or box.
 */
fun arePositionsRelated(row1: Int, col1: Int, row2: Int, col2: Int): Boolean {
    if (row1 == row2 && col1 == col2) return false
    return row1 == row2 || col1 == col2 || getBoxIndex(row1, col1) == getBoxIndex(row2, col2)
}

/**
 * Get all positions in the same row as the given position.
 */
fun getRowPositions(row: Int): List<Pair<Int, Int>> {
    return (0 until GRID_SIZE).map { col -> Pair(row, col) }
}

/**
 * Get all positions in the same column as the given position.
 */
fun getColumnPositions(col: Int): List<Pair<Int, Int>> {
    return (0 until GRID_SIZE).map { row -> Pair(row, col) }
}

/**
 * Get all positions in the same box as the given position.
 */
fun getBoxPositions(row: Int, col: Int): List<Pair<Int, Int>> {
    val (startRow, startCol) = getBoxStartPosition(row, col)
    val positions = mutableListOf<Pair<Int, Int>>()
    for (r in startRow until startRow + BOX_SIZE) {
        for (c in startCol until startCol + BOX_SIZE) {
            positions.add(Pair(r, c))
        }
    }
    return positions
}

/**
 * Get all related positions (same row, column, and box) for a given position.
 */
fun getRelatedPositions(row: Int, col: Int): Set<Pair<Int, Int>> {
    val positions = mutableSetOf<Pair<Int, Int>>()
    positions.addAll(getRowPositions(row))
    positions.addAll(getColumnPositions(col))
    positions.addAll(getBoxPositions(row, col))
    positions.remove(Pair(row, col))
    return positions
}

/**
 * Format elapsed time in seconds to MM:SS or HH:MM:SS format.
 */
fun formatTime(seconds: Long): String {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val secs = seconds % 60
    return if (hours > 0) {
        String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, secs)
    } else {
        String.format(Locale.US, "%02d:%02d", minutes, secs)
    }
}

/**
 * Format a date to display format (e.g., "7 JAN").
 */
fun LocalDate.toDisplayFormat(): String {
    val formatter = DateTimeFormatter.ofPattern("d MMM", Locale.US)
    return format(formatter).uppercase(Locale.US)
}

/**
 * Generate a seed from a date for daily challenge puzzle generation.
 */
fun LocalDate.toDailySeed(): Long {
    return year.toLong() * 10000 + monthValue * 100 + dayOfMonth
}

/**
 * Extension to check if a number is valid for Sudoku (1-9).
 */
fun Int.isValidSudokuNumber(): Boolean {
    return this in Constants.MIN_NUMBER..Constants.MAX_NUMBER
}

/**
 * Extension to convert a 1D index to 2D position (row, col).
 */
fun Int.toGridPosition(): Pair<Int, Int> {
    return Pair(this / GRID_SIZE, this % GRID_SIZE)
}

/**
 * Extension to convert a 2D position (row, col) to 1D index.
 */
fun Pair<Int, Int>.toGridIndex(): Int {
    return first * GRID_SIZE + second
}

/**
 * Shuffle a list with a specific seed for reproducible results.
 */
fun <T> List<T>.shuffledWithSeed(seed: Long): List<T> {
    val random = java.util.Random(seed)
    return this.shuffled(random)
}

/**
 * Get a random element from a list with a specific seed.
 */
fun <T> List<T>.randomWithSeed(seed: Long): T {
    val random = java.util.Random(seed)
    return this[random.nextInt(this.size)]
}
