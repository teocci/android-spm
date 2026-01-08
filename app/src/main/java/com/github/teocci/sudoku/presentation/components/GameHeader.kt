package com.github.teocci.sudoku.presentation.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.teocci.sudoku.core.Constants.MAX_MISTAKES
import com.github.teocci.sudoku.core.formatTime
import com.github.teocci.sudoku.domain.model.Difficulty
import com.github.teocci.sudoku.ui.theme.DateStyle
import com.github.teocci.sudoku.ui.theme.DifficultyEasy
import com.github.teocci.sudoku.ui.theme.DifficultyExpert
import com.github.teocci.sudoku.ui.theme.DifficultyHard
import com.github.teocci.sudoku.ui.theme.DifficultyMedium
import com.github.teocci.sudoku.ui.theme.ScoreStyle
import com.github.teocci.sudoku.ui.theme.SudokuPuzzleMasterTheme
import com.github.teocci.sudoku.ui.theme.SudokuTheme
import com.github.teocci.sudoku.ui.theme.TimerStyle
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Game header showing date, difficulty, mistakes, score, and timer.
 *
 * @param date The game date
 * @param difficulty The difficulty level
 * @param mistakes Current number of mistakes
 * @param maxMistakes Maximum allowed mistakes
 * @param score Current score
 * @param elapsedSeconds Elapsed time in seconds
 * @param isPaused Whether the game is paused
 * @param onBackClick Called when back button is tapped
 * @param onSettingsClick Called when settings button is tapped
 * @param onPauseClick Called when pause/play button is tapped
 * @param modifier Modifier for the header
 */
@Composable
fun GameHeader(
    date: LocalDate = LocalDate.now(),
    difficulty: Difficulty = Difficulty.MEDIUM,
    mistakes: Int = 0,
    maxMistakes: Int = MAX_MISTAKES,
    score: Int = 0,
    elapsedSeconds: Long = 0,
    isPaused: Boolean = false,
    onBackClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onPauseClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val colors = SudokuTheme.colors

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
    ) {
        // Top row: Back, Date, Settings
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = colors.controlButtonIcon
                )
            }

            // Date and difficulty
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = date.format(DateTimeFormatter.ofPattern("EEEE, MMM d")),
                    style = DateStyle,
                    color = colors.timerText
                )
                DifficultyBadge(difficulty = difficulty)
            }

            IconButton(onClick = onSettingsClick) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = colors.controlButtonIcon
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Bottom row: Mistakes, Score, Timer
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Mistakes counter
            MistakesCounter(
                mistakes = mistakes,
                maxMistakes = maxMistakes
            )

            // Score display
            ScoreDisplay(score = score)

            // Timer with pause button
            TimerDisplay(
                elapsedSeconds = elapsedSeconds,
                isPaused = isPaused,
                onPauseClick = onPauseClick
            )
        }
    }
}

/**
 * Difficulty badge with colored indicator.
 */
@Composable
fun DifficultyBadge(
    difficulty: Difficulty,
    modifier: Modifier = Modifier
) {
    val difficultyColor = when (difficulty) {
        Difficulty.EASY -> DifficultyEasy
        Difficulty.MEDIUM -> DifficultyMedium
        Difficulty.HARD -> DifficultyHard
        Difficulty.EXPERT -> DifficultyExpert
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(difficultyColor.copy(alpha = 0.2f))
            .padding(horizontal = 8.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(difficultyColor, CircleShape)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = difficulty.displayName,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = difficultyColor
        )
    }
}

/**
 * Mistakes counter with dot indicators.
 */
@Composable
fun MistakesCounter(
    mistakes: Int,
    maxMistakes: Int = MAX_MISTAKES,
    modifier: Modifier = Modifier
) {
    val colors = SudokuTheme.colors

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Mistakes",
            fontSize = 10.sp,
            color = colors.controlButtonIcon
        )

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            repeat(maxMistakes) { index ->
                val isFilled = index < mistakes

                val dotColor by animateColorAsState(
                    targetValue = if (isFilled) colors.mistakesDotFilled else colors.mistakesDotEmpty,
                    animationSpec = tween(durationMillis = 300),
                    label = "mistakeDot"
                )

                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(dotColor, CircleShape)
                )
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = "$mistakes/$maxMistakes",
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = if (mistakes >= maxMistakes) colors.error else colors.controlButtonIcon
        )
    }
}

