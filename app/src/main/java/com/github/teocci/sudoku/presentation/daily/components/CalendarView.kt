package com.github.teocci.sudoku.presentation.daily.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.teocci.sudoku.domain.model.Difficulty
import com.github.teocci.sudoku.presentation.daily.CalendarDay
import com.github.teocci.sudoku.ui.theme.CalendarDayStyle
import com.github.teocci.sudoku.ui.theme.DifficultyEasy
import com.github.teocci.sudoku.ui.theme.DifficultyExpert
import com.github.teocci.sudoku.ui.theme.DifficultyHard
import com.github.teocci.sudoku.ui.theme.DifficultyMedium
import com.github.teocci.sudoku.ui.theme.SudokuPuzzleMasterTheme
import com.github.teocci.sudoku.ui.theme.SudokuTheme
import com.github.teocci.sudoku.ui.theme.TrophyBronze
import com.github.teocci.sudoku.ui.theme.TrophyGold
import com.github.teocci.sudoku.ui.theme.TrophyPlatinum
import com.github.teocci.sudoku.ui.theme.TrophySilver
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

/**
 * Calendar view for daily challenges.
 *
 * @param yearMonth The month to display
 * @param calendarDays List of days to display (42 days for 6-week grid)
 * @param selectedDate Currently selected date
 * @param onDateClick Called when a date is clicked
 * @param onPreviousMonth Called when previous month button is clicked
 * @param onNextMonth Called when next month button is clicked
 * @param canNavigateNext Whether next month navigation is enabled
 * @param modifier Modifier for the calendar
 */
@Composable
fun CalendarView(
    yearMonth: YearMonth,
    calendarDays: List<CalendarDay>,
    selectedDate: LocalDate,
    onDateClick: (LocalDate) -> Unit,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    canNavigateNext: Boolean = true,
    modifier: Modifier = Modifier
) {
    val colors = SudokuTheme.colors

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = colors.controlButtonBackground
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Month header with navigation
            CalendarHeader(
                yearMonth = yearMonth,
                onPreviousMonth = onPreviousMonth,
                onNextMonth = onNextMonth,
                canNavigateNext = canNavigateNext
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Day of week headers
            DayOfWeekHeader()

            Spacer(modifier = Modifier.height(8.dp))

            // Calendar grid
            CalendarGrid(
                calendarDays = calendarDays,
                selectedDate = selectedDate,
                onDateClick = onDateClick
            )
        }
    }
}

/**
 * Calendar header with month/year and navigation arrows.
 */
@Composable
private fun CalendarHeader(
    yearMonth: YearMonth,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    canNavigateNext: Boolean
) {
    val colors = SudokuTheme.colors

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPreviousMonth) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = "Previous month",
                tint = colors.controlButtonIcon
            )
        }

        Text(
            text = "${yearMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${yearMonth.year}",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = colors.numberFixed
        )

        IconButton(
            onClick = onNextMonth,
            enabled = canNavigateNext
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Next month",
                tint = if (canNavigateNext) colors.controlButtonIcon else colors.controlButtonIcon.copy(alpha = 0.3f)
            )
        }
    }
}

/**
 * Day of week header row.
 */
