package com.example.trianglemerge.model

data class GameStats(
    val totalMerges: Int = 0,
    val totalSwipes: Int = 0,
    val highestTile: Int = 2,
    val tilesSpawned: Int = 2 // Start with 2 because initial game spawns 2 tiles
) {
    fun incrementSwipes(): GameStats = copy(totalSwipes = totalSwipes + 1)

    fun addMerges(count: Int): GameStats = copy(totalMerges = totalMerges + count)

    fun updateHighestTile(value: Int): GameStats =
        if (value > highestTile) copy(highestTile = value) else this

    fun incrementTilesSpawned(): GameStats = copy(tilesSpawned = tilesSpawned + 1)
}