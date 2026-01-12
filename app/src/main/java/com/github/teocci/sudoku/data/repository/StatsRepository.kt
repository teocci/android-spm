package com.github.teocci.sudoku.data.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.github.teocci.sudoku.domain.model.Difficulty
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

/**
 * Repository for user statistics and game history.
 * Persists all statistical data to SharedPreferences.
 */
class StatsRepository(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    // Observable overall stats
    private val _overallStats = MutableStateFlow(loadOverallStats())
    val overallStats: StateFlow<OverallStats> = _overallStats.asStateFlow()

    /**
     * Load overall statistics from SharedPreferences.
     */
    private fun loadOverallStats(): OverallStats {
        return OverallStats(
            gamesPlayed = prefs.getInt(KEY_GAMES_PLAYED, 0),
            gamesWon = prefs.getInt(KEY_GAMES_WON, 0),
            currentStreak = prefs.getInt(KEY_CURRENT_STREAK, 0),
            bestStreak = prefs.getInt(KEY_BEST_STREAK, 0),
            totalTime = prefs.getLong(KEY_TOTAL_TIME, 0L),
            bestTime = prefs.getLong(KEY_BEST_TIME, Long.MAX_VALUE).let {
                if (it == Long.MAX_VALUE) 0L else it
            },
            totalScore = prefs.getInt(KEY_TOTAL_SCORE, 0),
            perfectGames = prefs.getInt(KEY_PERFECT_GAMES, 0),
            hintsUsed = prefs.getInt(KEY_HINTS_USED, 0),
            gamesWithoutHints = prefs.getInt(KEY_GAMES_WITHOUT_HINTS, 0)
        )
    }

    /**
     * Record a completed game.
     */
    suspend fun recordGameCompleted(
        difficulty: Difficulty,
        time: Long,
        score: Int,
        mistakes: Int,
        hintsUsed: Int
    ) = withContext(Dispatchers.IO) {
        // Update overall stats
        val currentStats = _overallStats.value
        val isPerfect = mistakes == 0 && hintsUsed == 0

        val newStreak = currentStats.currentStreak + 1
        val newBestStreak = maxOf(currentStats.bestStreak, newStreak)
        val newBestTime = if (currentStats.bestTime == 0L) {
            time
        } else {
            minOf(currentStats.bestTime, time)
        }

        val gamesWithoutHintsIncrement = if (hintsUsed == 0) 1 else 0

        val newStats = OverallStats(
            gamesPlayed = currentStats.gamesPlayed + 1,
            gamesWon = currentStats.gamesWon + 1,
            currentStreak = newStreak,
            bestStreak = newBestStreak,
            totalTime = currentStats.totalTime + time,
            bestTime = newBestTime,
            totalScore = currentStats.totalScore + score,
            perfectGames = if (isPerfect) currentStats.perfectGames + 1 else currentStats.perfectGames,
            hintsUsed = currentStats.hintsUsed + hintsUsed,
            gamesWithoutHints = currentStats.gamesWithoutHints + gamesWithoutHintsIncrement
        )

        _overallStats.value = newStats
        saveOverallStats(newStats)

        // Update difficulty-specific stats
        updateDifficultyStats(difficulty, time, score, won = true)
    }

    /**
     * Record a failed game (3 mistakes).
     */
    suspend fun recordGameFailed(
        difficulty: Difficulty,
        time: Long,
        score: Int,
        hintsUsed: Int
    ) = withContext(Dispatchers.IO) {
        val currentStats = _overallStats.value

        val newStats = OverallStats(
            gamesPlayed = currentStats.gamesPlayed + 1,
            gamesWon = currentStats.gamesWon,
            currentStreak = 0, // Reset streak on failure
            bestStreak = currentStats.bestStreak,
            totalTime = currentStats.totalTime + time,
            bestTime = currentStats.bestTime,
            totalScore = currentStats.totalScore + score,
            perfectGames = currentStats.perfectGames,
            hintsUsed = currentStats.hintsUsed + hintsUsed
        )

        _overallStats.value = newStats
        saveOverallStats(newStats)

        // Update difficulty-specific stats
        updateDifficultyStats(difficulty, time, score, won = false)
    }

    /**
     * Save overall stats to SharedPreferences.
     */
    private fun saveOverallStats(stats: OverallStats) {
        prefs.edit {
            putInt(KEY_GAMES_PLAYED, stats.gamesPlayed)
            putInt(KEY_GAMES_WON, stats.gamesWon)
            putInt(KEY_CURRENT_STREAK, stats.currentStreak)
            putInt(KEY_BEST_STREAK, stats.bestStreak)
            putLong(KEY_TOTAL_TIME, stats.totalTime)
            putLong(KEY_BEST_TIME, stats.bestTime)
            putInt(KEY_TOTAL_SCORE, stats.totalScore)
            putInt(KEY_PERFECT_GAMES, stats.perfectGames)
            putInt(KEY_HINTS_USED, stats.hintsUsed)
            putInt(KEY_GAMES_WITHOUT_HINTS, stats.gamesWithoutHints)
        }
    }

    /**
     * Update difficulty-specific statistics.
     */
    private fun updateDifficultyStats(
        difficulty: Difficulty,
        time: Long,
        score: Int,
        won: Boolean
    ) {
        val prefix = "difficulty_${difficulty.name.lowercase()}_"

        val gamesPlayed = prefs.getInt("${prefix}games_played", 0) + 1
        val gamesWon = prefs.getInt("${prefix}games_won", 0) + if (won) 1 else 0
        val totalTime = prefs.getLong("${prefix}total_time", 0L) + time
        val currentBest = prefs.getLong("${prefix}best_time", Long.MAX_VALUE)
        val bestTime = if (won && time < currentBest) time else currentBest
        val totalScore = prefs.getInt("${prefix}total_score", 0) + score

        prefs.edit {
            putInt("${prefix}games_played", gamesPlayed)
            putInt("${prefix}games_won", gamesWon)
            putLong("${prefix}total_time", totalTime)
            putLong("${prefix}best_time", bestTime)
            putInt("${prefix}total_score", totalScore)
        }
    }

    /**
     * Get statistics for a specific difficulty.
     */
    fun getDifficultyStats(difficulty: Difficulty): DifficultyStats {
        val prefix = "difficulty_${difficulty.name.lowercase()}_"

        val gamesPlayed = prefs.getInt("${prefix}games_played", 0)
        val gamesWon = prefs.getInt("${prefix}games_won", 0)
        val totalTime = prefs.getLong("${prefix}total_time", 0L)
        val bestTime = prefs.getLong("${prefix}best_time", Long.MAX_VALUE).let {
            if (it == Long.MAX_VALUE) 0L else it
        }
        val totalScore = prefs.getInt("${prefix}total_score", 0)

        return DifficultyStats(
            difficulty = difficulty,
            gamesPlayed = gamesPlayed,
            gamesWon = gamesWon,
            averageTime = if (gamesWon > 0) totalTime / gamesWon else 0L,
            bestTime = bestTime,
            totalScore = totalScore
        )
    }

    /**
     * Get all difficulty stats.
     */
    fun getAllDifficultyStats(): Map<Difficulty, DifficultyStats> {
        return Difficulty.entries.associateWith { getDifficultyStats(it) }
    }

    /**
     * Get average time across all games.
     */
    fun getAverageTime(): Long {
        val stats = _overallStats.value
        return if (stats.gamesWon > 0) {
            stats.totalTime / stats.gamesWon
        } else {
            0L
        }
    }

    /**
     * Get win rate as percentage.
     */
    fun getWinRate(): Float {
        val stats = _overallStats.value
        return if (stats.gamesPlayed > 0) {
            (stats.gamesWon.toFloat() / stats.gamesPlayed) * 100
        } else {
            0f
        }
    }

    /**
     * Reset all statistics.
     */
    fun resetAllStats() {
        prefs.edit { clear() }
        _overallStats.value = OverallStats()
    }

    companion object {
        private const val PREFS_NAME = "stats_repository"

        // Overall stat keys
        private const val KEY_GAMES_PLAYED = "games_played"
        private const val KEY_GAMES_WON = "games_won"
        private const val KEY_CURRENT_STREAK = "current_streak"
        private const val KEY_BEST_STREAK = "best_streak"
        private const val KEY_TOTAL_TIME = "total_time"
        private const val KEY_BEST_TIME = "best_time"
        private const val KEY_TOTAL_SCORE = "total_score"
        private const val KEY_PERFECT_GAMES = "perfect_games"
        private const val KEY_HINTS_USED = "hints_used"
        private const val KEY_GAMES_WITHOUT_HINTS = "games_without_hints"

        @Volatile
        private var INSTANCE: StatsRepository? = null

        /**
         * Get singleton instance.
         */
        fun getInstance(context: Context): StatsRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: StatsRepository(context.applicationContext).also {
                    INSTANCE = it
                }
            }
        }
    }
}

