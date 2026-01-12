package com.github.teocci.sudoku.data.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.github.teocci.sudoku.data.local.DailyChallengeStore
import com.github.teocci.sudoku.domain.model.Difficulty
import com.github.teocci.sudoku.presentation.profile.Achievement
import com.github.teocci.sudoku.presentation.profile.AchievementIcon
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Repository for managing achievements and tracking progress.
 * Persists achievement data to SharedPreferences.
 */
class AchievementRepository(
    context: Context,
    private val statsRepository: StatsRepository,
    private val dailyChallengeStore: DailyChallengeStore
) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    // Observable achievements
    private val _achievements = MutableStateFlow<List<AchievementData>>(emptyList())
    val achievements: StateFlow<List<AchievementData>> = _achievements.asStateFlow()

    // Achievement definitions (const, never change)
    private val achievementDefinitions: List<AchievementDefinition> = listOf(
        AchievementDefinition(
            id = ID_FIRST_WIN,
            name = "First Victory",
            description = "Win your first game",
            iconType = AchievementIcon.TROPHY,
            maxProgress = 1,
            checkCondition = { overallStats, _, _ ->
                minOf(overallStats.gamesWon, 1)
            }
        ),
        AchievementDefinition(
            id = ID_SPEED_DEMON,
            name = "Speed Demon",
            description = "Complete a game in under 5 minutes",
            iconType = AchievementIcon.TIMER,
            maxProgress = 1,
            checkCondition = { overallStats, _, _ ->
                if (overallStats.bestTime > 0 && overallStats.bestTime < 300) 1 else 0
            }
        ),
        AchievementDefinition(
            id = ID_WEEK_WARRIOR,
            name = "Week Warrior",
            description = "Complete 7 daily challenges in a row",
            iconType = AchievementIcon.STREAK,
            maxProgress = 1,
            checkCondition = { _, dailyStore, _ ->
                if (dailyStore.getBestStreak() >= 7) 1 else 0
            }
        ),
        AchievementDefinition(
            id = ID_PERFECTIONIST,
            name = "Perfectionist",
            description = "Complete 10 perfect games (no mistakes or hints)",
            iconType = AchievementIcon.STAR,
            maxProgress = 10,
            checkCondition = { overallStats, _, _ ->
                overallStats.perfectGames
            }
        ),
        AchievementDefinition(
            id = ID_CENTURY,
            name = "Century",
            description = "Win 100 games",
            iconType = AchievementIcon.TROPHY,
            maxProgress = 100,
            checkCondition = { overallStats, _, _ ->
                overallStats.gamesWon
            }
        ),
        AchievementDefinition(
            id = ID_EXPERT_MASTER,
            name = "Expert Master",
            description = "Complete 25 expert difficulty games",
            iconType = AchievementIcon.EXPERT,
            maxProgress = 25,
            checkCondition = { _, _, expertStats ->
                expertStats.gamesWon
            }
        ),
        AchievementDefinition(
            id = ID_SELF_SUFFICIENT,
            name = "Self-Sufficient",
            description = "Complete 50 games without using hints",
            iconType = AchievementIcon.BRAIN,
            maxProgress = 50,
            checkCondition = { overallStats, _, _ ->
                overallStats.gamesWithoutHints
            }
        ),
        AchievementDefinition(
            id = ID_MONTH_MASTER,
            name = "Month Master",
            description = "Complete 31 daily challenges in a month",
            iconType = AchievementIcon.CALENDAR,
            maxProgress = 31,
            checkCondition = { _, dailyStore, _ ->
                val now = LocalDate.now()
                dailyStore.getMonthlyCompletion(now.year, now.monthValue)
            }
        )
    )

    init {
        _achievements.value = loadAllAchievementData()
    }

    /**
     * Check all achievements against current stats and update if conditions met.
     */
    suspend fun checkAndUpdateAchievements() = withContext(Dispatchers.IO) {
        // Get current stats
        val overallStats = statsRepository.overallStats.value
        val expertStats = statsRepository.getDifficultyStats(Difficulty.EXPERT)

        // Check each achievement
        achievementDefinitions.forEach { definition ->
            val progress = definition.checkCondition(overallStats, dailyChallengeStore, expertStats)
            updateAchievementProgress(definition.id, progress, definition.maxProgress)
        }

        // Reload achievements
        _achievements.value = loadAllAchievementData()
    }

    /**
     * Update achievement progress and unlock if conditions met.
     */
    private fun updateAchievementProgress(id: String, progress: Int, maxProgress: Int) {
        val wasUnlocked = prefs.getBoolean(keyUnlocked(id), false)
        val isNowUnlocked = progress >= maxProgress

        prefs.edit {
            putInt(keyProgress(id), progress)
            if (!wasUnlocked && isNowUnlocked) {
                putBoolean(keyUnlocked(id), true)
                putLong(keyTimestamp(id), System.currentTimeMillis())
            }
        }
    }

    /**
     * Load all achievement data from SharedPreferences.
     */
    private fun loadAllAchievementData(): List<AchievementData> {
        return achievementDefinitions.map { definition ->
            AchievementData(
                id = definition.id,
                isUnlocked = prefs.getBoolean(keyUnlocked(definition.id), false),
                unlockedTimestamp = prefs.getLong(keyTimestamp(definition.id), 0).let {
                    if (it > 0) it else null
                },
                currentProgress = prefs.getInt(keyProgress(definition.id), 0)
            )
        }
    }

    /**
     * Convert AchievementData to UI Achievement model.
     */
    fun toUIModel(data: AchievementData): Achievement {
        val definition = achievementDefinitions.first { it.id == data.id }

        return Achievement(
            id = data.id,
            name = definition.name,
            description = definition.description,
            iconType = definition.iconType,
            isUnlocked = data.isUnlocked,
            unlockedDate = data.unlockedTimestamp?.let { formatUnlockDate(it) },
            progress = data.currentProgress,
            maxProgress = definition.maxProgress
        )
    }

    /**
     * Format unlock date for display.
     */
    private fun formatUnlockDate(timestamp: Long): String {
        val date = Instant.ofEpochMilli(timestamp)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
        val formatter = DateTimeFormatter.ofPattern("MMM d, yyyy")
        return date.format(formatter)
    }

    /**
     * Reset all achievements (for testing or user request).
     */
    fun resetAchievements() {
        prefs.edit { clear() }
        _achievements.value = loadAllAchievementData()
    }

    // SharedPreferences key helpers
    private fun keyUnlocked(id: String) = "achievement_${id}_unlocked"
    private fun keyTimestamp(id: String) = "achievement_${id}_timestamp"
    private fun keyProgress(id: String) = "achievement_${id}_progress"

    companion object {
        private const val PREFS_NAME = "achievement_repository"

        // Achievement IDs
        const val ID_FIRST_WIN = "first_win"
        const val ID_SPEED_DEMON = "speed_demon"
        const val ID_WEEK_WARRIOR = "week_warrior"
        const val ID_PERFECTIONIST = "perfectionist"
        const val ID_CENTURY = "century"
        const val ID_EXPERT_MASTER = "expert_master"
        const val ID_SELF_SUFFICIENT = "self_sufficient"
        const val ID_MONTH_MASTER = "month_master"
    }
}

/**
 * Achievement data persisted to SharedPreferences.
 */
data class AchievementData(
    val id: String,
    val isUnlocked: Boolean,
    val unlockedTimestamp: Long?,  // epoch millis
    val currentProgress: Int
)

/**
 * Achievement definition with unlock condition.
 */
data class AchievementDefinition(
    val id: String,
    val name: String,
    val description: String,
    val iconType: AchievementIcon,
    val maxProgress: Int,
    val checkCondition: (OverallStats, DailyChallengeStore, DifficultyStats) -> Int
)
