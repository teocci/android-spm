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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EditOff
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.teocci.sudoku.ui.theme.ControlButtonLabelStyle
import com.github.teocci.sudoku.ui.theme.SudokuPuzzleMasterTheme
import com.github.teocci.sudoku.ui.theme.SudokuTheme

/**
 * Game control buttons: Undo, Erase, Notes, Hint.
 *
 * @param notesMode Whether notes mode is currently active
 * @param canUndo Whether undo is available
 * @param canErase Whether erase is available (cell selected with value)
 * @param hintsRemaining Number of hints remaining
 * @param onUndoClick Called when undo is tapped
 * @param onEraseClick Called when erase is tapped
 * @param onNotesClick Called when notes toggle is tapped
 * @param onHintClick Called when hint is tapped
 * @param enabled Whether all controls are enabled
 * @param modifier Modifier for the controls row
 */
@Composable
fun GameControls(
    notesMode: Boolean = false,
    canUndo: Boolean = true,
    canErase: Boolean = true,
    hintsRemaining: Int = 3,
    onUndoClick: () -> Unit = {},
    onEraseClick: () -> Unit = {},
    onNotesClick: () -> Unit = {},
    onHintClick: () -> Unit = {},
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Undo button
        ControlButton(
            icon = Icons.AutoMirrored.Filled.Undo,
            label = "Undo",
            onClick = onUndoClick,
            enabled = enabled && canUndo,
            isActive = false
        )

        // Erase button
        ControlButton(
            icon = Icons.Default.Backspace,
            label = "Erase",
            onClick = onEraseClick,
            enabled = enabled && canErase,
            isActive = false
        )

        // Notes toggle button
        ControlButton(
            icon = if (notesMode) Icons.Default.EditOff else Icons.Default.Edit,
            label = if (notesMode) "Notes ON" else "Notes",
            onClick = onNotesClick,
            enabled = enabled,
            isActive = notesMode
        )

        // Hint button with badge
        ControlButtonWithBadge(
            icon = Icons.Default.Lightbulb,
            label = "Hint",
            badgeCount = hintsRemaining,
            onClick = onHintClick,
            enabled = enabled && hintsRemaining > 0,
            isActive = false
        )
    }
}

/**
 * Individual control button.
 */
@Composable
private fun ControlButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    enabled: Boolean,
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    val colors = SudokuTheme.colors
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Animate background color
    val backgroundColor by animateColorAsState(
        targetValue = when {
            !enabled -> colors.controlButtonDisabled
            isActive -> colors.controlButtonActiveBackground
            isPressed -> colors.controlButtonActiveBackground.copy(alpha = 0.5f)
            else -> colors.controlButtonBackground
        },
        animationSpec = tween(durationMillis = 150),
        label = "controlBackground"
    )

    // Animate icon color
    val iconColor by animateColorAsState(
        targetValue = when {
            !enabled -> colors.controlButtonIcon.copy(alpha = 0.4f)
            isActive -> colors.controlButtonActiveIcon
            else -> colors.controlButtonIcon
        },
        animationSpec = tween(durationMillis = 150),
        label = "controlIcon"
    )

    // Scale animation for press
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        animationSpec = tween(durationMillis = 100),
        label = "controlScale"
    )

    // Alpha for disabled state
    val alpha by animateFloatAsState(
        targetValue = if (enabled) 1f else 0.5f,
        animationSpec = tween(durationMillis = 150),
        label = "controlAlpha"
    )

    Column(
        modifier = modifier
            .scale(scale)
            .alpha(alpha)
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = iconColor,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = label,
            style = ControlButtonLabelStyle,
            color = iconColor
        )
    }
}

/**
 * Control button with a badge showing remaining count.
 */
@Composable
private fun ControlButtonWithBadge(
    icon: ImageVector,
    label: String,
    badgeCount: Int,
    onClick: () -> Unit,
    enabled: Boolean,
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    val colors = SudokuTheme.colors
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Animate background color
    val backgroundColor by animateColorAsState(
        targetValue = when {
            !enabled -> colors.controlButtonDisabled
            isActive -> colors.controlButtonActiveBackground
            isPressed -> colors.controlButtonActiveBackground.copy(alpha = 0.5f)
            else -> colors.controlButtonBackground
        },
        animationSpec = tween(durationMillis = 150),
        label = "controlBackground"
    )

    // Animate icon color
    val iconColor by animateColorAsState(
        targetValue = when {
            !enabled -> colors.controlButtonIcon.copy(alpha = 0.4f)
            isActive -> colors.controlButtonActiveIcon
            else -> colors.controlButtonIcon
        },
        animationSpec = tween(durationMillis = 150),
        label = "controlIcon"
    )

    // Scale animation for press
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        animationSpec = tween(durationMillis = 100),
        label = "controlScale"
    )

    // Alpha for disabled state
    val alpha by animateFloatAsState(
        targetValue = if (enabled) 1f else 0.5f,
        animationSpec = tween(durationMillis = 150),
        label = "controlAlpha"
    )

    Column(
        modifier = modifier
            .scale(scale)
            .alpha(alpha)
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BadgedBox(
            badge = {
                if (badgeCount > 0) {
                    Badge(
                        containerColor = colors.warning,
                        contentColor = colors.numberFixed
                    ) {
                        Text(
                            text = badgeCount.toString(),
                            style = ControlButtonLabelStyle
                        )
                    }
                }
            }
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = iconColor,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = label,
            style = ControlButtonLabelStyle,
            color = iconColor
        )
    }
}

