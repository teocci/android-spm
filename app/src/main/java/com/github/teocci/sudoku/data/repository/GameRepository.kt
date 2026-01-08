package com.github.teocci.sudoku.data.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.github.teocci.sudoku.domain.model.Cell
import com.github.teocci.sudoku.domain.model.Difficulty
import com.github.teocci.sudoku.domain.model.GameState
import com.github.teocci.sudoku.domain.model.GameStatus
import com.github.teocci.sudoku.domain.model.Position
import com.github.teocci.sudoku.domain.model.SudokuBoard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

/**
 * Repository for game state persistence.
 * Handles saving and loading game state to/from SharedPreferences.
 */
class GameRepository(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    /**
     * Save current game state.
     */
    suspend fun saveGameState(gameState: GameState) = withContext(Dispatchers.IO) {
        val json = gameStateToJson(gameState)
        prefs.edit {
            putString(KEY_SAVED_GAME, json.toString())
            putLong(KEY_SAVED_TIMESTAMP, System.currentTimeMillis())
        }
    }

    /**
     * Load saved game state.
     */
    suspend fun loadGameState(): GameState? = withContext(Dispatchers.IO) {
        val jsonString = prefs.getString(KEY_SAVED_GAME, null) ?: return@withContext null

        try {
            val json = JSONObject(jsonString)
            jsonToGameState(json)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Check if there's a saved game.
     */
    fun hasSavedGame(): Boolean {
        return prefs.contains(KEY_SAVED_GAME)
    }

    /**
     * Get saved game info without loading full state.
     */
    fun getSavedGameInfo(): SavedGameInfo? {
        val jsonString = prefs.getString(KEY_SAVED_GAME, null) ?: return null

        return try {
            val json = JSONObject(jsonString)
            SavedGameInfo(
                difficulty = Difficulty.entries.getOrNull(
                    json.optInt("difficulty", Difficulty.MEDIUM.ordinal)
                ) ?: Difficulty.MEDIUM,
                progress = json.optDouble("progress", 0.0).toFloat(),
                elapsedTime = json.optLong("elapsedTime", 0L),
                savedAt = prefs.getLong(KEY_SAVED_TIMESTAMP, 0L)
            )
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Delete saved game.
     */
    fun deleteSavedGame() {
        prefs.edit {
            remove(KEY_SAVED_GAME)
            remove(KEY_SAVED_TIMESTAMP)
        }
    }

    /**
     * Convert game state to JSON.
     */
    private fun gameStateToJson(state: GameState): JSONObject {
        return JSONObject().apply {
            put("difficulty", state.difficulty.ordinal)
            put("score", state.score)
            put("mistakes", state.mistakes)
            put("maxMistakes", state.maxMistakes)
            put("elapsedTime", state.elapsedTimeSeconds)
            put("notesMode", state.notesMode)
            put("status", state.gameStatus.ordinal)
            put("progress", state.progress)

            // Selected cell
            state.selectedCell?.let {
                put("selectedRow", it.row)
                put("selectedCol", it.col)
            }

            // Active number
            state.activeNumber?.let {
                put("activeNumber", it)
            }

            // Board
            put("board", boardToJson(state.board))
        }
    }

    /**
     * Convert board to JSON.
     */
    private fun boardToJson(board: SudokuBoard): JSONArray {
        val cellsArray = JSONArray()

        for (row in 0 until 9) {
            for (col in 0 until 9) {
                val cell = board.getCell(row, col)
                val cellJson = JSONObject().apply {
                    put("row", row)
                    put("col", col)
                    put("value", cell.value)
                    put("solution", cell.solution)
                    put("isFixed", cell.isFixed)
                    put("isError", cell.isError)

                    // Notes as JSON array
                    val notesArray = JSONArray()
                    cell.notes.forEach { notesArray.put(it) }
                    put("notes", notesArray)
                }
                cellsArray.put(cellJson)
            }
        }

        return cellsArray
    }

    /**
     * Convert JSON to game state.
     */
    private fun jsonToGameState(json: JSONObject): GameState {
        val board = jsonToBoard(json.getJSONArray("board"))

        return GameState(
            board = board,
            difficulty = Difficulty.entries.getOrNull(
                json.optInt("difficulty", Difficulty.MEDIUM.ordinal)
            ) ?: Difficulty.MEDIUM,
            score = json.optInt("score", 0),
            mistakes = json.optInt("mistakes", 0),
            maxMistakes = json.optInt("maxMistakes", 3),
            elapsedTimeSeconds = json.optLong("elapsedTime", 0L),
            notesMode = json.optBoolean("notesMode", false),
            gameStatus = GameStatus.entries.getOrNull(
                json.optInt("status", GameStatus.IN_PROGRESS.ordinal)
            ) ?: GameStatus.IN_PROGRESS,
            selectedCell = if (json.has("selectedRow") && json.has("selectedCol")) {
                Position(json.getInt("selectedRow"), json.getInt("selectedCol"))
            } else null,
            activeNumber = if (json.has("activeNumber")) {
                json.getInt("activeNumber")
            } else null
        )
    }

    /**
     * Convert JSON to board.
     */
    private fun jsonToBoard(cellsArray: JSONArray): SudokuBoard {
        val cells = Array(9) { Array<Cell?>(9) { null } }

        for (i in 0 until cellsArray.length()) {
            val cellJson = cellsArray.getJSONObject(i)
            val row = cellJson.getInt("row")
            val col = cellJson.getInt("col")

            // Parse notes
            val notesArray = cellJson.optJSONArray("notes") ?: JSONArray()
            val notes = mutableSetOf<Int>()
            for (j in 0 until notesArray.length()) {
                notes.add(notesArray.getInt(j))
            }

            val value = cellJson.optInt("value", 0).takeIf { it > 0 }
            cells[row][col] = Cell(
                value = value,
                solution = cellJson.optInt("solution", 0),
                isFixed = cellJson.optBoolean("isFixed", false),
                isError = cellJson.optBoolean("isError", false),
                notes = notes
            )
        }

        // Convert Array to List and ensure no nulls
        val cellsList = cells.map { row ->
            row.map { cell -> cell ?: Cell(solution = 0) }
        }

        return SudokuBoard(cellsList)
    }

    companion object {
        private const val PREFS_NAME = "game_repository"
        private const val KEY_SAVED_GAME = "saved_game"
        private const val KEY_SAVED_TIMESTAMP = "saved_timestamp"

        @Volatile
        private var INSTANCE: GameRepository? = null

        /**
         * Get singleton instance.
         */
        fun getInstance(context: Context): GameRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: GameRepository(context.applicationContext).also {
                    INSTANCE = it
                }
            }
        }
    }
}

/**
 * Basic info about a saved game.
 */
data class SavedGameInfo(
    val difficulty: Difficulty,
    val progress: Float,
    val elapsedTime: Long,
    val savedAt: Long
)
