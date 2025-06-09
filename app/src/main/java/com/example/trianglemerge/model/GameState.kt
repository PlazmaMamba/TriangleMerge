package com.example.trianglemerge.model

import kotlin.random.Random

data class GameState(
    val grid: List<MutableList<Int>>, // Main triangular grid
    val gridB: List<MutableList<Int>>, // Grid B for vertical right swipes
    val gridC: List<MutableList<Int>>, // Grid C for vertical left swipes
    val score: Int = 0,
    val isGameOver: Boolean = false,
    val stats: GameStats = GameStats(),
    val lastSpawnedPosition: Pair<Int, Int>? = null // Track the latest spawned tile
) {
    companion object {
        const val GRID_SIZE = 4 // Number of rows: [1], [3], [5], [7]

        fun initial(): GameState {
            // Create main grid
            val grid = mutableListOf<MutableList<Int>>()
            for (row in 0 until GRID_SIZE) {
                val rowSize = 2 * row + 1 // Row 0: 1, Row 1: 3, Row 2: 5, Row 3: 7
                grid.add(MutableList(rowSize) { 0 })
            }

            // Create grids B and C with same structure
            val gridB = mutableListOf<MutableList<Int>>()
            val gridC = mutableListOf<MutableList<Int>>()
            for (row in 0 until GRID_SIZE) {
                val rowSize = 2 * row + 1
                gridB.add(MutableList(rowSize) { 0 })
                gridC.add(MutableList(rowSize) { 0 })
            }

            var newState = GameState(grid, gridB, gridC)
            // Spawn two initial tiles
            newState = newState.spawnRandomTile()
            newState = newState.spawnRandomTile()

            return newState
        }
    }

    fun spawnRandomTile(): GameState {
        val emptyCells = mutableListOf<Pair<Int, Int>>()

        // Find all empty cells in the main grid
        for (row in grid.indices) {
            for (col in grid[row].indices) {
                if (grid[row][col] == 0) {
                    emptyCells.add(row to col)
                }
            }
        }

        if (emptyCells.isEmpty()) {
            // No empty cells - check if game is over
            return this.copy(isGameOver = checkGameOver())
        }

        // Pick random empty cell
        val (row, col) = emptyCells.random()
        val value = if (Random.nextFloat() < 0.9f) 2 else 4

        // Create new grids with the spawned tile
        val newGrid = grid.map { it.toMutableList() }.toMutableList()
        newGrid[row][col] = value

        // Update grids B and C based on the new main grid
        val newGridB = convertMainToB(newGrid)
        val newGridC = convertMainToC(newGrid)

        // Update stats
        val newStats = stats.incrementTilesSpawned().updateHighestTile(value)

        // Create new state and check if game is over after spawning
        val newState = this.copy(
            grid = newGrid,
            gridB = newGridB,
            gridC = newGridC,
            stats = newStats,
            lastSpawnedPosition = row to col
        )
        return newState.copy(isGameOver = newState.checkGameOver())
    }

    fun isTriangleUpward(row: Int, col: Int): Boolean = col % 2 == 0

    // Public method to update auxiliary grids from main grid
    fun updateAuxiliaryGrids(): GameState {
        val newGridB = convertMainToB(grid)
        val newGridC = convertMainToC(grid)

        // Also update highest tile when loading from saved state
        var highestTile = stats.highestTile
        for (row in grid) {
            for (value in row) {
                if (value > highestTile) highestTile = value
            }
        }

        return this.copy(
            gridB = newGridB,
            gridC = newGridC,
            stats = stats.updateHighestTile(highestTile),
            lastSpawnedPosition = null // Clear when updating from saved state
        )
    }

    private fun checkGameOver(): Boolean {
        // Check if there are any empty cells
        for (row in grid.indices) {
            for (col in grid[row].indices) {
                if (grid[row][col] == 0) return false
            }
        }

        // Check if any moves are possible (without spawning new tiles)
        if (canMove(SwipeDirection.LEFT)) return false
        if (canMove(SwipeDirection.RIGHT)) return false
        if (canMove(SwipeDirection.TOP_LEFT)) return false
        if (canMove(SwipeDirection.TOP_RIGHT)) return false
        if (canMove(SwipeDirection.BOTTOM_LEFT)) return false
        if (canMove(SwipeDirection.BOTTOM_RIGHT)) return false

        return true
    }

    // Check if a move in a direction would change the board
    private fun canMove(direction: SwipeDirection): Boolean {
        val testState = when (direction) {
            SwipeDirection.LEFT -> slideLeft(false)
            SwipeDirection.RIGHT -> slideRight(false)
            SwipeDirection.TOP_LEFT -> slideTopLeft(false)
            SwipeDirection.TOP_RIGHT -> slideTopRight(false)
            SwipeDirection.BOTTOM_LEFT -> slideBottomLeft(false)
            SwipeDirection.BOTTOM_RIGHT -> slideBottomRight(false)
        }
        return testState.grid != this.grid
    }

    // Sliding functions for 6 directions
    fun slideLeft(spawn: Boolean = true): GameState {
        var newScore = score
        var totalMerges = 0
        var highestTile = stats.highestTile

        val newGrid = grid.map { row ->
            val (slidRow, scoreGained, mergeCount, maxTile) = slideRowLeft(row.toList())
            newScore += scoreGained
            totalMerges += mergeCount
            if (maxTile > highestTile) highestTile = maxTile
            slidRow.toMutableList()
        }.toMutableList()

        return if (newGrid != grid) {
            // Update grids B and C based on the new main grid
            val newGridB = convertMainToB(newGrid)
            val newGridC = convertMainToC(newGrid)
            val newStats = stats
                .incrementSwipes()
                .addMerges(totalMerges)
                .updateHighestTile(highestTile)
            val updated = copy(
                grid = newGrid,
                gridB = newGridB,
                gridC = newGridC,
                score = newScore,
                stats = newStats,
                lastSpawnedPosition = null // Clear previous spawn position
            )
            if (spawn) updated.spawnRandomTile() else updated
        } else {
            this
        }
    }

    fun slideRight(spawn: Boolean = true): GameState {
        var newScore = score
        var totalMerges = 0
        var highestTile = stats.highestTile

        val newGrid = grid.map { row ->
            val (slidRow, scoreGained, mergeCount, maxTile) = slideRowRight(row.toList())
            newScore += scoreGained
            totalMerges += mergeCount
            if (maxTile > highestTile) highestTile = maxTile
            slidRow.toMutableList()
        }.toMutableList()

        return if (newGrid != grid) {
            val newGridB = convertMainToB(newGrid)
            val newGridC = convertMainToC(newGrid)
            val newStats = stats
                .incrementSwipes()
                .addMerges(totalMerges)
                .updateHighestTile(highestTile)
            val updated = copy(
                grid = newGrid,
                gridB = newGridB,
                gridC = newGridC,
                score = newScore,
                stats = newStats,
                lastSpawnedPosition = null
            )
            if (spawn) updated.spawnRandomTile() else updated
        } else {
            this
        }
    }

    fun slideTopLeft(spawn: Boolean = true): GameState {
        var newScore = score
        var totalMerges = 0
        var highestTile = stats.highestTile

        val newGridC = gridC.map { row ->
            val (slidRow, scoreGained, mergeCount, maxTile) = slideRowRight(row.toList())
            newScore += scoreGained
            totalMerges += mergeCount
            if (maxTile > highestTile) highestTile = maxTile
            slidRow.toMutableList()
        }.toMutableList()

        return if (newGridC != gridC) {
            val newGrid = convertCToMain(newGridC)
            val newGridB = convertMainToB(newGrid)
            val newStats = stats
                .incrementSwipes()
                .addMerges(totalMerges)
                .updateHighestTile(highestTile)
            val updated = copy(
                grid = newGrid,
                gridB = newGridB,
                gridC = newGridC,
                score = newScore,
                stats = newStats,
                lastSpawnedPosition = null
            )
            if (spawn) updated.spawnRandomTile() else updated
        } else {
            this
        }
    }

    fun slideTopRight(spawn: Boolean = true): GameState {
        var newScore = score
        var totalMerges = 0
        var highestTile = stats.highestTile

        val newGridB = gridB.map { row ->
            val (slidRow, scoreGained, mergeCount, maxTile) = slideRowLeft(row.toList())
            newScore += scoreGained
            totalMerges += mergeCount
            if (maxTile > highestTile) highestTile = maxTile
            slidRow.toMutableList()
        }.toMutableList()

        return if (newGridB != gridB) {
            val newGrid = convertBToMain(newGridB)
            val newGridC = convertMainToC(newGrid)
            val newStats = stats
                .incrementSwipes()
                .addMerges(totalMerges)
                .updateHighestTile(highestTile)
            val updated = copy(
                grid = newGrid,
                gridB = newGridB,
                gridC = newGridC,
                score = newScore,
                stats = newStats,
                lastSpawnedPosition = null
            )
            if (spawn) updated.spawnRandomTile() else updated
        } else {
            this
        }
    }

    fun slideBottomLeft(spawn: Boolean = true): GameState {
        var newScore = score
        var totalMerges = 0
        var highestTile = stats.highestTile

        val newGridB = gridB.map { row ->
            val (slidRow, scoreGained, mergeCount, maxTile) = slideRowRight(row.toList())
            newScore += scoreGained
            totalMerges += mergeCount
            if (maxTile > highestTile) highestTile = maxTile
            slidRow.toMutableList()
        }.toMutableList()

        return if (newGridB != gridB) {
            val newGrid = convertBToMain(newGridB)
            val newGridC = convertMainToC(newGrid)
            val newStats = stats
                .incrementSwipes()
                .addMerges(totalMerges)
                .updateHighestTile(highestTile)
            val updated = copy(
                grid = newGrid,
                gridB = newGridB,
                gridC = newGridC,
                score = newScore,
                stats = newStats,
                lastSpawnedPosition = null
            )
            if (spawn) updated.spawnRandomTile() else updated
        } else {
            this
        }
    }

    fun slideBottomRight(spawn: Boolean = true): GameState {
        var newScore = score
        var totalMerges = 0
        var highestTile = stats.highestTile

        val newGridC = gridC.map { row ->
            val (slidRow, scoreGained, mergeCount, maxTile) = slideRowLeft(row.toList())
            newScore += scoreGained
            totalMerges += mergeCount
            if (maxTile > highestTile) highestTile = maxTile
            slidRow.toMutableList()
        }.toMutableList()

        return if (newGridC != gridC) {
            val newGrid = convertCToMain(newGridC)
            val newGridB = convertMainToB(newGrid)
            val newStats = stats
                .incrementSwipes()
                .addMerges(totalMerges)
                .updateHighestTile(highestTile)
            val updated = copy(
                grid = newGrid,
                gridB = newGridB,
                gridC = newGridC,
                score = newScore,
                stats = newStats,
                lastSpawnedPosition = null
            )
            if (spawn) updated.spawnRandomTile() else updated
        } else {
            this
        }
    }

    // Helper functions for sliding - now return merge count and highest tile
    private fun slideRowLeft(row: List<Int>): SlideResult {
        val nonZeros = row.filter { it != 0 }.toMutableList()
        var score = 0
        var mergeCount = 0
        var maxTile = 0

        // Merge adjacent identical values
        var i = 0
        while (i < nonZeros.size - 1) {
            if (nonZeros[i] == nonZeros[i + 1]) {
                nonZeros[i] *= 2
                score += nonZeros[i]
                mergeCount++
                if (nonZeros[i] > maxTile) maxTile = nonZeros[i]
                nonZeros.removeAt(i + 1)
            }
            i++
        }

        // Find max tile in the row
        nonZeros.forEach { if (it > maxTile) maxTile = it }

        // Pad with zeros to maintain row size
        while (nonZeros.size < row.size) {
            nonZeros.add(0)
        }

        return SlideResult(nonZeros, score, mergeCount, maxTile)
    }

    private fun slideRowRight(row: List<Int>): SlideResult {
        val nonZeros = row.filter { it != 0 }.toMutableList()
        var score = 0
        var mergeCount = 0
        var maxTile = 0

        // Merge adjacent identical values from right
        var i = nonZeros.size - 1
        while (i > 0) {
            if (nonZeros[i] == nonZeros[i - 1]) {
                nonZeros[i] *= 2
                score += nonZeros[i]
                mergeCount++
                if (nonZeros[i] > maxTile) maxTile = nonZeros[i]
                nonZeros.removeAt(i - 1)
                i--
            }
            i--
        }

        // Find max tile in the row
        nonZeros.forEach { if (it > maxTile) maxTile = it }

        // Pad with zeros at the beginning
        val result = mutableListOf<Int>()
        repeat(row.size - nonZeros.size) { result.add(0) }
        result.addAll(nonZeros)

        return SlideResult(result, score, mergeCount, maxTile)
    }

    // Grid conversion functions
    private fun convertMainToB(main: List<MutableList<Int>>): List<MutableList<Int>> {
        val gridB = mutableListOf<MutableList<Int>>()
        for (row in 0 until GRID_SIZE) {
            val rowSize = 2 * row + 1
            gridB.add(MutableList(rowSize) { 0 })
        }

        // Mapping from main to B as provided
        gridB[0][0] = main[3][6]
        gridB[1][0] = main[2][4]
        gridB[1][1] = main[3][5]
        gridB[1][2] = main[3][4]
        gridB[2][0] = main[1][2]
        gridB[2][1] = main[2][3]
        gridB[2][2] = main[2][2]
        gridB[2][3] = main[3][3]
        gridB[2][4] = main[3][2]
        gridB[3][0] = main[0][0]
        gridB[3][1] = main[1][1]
        gridB[3][2] = main[1][0]
        gridB[3][3] = main[2][1]
        gridB[3][4] = main[2][0]
        gridB[3][5] = main[3][1]
        gridB[3][6] = main[3][0]

        return gridB
    }

    private fun convertMainToC(main: List<MutableList<Int>>): List<MutableList<Int>> {
        val gridC = mutableListOf<MutableList<Int>>()
        for (row in 0 until GRID_SIZE) {
            val rowSize = 2 * row + 1
            gridC.add(MutableList(rowSize) { 0 })
        }

        // Mapping from main to C as provided
        gridC[0][0] = main[3][0]
        gridC[1][0] = main[3][2]
        gridC[1][1] = main[3][1]
        gridC[1][2] = main[2][0]
        gridC[2][0] = main[3][4]
        gridC[2][1] = main[3][3]
        gridC[2][2] = main[2][2]
        gridC[2][3] = main[2][1]
        gridC[2][4] = main[1][0]
        gridC[3][0] = main[3][6]
        gridC[3][1] = main[3][5]
        gridC[3][2] = main[2][4]
        gridC[3][3] = main[2][3]
        gridC[3][4] = main[1][2]
        gridC[3][5] = main[1][1]
        gridC[3][6] = main[0][0]

        return gridC
    }

    private fun convertBToMain(b: List<MutableList<Int>>): List<MutableList<Int>> {
        val main = mutableListOf<MutableList<Int>>()
        for (row in 0 until GRID_SIZE) {
            val rowSize = 2 * row + 1
            main.add(MutableList(rowSize) { 0 })
        }

        // Reverse mapping from B to main
        main[3][6] = b[0][0]
        main[2][4] = b[1][0]
        main[3][5] = b[1][1]
        main[3][4] = b[1][2]
        main[1][2] = b[2][0]
        main[2][3] = b[2][1]
        main[2][2] = b[2][2]
        main[3][3] = b[2][3]
        main[3][2] = b[2][4]
        main[0][0] = b[3][0]
        main[1][1] = b[3][1]
        main[1][0] = b[3][2]
        main[2][1] = b[3][3]
        main[2][0] = b[3][4]
        main[3][1] = b[3][5]
        main[3][0] = b[3][6]

        return main
    }

    private fun convertCToMain(c: List<MutableList<Int>>): List<MutableList<Int>> {
        val main = mutableListOf<MutableList<Int>>()
        for (row in 0 until GRID_SIZE) {
            val rowSize = 2 * row + 1
            main.add(MutableList(rowSize) { 0 })
        }

        // Reverse mapping from C to main
        main[3][0] = c[0][0]
        main[3][2] = c[1][0]
        main[3][1] = c[1][1]
        main[2][0] = c[1][2]
        main[3][4] = c[2][0]
        main[3][3] = c[2][1]
        main[2][2] = c[2][2]
        main[2][1] = c[2][3]
        main[1][0] = c[2][4]
        main[3][6] = c[3][0]
        main[3][5] = c[3][1]
        main[2][4] = c[3][2]
        main[2][3] = c[3][3]
        main[1][2] = c[3][4]
        main[1][1] = c[3][5]
        main[0][0] = c[3][6]

        return main
    }
}

// Helper data class for slide results
private data class SlideResult(
    val row: List<Int>,
    val score: Int,
    val mergeCount: Int,
    val maxTile: Int
)

// Add the enum to this file for use in the canMove function
enum class SwipeDirection {
    LEFT, RIGHT, TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT
}