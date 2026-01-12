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
import org.json.JSONArray
import org.json.JSONObject
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Repository for managing game history.
 * Stores the last N games with full details.
 */
class GameHistoryRepository(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    // Observable recent games
    private val _recentGames = MutableStateFlow<List<GameHistoryEntry>>(emptyList())
    val recentGames: StateFlow<List<GameHistoryEntry>> = _recentGames.asStateFlow()

    init {
        _recentGames.value = loadHistory()
    }

    /**
     * Record a game in history (circular buffer).
     */
    suspend fun recordGame(entry: GameHistoryEntry) = withContext(Dispatchers.IO) {
        val currentHistory = _recentGames.value.toMutableList()

        // Add to front (most recent first)
        currentHistory.add(0, entry)

        // Trim to max size
        if (currentHistory.size > MAX_HISTORY_SIZE) {
            currentHistory.subList(MAX_HISTORY_SIZE, currentHistory.size).clear()
        }

        _recentGames.value = currentHistory
        saveHistory(currentHistory)
    }

    /**
     * Load history from SharedPreferences.
     */
    private fun loadHistory(): List<GameHistoryEntry> {
        val jsonString = prefs.getString(KEY_GAME_HISTORY, null) ?: return emptyList()

        return try {
            val jsonArray = JSONArray(jsonString)
            List(jsonArray.length()) { index ->
                val jsonObject = jsonArray.getJSONObject(index)
                GameHistoryEntry(
                    timestamp = jsonObject.getLong("timestamp"),
                    difficulty = Difficulty.entries[jsonObject.getInt("difficulty")],
                    timeSeconds = jsonObject.getLong("timeSeconds"),
                    score = jsonObject.getInt("score"),
                    isCompleted = jsonObject.getBoolean("isCompleted"),
                    mistakes = jsonObject.getInt("mistakes"),
                    hintsUsed = jsonObject.getInt("hintsUsed"),
                    isDailyChallenge = jsonObject.getBoolean("isDailyChallenge")
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Save history to SharedPreferences.
     */
    private fun saveHistory(history: List<GameHistoryEntry>) {
        val jsonArray = JSONArray()
        history.forEach { entry ->
            val jsonObject = JSONObject().apply {
                put("timestamp", entry.timestamp)
                put("difficulty", entry.difficulty.ordinal)
                put("timeSeconds", entry.timeSeconds)
                put("score", entry.score)
                put("isCompleted", entry.isCompleted)
                put("mistakes", entry.mistakes)
                put("hintsUsed", entry.hintsUsed)
                put("isDailyChallenge", entry.isDailyChallenge)
            }
            jsonArray.put(jsonObject)
        }

        prefs.edit {
            putString(KEY_GAME_HISTORY, jsonArray.toString())
        }
    }

    /**
     * Format game date for UI (Today, Yesterday, MMM d).
     */
    fun formatGameDate(timestamp: Long): String {
        val gameDate = Instant.ofEpochMilli(timestamp)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
        val today = LocalDate.now()
        val yesterday = today.minusDays(1)

        return when (gameDate) {
            today -> "Today"
            yesterday -> "Yesterday"
            else -> {
                val formatter = DateTimeFormatter.ofPattern("MMM d")
                gameDate.format(formatter)
            }
        }
    }

    /**
     * Get games by date range.
     */
    fun getGamesByDateRange(startMillis: Long, endMillis: Long): List<GameHistoryEntry> {
        return _recentGames.value.filter { entry ->
            entry.timestamp in startMillis..endMillis
        }
    }

    /**
     * Clear all history.
     */
    fun clearHistory() {
        prefs.edit { clear() }
        _recentGames.value = emptyList()
    }

    companion object {
        private const val PREFS_NAME = "game_history_repository"
        private const val KEY_GAME_HISTORY = "game_history"
        const val MAX_HISTORY_SIZE = 20
    }
}

/**
 * Game history entry.
 */
data class GameHistoryEntry(
    val timestamp: Long,           // Epoch millis for sorting
    val difficulty: Difficulty,
    val timeSeconds: Long,
    val score: Int,
    val isCompleted: Boolean,
    val mistakes: Int,
    val hintsUsed: Int,
    val isDailyChallenge: Boolean
)