/**
 * Overall statistics across all games.
 */
data class OverallStats(
    val gamesPlayed: Int = 0,
    val gamesWon: Int = 0,
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val totalTime: Long = 0L,
    val bestTime: Long = 0L,
    val totalScore: Int = 0,
    val perfectGames: Int = 0,
    val hintsUsed: Int = 0,
    val gamesWithoutHints: Int = 0  // For Self-Sufficient achievement
) {
    /**
     * Win rate as percentage.
     */
    val winRate: Float
        get() = if (gamesPlayed > 0) {
            (gamesWon.toFloat() / gamesPlayed) * 100
        } else {
            0f
        }

    /**
     * Average time per won game.
     */
    val averageTime: Long
        get() = if (gamesWon > 0) totalTime / gamesWon else 0L
}

/**
 * Statistics for a specific difficulty level.
 */
data class DifficultyStats(
    val difficulty: Difficulty,
    val gamesPlayed: Int = 0,
    val gamesWon: Int = 0,
    val averageTime: Long = 0L,
    val bestTime: Long = 0L,
    val totalScore: Int = 0
) {
    /**
     * Win rate as percentage.
     */
    val winRate: Float
        get() = if (gamesPlayed > 0) {
            (gamesWon.toFloat() / gamesPlayed) * 100
        } else {
            0f
        }
}
