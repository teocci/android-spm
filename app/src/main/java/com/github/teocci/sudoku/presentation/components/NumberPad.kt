package com.github.teocci.sudoku.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.teocci.sudoku.core.Constants.GRID_SIZE
import com.github.teocci.sudoku.core.Constants.MAX_NUMBER
import com.github.teocci.sudoku.core.Constants.MIN_NUMBER
import com.github.teocci.sudoku.ui.theme.NumberPadStyle
import com.github.teocci.sudoku.ui.theme.SudokuPuzzleMasterTheme
import com.github.teocci.sudoku.ui.theme.SudokuTheme

/**
 * Number pad for inputting numbers 1-9.
 *
 * @param activeNumber Currently active/selected number
 * @param isNumberLocked Whether the active number is locked (long-press mode)
 * @param numberCounts Map of number to count of how many times it appears on the board
 * @param onNumberClick Called when a number is tapped
 * @param onNumberLongClick Called when a number is long-pressed
 * @param enabled Whether the number pad is enabled
 * @param modifier Modifier for the number pad
 */
@Composable
fun NumberPad(
    activeNumber: Int? = null,
    isNumberLocked: Boolean = false,
    numberCounts: Map<Int, Int> = emptyMap(),
    onNumberClick: (Int) -> Unit = {},
    onNumberLongClick: (Int) -> Unit = {},
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (number in MIN_NUMBER..MAX_NUMBER) {
            val count = numberCounts[number] ?: 0
            val isComplete = count >= GRID_SIZE
            val isActive = activeNumber == number

            NumberPadButton(
                number = number,
                isActive = isActive,
                isLocked = isActive && isNumberLocked,
                isComplete = isComplete,
                count = count,
                onClick = { onNumberClick(number) },
                onLongClick = { onNumberLongClick(number) },
                enabled = enabled && !isComplete,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * Individual number button on the number pad.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun NumberPadButton(
    number: Int,
    isActive: Boolean,
    isLocked: Boolean,
    isComplete: Boolean,
    count: Int,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    val colors = SudokuTheme.colors
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Animate background color
    val backgroundColor by animateColorAsState(
        targetValue = Color.Transparent,
        animationSpec = tween(durationMillis = 150),
        label = "buttonBackground"
    )

    // Animate text color
    val textColor by animateColorAsState(
        targetValue = when {
            isComplete -> colors.numberCompleted
            !enabled -> colors.numberCompleted
            isActive -> colors.numberPadText
            else -> colors.numberPadText
        },
        animationSpec = tween(durationMillis = 150),
        label = "buttonText"
    )

    // Scale animation for press
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        animationSpec = tween(durationMillis = 100),
        label = "buttonScale"
    )

    // Alpha for disabled state
    val alpha by animateFloatAsState(
        targetValue = if (enabled) 1f else 0.5f,
        animationSpec = tween(durationMillis = 150),
        label = "buttonAlpha"
    )

    Column(
        modifier = modifier
            .padding(horizontal = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .scale(scale)
                .alpha(alpha)
                .clip(RoundedCornerShape(8.dp))
                .combinedClickable(
                    interactionSource = interactionSource,
                    indication = null,
                    enabled = enabled,
                    onClick = onClick,
                    onLongClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onLongClick()
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = number.toString(),
                    style = NumberPadStyle,
                    color = textColor,
                    textAlign = TextAlign.Center
                )

                // Show remaining count indicator
                if (!isComplete) {
                    Spacer(modifier = Modifier.height(2.dp))
                    NumberCountIndicator(
                        count = count,
                        maxCount = GRID_SIZE,
                        activeColor = if (isActive) colors.numberPadText else colors.numberNote,
                        inactiveColor = colors.mistakesDotEmpty
                    )
                }
            }

            // Lock indicator for long-press mode
            if (isLocked) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .size(8.dp)
                        .background(colors.warning, CircleShape)
                )
            }
        }
    }
}

/**
 * Dot indicator showing how many of a number have been placed.
 */
