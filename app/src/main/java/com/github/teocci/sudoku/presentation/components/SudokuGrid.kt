package com.github.teocci.sudoku.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.teocci.sudoku.core.Constants.BOX_SIZE
import com.github.teocci.sudoku.core.Constants.GRID_SIZE
import com.github.teocci.sudoku.domain.model.Cell
import com.github.teocci.sudoku.domain.model.Position
import com.github.teocci.sudoku.domain.model.SudokuBoard
import com.github.teocci.sudoku.ui.theme.SudokuPuzzleMasterTheme
import com.github.teocci.sudoku.ui.theme.SudokuTheme

/**
 * Complete 9x9 Sudoku grid with cells and grid lines.
 *
 * @param board The current board state
 * @param selectedCell Currently selected cell position
 * @param activeNumber Currently active number (for highlighting same numbers)
 * @param completingCells Set of cells currently in completion animation
 * @param hintCell Cell being hinted (if any)
 * @param onCellClick Called when a cell is tapped
 * @param onCellDrag Called when dragging over cells (for notes mode)
 * @param modifier Modifier for the grid
 */
@Composable
fun SudokuGrid(
    board: SudokuBoard,
    selectedCell: Position? = null,
    activeNumber: Int? = null,
    completingCells: Set<Position> = emptySet(),
    hintCell: Position? = null,
    onCellClick: (Position) -> Unit = {},
    onCellDrag: (Position) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val colors = SudokuTheme.colors

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .background(colors.gridLineThick)
    ) {
        val gridSize = this.constraints.maxWidth.toFloat()
        val cellSize = gridSize / GRID_SIZE

        // Track visited cells during drag to avoid duplicate callbacks
        var lastDraggedCell by remember { mutableStateOf<Position?>(null) }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        val position = offsetToPosition(offset, cellSize)
                        if (position != null) {
                            onCellClick(position)
                        }
                    }
                }
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            lastDraggedCell = offsetToPosition(offset, cellSize)
                            lastDraggedCell?.let { onCellDrag(it) }
                        },
                        onDrag = { change, _ ->
                            val position = offsetToPosition(change.position, cellSize)
                            if (position != null && position != lastDraggedCell) {
                                lastDraggedCell = position
                                onCellDrag(position)
                            }
                        },
                        onDragEnd = {
                            lastDraggedCell = null
                        },
                        onDragCancel = {
                            lastDraggedCell = null
                        }
                    )
                }
        ) {
            // Grid cells
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                for (row in 0 until GRID_SIZE) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        for (col in 0 until GRID_SIZE) {
                            val position = Position(row, col)
                            val cell = board.getCell(position)
                            val highlightState = calculateHighlightState(
                                position = position,
                                cell = cell,
                                selectedCell = selectedCell,
                                activeNumber = activeNumber,
                                completingCells = completingCells,
                                hintCell = hintCell
                            )

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                            ) {
                                SudokuCell(
                                    cell = cell,
                                    highlightState = highlightState,
                                    isInAlternateBox = false,
                                    onClick = { onCellClick(position) }
                                )
                            }
                        }
                    }
                }
            }

            // Grid lines overlay
            GridLines(
                gridSize = gridSize,
                thinColor = colors.gridLineThin,
                thickColor = colors.gridLineThick
            )
        }
    }
}

/**
 * Calculate the highlight state for a cell.
 */
private fun calculateHighlightState(
    position: Position,
    cell: Cell,
    selectedCell: Position?,
    activeNumber: Int?,
    completingCells: Set<Position>,
    hintCell: Position?
): CellHighlightState {
    val isSelected = position == selectedCell
    val isRelated = selectedCell?.let { position.isRelatedTo(it) } ?: false
    val hasSameNumber = activeNumber != null && cell.value == activeNumber
    val isError = cell.isError
    val isCompleting = position in completingCells
    val isHintTarget = position == hintCell

    return determineCellHighlightState(
        isSelected = isSelected,
        isRelated = isRelated,
        hasSameNumber = hasSameNumber,
        isError = isError,
        isCompleting = isCompleting,
        isHintTarget = isHintTarget
    )
}

