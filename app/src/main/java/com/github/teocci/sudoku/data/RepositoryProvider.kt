package com.github.teocci.sudoku.data

import android.content.Context
import com.github.teocci.sudoku.data.local.DailyChallengeStore
import com.github.teocci.sudoku.data.local.GamePreferences
import com.github.teocci.sudoku.data.repository.GameRepository
import com.github.teocci.sudoku.data.repository.StatsRepository

/**
 * Singleton holder for repository instances.
 * Must be initialized with Application context before use.
 */
object RepositoryProvider {

    private var statsRepository: StatsRepository? = null
    private var gameRepository: GameRepository? = null
    private var dailyChallengeStore: DailyChallengeStore? = null
    private var gamePreferences: GamePreferences? = null

    /**
     * Initialize all repositories with application context.
     * Should be called once in Application.onCreate() or MainActivity.onCreate().
     */
    fun initialize(context: Context) {
        val appContext = context.applicationContext
        statsRepository = StatsRepository(appContext)
        gameRepository = GameRepository(appContext)
        dailyChallengeStore = DailyChallengeStore(appContext)
        gamePreferences = GamePreferences(appContext)
    }

    /**
     * Get StatsRepository instance.
     * Throws IllegalStateException if not initialized.
     */
    fun getStatsRepository(): StatsRepository {
        return statsRepository ?: throw IllegalStateException(
            "RepositoryProvider not initialized. Call initialize(context) first."
        )
    }

    /**
     * Get GameRepository instance.
     * Throws IllegalStateException if not initialized.
     */
    fun getGameRepository(): GameRepository {
        return gameRepository ?: throw IllegalStateException(
            "RepositoryProvider not initialized. Call initialize(context) first."
        )
    }

    /**
     * Get DailyChallengeStore instance.
     * Throws IllegalStateException if not initialized.
     */
    fun getDailyChallengeStore(): DailyChallengeStore {
        return dailyChallengeStore ?: throw IllegalStateException(
            "RepositoryProvider not initialized. Call initialize(context) first."
        )
    }

    /**
     * Get GamePreferences instance.
     * Throws IllegalStateException if not initialized.
     */
    fun getGamePreferences(): GamePreferences {
        return gamePreferences ?: throw IllegalStateException(
            "RepositoryProvider not initialized. Call initialize(context) first."
        )
    }
}
