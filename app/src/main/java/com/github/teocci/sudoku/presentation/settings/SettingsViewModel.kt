package com.github.teocci.sudoku.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for the settings screen.
 * Manages user preferences and app settings.
 */
class SettingsViewModel : ViewModel() {

    // UI State
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    // Events
    private val _event = MutableSharedFlow<SettingsEvent>()
    val event = _event.asSharedFlow()

    init {
        loadSettings()
    }

    /**
     * Load settings from preferences.
     */
    private fun loadSettings() {
        // TODO: Load from GamePreferences
        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(
                    soundEnabled = true,
                    hapticEnabled = true,
                    autoRemoveNotes = true,
                    highlightRelatedCells = true,
                    highlightSameNumbers = true,
                    showTimer = true,
                    showMistakes = true,
                    darkTheme = true,
                    autoSave = true
                )
            }
        }
    }

    /**
     * Toggle sound effects.
     */
    fun toggleSound() {
        _uiState.update { it.copy(soundEnabled = !it.soundEnabled) }
        saveSettings()
    }

    /**
     * Toggle haptic feedback.
     */
    fun toggleHaptic() {
        _uiState.update { it.copy(hapticEnabled = !it.hapticEnabled) }
        saveSettings()
    }

    /**
     * Toggle auto-remove notes when placing numbers.
     */
    fun toggleAutoRemoveNotes() {
        _uiState.update { it.copy(autoRemoveNotes = !it.autoRemoveNotes) }
        saveSettings()
    }

    /**
     * Toggle highlight related cells (same row/col/box).
     */
    fun toggleHighlightRelatedCells() {
        _uiState.update { it.copy(highlightRelatedCells = !it.highlightRelatedCells) }
        saveSettings()
    }

    /**
     * Toggle highlight same numbers.
     */
    fun toggleHighlightSameNumbers() {
        _uiState.update { it.copy(highlightSameNumbers = !it.highlightSameNumbers) }
        saveSettings()
    }

    /**
     * Toggle timer visibility.
     */
    fun toggleShowTimer() {
        _uiState.update { it.copy(showTimer = !it.showTimer) }
        saveSettings()
    }

    /**
     * Toggle mistakes counter visibility.
     */
    fun toggleShowMistakes() {
        _uiState.update { it.copy(showMistakes = !it.showMistakes) }
        saveSettings()
    }

    /**
     * Toggle dark theme.
     */
    fun toggleDarkTheme() {
        _uiState.update { it.copy(darkTheme = !it.darkTheme) }
        saveSettings()
        viewModelScope.launch {
            _event.emit(SettingsEvent.ThemeChanged(_uiState.value.darkTheme))
        }
    }

    /**
     * Toggle auto-save.
     */
    fun toggleAutoSave() {
        _uiState.update { it.copy(autoSave = !it.autoSave) }
        saveSettings()
    }

    /**
     * Show reset statistics confirmation dialog.
     */
    fun showResetConfirmation() {
        _uiState.update { it.copy(showResetDialog = true) }
    }

    /**
     * Hide reset statistics confirmation dialog.
     */
    fun hideResetConfirmation() {
        _uiState.update { it.copy(showResetDialog = false) }
    }

    /**
     * Confirm and reset all statistics.
     */
    fun confirmResetStatistics() {
        // TODO: Implement reset via repository
        viewModelScope.launch {
            _uiState.update { it.copy(showResetDialog = false) }
            _event.emit(SettingsEvent.StatisticsReset)
        }
    }

    /**
     * Show clear saved games confirmation dialog.
     */
    fun showClearGamesConfirmation() {
        _uiState.update { it.copy(showClearGamesDialog = true) }
    }

    /**
     * Hide clear saved games confirmation dialog.
     */
    fun hideClearGamesConfirmation() {
        _uiState.update { it.copy(showClearGamesDialog = false) }
    }

    /**
     * Confirm and clear all saved games.
     */
    fun confirmClearSavedGames() {
        // TODO: Implement clear via repository
        viewModelScope.launch {
            _uiState.update { it.copy(showClearGamesDialog = false) }
            _event.emit(SettingsEvent.SavedGamesCleared)
        }
    }

    /**
     * Save settings to preferences.
     */
    private fun saveSettings() {
        // TODO: Save to GamePreferences
        viewModelScope.launch {
            // Preferences save logic
        }
    }

    /**
     * Get app version.
     */
    fun getAppVersion(): String {
        return "1.0.0" // TODO: Get from BuildConfig
    }
}

/**
 * UI state for settings screen.
 */
data class SettingsUiState(
    // Gameplay settings
    val soundEnabled: Boolean = true,
    val hapticEnabled: Boolean = true,
    val autoRemoveNotes: Boolean = true,
    val highlightRelatedCells: Boolean = true,
    val highlightSameNumbers: Boolean = true,

    // Display settings
    val showTimer: Boolean = true,
    val showMistakes: Boolean = true,
    val darkTheme: Boolean = true,

    // Data settings
    val autoSave: Boolean = true,

    // Dialogs
    val showResetDialog: Boolean = false,
    val showClearGamesDialog: Boolean = false
)

/**
 * Settings events.
 */
sealed class SettingsEvent {
    data class ThemeChanged(val darkTheme: Boolean) : SettingsEvent()
    data object StatisticsReset : SettingsEvent()
    data object SavedGamesCleared : SettingsEvent()
}
