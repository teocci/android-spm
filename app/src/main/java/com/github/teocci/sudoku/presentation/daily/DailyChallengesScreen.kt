package com.github.teocci.sudoku.presentation.daily

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.teocci.sudoku.domain.model.Difficulty
import com.github.teocci.sudoku.presentation.daily.components.CalendarView
import com.github.teocci.sudoku.presentation.daily.components.TrophyDisplay
import com.github.teocci.sudoku.ui.theme.DifficultyEasy
import com.github.teocci.sudoku.ui.theme.DifficultyExpert
import com.github.teocci.sudoku.ui.theme.DifficultyHard
import com.github.teocci.sudoku.ui.theme.DifficultyMedium
import com.github.teocci.sudoku.ui.theme.SudokuPuzzleMasterTheme
import com.github.teocci.sudoku.ui.theme.SudokuTheme
import kotlinx.coroutines.flow.collectLatest
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

/**
 * Daily challenges screen with calendar and progress tracking.
 *
 * @param viewModel The daily challenges ViewModel
 * @param autoSelectNext Whether to automatically select the next unsolved challenge
 * @param onNavigateToGame Called when starting a daily challenge
 */
@Composable
fun DailyChallengesScreen(
    viewModel: DailyChallengesViewModel = viewModel(),
    autoSelectNext: Boolean = false,
    onNavigateToGame: (LocalDate, Difficulty) -> Unit = { _, _ -> }
) {
    val uiState by viewModel.uiState.collectAsState()

    // Handle navigation events
    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collectLatest { event ->
            when (event) {
                is DailyChallengesNavigationEvent.NavigateToDailyGame -> {
                    onNavigateToGame(event.date, event.difficulty)
                }
            }
        }
    }

    // Auto-select next unsolved challenge if requested
    LaunchedEffect(autoSelectNext) {
        if (autoSelectNext) {
            viewModel.selectNextUnsolvedChallenge()
        }
    }

    DailyChallengesContent(
        uiState = uiState,
        onPreviousMonth = viewModel::previousMonth,
        onNextMonth = viewModel::nextMonth,
        onDateClick = viewModel::selectDate,
        onPlayClick = viewModel::playSelectedDay,
        onPlayTodayClick = viewModel::playToday
    )
}

/**
 * Daily challenges content (stateless for preview).
 */
