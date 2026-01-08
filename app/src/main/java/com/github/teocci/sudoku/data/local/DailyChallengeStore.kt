package com.github.teocci.sudoku.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.github.teocci.sudoku.domain.model.Difficulty
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Storage for daily challenge completion data and progress.
 * Tracks which daily challenges have been completed and associated stats.
 */
class DailyChallengeStore(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    // Observable completed days
    private val _completedDays = MutableStateFlow(loadCompletedDays())
    val completedDays: StateFlow<Set<LocalDate>> = _completedDays.asStateFlow()

    // Observable current streak
    private val _currentStreak = MutableStateFlow(calculateCurrentStreak())
    val currentStreak: StateFlow<Int> = _currentStreak.asStateFlow()

    /**
     * Load completed days from SharedPreferences.
     */
    private fun loadCompletedDays(): Set<LocalDate> {
        val daysString = prefs.getString(KEY_COMPLETED_DAYS, "") ?: ""
        if (daysString.isEmpty()) return emptySet()

        return daysString.split(",")
            .mapNotNull { dateStr ->
                try {
                    LocalDate.parse(dateStr, dateFormatter)
                } catch (e: Exception) {
                    null
                }
            }
            .toSet()
    }

    /**
     * Save completed days to SharedPreferences.
     */
    private fun saveCompletedDays(days: Set<LocalDate>) {
        val daysString = days.joinToString(",") { it.format(dateFormatter) }
        prefs.edit { putString(KEY_COMPLETED_DAYS, daysString) }
    }

    /**
     * Mark a day as completed.
     */
    fun markDayCompleted(date: LocalDate, time: Long, score: Int, mistakes: Int) {
        val currentDays = _completedDays.value.toMutableSet()
        currentDays.add(date)
        _completedDays.value = currentDays
        saveCompletedDays(currentDays)

        // Save day-specific stats
        saveDayStats(date, time, score, mistakes)

        // Update streak
        _currentStreak.value = calculateCurrentStreak()

        // Update best streak if needed
        val bestStreak = prefs.getInt(KEY_BEST_STREAK, 0)
        if (_currentStreak.value > bestStreak) {
            prefs.edit { putInt(KEY_BEST_STREAK, _currentStreak.value) }
        }
    }

    /**
     * Check if a day is completed.
     */
    fun isDayCompleted(date: LocalDate): Boolean {
        return date in _completedDays.value
    }

    /**
     * Get stats for a specific day.
     */
    fun getDayStats(date: LocalDate): DayStats? {
        val dateKey = date.format(dateFormatter)
        val time = prefs.getLong("${KEY_DAY_TIME}_$dateKey", -1L)
        if (time < 0) return null

        return DayStats(
            date = date,
            time = time,
            score = prefs.getInt("${KEY_DAY_SCORE}_$dateKey", 0),
            mistakes = prefs.getInt("${KEY_DAY_MISTAKES}_$dateKey", 0),
            difficulty = Difficulty.entries.getOrNull(
                prefs.getInt("${KEY_DAY_DIFFICULTY}_$dateKey", Difficulty.MEDIUM.ordinal)
            ) ?: Difficulty.MEDIUM
        )
    }

    /**
     * Save stats for a specific day.
     */
    private fun saveDayStats(date: LocalDate, time: Long, score: Int, mistakes: Int) {
        val dateKey = date.format(dateFormatter)
        val difficulty = getDifficultyForDate(date)

        prefs.edit {
            putLong("${KEY_DAY_TIME}_$dateKey", time)
            putInt("${KEY_DAY_SCORE}_$dateKey", score)
            putInt("${KEY_DAY_MISTAKES}_$dateKey", mistakes)
            putInt("${KEY_DAY_DIFFICULTY}_$dateKey", difficulty.ordinal)
        }
    }

    /**
     * Calculate current streak from completed days.
     */
    private fun calculateCurrentStreak(): Int {
        val today = LocalDate.now()
        val completedDays = _completedDays.value

        var streak = 0
        var checkDate = today

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
     * Get best streak ever.
     */
    fun getBestStreak(): Int {
        return prefs.getInt(KEY_BEST_STREAK, 0)
    }

    /**
     * Get monthly completion count.
     */
    fun getMonthlyCompletion(year: Int, month: Int): Int {
        return _completedDays.value.count { date ->
            date.year == year && date.monthValue == month
        }
    }

    /**
     * Get all completed days for a specific month.
     */
    fun getCompletedDaysForMonth(year: Int, month: Int): List<LocalDate> {
        return _completedDays.value.filter { date ->
            date.year == year && date.monthValue == month
        }.sortedBy { it.dayOfMonth }
    }

    /**
     * Get total completed challenges count.
     */
    fun getTotalCompleted(): Int {
        return _completedDays.value.size
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
     * Clear all daily challenge data.
     */
    fun clearAll() {
        prefs.edit { clear() }
        _completedDays.value = emptySet()
        _currentStreak.value = 0
    }

    companion object {
        private const val PREFS_NAME = "daily_challenge_store"

        private const val KEY_COMPLETED_DAYS = "completed_days"
        private const val KEY_BEST_STREAK = "best_streak"
        private const val KEY_DAY_TIME = "day_time"
        private const val KEY_DAY_SCORE = "day_score"
        private const val KEY_DAY_MISTAKES = "day_mistakes"
        private const val KEY_DAY_DIFFICULTY = "day_difficulty"

        private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

        @Volatile
        private var INSTANCE: DailyChallengeStore? = null

        /**
         * Get singleton instance.
         */
        fun getInstance(context: Context): DailyChallengeStore {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: DailyChallengeStore(context.applicationContext).also {
                    INSTANCE = it
                }
            }
        }
    }
}

/**
 * Stats for a completed daily challenge.
 */
data class DayStats(
    val date: LocalDate,
    val time: Long,
    val score: Int,
    val mistakes: Int,
    val difficulty: Difficulty
)