/**
 * Compact version of game controls for smaller screens.
 */
@Composable
fun GameControlsCompact(
    notesMode: Boolean = false,
    canUndo: Boolean = true,
    canErase: Boolean = true,
    hintsRemaining: Int = 3,
    onUndoClick: () -> Unit = {},
    onEraseClick: () -> Unit = {},
    onNotesClick: () -> Unit = {},
    onHintClick: () -> Unit = {},
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    val colors = SudokuTheme.colors

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Undo
        CompactControlButton(
            icon = Icons.AutoMirrored.Filled.Undo,
            contentDescription = "Undo",
            onClick = onUndoClick,
            enabled = enabled && canUndo,
            isActive = false
        )

        // Erase
        CompactControlButton(
            icon = Icons.Default.Backspace,
            contentDescription = "Erase",
            onClick = onEraseClick,
            enabled = enabled && canErase,
            isActive = false
        )

        // Notes
        CompactControlButton(
            icon = if (notesMode) Icons.Default.EditOff else Icons.Default.Edit,
            contentDescription = if (notesMode) "Notes ON" else "Notes",
            onClick = onNotesClick,
            enabled = enabled,
            isActive = notesMode
        )

        // Hint
        CompactControlButton(
            icon = Icons.Default.Lightbulb,
            contentDescription = "Hint ($hintsRemaining)",
            onClick = onHintClick,
            enabled = enabled && hintsRemaining > 0,
            isActive = false,
            badgeCount = hintsRemaining
        )
    }
}

/**
 * Compact control button (icon only).
 */
@Composable
private fun CompactControlButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    enabled: Boolean,
    isActive: Boolean,
    badgeCount: Int? = null,
    modifier: Modifier = Modifier
) {
    val colors = SudokuTheme.colors
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val backgroundColor by animateColorAsState(
        targetValue = when {
            !enabled -> colors.controlButtonDisabled
            isActive -> colors.controlButtonActiveBackground
            isPressed -> colors.controlButtonActiveBackground.copy(alpha = 0.5f)
            else -> colors.controlButtonBackground
        },
        animationSpec = tween(durationMillis = 150),
        label = "compactBackground"
    )

    val iconColor by animateColorAsState(
        targetValue = when {
            !enabled -> colors.controlButtonIcon.copy(alpha = 0.4f)
            isActive -> colors.controlButtonActiveIcon
            else -> colors.controlButtonIcon
        },
        animationSpec = tween(durationMillis = 150),
        label = "compactIcon"
    )

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        animationSpec = tween(durationMillis = 100),
        label = "compactScale"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .size(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        if (badgeCount != null && badgeCount > 0) {
            BadgedBox(
                badge = {
                    Badge(
                        containerColor = colors.warning,
                        contentColor = colors.numberFixed
                    ) {
                        Text(
                            text = badgeCount.toString(),
                            style = ControlButtonLabelStyle
                        )
                    }
                }
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = contentDescription,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }
        } else {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = iconColor,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

// ==================== PREVIEWS ====================

@Preview(showBackground = true, backgroundColor = 0xFF0D1B2A)
@Composable
private fun PreviewGameControls_Default() {
    SudokuPuzzleMasterTheme(darkTheme = true) {
        Box(modifier = Modifier.padding(16.dp)) {
            GameControls()
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0D1B2A)
@Composable
private fun PreviewGameControls_NotesActive() {
    SudokuPuzzleMasterTheme(darkTheme = true) {
        Box(modifier = Modifier.padding(16.dp)) {
            GameControls(
                notesMode = true,
                hintsRemaining = 2
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0D1B2A)
@Composable
private fun PreviewGameControls_SomeDisabled() {
    SudokuPuzzleMasterTheme(darkTheme = true) {
        Box(modifier = Modifier.padding(16.dp)) {
            GameControls(
                canUndo = false,
                canErase = false,
                hintsRemaining = 0
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0D1B2A)
@Composable
private fun PreviewGameControls_AllDisabled() {
    SudokuPuzzleMasterTheme(darkTheme = true) {
        Box(modifier = Modifier.padding(16.dp)) {
            GameControls(enabled = false)
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0D1B2A)
@Composable
private fun PreviewGameControlsCompact() {
    SudokuPuzzleMasterTheme(darkTheme = true) {
        Box(modifier = Modifier.padding(16.dp)) {
            GameControlsCompact(
                notesMode = true,
                hintsRemaining = 2
            )
        }
    }
}
