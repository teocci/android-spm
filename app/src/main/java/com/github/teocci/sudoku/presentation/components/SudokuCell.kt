package com.github.teocci.sudoku.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.teocci.sudoku.core.Constants.BOX_SIZE
import com.github.teocci.sudoku.domain.model.Cell
import com.github.teocci.sudoku.ui.theme.CellNoteStyle
import com.github.teocci.sudoku.ui.theme.CellNumberFixedStyle
import com.github.teocci.sudoku.ui.theme.CellNumberStyle
import com.github.teocci.sudoku.ui.theme.SudokuPuzzleMasterTheme
import com.github.teocci.sudoku.ui.theme.SudokuTheme

/**
 * Visual state of a Sudoku cell.
 * Priority order: Error > Selected > SameNumber > Related > Completed > Default
 */
enum class CellHighlightState {
    DEFAULT,
    RELATED,        // Same row, column, or box
    SAME_NUMBER,    // Same number as selected/active
    SELECTED,       // Currently selected cell
    ERROR,          // Contains an error
    COMPLETED,      // Part of a just-completed row/col/box (animation)
    HINT            // Hint cell highlight
}

/**
 * Individual Sudoku cell composable.
 *
 * @param cell The cell data to display
 * @param highlightState The current highlight state of the cell
 * @param isInAlternateBox Whether the cell is in an alternate-colored box
 * @param onClick Called when the cell is tapped
 * @param modifier Modifier for the cell
 */
@Composable
fun SudokuCell(
    cell: Cell,
    highlightState: CellHighlightState = CellHighlightState.DEFAULT,
    isInAlternateBox: Boolean = false,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val colors = SudokuTheme.colors
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Determine background color based on highlight state
    val backgroundColor by animateColorAsState(
        targetValue = when (highlightState) {
            CellHighlightState.ERROR -> colors.cellError
            CellHighlightState.SELECTED -> if (isPressed) colors.cellSelectedPressed else colors.cellSelected
            CellHighlightState.SAME_NUMBER -> colors.cellSameNumber
            CellHighlightState.RELATED -> colors.cellRelated
            CellHighlightState.COMPLETED -> colors.cellCompleted
            CellHighlightState.HINT -> colors.cellHint
            CellHighlightState.DEFAULT -> colors.cellBackground
        },
        animationSpec = tween(durationMillis = 150),
        label = "cellBackground"
    )

    // Scale animation for press feedback
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = tween(durationMillis = 100),
        label = "cellScale"
    )

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .scale(scale)
            .background(backgroundColor)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        when {
            // Display value if present
            cell.value != null -> {
                CellNumber(
                    number = cell.value,
                    isFixed = cell.isFixed,
                    isError = cell.isError,
                    isRevealed = cell.isRevealed
                )
            }
            // Display notes if no value
            cell.notes.isNotEmpty() -> {
                CellNotes(notes = cell.notes)
            }
        }
    }
}

/**
 * Displays a number in a cell.
 */
@Composable
private fun CellNumber(
    number: Int,
    isFixed: Boolean,
    isError: Boolean,
    isRevealed: Boolean
) {
    val colors = SudokuTheme.colors

    val textColor = when {
        isError -> colors.numberError
        isRevealed -> colors.numberHint
        isFixed -> colors.numberFixed
        else -> colors.numberUser
    }

    val textStyle = if (isFixed) CellNumberFixedStyle else CellNumberStyle

    Text(
        text = number.toString(),
        style = textStyle,
        color = textColor,
        textAlign = TextAlign.Center
    )
}

/**
 * Displays notes in a 3x3 mini-grid within a cell.
 */
