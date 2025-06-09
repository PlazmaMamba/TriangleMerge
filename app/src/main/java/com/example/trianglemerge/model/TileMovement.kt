package com.example.trianglemerge.model

import com.example.trianglemerge.ui.screens.SwipeDirection

data class TileMovement(
    val value: Int,
    val fromRow: Int,
    val fromCol: Int,
    val toRow: Int,
    val toCol: Int,
    val isMerge: Boolean = false
)

data class AnimationState(
    val movements: List<TileMovement> = emptyList(),
    val direction: SwipeDirection? = null,
    val newTilePosition: Pair<Int, Int>? = null
)

// Extension function to calculate movements for a swipe
fun GameState.calculateMovements(direction: SwipeDirection, newState: GameState): AnimationState {
    val movements = mutableListOf<TileMovement>()

    // For now, we'll just track that an animation should occur
    // In a full implementation, you would track each tile's movement

    return AnimationState(
        movements = movements,
        direction = direction,
        newTilePosition = newState.lastSpawnedPosition
    )
}