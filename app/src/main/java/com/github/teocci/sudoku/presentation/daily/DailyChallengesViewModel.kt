package com.github.teocci.sudoku.presentation.daily

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.teocci.sudoku.data.RepositoryProvider
import com.github.teocci.sudoku.data.local.DailyChallengeStore
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
class DailyChallengesViewModel(
    private val dailyChallengeStore: DailyChallengeStore = RepositoryProvider.getDailyChallengeStore()
) : ViewModel() {

    // UI State
    private val _uiState = MutableStateFlow(DailyChallengesUiState())
    val uiState: StateFlow<DailyChallengesUiState> = _uiState.asStateFlow()

    // Navigation events
    private val _navigationEvent = MutableSharedFlow<DailyChallengesNavigationEvent>()
    val navigationEvent = _navigationEvent.asSharedFlow()

    init {
        // Load initial data synchronously to ensure UI has data on first render
        loadInitialData()

        // Set up reactive observers for future updates
        observeCompletedDaysAndStreak()
        loadTrophies()
    }

    /**
     * Load initial data synchronously before first render.
     * This ensures the calendar displays completion status on first load.
     */
    private fun loadInitialData() {
        val today = LocalDate.now()
        val yearMonth = YearMonth.from(today)

        // Read initial values from StateFlows (already loaded in DailyChallengeStore constructor)
        val initialCompletedDays = dailyChallengeStore.completedDays.value
        val initialStreak = dailyChallengeStore.currentStreak.value

        _uiState.update { state ->
            state.copy(
                currentYearMonth = yearMonth,
                selectedDate = today,
                today = today,
                completedDays = initialCompletedDays,
                currentStreak = initialStreak,
                calendarDays = generateCalendarDaysWithCompletedDays(
                    yearMonth = yearMonth,
                    completedDays = initialCompletedDays
                ),
                monthlyProgress = calculateMonthlyProgress(initialCompletedDays, yearMonth)
            )
        }
    }

    /**
     * Observe completed daily challenges and current streak for reactive updates.
     * This handles updates when challenges are completed during app usage.
     */
    private fun observeCompletedDaysAndStreak() {
        viewModelScope.launch {
            // Observe completed days
            launch {
                dailyChallengeStore.completedDays.collect { completedDates ->
                    _uiState.update { state ->
                        state.copy(
                            completedDays = completedDates,
                            calendarDays = generateCalendarDaysWithCompletedDays(
                                yearMonth = state.currentYearMonth,
                                completedDays = completedDates
                            ),
                            monthlyProgress = calculateMonthlyProgress(
                                completedDates,
                                state.currentYearMonth
                            )
                        )
                    }
                }
            }

            // Observe current streak
            launch {
                dailyChallengeStore.currentStreak.collect { streak ->
                    _uiState.update { state ->
                        state.copy(currentStreak = streak)
                    }
                }
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
                calendarDays = generateCalendarDaysWithCompletedDays(newYearMonth),
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
                    calendarDays = generateCalendarDaysWithCompletedDays(newYearMonth),
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
     * Select next unsolved challenge.
     * Used after collecting rewards to guide user to next challenge.
     */
    fun selectNextUnsolvedChallenge() {
        val today = LocalDate.now()
        val completedDays = _uiState.value.completedDays
        var currentDate = today

        // Find the nearest unsolved challenge (working backwards from today)
        while (currentDate.isAfter(today.minusMonths(1))) {
            if (currentDate !in completedDays && !currentDate.isAfter(today)) {
                _uiState.update { it.copy(selectedDate = currentDate) }
                return
            }
            currentDate = currentDate.minusDays(1)
        }

        // If all recent challenges completed, select today
        _uiState.update { it.copy(selectedDate = today) }
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
     *
     * @param yearMonth The month to generate calendar for
     * @param completedDays Set of completed dates (defaults to current state)
     */
    private fun generateCalendarDaysWithCompletedDays(
        yearMonth: YearMonth,
        completedDays: Set<LocalDate> = _uiState.value.completedDays
    ): List<CalendarDay> {
        val days = mutableListOf<CalendarDay>()
        val today = LocalDate.now()

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
     * Calculate monthly progress (completed days / total days in month).
     */
    private fun calculateMonthlyProgress(
        completedDays: Set<LocalDate>,
        yearMonth: YearMonth
    ): MonthlyProgress {
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
