package com.github.teocci.sudoku.core

object Constants {
    // Grid dimensions
    const val GRID_SIZE = 9
    const val BOX_SIZE = 3
    const val TOTAL_CELLS = GRID_SIZE * GRID_SIZE

    // Game limits
    const val MAX_MISTAKES = 3
    const val MIN_NUMBER = 1
    const val MAX_NUMBER = 9
    const val MAX_HINTS = 3

    // Scoring
    const val POINTS_CORRECT_NUMBER = 10
    const val POINTS_HINT_PENALTY = -50
    const val POINTS_MISTAKE_PENALTY = -20
    const val POINTS_ROW_COMPLETION = 100
    const val POINTS_COLUMN_COMPLETION = 100
    const val POINTS_BOX_COMPLETION = 100
    const val POINTS_GAME_COMPLETION = 500
    const val POINTS_TIME_BONUS_MAX = 200

    // Time bonus thresholds (in seconds)
    const val TIME_BONUS_THRESHOLD_EXCELLENT = 300  // 5 minutes
    const val TIME_BONUS_THRESHOLD_GOOD = 600       // 10 minutes
    const val TIME_BONUS_THRESHOLD_FAIR = 900       // 15 minutes
    const val TIME_BONUS_EXCELLENT = 200
    const val TIME_BONUS_GOOD = 100
    const val TIME_BONUS_FAIR = 50

    // Animation durations (in milliseconds)
    const val ANIMATION_DURATION_SHORT = 150L
    const val ANIMATION_DURATION_MEDIUM = 300L
    const val ANIMATION_DURATION_LONG = 400L
    const val ANIMATION_SCALE_FACTOR = 1.05f
    const val ANIMATION_COMPLETION_DURATION = 300
    const val ANIMATION_CONFETTI_DURATION = 2000
    const val ANIMATION_SCORE_FLOAT_DURATION = 1500

    // UI dimensions (in dp)
    const val CELL_MIN_SIZE_DP = 36
    const val NUMBER_PAD_BUTTON_SIZE_DP = 48
    const val CONTROL_BUTTON_SIZE_DP = 56
    const val GRID_BORDER_WIDTH_DP = 2
    const val BOX_BORDER_WIDTH_DP = 2
    const val CELL_BORDER_WIDTH_DP = 1

    // Note font scale relative to cell size
    const val NOTE_FONT_SCALE = 0.3f

    // Long press duration for number lock (in milliseconds)
    const val LONG_PRESS_DURATION_MS = 500L

    // Difficulty settings - cells to remove from solved board
    const val DIFFICULTY_EASY_CELLS_TO_REMOVE = 30
    const val DIFFICULTY_MEDIUM_CELLS_TO_REMOVE = 40
    const val DIFFICULTY_HARD_CELLS_TO_REMOVE = 50
    const val DIFFICULTY_EXPERT_CELLS_TO_REMOVE = 55

    // Daily challenge seed multiplier
    const val DAILY_CHALLENGE_SEED_MULTIPLIER = 1000000007L
}