@Composable
private fun NumberCountIndicator(
    count: Int,
    maxCount: Int,
    activeColor: Color,
    inactiveColor: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        // Show up to 9 small dots
        repeat(maxCount) { index ->
            Box(
                modifier = Modifier
                    .size(3.dp)
                    .background(
                        color = if (index < count) activeColor else inactiveColor,
                        shape = CircleShape
                    )
            )
        }
    }
}

/**
 * Compact number pad for smaller screens.
 * Displays numbers in a 3x3 grid.
 */
@Composable
fun NumberPadCompact(
    activeNumber: Int? = null,
    isNumberLocked: Boolean = false,
    numberCounts: Map<Int, Int> = emptyMap(),
    onNumberClick: (Int) -> Unit = {},
    onNumberLongClick: (Int) -> Unit = {},
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        for (row in 0 until 3) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                for (col in 0 until 3) {
                    val number = row * 3 + col + 1
                    val count = numberCounts[number] ?: 0
                    val isComplete = count >= GRID_SIZE
                    val isActive = activeNumber == number

                    NumberPadButton(
                        number = number,
                        isActive = isActive,
                        isLocked = isActive && isNumberLocked,
                        isComplete = isComplete,
                        count = count,
                        onClick = { onNumberClick(number) },
                        onLongClick = { onNumberLongClick(number) },
                        enabled = enabled && !isComplete,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

// ==================== PREVIEWS ====================

@Preview(showBackground = true, backgroundColor = 0xFF0D1B2A)
@Composable
private fun PreviewNumberPad_Default() {
    SudokuPuzzleMasterTheme(darkTheme = true) {
        Box(modifier = Modifier.padding(16.dp)) {
            NumberPad(
                numberCounts = mapOf(
                    1 to 3,
                    2 to 5,
                    3 to 9,
                    4 to 0,
                    5 to 7,
                    6 to 2,
                    7 to 4,
                    8 to 6,
                    9 to 1
                )
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0D1B2A)
@Composable
private fun PreviewNumberPad_WithActive() {
    SudokuPuzzleMasterTheme(darkTheme = true) {
        Box(modifier = Modifier.padding(16.dp)) {
            NumberPad(
                activeNumber = 5,
                isNumberLocked = false,
                numberCounts = mapOf(
                    1 to 3,
                    2 to 5,
                    3 to 9,
                    4 to 0,
                    5 to 7,
                    6 to 2,
                    7 to 4,
                    8 to 6,
                    9 to 1
                )
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0D1B2A)
@Composable
private fun PreviewNumberPad_WithLocked() {
    SudokuPuzzleMasterTheme(darkTheme = true) {
        Box(modifier = Modifier.padding(16.dp)) {
            NumberPad(
                activeNumber = 7,
                isNumberLocked = true,
                numberCounts = mapOf(
                    1 to 3,
                    2 to 5,
                    3 to 9,
                    4 to 0,
                    5 to 7,
                    6 to 2,
                    7 to 4,
                    8 to 6,
                    9 to 1
                )
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0D1B2A)
@Composable
private fun PreviewNumberPad_Disabled() {
    SudokuPuzzleMasterTheme(darkTheme = true) {
        Box(modifier = Modifier.padding(16.dp)) {
            NumberPad(
                enabled = false,
                numberCounts = mapOf(
                    1 to 3,
                    2 to 5,
                    3 to 9,
                    4 to 0,
                    5 to 7,
                    6 to 2,
                    7 to 4,
                    8 to 6,
                    9 to 1
                )
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0D1B2A)
@Composable
private fun PreviewNumberPadCompact() {
    SudokuPuzzleMasterTheme(darkTheme = true) {
        Box(modifier = Modifier.padding(16.dp)) {
            NumberPadCompact(
                activeNumber = 5,
                numberCounts = mapOf(
                    1 to 3,
                    2 to 5,
                    3 to 9,
                    4 to 0,
                    5 to 7,
                    6 to 2,
                    7 to 4,
                    8 to 6,
                    9 to 1
                )
            )
        }
    }
}
