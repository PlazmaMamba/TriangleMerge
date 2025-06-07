package com.example.trianglemerge.model

import kotlin.random.Random

data class GameState(
    val grid: List<MutableList<Int>>, // Main triangular grid
    val gridB: List<MutableList<Int>>, // Grid B for vertical right swipes
    val gridC: List<MutableList<Int>>, // Grid C for vertical left swipes
    val score: Int = 0,
    val isGameOver: Boolean = false
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

        return this.copy(grid = newGrid, gridB = newGridB, gridC = newGridC, isGameOver = checkGameOver())
    }

    fun isTriangleUpward(row: Int, col: Int): Boolean = col % 2 == 0

    private fun checkGameOver(): Boolean {
        // Check if there are any empty cells
        for (row in grid.indices) {
            for (col in grid[row].indices) {
                if (grid[row][col] == 0) return false
            }
        }

        // Check if any moves are possible
        if (slideLeft(false).grid != grid) return false
        if (slideRight(false).grid != grid) return false
        if (slideTopLeft(false).grid != grid) return false
        if (slideTopRight(false).grid != grid) return false
        if (slideBottomLeft(false).grid != grid) return false
        if (slideBottomRight(false).grid != grid) return false

        return true
    }

    // Sliding functions for 6 directions
    fun slideLeft(spawn: Boolean = true): GameState {
        var newScore = score
        val newGrid = grid.map { row ->
            val (slidRow, scoreGained) = slideRowLeft(row.toList())
            newScore += scoreGained
            slidRow.toMutableList()
        }.toMutableList()

        return if (newGrid != grid) {
            // Update grids B and C based on the new main grid
            val newGridB = convertMainToB(newGrid)
            val newGridC = convertMainToC(newGrid)
            val updated = GameState(newGrid, newGridB, newGridC, newScore)
            if (spawn) updated.spawnRandomTile() else updated
        } else {
            this
        }
    }

    fun slideRight(spawn: Boolean = true): GameState {
        var newScore = score
        val newGrid = grid.map { row ->
            val (slidRow, scoreGained) = slideRowRight(row.toList())
            newScore += scoreGained
            slidRow.toMutableList()
        }.toMutableList()

        return if (newGrid != grid) {
            // Update grids B and C based on the new main grid
            val newGridB = convertMainToB(newGrid)
            val newGridC = convertMainToC(newGrid)
            val updated = GameState(newGrid, newGridB, newGridC, newScore)
            if (spawn) updated.spawnRandomTile() else updated
        } else {
            this
        }
    }

    fun slideTopLeft(spawn: Boolean = true): GameState {
        var newScore = score
        // Use grid C for top-left swipes (slide right on C)
        val newGridC = gridC.map { row ->
            val (slidRow, scoreGained) = slideRowRight(row.toList())
            newScore += scoreGained
            slidRow.toMutableList()
        }.toMutableList()

        return if (newGridC != gridC) {
            // Convert back to main grid and update B
            val newGrid = convertCToMain(newGridC)
            val newGridB = convertMainToB(newGrid)
            val updated = GameState(newGrid, newGridB, newGridC, newScore)
            if (spawn) updated.spawnRandomTile() else updated
        } else {
            this
        }
    }

    fun slideTopRight(spawn: Boolean = true): GameState {
        var newScore = score
        // Use grid B for top-right swipes (slide left on B)
        val newGridB = gridB.map { row ->
            val (slidRow, scoreGained) = slideRowLeft(row.toList())
            newScore += scoreGained
            slidRow.toMutableList()
        }.toMutableList()

        return if (newGridB != gridB) {
            // Convert back to main grid and update C
            val newGrid = convertBToMain(newGridB)
            val newGridC = convertMainToC(newGrid)
            val updated = GameState(newGrid, newGridB, newGridC, newScore)
            if (spawn) updated.spawnRandomTile() else updated
        } else {
            this
        }
    }

    fun slideBottomLeft(spawn: Boolean = true): GameState {
        var newScore = score
        // Use grid B for bottom-left swipes (slide right on B)
        val newGridB = gridB.map { row ->
            val (slidRow, scoreGained) = slideRowRight(row.toList())
            newScore += scoreGained
            slidRow.toMutableList()
        }.toMutableList()

        return if (newGridB != gridB) {
            // Convert back to main grid and update C
            val newGrid = convertBToMain(newGridB)
            val newGridC = convertMainToC(newGrid)
            val updated = GameState(newGrid, newGridB, newGridC, newScore)
            if (spawn) updated.spawnRandomTile() else updated
        } else {
            this
        }
    }

    fun slideBottomRight(spawn: Boolean = true): GameState {
        var newScore = score
        // Use grid C for bottom-right swipes (slide left on C)
        val newGridC = gridC.map { row ->
            val (slidRow, scoreGained) = slideRowLeft(row.toList())
            newScore += scoreGained
            slidRow.toMutableList()
        }.toMutableList()

        return if (newGridC != gridC) {
            // Convert back to main grid and update B
            val newGrid = convertCToMain(newGridC)
            val newGridB = convertMainToB(newGrid)
            val updated = GameState(newGrid, newGridB, newGridC, newScore)
            if (spawn) updated.spawnRandomTile() else updated
        } else {
            this
        }
    }

    // Helper functions for sliding
    private fun slideRowLeft(row: List<Int>): Pair<List<Int>, Int> {
        val nonZeros = row.filter { it != 0 }.toMutableList()
        var score = 0

        // Merge adjacent identical values
        var i = 0
        while (i < nonZeros.size - 1) {
            if (nonZeros[i] == nonZeros[i + 1]) {
                nonZeros[i] *= 2
                score += nonZeros[i]
                nonZeros.removeAt(i + 1)
            }
            i++
        }

        // Pad with zeros to maintain row size
        while (nonZeros.size < row.size) {
            nonZeros.add(0)
        }

        return Pair(nonZeros, score)
    }

    private fun slideRowRight(row: List<Int>): Pair<List<Int>, Int> {
        val nonZeros = row.filter { it != 0 }.toMutableList()
        var score = 0

        // Merge adjacent identical values from right
        var i = nonZeros.size - 1
        while (i > 0) {
            if (nonZeros[i] == nonZeros[i - 1]) {
                nonZeros[i] *= 2
                score += nonZeros[i]
                nonZeros.removeAt(i - 1)
                i--
            }
            i--
        }

        // Pad with zeros at the beginning
        val result = mutableListOf<Int>()
        repeat(row.size - nonZeros.size) { result.add(0) }
        result.addAll(nonZeros)

        return Pair(result, score)
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