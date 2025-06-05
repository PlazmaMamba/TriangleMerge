package com.example.trianglemerge

import com.example.trianglemerge.model.GameState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DiagonalMovementTest {

    private fun emptyGrid(): MutableList<MutableList<Int>> {
        val grid = mutableListOf<MutableList<Int>>()
        for (row in 0 until GameState.GRID_SIZE) {
            grid.add(MutableList(2 * row + 1) { 0 })
        }
        return grid
    }

    private fun assertGridIgnoringRandom(actual: List<MutableList<Int>>, expected: List<List<Int>>) {
        var diffCount = 0
        for (r in expected.indices) {
            for (c in expected[r].indices) {
                val exp = expected[r][c]
                val act = actual[r][c]
                if (exp != act) {
                    diffCount++
                    assertEquals("Expected empty cell at diff position", 0, exp)
                    assertTrue("Random tile value should be 2 or 4", act == 2 || act == 4)
                }
            }
        }
        assertEquals("Only one random tile should differ", 1, diffCount)
    }

    @Test
    fun slideTopLeft_merges_diagonal() {
        val grid = emptyGrid()
        grid[1][1] = 2
        grid[2][2] = 2
        val state = GameState(grid)
        val newState = state.slideTopLeft()

        val expected = emptyGrid()
        expected[1][1] = 4

        assertEquals(4, newState.score)
        assertGridIgnoringRandom(newState.grid, expected)
    }

    @Test
    fun slideTopRight_merges_diagonal() {
        val grid = emptyGrid()
        grid[1][1] = 2
        grid[2][0] = 2
        val state = GameState(grid)
        val newState = state.slideTopRight()

        val expected = emptyGrid()
        expected[3][0] = 4

        assertEquals(4, newState.score)
        assertGridIgnoringRandom(newState.grid, expected)
    }

    @Test
    fun slideBottomLeft_merges_diagonal() {
        val grid = emptyGrid()
        grid[3][2] = 2
        grid[2][2] = 2
        val state = GameState(grid)
        val newState = state.slideBottomLeft()

        val expected = emptyGrid()
        expected[3][1] = 4

        assertEquals(4, newState.score)
        assertGridIgnoringRandom(newState.grid, expected)
    }

    @Test
    fun slideBottomRight_merges_diagonal() {
        val grid = emptyGrid()
        grid[3][3] = 2
        grid[2][2] = 2
        val state = GameState(grid)
        val newState = state.slideBottomRight()

        val expected = emptyGrid()
        expected[1][2] = 4

        assertEquals(4, newState.score)
        assertGridIgnoringRandom(newState.grid, expected)
    }
}