@Composable
private fun DayOfWeekHeader() {
    val colors = SudokuTheme.colors
    val daysOfWeek = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        daysOfWeek.forEach { day ->
            Text(
                text = day,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = colors.controlButtonIcon,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * Calendar grid of days.
 */
@Composable
private fun CalendarGrid(
    calendarDays: List<CalendarDay>,
    selectedDate: LocalDate,
    onDateClick: (LocalDate) -> Unit
) {
    // 6 rows of 7 days
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        for (week in 0 until 6) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                for (day in 0 until 7) {
                    val index = week * 7 + day
                    if (index < calendarDays.size) {
                        val calendarDay = calendarDays[index]
                        CalendarDayCell(
                            calendarDay = calendarDay,
                            isSelected = calendarDay.date == selectedDate,
                            onClick = { onDateClick(calendarDay.date) },
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

/**
 * Individual calendar day cell.
 */
@Composable
private fun CalendarDayCell(
    calendarDay: CalendarDay,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = SudokuTheme.colors

    // Determine cell colors
    val backgroundColor by animateColorAsState(
        targetValue = when {
            isSelected -> colors.calendarDayCurrent
            calendarDay.isToday -> colors.calendarDayCurrent.copy(alpha = 0.3f)
            calendarDay.isCompleted -> colors.calendarDayCompleted.copy(alpha = 0.2f)
            else -> Color.Transparent
        },
        animationSpec = tween(200),
        label = "dayBackground"
    )

    val textColor by animateColorAsState(
        targetValue = when {
            !calendarDay.isInCurrentMonth -> colors.controlButtonIcon.copy(alpha = 0.3f)
            calendarDay.isFuture -> colors.controlButtonIcon.copy(alpha = 0.5f)
            isSelected -> colors.numberFixed
            calendarDay.isToday -> colors.calendarDayCompleted
            calendarDay.isCompleted -> colors.calendarDayCompleted
            else -> colors.numberFixed
        },
        animationSpec = tween(200),
        label = "dayText"
    )

    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.1f else 1f,
        animationSpec = tween(200),
        label = "dayScale"
    )

    val isClickable = calendarDay.isInCurrentMonth && !calendarDay.isFuture

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .scale(scale)
            .clip(CircleShape)
            .background(backgroundColor)
            .then(
                if (calendarDay.isToday && !isSelected) {
                    Modifier.border(1.dp, colors.calendarDayCompleted, CircleShape)
                } else {
                    Modifier
                }
            )
            .clickable(enabled = isClickable, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Show trophy icon if completed, otherwise show day number
            if (calendarDay.isCompleted && calendarDay.isInCurrentMonth && calendarDay.difficulty != null) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = "Completed Challenge",
                    tint = getTrophyColor(calendarDay.difficulty),
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Text(
                    text = calendarDay.dayOfMonth.toString(),
                    style = CalendarDayStyle,
                    color = textColor,
                    fontWeight = if (calendarDay.isToday || isSelected) FontWeight.Bold else FontWeight.Normal
                )

                // Difficulty indicator for non-future days (only if not completed)
                if (!calendarDay.isFuture && calendarDay.isInCurrentMonth && !calendarDay.isCompleted) {
                    calendarDay.difficulty?.let { difficulty ->
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(getDifficultyColor(difficulty), CircleShape)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Compact calendar view showing just current week.
 */
@Composable
fun CalendarWeekView(
    calendarDays: List<CalendarDay>,
    selectedDate: LocalDate,
    onDateClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = SudokuTheme.colors

    // Find the week containing selected date
    val selectedWeekStart = selectedDate.minusDays(selectedDate.dayOfWeek.value.toLong() % 7)
    val weekDays = calendarDays.filter { day ->
        !day.date.isBefore(selectedWeekStart) && day.date.isBefore(selectedWeekStart.plusDays(7))
    }.take(7)

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = colors.controlButtonBackground
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            weekDays.forEach { day ->
                CalendarDayCell(
                    calendarDay = day,
                    isSelected = day.date == selectedDate,
                    onClick = { onDateClick(day.date) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * Get the color associated with a difficulty level.
 */
private fun getDifficultyColor(difficulty: Difficulty): Color {
    return when (difficulty) {
        Difficulty.EASY -> DifficultyEasy
        Difficulty.MEDIUM -> DifficultyMedium
        Difficulty.HARD -> DifficultyHard
        Difficulty.EXPERT -> DifficultyExpert
    }
}

/**
 * Get the trophy color based on difficulty level.
 */
private fun getTrophyColor(difficulty: Difficulty): Color {
    return when (difficulty) {
        Difficulty.EASY -> TrophyBronze
        Difficulty.MEDIUM -> TrophySilver
        Difficulty.HARD -> TrophyGold
        Difficulty.EXPERT -> TrophyPlatinum
    }
}

// ==================== PREVIEWS ====================

@Preview(showBackground = true, backgroundColor = 0xFF0D1B2A)
@Composable
private fun PreviewCalendarView() {
    SudokuPuzzleMasterTheme(darkTheme = true) {
        val today = LocalDate.now()
        val yearMonth = YearMonth.from(today)

        CalendarView(
            yearMonth = yearMonth,
            calendarDays = generatePreviewDays(yearMonth, today),
            selectedDate = today,
            onDateClick = {},
            onPreviousMonth = {},
            onNextMonth = {},
            canNavigateNext = false,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0D1B2A)
@Composable
private fun PreviewCalendarWeekView() {
    SudokuPuzzleMasterTheme(darkTheme = true) {
        val today = LocalDate.now()
        val yearMonth = YearMonth.from(today)

        CalendarWeekView(
            calendarDays = generatePreviewDays(yearMonth, today),
            selectedDate = today,
            onDateClick = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}

/**
 * Generate preview calendar days.
 */
private fun generatePreviewDays(yearMonth: YearMonth, today: LocalDate): List<CalendarDay> {
    val days = mutableListOf<CalendarDay>()
    val firstOfMonth = yearMonth.atDay(1)
    val firstDisplayDay = firstOfMonth.minusDays(firstOfMonth.dayOfWeek.value.toLong() % 7)

    var currentDay = firstDisplayDay
    repeat(42) {
        val isInCurrentMonth = YearMonth.from(currentDay) == yearMonth
        val isToday = currentDay == today
        val isFuture = currentDay.isAfter(today)
        val isCompleted = !isFuture && (currentDay.dayOfMonth % 3 == 0 || currentDay.dayOfMonth % 5 == 0)

        days.add(
            CalendarDay(
                date = currentDay,
                dayOfMonth = currentDay.dayOfMonth,
                isInCurrentMonth = isInCurrentMonth,
                isToday = isToday,
                isFuture = isFuture,
                isCompleted = isCompleted,
                difficulty = if (!isFuture) Difficulty.entries[currentDay.dayOfMonth % 4] else null
            )
        )

        currentDay = currentDay.plusDays(1)
    }

    return days
}