@Composable
private fun CellNotes(
    notes: Set<Int>
) {
    val colors = SudokuTheme.colors

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(2.dp),
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        for (row in 0 until BOX_SIZE) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (col in 0 until BOX_SIZE) {
                    val noteNumber = row * BOX_SIZE + col + 1
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        if (noteNumber in notes) {
                            Text(
                                text = noteNumber.toString(),
                                style = CellNoteStyle,
                                color = colors.numberNote,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Determines the highlight state for a cell based on game state.
 *
 * @param isSelected Whether this cell is currently selected
 * @param isRelated Whether this cell is in same row/col/box as selected
 * @param hasSameNumber Whether this cell has the same number as active number
 * @param isError Whether this cell has an error
 * @param isCompleting Whether this cell is part of a completing animation
 * @param isHintTarget Whether this cell is being hinted
 * @return The appropriate CellHighlightState
 */
fun determineCellHighlightState(
    isSelected: Boolean = false,
    isRelated: Boolean = false,
    hasSameNumber: Boolean = false,
    isError: Boolean = false,
    isCompleting: Boolean = false,
    isHintTarget: Boolean = false
): CellHighlightState {
    return when {
        isError -> CellHighlightState.ERROR
        isSelected -> CellHighlightState.SELECTED
        isHintTarget -> CellHighlightState.HINT
        isCompleting -> CellHighlightState.COMPLETED
        hasSameNumber -> CellHighlightState.SAME_NUMBER
        isRelated -> CellHighlightState.RELATED
        else -> CellHighlightState.DEFAULT
    }
}

/**
 * Checks if a cell is in an alternate-colored box.
 * Boxes 1, 3, 5, 7 (indices) are alternate colored for visual distinction.
 */
fun isAlternateBox(row: Int, col: Int): Boolean {
    val boxRow = row / BOX_SIZE
    val boxCol = col / BOX_SIZE
    val boxIndex = boxRow * BOX_SIZE + boxCol
    // Checkerboard pattern: boxes 1, 3, 5, 7 are alternate
    return (boxRow + boxCol) % 2 == 1
}

// ==================== PREVIEWS ====================

@Preview(showBackground = true, backgroundColor = 0xFF0D1B2A)
@Composable
private fun PreviewSudokuCell_Empty() {
    SudokuPuzzleMasterTheme(darkTheme = true) {
        Box(modifier = Modifier.size(60.dp)) {
            SudokuCell(
                cell = Cell(solution = 5),
                highlightState = CellHighlightState.DEFAULT
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0D1B2A)
@Composable
private fun PreviewSudokuCell_FixedNumber() {
    SudokuPuzzleMasterTheme(darkTheme = true) {
        Box(modifier = Modifier.size(60.dp)) {
            SudokuCell(
                cell = Cell.fixed(7),
                highlightState = CellHighlightState.DEFAULT
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0D1B2A)
@Composable
private fun PreviewSudokuCell_UserNumber() {
    SudokuPuzzleMasterTheme(darkTheme = true) {
        Box(modifier = Modifier.size(60.dp)) {
            SudokuCell(
                cell = Cell(value = 3, solution = 3),
                highlightState = CellHighlightState.DEFAULT
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0D1B2A)
@Composable
private fun PreviewSudokuCell_Error() {
    SudokuPuzzleMasterTheme(darkTheme = true) {
        Box(modifier = Modifier.size(60.dp)) {
            SudokuCell(
                cell = Cell(value = 5, solution = 3, isError = true),
                highlightState = CellHighlightState.ERROR
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0D1B2A)
@Composable
private fun PreviewSudokuCell_Selected() {
    SudokuPuzzleMasterTheme(darkTheme = true) {
        Box(modifier = Modifier.size(60.dp)) {
            SudokuCell(
                cell = Cell(solution = 5),
                highlightState = CellHighlightState.SELECTED
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0D1B2A)
@Composable
private fun PreviewSudokuCell_Related() {
    SudokuPuzzleMasterTheme(darkTheme = true) {
        Box(modifier = Modifier.size(60.dp)) {
            SudokuCell(
                cell = Cell.fixed(2),
                highlightState = CellHighlightState.RELATED
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0D1B2A)
@Composable
private fun PreviewSudokuCell_SameNumber() {
    SudokuPuzzleMasterTheme(darkTheme = true) {
        Box(modifier = Modifier.size(60.dp)) {
            SudokuCell(
                cell = Cell.fixed(5),
                highlightState = CellHighlightState.SAME_NUMBER
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0D1B2A)
@Composable
private fun PreviewSudokuCell_WithNotes() {
    SudokuPuzzleMasterTheme(darkTheme = true) {
        Box(modifier = Modifier.size(60.dp)) {
            SudokuCell(
                cell = Cell(solution = 5, notes = setOf(1, 3, 5, 7, 9)),
                highlightState = CellHighlightState.DEFAULT
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0D1B2A)
@Composable
private fun PreviewSudokuCell_Hint() {
    SudokuPuzzleMasterTheme(darkTheme = true) {
        Box(modifier = Modifier.size(60.dp)) {
            SudokuCell(
                cell = Cell(value = 5, solution = 5, isRevealed = true),
                highlightState = CellHighlightState.HINT
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0D1B2A)
@Composable
private fun PreviewSudokuCell_AlternateBox() {
    SudokuPuzzleMasterTheme(darkTheme = true) {
        Box(modifier = Modifier.size(60.dp)) {
            SudokuCell(
                cell = Cell.fixed(4),
                highlightState = CellHighlightState.DEFAULT,
                isInAlternateBox = true
            )
        }
    }
}
