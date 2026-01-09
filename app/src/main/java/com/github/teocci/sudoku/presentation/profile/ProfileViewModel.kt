package com.github.teocci.sudoku.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.teocci.sudoku.data.RepositoryProvider
import com.github.teocci.sudoku.data.repository.StatsRepository
import com.github.teocci.sudoku.domain.model.Difficulty
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for the profile/statistics screen.
 * Manages user statistics and achievement data.
 */
class ProfileViewModel(
    private val statsRepository: StatsRepository = RepositoryProvider.getStatsRepository()
) : ViewModel() {

    // UI State
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    /**
     * Load all profile data.
     */
    private fun loadProfile() {
        viewModelScope.launch {
            loadStatistics()
            loadDifficultyStats()
            loadAchievements()
            loadRecentGames()
        }
    }

    /**
     * Load overall statistics.
     */
    private fun loadStatistics() {
        viewModelScope.launch {
            statsRepository.overallStats.collect { stats ->
                _uiState.update { state ->
                    state.copy(
                        overallStats = OverallStatistics(
                            gamesPlayed = stats.gamesPlayed,
                            gamesWon = stats.gamesWon,
                            currentStreak = stats.currentStreak,
                            bestStreak = stats.bestStreak,
                            averageTime = if (stats.gamesPlayed > 0) stats.totalTime / stats.gamesPlayed else 0L,
                            bestTime = stats.bestTime,
                            totalPlayTime = stats.totalTime,
                            perfectGames = stats.perfectGames
                        )
                    )
                }
            }
        }
    }

    /**
     * Load per-difficulty statistics.
     */
    private fun loadDifficultyStats() {
        viewModelScope.launch {
            val difficultyStats = Difficulty.entries.associate { difficulty ->
                difficulty to statsRepository.getDifficultyStats(difficulty)
            }

            _uiState.update { state ->
                state.copy(
                    difficultyStats = difficultyStats.mapValues { (_, stats) ->
                        DifficultyStatistics(
                            gamesPlayed = stats.gamesPlayed,
                            gamesWon = stats.gamesWon,
                            averageTime = stats.averageTime,
                            bestTime = stats.bestTime
                        )
                    }
                )
            }
        }
    }

    /**
     * Load achievements/trophies.
     */
    private fun loadAchievements() {
        // TODO: Load from repository
        _uiState.update { state ->
            state.copy(
                achievements = listOf(
                    Achievement(
                        id = "first_win",
                        name = "First Victory",
                        description = "Complete your first puzzle",
                        iconType = AchievementIcon.TROPHY,
                        isUnlocked = true,
                        unlockedDate = "Jan 1, 2026"
                    ),
                    Achievement(
                        id = "speed_demon",
                        name = "Speed Demon",
                        description = "Complete a puzzle in under 3 minutes",
                        iconType = AchievementIcon.TIMER,
                        isUnlocked = true,
                        unlockedDate = "Jan 3, 2026"
                    ),
                    Achievement(
                        id = "week_streak",
                        name = "Week Warrior",
                        description = "Complete puzzles 7 days in a row",
                        iconType = AchievementIcon.STREAK,
                        isUnlocked = true,
                        unlockedDate = "Jan 7, 2026"
                    ),
                    Achievement(
                        id = "perfectionist",
                        name = "Perfectionist",
                        description = "Complete 10 puzzles without mistakes",
                        iconType = AchievementIcon.STAR,
                        isUnlocked = false,
                        progress = 4,
                        maxProgress = 10
                    ),
                    Achievement(
                        id = "century",
                        name = "Century",
                        description = "Complete 100 puzzles",
                        iconType = AchievementIcon.TROPHY,
                        isUnlocked = true,
                        unlockedDate = "Jan 5, 2026"
                    ),
                    Achievement(
                        id = "expert_master",
                        name = "Expert Master",
                        description = "Complete 25 Expert puzzles",
                        iconType = AchievementIcon.EXPERT,
                        isUnlocked = false,
                        progress = 15,
                        maxProgress = 25
                    ),
                    Achievement(
                        id = "no_hints",
                        name = "Self-Sufficient",
                        description = "Complete 50 puzzles without hints",
                        iconType = AchievementIcon.BRAIN,
                        isUnlocked = false,
                        progress = 32,
                        maxProgress = 50
                    ),
                    Achievement(
                        id = "month_master",
                        name = "Month Master",
                        description = "Complete all daily challenges in a month",
                        iconType = AchievementIcon.CALENDAR,
                        isUnlocked = false,
                        progress = 7,
                        maxProgress = 31
                    )
                )
            )
        }
    }

    /**
     * Load recent game history.
     */
    private fun loadRecentGames() {
        // TODO: Load from repository
        _uiState.update { state ->
            state.copy(
                recentGames = listOf(
                    RecentGame(
                        date = "Today",
                        difficulty = Difficulty.MEDIUM,
                        time = 425L,
                        score = 1850,
                        isCompleted = true,
                        mistakes = 1
                    ),
                    RecentGame(
                        date = "Yesterday",
                        difficulty = Difficulty.HARD,
                        time = 680L,
                        score = 2400,
                        isCompleted = true,
                        mistakes = 0
                    ),
                    RecentGame(
                        date = "Jan 5",
                        difficulty = Difficulty.EXPERT,
                        time = 0L,
                        score = 0,
                        isCompleted = false,
                        mistakes = 3
                    ),
                    RecentGame(
                        date = "Jan 4",
                        difficulty = Difficulty.EASY,
                        time = 198L,
                        score = 980,
                        isCompleted = true,
                        mistakes = 0
                    ),
                    RecentGame(
                        date = "Jan 3",
                        difficulty = Difficulty.MEDIUM,
                        time = 512L,
                        score = 1720,
                        isCompleted = true,
                        mistakes = 2
                    )
                )
            )
        }
    }

    /**
     * Refresh all profile data.
     */
    fun refresh() {
        _uiState.update { it.copy(isLoading = true) }
        loadProfile()
        _uiState.update { it.copy(isLoading = false) }
    }

    /**
     * Reset all statistics.
     */
    fun resetStatistics() {
        viewModelScope.launch {
            statsRepository.resetAllStats()
            // Stats will automatically update via the Flow
        }
    }
}

