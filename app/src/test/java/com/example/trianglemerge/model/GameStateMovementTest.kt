package com.example.trianglemerge.model

import org.junit.Assert.assertEquals
import org.junit.Test

class GameStateMovementTest {
    private fun baseGrid(): List<MutableList<Int>> {
        return listOf(
            mutableListOf(0),
            mutableListOf(2, 2, 0),
            mutableListOf(0, 2, 0, 2, 0),
            mutableListOf(0, 2, 0, 2, 0, 2, 0)
        )
    }

    @Test
    fun slideLeftMovesCorrectly() {
        val state = GameState(baseGrid())
        val result = state.slideLeft(spawn = false)
        val expected = listOf(
            listOf(0),
            listOf(4, 0, 0),
            listOf(4, 0, 0, 0, 0),
            listOf(4, 2, 0, 0, 0, 0, 0)
        )
        assertEquals(expected, result.grid)
    }

    @Test
    fun slideRightMovesCorrectly() {
        val state = GameState(baseGrid())
        val result = state.slideRight(spawn = false)
        val expected = listOf(
            listOf(0),
            listOf(0, 0, 4),
            listOf(0, 0, 0, 0, 4),
            listOf(0, 0, 0, 0, 0, 2, 4)
        )
        assertEquals(expected, result.grid)
    }

    @Test
    fun slideTopLeftMovesCorrectly() {
        val state = GameState(baseGrid())
        val result = state.slideTopLeft(spawn = false)
        val expected = listOf(
            listOf(0),
            listOf(2, 4, 2),
            listOf(0, 0, 0, 4, 0),
            listOf(0, 0, 0, 0, 0, 2, 0)
        )
        assertEquals(expected, result.grid)
    }

    @Test
    fun slideTopRightMovesCorrectly() {
        val state = GameState(baseGrid())
        val result = state.slideTopRight(spawn = false)
        val expected = listOf(
            listOf(0),
            listOf(0, 0, 0),
            listOf(0, 0, 0, 0, 0),
            listOf(4, 4, 4, 0, 0, 2, 0)
        )
        assertEquals(expected, result.grid)
    }

    @Test
    fun slideBottomLeftMovesCorrectly() {
        val state = GameState(baseGrid())
        val result = state.slideBottomLeft(spawn = false)
        val expected = listOf(
            listOf(0),
            listOf(2, 0, 0),
            listOf(0, 0, 0, 0, 0),
            listOf(0, 4, 2, 4, 0, 2, 0)
        )
        assertEquals(expected, result.grid)
    }

    @Test
    fun slideBottomRightMovesCorrectly() {
        val state = GameState(baseGrid())
        val result = state.slideBottomRight(spawn = false)
        val expected = listOf(
            listOf(0),
            listOf(4, 4, 4),
            listOf(0, 0, 0, 0, 0),
            listOf(0, 0, 0, 0, 0, 2, 0)
        )
        assertEquals(expected, result.grid)
    }
}
