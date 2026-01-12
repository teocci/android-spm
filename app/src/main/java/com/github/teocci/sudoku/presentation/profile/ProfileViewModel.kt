package com.github.teocci.sudoku.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.teocci.sudoku.data.RepositoryProvider
import com.github.teocci.sudoku.data.repository.AchievementRepository
import com.github.teocci.sudoku.data.repository.GameHistoryRepository
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
    private val statsRepository: StatsRepository = RepositoryProvider.getStatsRepository(),
    private val achievementRepository: AchievementRepository = RepositoryProvider.getAchievementRepository(),
    private val gameHistoryRepository: GameHistoryRepository = RepositoryProvider.getGameHistoryRepository()
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
        viewModelScope.launch {
            achievementRepository.achievements.collect { achievementDataList ->
                _uiState.update { state ->
                    state.copy(
                        achievements = achievementDataList.map { data ->
                            achievementRepository.toUIModel(data)
                        }
                    )
                }
            }
        }
    }

    /**
     * Load recent game history.
     */
    private fun loadRecentGames() {
        viewModelScope.launch {
            gameHistoryRepository.recentGames.collect { historyEntries ->
                _uiState.update { state ->
                    state.copy(
                        recentGames = historyEntries.map { entry ->
                            RecentGame(
                                date = gameHistoryRepository.formatGameDate(entry.timestamp),
                                difficulty = entry.difficulty,
                                time = entry.timeSeconds,
                                score = entry.score,
                                isCompleted = entry.isCompleted,
                                mistakes = entry.mistakes
                            )
                        }
                    )
                }
            }
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
