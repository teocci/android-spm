package com.github.teocci.sudoku.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.github.teocci.sudoku.domain.model.Difficulty
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * SharedPreferences wrapper for game settings and preferences.
 * Provides type-safe access to user preferences.
 */
class GamePreferences(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    // Observable settings flow
    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<GameSettings> = _settings.asStateFlow()

    /**
     * Load all settings from SharedPreferences.
     */
    private fun loadSettings(): GameSettings {
        return GameSettings(
            soundEnabled = prefs.getBoolean(KEY_SOUND_ENABLED, true),
            hapticEnabled = prefs.getBoolean(KEY_HAPTIC_ENABLED, true),
            autoRemoveNotes = prefs.getBoolean(KEY_AUTO_REMOVE_NOTES, true),
            highlightRelatedCells = prefs.getBoolean(KEY_HIGHLIGHT_RELATED, true),
            highlightSameNumbers = prefs.getBoolean(KEY_HIGHLIGHT_SAME, true),
            showTimer = prefs.getBoolean(KEY_SHOW_TIMER, true),
            showMistakes = prefs.getBoolean(KEY_SHOW_MISTAKES, true),
            darkTheme = prefs.getBoolean(KEY_DARK_THEME, true),
            autoSave = prefs.getBoolean(KEY_AUTO_SAVE, true),
            lastPlayedDifficulty = Difficulty.entries.getOrNull(
                prefs.getInt(KEY_LAST_DIFFICULTY, Difficulty.MEDIUM.ordinal)
            ) ?: Difficulty.MEDIUM
        )
    }

    /**
     * Update settings and persist to SharedPreferences.
     */
    private fun updateSettings(update: GameSettings.() -> GameSettings) {
        val newSettings = _settings.value.update()
        _settings.value = newSettings
        saveSettings(newSettings)
    }

    /**
     * Save settings to SharedPreferences.
     */
    private fun saveSettings(settings: GameSettings) {
        prefs.edit {
            putBoolean(KEY_SOUND_ENABLED, settings.soundEnabled)
            putBoolean(KEY_HAPTIC_ENABLED, settings.hapticEnabled)
            putBoolean(KEY_AUTO_REMOVE_NOTES, settings.autoRemoveNotes)
            putBoolean(KEY_HIGHLIGHT_RELATED, settings.highlightRelatedCells)
            putBoolean(KEY_HIGHLIGHT_SAME, settings.highlightSameNumbers)
            putBoolean(KEY_SHOW_TIMER, settings.showTimer)
            putBoolean(KEY_SHOW_MISTAKES, settings.showMistakes)
            putBoolean(KEY_DARK_THEME, settings.darkTheme)
            putBoolean(KEY_AUTO_SAVE, settings.autoSave)
            putInt(KEY_LAST_DIFFICULTY, settings.lastPlayedDifficulty.ordinal)
        }
    }

    // Individual setting accessors

    var soundEnabled: Boolean
        get() = _settings.value.soundEnabled
        set(value) = updateSettings { copy(soundEnabled = value) }

    var hapticEnabled: Boolean
        get() = _settings.value.hapticEnabled
        set(value) = updateSettings { copy(hapticEnabled = value) }

    var autoRemoveNotes: Boolean
        get() = _settings.value.autoRemoveNotes
        set(value) = updateSettings { copy(autoRemoveNotes = value) }

    var highlightRelatedCells: Boolean
        get() = _settings.value.highlightRelatedCells
        set(value) = updateSettings { copy(highlightRelatedCells = value) }

    var highlightSameNumbers: Boolean
        get() = _settings.value.highlightSameNumbers
        set(value) = updateSettings { copy(highlightSameNumbers = value) }

    var showTimer: Boolean
        get() = _settings.value.showTimer
        set(value) = updateSettings { copy(showTimer = value) }

    var showMistakes: Boolean
        get() = _settings.value.showMistakes
        set(value) = updateSettings { copy(showMistakes = value) }

    var darkTheme: Boolean
        get() = _settings.value.darkTheme
        set(value) = updateSettings { copy(darkTheme = value) }

    var autoSave: Boolean
        get() = _settings.value.autoSave
        set(value) = updateSettings { copy(autoSave = value) }

    var lastPlayedDifficulty: Difficulty
        get() = _settings.value.lastPlayedDifficulty
        set(value) = updateSettings { copy(lastPlayedDifficulty = value) }

    /**
     * Check if this is the first launch of the app.
     */
    fun isFirstLaunch(): Boolean {
        val isFirst = prefs.getBoolean(KEY_FIRST_LAUNCH, true)
        if (isFirst) {
            prefs.edit { putBoolean(KEY_FIRST_LAUNCH, false) }
        }
        return isFirst
    }

    /**
     * Get hint count remaining.
     */
    fun getHintCount(): Int {
        return prefs.getInt(KEY_HINT_COUNT, DEFAULT_HINTS)
    }

    /**
     * Set hint count.
     */
    fun setHintCount(count: Int) {
        prefs.edit { putInt(KEY_HINT_COUNT, count) }
    }

    /**
     * Reset hints to default.
     */
    fun resetHints() {
        setHintCount(DEFAULT_HINTS)
    }

    /**
     * Clear all preferences (for reset).
     */
    fun clearAll() {
        prefs.edit { clear() }
        _settings.value = loadSettings()
    }

    companion object {
        private const val PREFS_NAME = "sudoku_preferences"

        // Setting keys
        private const val KEY_SOUND_ENABLED = "sound_enabled"
        private const val KEY_HAPTIC_ENABLED = "haptic_enabled"
        private const val KEY_AUTO_REMOVE_NOTES = "auto_remove_notes"
        private const val KEY_HIGHLIGHT_RELATED = "highlight_related"
        private const val KEY_HIGHLIGHT_SAME = "highlight_same"
        private const val KEY_SHOW_TIMER = "show_timer"
        private const val KEY_SHOW_MISTAKES = "show_mistakes"
        private const val KEY_DARK_THEME = "dark_theme"
        private const val KEY_AUTO_SAVE = "auto_save"
        private const val KEY_LAST_DIFFICULTY = "last_difficulty"
        private const val KEY_FIRST_LAUNCH = "first_launch"
        private const val KEY_HINT_COUNT = "hint_count"

        private const val DEFAULT_HINTS = 3

        @Volatile
        private var INSTANCE: GamePreferences? = null

        /**
         * Get singleton instance.
         */
        fun getInstance(context: Context): GamePreferences {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: GamePreferences(context.applicationContext).also {
                    INSTANCE = it
                }
            }
        }
    }
}

/**
 * Data class representing all game settings.
 */
data class GameSettings(
    val soundEnabled: Boolean = true,
    val hapticEnabled: Boolean = true,
    val autoRemoveNotes: Boolean = true,
    val highlightRelatedCells: Boolean = true,
    val highlightSameNumbers: Boolean = true,
    val showTimer: Boolean = true,
    val showMistakes: Boolean = true,
    val darkTheme: Boolean = true,
    val autoSave: Boolean = true,
    val lastPlayedDifficulty: Difficulty = Difficulty.MEDIUM
)
