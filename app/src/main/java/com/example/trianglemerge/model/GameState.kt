package com.example.trianglemerge.model

import androidx.compose.ui.graphics.Color
import kotlin.random.Random

data class GameState(
    val grid: List<MutableList<Int>>, // Triangular grid: [[1], [3], [5], [7]] - each row has 2*row+1 cells
    val score: Int = 0,
    val isGameOver: Boolean = false
) {
    companion object {
        const val GRID_SIZE = 4 // Number of rows: [1], [3], [5], [7]

        fun initial(): GameState {
            val grid = mutableListOf<MutableList<Int>>()
            for (row in 0 until GRID_SIZE) {
                val rowSize = 2 * row + 1 // Row 0: 1, Row 1: 3, Row 2: 5, Row 3: 7
                grid.add(MutableList(rowSize) { 0 })
            }

            var newState = GameState(grid)
            // Spawn two initial tiles
            newState = newState.spawnRandomTile()
            newState = newState.spawnRandomTile()

            return newState
        }
    }

    fun spawnRandomTile(): GameState {
        val emptyCells = mutableListOf<Pair<Int, Int>>()

        // Find all empty cells in the triangular grid
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

        // Create new grid with the spawned tile
        val newGrid = grid.map { it.toMutableList() }.toMutableList()
        newGrid[row][col] = value

        return this.copy(grid = newGrid, isGameOver = checkGameOver())
    }

    fun isTriangleUpward(row: Int, col: Int): Boolean {
        // In a triangular tessellation, triangles alternate
        // Even columns are upward triangles, odd columns are downward
        return col % 2 == 0
    }

    private fun checkGameOver(): Boolean {
        // Check if there are any empty cells
        for (row in grid.indices) {
            for (col in grid[row].indices) {
                if (grid[row][col] == 0) return false
            }
        }

        // Check if any moves are possible
        val testState = this
        if (testState.slideLeft().grid != grid) return false
        if (testState.slideRight().grid != grid) return false
        if (testState.slideTopLeft().grid != grid) return false
        if (testState.slideTopRight().grid != grid) return false
        if (testState.slideBottomLeft().grid != grid) return false
        if (testState.slideBottomRight().grid != grid) return false

        return true
    }

    // Sliding functions for 6 directions
    fun slideLeft(): GameState {
        var newScore = score
        val newGrid = grid.map { row ->
            val (slidRow, scoreGained) = slideRowLeft(row.toList())
            newScore += scoreGained
            slidRow.toMutableList()
        }.toMutableList()

        return if (newGrid != grid) {
            GameState(newGrid, newScore).spawnRandomTile()
        } else {
            this
        }
    }

    fun slideRight(): GameState {
        var newScore = score
        val newGrid = grid.map { row ->
            val (slidRow, scoreGained) = slideRowRight(row.toList())
            newScore += scoreGained
            slidRow.toMutableList()
        }.toMutableList()

        return if (newGrid != grid) {
            GameState(newGrid, newScore).spawnRandomTile()
        } else {
            this
        }
    }

    fun slideTopLeft(): GameState {
        var newScore = score
        val newGrid = grid.map { it.toMutableList() }.toMutableList()
        var changed = false
        val processed = mutableSetOf<Pair<Int, Int>>()

        // Process all diagonals going from top-left to bottom-right
        for (row in 0 until GRID_SIZE) {
            for (col in 0 until grid[row].size) {
                if (!processed.contains(row to col)) {
                    val positions = extractDiagonalTopLeftPositions(row, col)
                    if (positions.size > 1) {
                        positions.forEach { processed.add(it) }
                        val diagonal = positions.map { (r, c) -> grid[r][c] }
                        val (slidDiagonal, scoreGained) = slideRowLeft(diagonal)
                        newScore += scoreGained
                        if (slidDiagonal != diagonal) {
                            changed = true
                            for (i in positions.indices) {
                                val (r, c) = positions[i]
                                newGrid[r][c] = slidDiagonal[i]
                            }
                        }
                    }
                }
            }
        }

        return if (changed) {
            GameState(newGrid, newScore).spawnRandomTile()
        } else {
            this
        }
    }

    fun slideTopRight(): GameState {
        var newScore = score
        val newGrid = grid.map { it.toMutableList() }.toMutableList()
        var changed = false
        val processed = mutableSetOf<Pair<Int, Int>>()

        // Process all diagonals going from top-right to bottom-left
        for (row in 0 until GRID_SIZE) {
            for (col in 0 until grid[row].size) {
                if (!processed.contains(row to col)) {
                    val positions = extractDiagonalTopRightPositions(row, col)
                    if (positions.size > 1) {
                        positions.forEach { processed.add(it) }
                        val diagonal = positions.map { (r, c) -> grid[r][c] }
                        val (slidDiagonal, scoreGained) = slideRowRight(diagonal)
                        newScore += scoreGained
                        if (slidDiagonal != diagonal) {
                            changed = true
                            for (i in positions.indices) {
                                val (r, c) = positions[i]
                                newGrid[r][c] = slidDiagonal[i]
                            }
                        }
                    }
                }
            }
        }

        return if (changed) {
            GameState(newGrid, newScore).spawnRandomTile()
        } else {
            this
        }
    }

    fun slideBottomLeft(): GameState {
        var newScore = score
        val newGrid = grid.map { it.toMutableList() }.toMutableList()
        var changed = false
        val processed = mutableSetOf<Pair<Int, Int>>()

        // Process all diagonals going from bottom-left to top-right
        for (row in GRID_SIZE - 1 downTo 0) {
            for (col in 0 until grid[row].size) {
                if (!processed.contains(row to col)) {
                    val positions = extractDiagonalBottomLeftPositions(row, col)
                    if (positions.size > 1) {
                        positions.forEach { processed.add(it) }
                        val diagonal = positions.map { (r, c) -> grid[r][c] }
                        val (slidDiagonal, scoreGained) = slideRowLeft(diagonal)
                        newScore += scoreGained
                        if (slidDiagonal != diagonal) {
                            changed = true
                            for (i in positions.indices) {
                                val (r, c) = positions[i]
                                newGrid[r][c] = slidDiagonal[i]
                            }
                        }
                    }
                }
            }
        }

        return if (changed) {
            GameState(newGrid, newScore).spawnRandomTile()
        } else {
            this
        }
    }

    fun slideBottomRight(): GameState {
        var newScore = score
        val newGrid = grid.map { it.toMutableList() }.toMutableList()
        var changed = false
        val processed = mutableSetOf<Pair<Int, Int>>()

        // Process all diagonals going from bottom-right to top-left
        for (row in GRID_SIZE - 1 downTo 0) {
            for (col in 0 until grid[row].size) {
                if (!processed.contains(row to col)) {
                    val positions = extractDiagonalBottomRightPositions(row, col)
                    if (positions.size > 1) {
                        positions.forEach { processed.add(it) }
                        val diagonal = positions.map { (r, c) -> grid[r][c] }
                        val (slidDiagonal, scoreGained) = slideRowRight(diagonal)
                        newScore += scoreGained
                        if (slidDiagonal != diagonal) {
                            changed = true
                            for (i in positions.indices) {
                                val (r, c) = positions[i]
                                newGrid[r][c] = slidDiagonal[i]
                            }
                        }
                    }
                }
            }
        }

        return if (changed) {
            GameState(newGrid, newScore).spawnRandomTile()
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

    // Diagonal extraction functions for triangular grid
    // Based on the triangular tessellation pattern where nodes connect in specific patterns

    private fun extractDiagonalTopRight(startRow: Int, startCol: Int): List<Int> {
        val diagonal = mutableListOf<Int>()
        var row = startRow
        var col = startCol
        var moveToSameRow = true

        diagonal.add(grid[row][col])

        while (true) {
            if (moveToSameRow) {
                // Move within same row
                if (col % 2 == 0) {
                    // Even index: move right
                    col++
                } else {
                    // Odd index: move left
                    col--
                }

                if (col >= 0 && col < grid[row].size) {
                    diagonal.add(grid[row][col])
                    moveToSameRow = false
                } else {
                    break
                }
            } else {
                // Move to next row
                row++
                if (row < GRID_SIZE && col < grid[row].size) {
                    diagonal.add(grid[row][col])
                    moveToSameRow = true
                } else {
                    break
                }
            }
        }

        return diagonal
    }

    private fun extractDiagonalTopRightPositions(startRow: Int, startCol: Int): List<Pair<Int, Int>> {
        val positions = mutableListOf<Pair<Int, Int>>()
        var row = startRow
        var col = startCol
        var moveToSameRow = true

        positions.add(row to col)

        while (true) {
            if (moveToSameRow) {
                if (col % 2 == 0) {
                    col++
                } else {
                    col--
                }

                if (col >= 0 && col < grid[row].size) {
                    positions.add(row to col)
                    moveToSameRow = false
                } else {
                    break
                }
            } else {
                row++
                if (row < GRID_SIZE && col < grid[row].size) {
                    positions.add(row to col)
                    moveToSameRow = true
                } else {
                    break
                }
            }
        }

        return positions
    }

    private fun extractDiagonalTopLeft(startRow: Int, startCol: Int): List<Int> {
        val diagonal = mutableListOf<Int>()
        var row = startRow
        var col = startCol
        var moveToSameRow = true

        diagonal.add(grid[row][col])

        while (true) {
            if (moveToSameRow) {
                // Move within same row - opposite of TopRight
                if (col % 2 == 0) {
                    // Even index: move left
                    col--
                } else {
                    // Odd index: move right
                    col++
                }

                if (col >= 0 && col < grid[row].size) {
                    diagonal.add(grid[row][col])
                    moveToSameRow = false
                } else {
                    break
                }
            } else {
                // Move to next row
                row++
                if (row < GRID_SIZE && col < grid[row].size) {
                    diagonal.add(grid[row][col])
                    moveToSameRow = true
                } else {
                    break
                }
            }
        }

        return diagonal
    }

    private fun extractDiagonalTopLeftPositions(startRow: Int, startCol: Int): List<Pair<Int, Int>> {
        val positions = mutableListOf<Pair<Int, Int>>()
        var row = startRow
        var col = startCol
        var moveToSameRow = true

        positions.add(row to col)

        while (true) {
            if (moveToSameRow) {
                if (col % 2 == 0) {
                    col--
                } else {
                    col++
                }

                if (col >= 0 && col < grid[row].size) {
                    positions.add(row to col)
                    moveToSameRow = false
                } else {
                    break
                }
            } else {
                row++
                if (row < GRID_SIZE && col < grid[row].size) {
                    positions.add(row to col)
                    moveToSameRow = true
                } else {
                    break
                }
            }
        }

        return positions
    }

    private fun extractDiagonalBottomRight(startRow: Int, startCol: Int): List<Int> {
        val diagonal = mutableListOf<Int>()
        var row = startRow
        var col = startCol
        var moveToSameRow = true

        diagonal.add(grid[row][col])

        while (true) {
            if (moveToSameRow) {
                // Move within same row - same as TopRight but going up
                if (col % 2 == 0) {
                    col++
                } else {
                    col--
                }

                if (col >= 0 && col < grid[row].size) {
                    diagonal.add(grid[row][col])
                    moveToSameRow = false
                } else {
                    break
                }
            } else {
                // Move to previous row
                row--
                if (row >= 0 && col < grid[row].size) {
                    diagonal.add(grid[row][col])
                    moveToSameRow = true
                } else {
                    break
                }
            }
        }

        return diagonal
    }

    private fun extractDiagonalBottomRightPositions(startRow: Int, startCol: Int): List<Pair<Int, Int>> {
        val positions = mutableListOf<Pair<Int, Int>>()
        var row = startRow
        var col = startCol
        var moveToSameRow = true

        positions.add(row to col)

        while (true) {
            if (moveToSameRow) {
                if (col % 2 == 0) {
                    col++
                } else {
                    col--
                }

                if (col >= 0 && col < grid[row].size) {
                    positions.add(row to col)
                    moveToSameRow = false
                } else {
                    break
                }
            } else {
                row--
                if (row >= 0 && col < grid[row].size) {
                    positions.add(row to col)
                    moveToSameRow = true
                } else {
                    break
                }
            }
        }

        return positions
    }

    private fun extractDiagonalBottomLeft(startRow: Int, startCol: Int): List<Int> {
        val diagonal = mutableListOf<Int>()
        var row = startRow
        var col = startCol
        var moveToSameRow = true

        diagonal.add(grid[row][col])

        while (true) {
            if (moveToSameRow) {
                // Move within same row - same as TopLeft but going up
                if (col % 2 == 0) {
                    col--
                } else {
                    col++
                }

                if (col >= 0 && col < grid[row].size) {
                    diagonal.add(grid[row][col])
                    moveToSameRow = false
                } else {
                    break
                }
            } else {
                // Move to previous row
                row--
                if (row >= 0 && col < grid[row].size) {
                    diagonal.add(grid[row][col])
                    moveToSameRow = true
                } else {
                    break
                }
            }
        }

        return diagonal
    }

    private fun extractDiagonalBottomLeftPositions(startRow: Int, startCol: Int): List<Pair<Int, Int>> {
        val positions = mutableListOf<Pair<Int, Int>>()
        var row = startRow
        var col = startCol
        var moveToSameRow = true

        positions.add(row to col)

        while (true) {
            if (moveToSameRow) {
                if (col % 2 == 0) {
                    col--
                } else {
                    col++
                }

                if (col >= 0 && col < grid[row].size) {
                    positions.add(row to col)
                    moveToSameRow = false
                } else {
                    break
                }
            } else {
                row--
                if (row >= 0 && col < grid[row].size) {
                    positions.add(row to col)
                    moveToSameRow = true
                } else {
                    break
                }
            }
        }

        return positions
    }
}