/**
 * UI state for the profile screen.
 */
data class ProfileUiState(
    val overallStats: OverallStatistics = OverallStatistics(),
    val difficultyStats: Map<Difficulty, DifficultyStatistics> = emptyMap(),
    val achievements: List<Achievement> = emptyList(),
    val recentGames: List<RecentGame> = emptyList(),
    val isLoading: Boolean = false
) {
    /**
     * Get unlocked achievements count.
     */
    val unlockedAchievements: Int
        get() = achievements.count { it.isUnlocked }

    /**
     * Get total achievements count.
     */
    val totalAchievements: Int
        get() = achievements.size
}

/**
 * Overall statistics for all games.
 */
data class OverallStatistics(
    val gamesPlayed: Int = 0,
    val gamesWon: Int = 0,
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val averageTime: Long = 0L,
    val bestTime: Long = 0L,
    val totalPlayTime: Long = 0L,
    val perfectGames: Int = 0
) {
    /**
     * Win rate as percentage (0-100).
     */
    val winRate: Float
        get() = if (gamesPlayed > 0) {
            (gamesWon.toFloat() / gamesPlayed) * 100
        } else {
            0f
        }

    /**
     * Win rate formatted as string.
     */
    val winRateFormatted: String
        get() = String.format("%.0f%%", winRate)
}

/**
 * Statistics for a specific difficulty level.
 */
data class DifficultyStatistics(
    val gamesPlayed: Int = 0,
    val gamesWon: Int = 0,
    val averageTime: Long = 0L,
    val bestTime: Long = 0L
) {
    /**
     * Win rate as percentage (0-100).
     */
    val winRate: Float
        get() = if (gamesPlayed > 0) {
            (gamesWon.toFloat() / gamesPlayed) * 100
        } else {
            0f
        }
}

/**
 * Achievement/trophy data.
 */
data class Achievement(
    val id: String,
    val name: String,
    val description: String,
    val iconType: AchievementIcon,
    val isUnlocked: Boolean,
    val unlockedDate: String? = null,
    val progress: Int = 0,
    val maxProgress: Int = 1
) {
    /**
     * Progress percentage for locked achievements.
     */
    val progressPercentage: Float
        get() = if (maxProgress > 0) progress.toFloat() / maxProgress else 0f
}

/**
 * Achievement icon types.
 */
enum class AchievementIcon {
    TROPHY,
    STAR,
    TIMER,
    STREAK,
    BRAIN,
    EXPERT,
    CALENDAR
}

/**
 * Recent game entry.
 */
data class RecentGame(
    val date: String,
    val difficulty: Difficulty,
    val time: Long,
    val score: Int,
    val isCompleted: Boolean,
    val mistakes: Int
)
