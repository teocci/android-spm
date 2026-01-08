package com.github.teocci.sudoku.presentation.daily

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.teocci.sudoku.domain.model.Difficulty
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.TemporalAdjusters

/**
 * ViewModel for the daily challenges screen.
 * Manages calendar state and daily challenge progress.
 */
class DailyChallengesViewModel : ViewModel() {

    // UI State
    private val _uiState = MutableStateFlow(DailyChallengesUiState())
    val uiState: StateFlow<DailyChallengesUiState> = _uiState.asStateFlow()

    // Navigation events
    private val _navigationEvent = MutableSharedFlow<DailyChallengesNavigationEvent>()
    val navigationEvent = _navigationEvent.asSharedFlow()

    init {
        loadCurrentMonth()
        loadCompletedDays()
        loadTrophies()
    }

    /**
     * Load data for the current month.
     */
    private fun loadCurrentMonth() {
        val today = LocalDate.now()
        val yearMonth = YearMonth.from(today)

        _uiState.update { state ->
            state.copy(
                currentYearMonth = yearMonth,
                selectedDate = today,
                today = today,
                calendarDays = generateCalendarDays(yearMonth)
            )
        }
    }

    /**
     * Load completed daily challenges.
     */
    private fun loadCompletedDays() {
        // TODO: Load from repository
        viewModelScope.launch {
            // Simulate some completed days for demo
            val today = LocalDate.now()
            val completed = mutableSetOf<LocalDate>()

            // Mark some random past days as completed
            for (i in 1..15) {
                val day = today.minusDays(i.toLong())
                if (i % 2 == 0 || i % 3 == 0) {
                    completed.add(day)
                }
            }

            _uiState.update { state ->
                state.copy(
                    completedDays = completed,
                    currentStreak = calculateStreak(completed, today),
                    monthlyProgress = calculateMonthlyProgress(completed, YearMonth.from(today))
                )
            }
        }
    }

