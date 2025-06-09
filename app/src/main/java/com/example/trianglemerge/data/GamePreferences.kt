package com.example.trianglemerge.data

import android.content.Context
import android.content.SharedPreferences
import com.example.trianglemerge.model.GameState
import com.google.gson.Gson
import com.example.trianglemerge.model.GameStats
import com.google.gson.reflect.TypeToken

data class SavedGameData(
    val grid: List<List<Int>>,
    val score: Int,
    val highScore: Int,
    val isGameOver: Boolean,
    val stats: GameStats? = null
)

class GamePreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("TriangleMergePrefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val KEY_SAVED_GAME = "saved_game"
        private const val KEY_HIGH_SCORE = "high_score"
        private const val KEY_HAS_SAVED_GAME = "has_saved_game"
    }

    fun saveGame(gameState: GameState) {
        val currentHighScore = getHighScore()
        val newHighScore = maxOf(currentHighScore, gameState.score)

        val savedData = SavedGameData(
            grid = gameState.grid.map { it.toList() },
            score = gameState.score,
            highScore = newHighScore,
            isGameOver = gameState.isGameOver,
            stats = gameState.stats
        )

        prefs.edit().apply {
            putString(KEY_SAVED_GAME, gson.toJson(savedData))
            putInt(KEY_HIGH_SCORE, newHighScore)
            putBoolean(KEY_HAS_SAVED_GAME, true)
            apply()
        }
    }

    fun loadGame(): GameState? {
        if (!hasSavedGame()) return null

        return try {
            val savedJson = prefs.getString(KEY_SAVED_GAME, null) ?: return null
            val savedData = gson.fromJson(savedJson, SavedGameData::class.java)

            // Reconstruct the game state
            val grid = savedData.grid.mapIndexed { _, row ->
                row.toMutableList()
            }.toMutableList()

            // Create GameState and update grids B and C
            val tempState = GameState(
                grid = grid,
                gridB = mutableListOf(),
                gridC = mutableListOf(),
                score = savedData.score,
                isGameOver = savedData.isGameOver
            )

            // Use the initial() method to create properly sized grids, then update them
            val initialState = GameState.initial()
            val loadedState = GameState(
                grid = grid,
                gridB = initialState.gridB,
                gridC = initialState.gridC,
                score = savedData.score,
                isGameOver = savedData.isGameOver,
                stats = savedData.stats ?: GameStats(),
                lastSpawnedPosition = null // Don't restore last spawned position
            )

            // Convert main grid to B and C
            loadedState.updateAuxiliaryGrids()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun clearSavedGame() {
        prefs.edit().apply {
            remove(KEY_SAVED_GAME)
            putBoolean(KEY_HAS_SAVED_GAME, false)
            apply()
        }
    }

    fun hasSavedGame(): Boolean {
        return prefs.getBoolean(KEY_HAS_SAVED_GAME, false)
    }

    fun getHighScore(): Int {
        return prefs.getInt(KEY_HIGH_SCORE, 0)
    }

    fun updateHighScore(score: Int) {
        val currentHigh = getHighScore()
        if (score > currentHigh) {
            prefs.edit().putInt(KEY_HIGH_SCORE, score).apply()
        }
    }
}