/**
 * Score display with animated value.
 */
@Composable
fun ScoreDisplay(
    score: Int,
    modifier: Modifier = Modifier
) {
    val colors = SudokuTheme.colors

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Score",
            fontSize = 10.sp,
            color = colors.controlButtonIcon
        )

        Spacer(modifier = Modifier.height(4.dp))

        AnimatedContent(
            targetState = score,
            label = "scoreAnimation"
        ) { targetScore ->
            Text(
                text = String.format("%,d", targetScore),
                style = ScoreStyle,
                color = colors.scoreText
            )
        }
    }
}

/**
 * Timer display with pause/play button.
 */
@Composable
fun TimerDisplay(
    elapsedSeconds: Long,
    isPaused: Boolean,
    onPauseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = SudokuTheme.colors
    val interactionSource = remember { MutableInteractionSource() }

    val timerColor by animateColorAsState(
        targetValue = if (isPaused) colors.timerPaused else colors.timerText,
        animationSpec = tween(durationMillis = 300),
        label = "timerColor"
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Time",
            fontSize = 10.sp,
            color = colors.controlButtonIcon
        )

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onPauseClick
                )
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = formatTime(elapsedSeconds),
                style = TimerStyle,
                color = timerColor
            )

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                imageVector = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                contentDescription = if (isPaused) "Resume" else "Pause",
                tint = timerColor,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/**
 * Compact game header for smaller screens.
 */
@Composable
fun GameHeaderCompact(
    mistakes: Int = 0,
    maxMistakes: Int = MAX_MISTAKES,
    score: Int = 0,
    elapsedSeconds: Long = 0,
    isPaused: Boolean = false,
    onPauseClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val colors = SudokuTheme.colors

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Mistakes
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Mistakes: ",
                fontSize = 12.sp,
                color = colors.controlButtonIcon
            )
            Text(
                text = "$mistakes/$maxMistakes",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = if (mistakes >= maxMistakes) colors.error else colors.timerText
            )
        }

        // Score
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Score: ",
                fontSize = 12.sp,
                color = colors.controlButtonIcon
            )
            Text(
                text = String.format("%,d", score),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = colors.scoreText
            )
        }

        // Timer
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .clickable(onClick = onPauseClick)
                .padding(4.dp)
        ) {
            Text(
                text = formatTime(elapsedSeconds),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = if (isPaused) colors.timerPaused else colors.timerText
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                contentDescription = if (isPaused) "Resume" else "Pause",
                tint = if (isPaused) colors.timerPaused else colors.timerText,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

// ==================== PREVIEWS ====================

@Preview(showBackground = true, backgroundColor = 0xFF0D1B2A)
@Composable
private fun PreviewGameHeader_Default() {
    SudokuPuzzleMasterTheme(darkTheme = true) {
        GameHeader(
            mistakes = 1,
            score = 1250,
            elapsedSeconds = 325
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0D1B2A)
@Composable
private fun PreviewGameHeader_Paused() {
    SudokuPuzzleMasterTheme(darkTheme = true) {
        GameHeader(
            mistakes = 2,
            score = 2500,
            elapsedSeconds = 612,
            isPaused = true
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0D1B2A)
@Composable
private fun PreviewGameHeader_MaxMistakes() {
    SudokuPuzzleMasterTheme(darkTheme = true) {
        GameHeader(
            mistakes = 3,
            score = 500,
            elapsedSeconds = 180
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0D1B2A)
@Composable
private fun PreviewDifficultyBadges() {
    SudokuPuzzleMasterTheme(darkTheme = true) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DifficultyBadge(Difficulty.EASY)
            DifficultyBadge(Difficulty.MEDIUM)
            DifficultyBadge(Difficulty.HARD)
            DifficultyBadge(Difficulty.EXPERT)
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0D1B2A)
@Composable
private fun PreviewMistakesCounter() {
    SudokuPuzzleMasterTheme(darkTheme = true) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            MistakesCounter(mistakes = 0)
            MistakesCounter(mistakes = 1)
            MistakesCounter(mistakes = 2)
            MistakesCounter(mistakes = 3)
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0D1B2A)
@Composable
private fun PreviewGameHeaderCompact() {
    SudokuPuzzleMasterTheme(darkTheme = true) {
        GameHeaderCompact(
            mistakes = 1,
            score = 1250,
            elapsedSeconds = 325
        )
    }
}