    /**
     * Load trophy/achievement data.
     */
    private fun loadTrophies() {
        // TODO: Load from repository
        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(
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
                            progress = 15,
                            maxProgress = 31
                        ),
                        Trophy(
                            id = "speed_demon",
                            name = "Speed Demon",
                            description = "Complete a puzzle in under 3 minutes",
                            isUnlocked = true,
                            progress = 1,
                            maxProgress = 1
                        )
                    )
                )
            }
        }
    }

    /**
     * Navigate to previous month.
     */
    fun previousMonth() {
        _uiState.update { state ->
            val newYearMonth = state.currentYearMonth.minusMonths(1)
            state.copy(
                currentYearMonth = newYearMonth,
                calendarDays = generateCalendarDays(newYearMonth),
                monthlyProgress = calculateMonthlyProgress(state.completedDays, newYearMonth)
            )
        }
    }

    /**
     * Navigate to next month.
     */
    fun nextMonth() {
        val today = LocalDate.now()
        val currentMonth = _uiState.value.currentYearMonth

        // Don't allow navigating past current month
        if (currentMonth.isBefore(YearMonth.from(today))) {
            _uiState.update { state ->
                val newYearMonth = state.currentYearMonth.plusMonths(1)
                state.copy(
                    currentYearMonth = newYearMonth,
                    calendarDays = generateCalendarDays(newYearMonth),
                    monthlyProgress = calculateMonthlyProgress(state.completedDays, newYearMonth)
                )
            }
        }
    }

    /**
     * Select a date on the calendar.
     */
    fun selectDate(date: LocalDate) {
        val today = LocalDate.now()

        // Can only select today or past dates
        if (!date.isAfter(today)) {
            _uiState.update { it.copy(selectedDate = date) }
        }
    }

    /**
     * Start the daily challenge for the selected date.
     */
    fun playSelectedDay() {
        val selectedDate = _uiState.value.selectedDate
        val today = LocalDate.now()

        // Can only play today or past dates
        if (!selectedDate.isAfter(today)) {
            viewModelScope.launch {
                _navigationEvent.emit(
                    DailyChallengesNavigationEvent.NavigateToDailyGame(
                        date = selectedDate,
                        difficulty = getDifficultyForDate(selectedDate)
                    )
                )
            }
        }
    }

    /**
     * Start today's daily challenge.
     */
    fun playToday() {
        val today = LocalDate.now()
        _uiState.update { it.copy(selectedDate = today) }

        viewModelScope.launch {
            _navigationEvent.emit(
                DailyChallengesNavigationEvent.NavigateToDailyGame(
                    date = today,
                    difficulty = getDifficultyForDate(today)
                )
            )
        }
    }

    /**
     * Get difficulty for a specific date.
     * Rotates through difficulties based on day of week.
     */
    private fun getDifficultyForDate(date: LocalDate): Difficulty {
        return when (date.dayOfWeek) {
            DayOfWeek.MONDAY, DayOfWeek.TUESDAY -> Difficulty.EASY
            DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY -> Difficulty.MEDIUM
            DayOfWeek.FRIDAY, DayOfWeek.SATURDAY -> Difficulty.HARD
            DayOfWeek.SUNDAY -> Difficulty.EXPERT
        }
    }

    /**
     * Generate calendar days for a month (including padding days from adjacent months).
     */
    private fun generateCalendarDays(yearMonth: YearMonth): List<CalendarDay> {
        val days = mutableListOf<CalendarDay>()
        val today = LocalDate.now()
        val completedDays = _uiState.value.completedDays

        // First day of the month
        val firstOfMonth = yearMonth.atDay(1)

        // Find the first day to display (start of week containing first of month)
        val firstDisplayDay = firstOfMonth.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY))

        // Generate 6 weeks of days (42 days)
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
     * Calculate current streak of consecutive completed days.
     */
    private fun calculateStreak(completedDays: Set<LocalDate>, fromDate: LocalDate): Int {
        var streak = 0
        var checkDate = fromDate

        // If today isn't completed, start from yesterday
        if (checkDate !in completedDays) {
            checkDate = checkDate.minusDays(1)
        }

        while (checkDate in completedDays) {
            streak++
            checkDate = checkDate.minusDays(1)
        }

        return streak
    }

    /**
     * Calculate monthly progress (completed days / total days in month).
     */
    private fun calculateMonthlyProgress(completedDays: Set<LocalDate>, yearMonth: YearMonth): MonthlyProgress {
        val today = LocalDate.now()
        val daysInMonth = yearMonth.lengthOfMonth()

        // Count completed days in this month
        val completedInMonth = completedDays.count {
            YearMonth.from(it) == yearMonth
        }

        // Days that could have been completed (up to today if current month)
        val possibleDays = if (yearMonth == YearMonth.from(today)) {
            today.dayOfMonth
        } else if (yearMonth.isBefore(YearMonth.from(today))) {
            daysInMonth
        } else {
            0
        }

        return MonthlyProgress(
            completed = completedInMonth,
            total = daysInMonth,
            possible = possibleDays
        )
    }
}

/**
 * UI state for daily challenges screen.
 */
data class DailyChallengesUiState(
    val currentYearMonth: YearMonth = YearMonth.now(),
    val selectedDate: LocalDate = LocalDate.now(),
    val today: LocalDate = LocalDate.now(),
    val calendarDays: List<CalendarDay> = emptyList(),
    val completedDays: Set<LocalDate> = emptySet(),
    val currentStreak: Int = 0,
    val monthlyProgress: MonthlyProgress = MonthlyProgress(),
    val trophies: List<Trophy> = emptyList(),
    val isLoading: Boolean = false
)

/**
 * Represents a day in the calendar grid.
 */
data class CalendarDay(
    val date: LocalDate,
    val dayOfMonth: Int,
    val isInCurrentMonth: Boolean,
    val isToday: Boolean,
    val isFuture: Boolean,
    val isCompleted: Boolean,
    val difficulty: Difficulty?
)

/**
 * Monthly progress tracking.
 */
data class MonthlyProgress(
    val completed: Int = 0,
    val total: Int = 31,
    val possible: Int = 0
) {
    val percentage: Float
        get() = if (possible > 0) completed.toFloat() / possible else 0f

    val displayText: String
        get() = "$completed/$total"
}

/**
 * Trophy/achievement data.
 */
data class Trophy(
    val id: String,
    val name: String,
    val description: String,
    val isUnlocked: Boolean,
    val progress: Int,
    val maxProgress: Int
) {
    val progressPercentage: Float
        get() = progress.toFloat() / maxProgress
}

/**
 * Navigation events from daily challenges screen.
 */
sealed class DailyChallengesNavigationEvent {
    data class NavigateToDailyGame(
        val date: LocalDate,
        val difficulty: Difficulty
    ) : DailyChallengesNavigationEvent()
}
