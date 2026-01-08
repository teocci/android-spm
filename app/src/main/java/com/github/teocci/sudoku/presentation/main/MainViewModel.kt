package com.github.teocci.sudoku.presentation.main

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

/**
 * ViewModel for the main/home screen.
 * Handles difficulty selection and navigation to game.
 */
class MainViewModel : ViewModel() {

    // UI State
    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    // Navigation events
    private val _navigationEvent = MutableSharedFlow<MainNavigationEvent>()
    val navigationEvent = _navigationEvent.asSharedFlow()

    /**
     * Select a difficulty level.
     */
    fun selectDifficulty(difficulty: Difficulty) {
        _uiState.update { it.copy(selectedDifficulty = difficulty) }
    }

    /**
     * Start a new game with the selected difficulty.
     */
    fun startGame() {
        val difficulty = _uiState.value.selectedDifficulty
        viewModelScope.launch {
            _navigationEvent.emit(MainNavigationEvent.NavigateToGame(difficulty))
        }
    }

    /**
     * Start a game with a specific difficulty.
     */
    fun startGame(difficulty: Difficulty) {
        viewModelScope.launch {
            _navigationEvent.emit(MainNavigationEvent.NavigateToGame(difficulty))
        }
    }

    /**
     * Continue a saved game (if available).
     */
    fun continueGame() {
        // TODO: Implement saved game loading
        viewModelScope.launch {
            _uiState.value.savedGameDifficulty?.let { difficulty ->
                _navigationEvent.emit(MainNavigationEvent.NavigateToGame(difficulty))
            }
        }
    }

    /**
     * Show difficulty selection dialog.
     */
    fun showDifficultySelector() {
        _uiState.update { it.copy(showDifficultyDialog = true) }
    }

    /**
     * Hide difficulty selection dialog.
     */
    fun hideDifficultySelector() {
        _uiState.update { it.copy(showDifficultyDialog = false) }
    }

    /**
     * Navigate to settings.
     */
    fun navigateToSettings() {
        viewModelScope.launch {
            _navigationEvent.emit(MainNavigationEvent.NavigateToSettings)
        }
    }

    /**
     * Navigate to daily challenges.
     */
    fun navigateToDailyChallenges() {
        viewModelScope.launch {
            _navigationEvent.emit(MainNavigationEvent.NavigateToDailyChallenges)
        }
    }

    /**
     * Navigate to profile/statistics.
     */
    fun navigateToProfile() {
        viewModelScope.launch {
            _navigationEvent.emit(MainNavigationEvent.NavigateToProfile)
        }
    }

    /**
     * Load saved game state (if any).
     */
    fun loadSavedGame() {
        // TODO: Implement loading saved game from repository
        viewModelScope.launch {
            // For now, just set hasSavedGame to false
            _uiState.update { it.copy(hasSavedGame = false) }
        }
    }

    /**
     * Load user statistics.
     */
    fun loadStatistics() {
        // TODO: Implement loading statistics from repository
        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(
                    statistics = UserStatistics(
                        gamesPlayed = 42,
                        gamesWon = 38,
                        currentStreak = 5,
                        bestStreak = 12,
                        averageTime = 480L, // 8 minutes
                        bestTime = 245L // 4:05
                    )
                )
            }
        }
    }

    init {
        loadSavedGame()
        loadStatistics()
    }
}

/**
 * UI state for the main screen.
 */
data class MainUiState(
    val selectedDifficulty: Difficulty = Difficulty.MEDIUM,
    val showDifficultyDialog: Boolean = false,
    val hasSavedGame: Boolean = false,
    val savedGameDifficulty: Difficulty? = null,
    val savedGameProgress: Float = 0f,
    val statistics: UserStatistics? = null,
    val isLoading: Boolean = false
)

/**
 * User statistics for display on main screen.
 */
data class UserStatistics(
    val gamesPlayed: Int = 0,
    val gamesWon: Int = 0,
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
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

    /**
     * Win rate formatted as string.
     */
    val winRateFormatted: String
        get() = String.format("%.0f%%", winRate)
}

/**
 * Navigation events from main screen.
 */
sealed class MainNavigationEvent {
    data class NavigateToGame(val difficulty: Difficulty) : MainNavigationEvent()
    data object NavigateToSettings : MainNavigationEvent()
    data object NavigateToDailyChallenges : MainNavigationEvent()
    data object NavigateToProfile : MainNavigationEvent()
}