/**
 * Convert touch offset to grid position.
 */
private fun offsetToPosition(offset: Offset, cellSize: Float): Position? {
    val col = (offset.x / cellSize).toInt()
    val row = (offset.y / cellSize).toInt()

    return if (row in 0 until GRID_SIZE && col in 0 until GRID_SIZE) {
        Position(row, col)
    } else {
        null
    }
}

/**
 * Get padding for cell borders to create grid lines effect.
 * Thicker padding at box boundaries (every 3 cells).
 */
private fun getCellPadding(index: Int, isStart: Boolean): androidx.compose.ui.unit.Dp {
    val thinLine = 0.5.dp
    val thickLine = 1.5.dp

    return when {
        // Box boundaries get thick lines
        isStart && index % BOX_SIZE == 0 -> thickLine
        !isStart && (index + 1) % BOX_SIZE == 0 -> thickLine
        // Regular cell boundaries get thin lines
        else -> thinLine
    }
}

/**
 * Draws grid lines on top of cells.
 */
@Composable
private fun GridLines(
    gridSize: Float,
    thinColor: Color,
    thickColor: Color
) {
    val density = LocalDensity.current
    val thinWidth = with(density) { 0.5.dp.toPx() }
    val thickWidth = with(density) { 2.dp.toPx() }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val cellSize = gridSize / GRID_SIZE

        // Draw vertical lines
        for (i in 1 until GRID_SIZE) {
            val x = i * cellSize
            val isThick = i % BOX_SIZE == 0
            drawLine(
                color = if (isThick) thickColor else thinColor,
                start = Offset(x, 0f),
                end = Offset(x, gridSize),
                strokeWidth = if (isThick) thickWidth else thinWidth
            )
        }

        // Draw horizontal lines
        for (i in 1 until GRID_SIZE) {
            val y = i * cellSize
            val isThick = i % BOX_SIZE == 0
            drawLine(
                color = if (isThick) thickColor else thinColor,
                start = Offset(0f, y),
                end = Offset(gridSize, y),
                strokeWidth = if (isThick) thickWidth else thinWidth
            )
        }
    }
}

/**
 * Simplified grid for paused/hidden state.
 */
@Composable
fun SudokuGridPaused(
    modifier: Modifier = Modifier
) {
    val colors = SudokuTheme.colors

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .background(colors.cellBackground)
    ) {
        val gridSize = this.constraints.maxWidth.toFloat()

        GridLines(
            gridSize = gridSize,
            thinColor = colors.gridLineThin,
            thickColor = colors.gridLineThick
        )
    }
}

// ==================== PREVIEWS ====================

@Preview(showBackground = true, backgroundColor = 0xFF0D1B2A)
@Composable
private fun PreviewSudokuGrid_Empty() {
    SudokuPuzzleMasterTheme(darkTheme = true) {
        Box(modifier = Modifier.padding(16.dp)) {
            SudokuGrid(
                board = SudokuBoard.empty()
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0D1B2A)
@Composable
private fun PreviewSudokuGrid_WithSelection() {
    SudokuPuzzleMasterTheme(darkTheme = true) {
        // Create a sample board with some values
        val solution = Array(GRID_SIZE) { row ->
            IntArray(GRID_SIZE) { col ->
                ((row * 3 + row / 3 + col) % 9) + 1
            }
        }
        val puzzle = solution.map { it.copyOf() }.toTypedArray()
        // Remove some cells
        puzzle[0][0] = 0
        puzzle[1][1] = 0
        puzzle[2][2] = 0

        val board = SudokuBoard.fromPuzzle(solution, puzzle)

        Box(modifier = Modifier.padding(16.dp)) {
            SudokuGrid(
                board = board,
                selectedCell = Position(4, 4),
                activeNumber = 5
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0D1B2A)
@Composable
private fun PreviewSudokuGrid_Paused() {
    SudokuPuzzleMasterTheme(darkTheme = true) {
        Box(modifier = Modifier.padding(16.dp)) {
            SudokuGridPaused()
        }
    }
}