@Composable
private fun DailyChallengesContent(
    uiState: DailyChallengesUiState,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onDateClick: (LocalDate) -> Unit,
    onPlayClick: () -> Unit,
    onPlayTodayClick: () -> Unit
) {
    val colors = SudokuTheme.colors
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Header
        Text(
            text = "Daily Challenges",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = colors.numberFixed
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Calendar view
        CalendarView(
            yearMonth = uiState.currentYearMonth,
            calendarDays = uiState.calendarDays,
            selectedDate = uiState.selectedDate,
            onDateClick = onDateClick,
            onPreviousMonth = onPreviousMonth,
            onNextMonth = onNextMonth,
            canNavigateNext = uiState.currentYearMonth.isBefore(YearMonth.from(uiState.today))
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Selected day info card
        SelectedDayCard(
            selectedDate = uiState.selectedDate,
            isCompleted = uiState.selectedDate in uiState.completedDays,
            isToday = uiState.selectedDate == uiState.today,
            onPlayClick = onPlayClick
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Streak and progress row
        StreakAndProgressRow(
            currentStreak = uiState.currentStreak,
            monthlyProgress = uiState.monthlyProgress
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Today's challenge button (if not already selected)
        if (uiState.selectedDate != uiState.today) {
            TodaysChallengeButton(onClick = onPlayTodayClick)
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Trophy display
        if (uiState.trophies.isNotEmpty()) {
            TrophyDisplay(
                trophies = uiState.trophies
            )
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Bottom padding for navigation bar
        Spacer(modifier = Modifier.height(80.dp))
    }
}

/**
 * Streak and monthly progress row.
 */
@Composable
private fun StreakAndProgressRow(
    currentStreak: Int,
    monthlyProgress: MonthlyProgress,
    modifier: Modifier = Modifier
) {
    val colors = SudokuTheme.colors

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Streak card
        Card(
            modifier = Modifier.weight(1f),
            colors = CardDefaults.cardColors(
                containerColor = colors.controlButtonBackground
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(colors.streakFire.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Whatshot,
                        contentDescription = "Streak",
                        tint = colors.streakFire,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column(
                    modifier = Modifier.padding(start = 12.dp)
                ) {
                    Text(
                        text = "$currentStreak",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.numberFixed
                    )
                    Text(
                        text = "Day Streak",
                        fontSize = 12.sp,
                        color = colors.controlButtonIcon
                    )
                }
            }
        }

        // Monthly progress card
        Card(
            modifier = Modifier.weight(1f),
            colors = CardDefaults.cardColors(
                containerColor = colors.controlButtonBackground
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "This Month",
                        fontSize = 12.sp,
                        color = colors.controlButtonIcon
                    )
                    Text(
                        text = monthlyProgress.displayText,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = colors.numberFixed
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                LinearProgressIndicator(
                    progress = { monthlyProgress.percentage },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = colors.calendarDayCompleted,
                    trackColor = colors.controlButtonIcon.copy(alpha = 0.2f),
                    strokeCap = StrokeCap.Round
                )
            }
        }
    }
}

/**
 * Selected day info card with play button.
 */
@Composable
private fun SelectedDayCard(
    selectedDate: LocalDate,
    isCompleted: Boolean,
    isToday: Boolean,
    onPlayClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = SudokuTheme.colors
    val dateFormatter = DateTimeFormatter.ofPattern("EEEE, MMMM d")

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
                .padding(20.dp)
        ) {
            // Date header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (isToday) "Today's Challenge" else "Challenge",
                        fontSize = 14.sp,
                        color = colors.controlButtonIcon
                    )
                    Text(
                        text = selectedDate.format(dateFormatter),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.numberFixed
                    )
                }

                // Status indicator
                if (isCompleted) {
                    Box(
                        modifier = Modifier
                            .background(
                                colors.calendarDayCompleted.copy(alpha = 0.2f),
                                RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Completed",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = colors.calendarDayCompleted
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Difficulty info
            val difficulty = getDifficultyForDate(selectedDate)
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(getDifficultyColor(difficulty), CircleShape)
                )
                Text(
                    text = difficulty.displayName,
                    fontSize = 14.sp,
                    color = colors.controlButtonIcon,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Play button
            Button(
                onClick = onPlayClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isCompleted) {
                        colors.controlButtonIcon.copy(alpha = 0.3f)
                    } else {
                        MaterialTheme.colorScheme.primary
                    }
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = if (isCompleted) "Play Again" else "Play",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}

/**
 * Today's challenge quick access button.
 */
@Composable
private fun TodaysChallengeButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = SudokuTheme.colors

    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Icon(
            imageVector = Icons.Default.PlayArrow,
            contentDescription = null,
            modifier = Modifier.size(28.dp)
        )
        Text(
            text = "Play Today's Challenge",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(start = 12.dp)
        )
    }
}

/**
 * Get difficulty for a specific date (matches ViewModel logic).
 */
private fun getDifficultyForDate(date: LocalDate): Difficulty {
    return when (date.dayOfWeek) {
        java.time.DayOfWeek.MONDAY, java.time.DayOfWeek.TUESDAY -> Difficulty.EASY
        java.time.DayOfWeek.WEDNESDAY, java.time.DayOfWeek.THURSDAY -> Difficulty.MEDIUM
        java.time.DayOfWeek.FRIDAY, java.time.DayOfWeek.SATURDAY -> Difficulty.HARD
        java.time.DayOfWeek.SUNDAY -> Difficulty.EXPERT
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

// ==================== PREVIEWS ====================

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun PreviewDailyChallengesScreen() {
    SudokuPuzzleMasterTheme(darkTheme = true) {
        val today = LocalDate.now()
        val yearMonth = YearMonth.from(today)

        DailyChallengesContent(
            uiState = DailyChallengesUiState(
                currentYearMonth = yearMonth,
                selectedDate = today,
                today = today,
                calendarDays = generatePreviewCalendarDays(yearMonth, today),
                completedDays = generatePreviewCompletedDays(today),
                currentStreak = 5,
                monthlyProgress = MonthlyProgress(
                    completed = 12,
                    total = 31,
                    possible = today.dayOfMonth
                ),
                trophies = listOf(
                    Trophy(
                        id = "week_streak",
                        name = "Week Warrior",
                        description = "Complete 7 days in a row",
                        isUnlocked = true,
                        progress = 7,
                        maxProgress = 7
                    ),
                    Trophy(
                        id = "month_master",
                        name = "Month Master",
                        description = "Complete all days in a month",
                        isUnlocked = false,
                        progress = 12,
                        maxProgress = 31
                    )
                )
            ),
            onPreviousMonth = {},
            onNextMonth = {},
            onDateClick = {},
            onPlayClick = {},
            onPlayTodayClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewStreakAndProgressRow() {
    SudokuPuzzleMasterTheme(darkTheme = true) {
        StreakAndProgressRow(
            currentStreak = 5,
            monthlyProgress = MonthlyProgress(
                completed = 12,
                total = 31,
                possible = 15
            ),
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewSelectedDayCard() {
    SudokuPuzzleMasterTheme(darkTheme = true) {
        SelectedDayCard(
            selectedDate = LocalDate.now(),
            isCompleted = false,
            isToday = true,
            onPlayClick = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewSelectedDayCard_Completed() {
    SudokuPuzzleMasterTheme(darkTheme = true) {
        SelectedDayCard(
            selectedDate = LocalDate.now().minusDays(1),
            isCompleted = true,
            isToday = false,
            onPlayClick = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}

/**
 * Generate preview calendar days.
 */
private fun generatePreviewCalendarDays(yearMonth: YearMonth, today: LocalDate): List<CalendarDay> {
    val days = mutableListOf<CalendarDay>()
    val firstOfMonth = yearMonth.atDay(1)
    val firstDisplayDay = firstOfMonth.minusDays(firstOfMonth.dayOfWeek.value.toLong() % 7)
    val completedDays = generatePreviewCompletedDays(today)

    var currentDay = firstDisplayDay
    repeat(42) {
        val isInCurrentMonth = YearMonth.from(currentDay) == yearMonth
        val isToday = currentDay == today
        val isFuture = currentDay.isAfter(today)
        val isCompleted = currentDay in completedDays

        days.add(
            CalendarDay(
                date = currentDay,
                dayOfMonth = currentDay.dayOfMonth,
                isInCurrentMonth = isInCurrentMonth,
                isToday = isToday,
                isFuture = isFuture,
                isCompleted = isCompleted,
                difficulty = if (!isFuture) getDifficultyForDate(currentDay) else null
            )
        )

        currentDay = currentDay.plusDays(1)
    }

    return days
}

/**
 * Generate preview completed days.
 */
private fun generatePreviewCompletedDays(today: LocalDate): Set<LocalDate> {
    val completed = mutableSetOf<LocalDate>()
    for (i in 1..15) {
        val day = today.minusDays(i.toLong())
        if (i % 2 == 0 || i % 3 == 0) {
            completed.add(day)
        }
    }
    return completed
}
