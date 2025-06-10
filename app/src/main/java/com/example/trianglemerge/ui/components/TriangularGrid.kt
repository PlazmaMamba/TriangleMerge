package com.example.trianglemerge.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.example.trianglemerge.model.GameState
import com.example.trianglemerge.ui.theme.TileColors
import kotlinx.coroutines.launch
import kotlin.math.sqrt

// Data class to hold triangle position information
data class TriangleInfo(
    val row: Int,
    val col: Int,
    val center: Offset,
    val path: Path,
    val isUpward: Boolean
)

// Data class for animated tiles
data class AnimatedTile(
    val id: String, // Unique ID for tracking tiles
    val value: Int,
    val fromPosition: Offset,
    val toPosition: Offset,
    val fromRow: Int,
    val fromCol: Int,
    val toRow: Int,
    val toCol: Int,
    val animationProgress: Animatable<Float, AnimationVector1D> = Animatable(0f),
    val scaleAnimation: Animatable<Float, AnimationVector1D> = Animatable(1f),
    val isNew: Boolean = false,
    val isMerging: Boolean = false
)

@Composable
fun TriangularGrid(
    gameState: GameState,
    modifier: Modifier = Modifier
) {
    // Store triangle positions for reference
    var trianglePositions by remember { mutableStateOf(emptyList<TriangleInfo>()) }

    // Store animated tiles
    var animatedTiles by remember { mutableStateOf(emptyList<AnimatedTile>()) }

    // Track previous game state for animations
    var previousGameState by remember { mutableStateOf(gameState) }

    val coroutineScope = rememberCoroutineScope()

    // Animation for the new tile highlight
    val infiniteTransition = rememberInfiniteTransition()
    val highlightAnimation by infiniteTransition.animateFloat(
        initialValue = 3f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    val glowAnimation by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    // Update animations when game state changes
    LaunchedEffect(gameState) {
        if (trianglePositions.isNotEmpty() && previousGameState != gameState) {
            // Create animations for tiles
            val newAnimatedTiles = mutableListOf<AnimatedTile>()

            // Find all non-zero tiles in current state
            for (row in gameState.grid.indices) {
                for (col in gameState.grid[row].indices) {
                    val value = gameState.grid[row][col]
                    if (value > 0) {
                        val currentPos = trianglePositions.find { it.row == row && it.col == col }
                        val isNewTile = gameState.lastSpawnedPosition == (row to col)

                        if (currentPos != null) {
                            // Check if this tile existed before
                            val previousValue = previousGameState.grid.getOrNull(row)?.getOrNull(col) ?: 0

                            val tile = AnimatedTile(
                                id = "$row-$col-$value",
                                value = value,
                                fromPosition = currentPos.center,
                                toPosition = currentPos.center,
                                fromRow = row,
                                fromCol = col,
                                toRow = row,
                                toCol = col,
                                isNew = isNewTile || previousValue == 0,
                                isMerging = previousValue > 0 && previousValue != value
                            )

                            newAnimatedTiles.add(tile)
                        }
                    }
                }
            }

            animatedTiles = newAnimatedTiles

            // Animate new tiles and merges
            coroutineScope.launch {
                animatedTiles.forEach { tile ->
                    launch {
                        if (tile.isNew) {
                            // Spawn animation
                            tile.scaleAnimation.animateTo(
                                targetValue = 1f,
                                animationSpec = spring(
                                    dampingRatio = 0.6f,
                                    stiffness = 300f
                                ),
                                initialVelocity = 0f
                            )
                        } else if (tile.isMerging) {
                            // Merge animation
                            tile.scaleAnimation.animateTo(
                                targetValue = 1.2f,
                                animationSpec = tween(150)
                            )
                            tile.scaleAnimation.animateTo(
                                targetValue = 1f,
                                animationSpec = tween(150)
                            )
                        }
                    }
                }
            }

            previousGameState = gameState
        }
    }

    Box(modifier = modifier) {
        // Layer 1: Static grid background
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val positions = calculateTrianglePositions(size.width, size.height, gameState)
            trianglePositions = positions

            // Draw static grid structure
            drawStaticGrid(positions)
        }

        // Layer 2: Animated tiles
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            // Draw current state tiles (non-animated)
            trianglePositions.forEach { triangleInfo ->
                val value = gameState.grid[triangleInfo.row][triangleInfo.col]
                if (value > 0) {
                    val isNewTile = gameState.lastSpawnedPosition == (triangleInfo.row to triangleInfo.col)

                    // Find if this tile has animation
                    val animatedTile = animatedTiles.find {
                        it.toRow == triangleInfo.row && it.toCol == triangleInfo.col
                    }

                    val scale = animatedTile?.scaleAnimation?.value ?: 1f

                    drawTile(
                        triangleInfo = triangleInfo,
                        value = value,
                        isNewTile = isNewTile,
                        highlightWidth = highlightAnimation,
                        glowAlpha = glowAnimation,
                        scale = scale
                    )
                }
            }
        }
    }
}

// Calculate all triangle positions once
private fun calculateTrianglePositions(
    canvasWidth: Float,
    canvasHeight: Float,
    gameState: GameState
): List<TriangleInfo> {
    val positions = mutableListOf<TriangleInfo>()
    val centerX = canvasWidth / 2

    val totalRows = GameState.GRID_SIZE
    val maxRowWidth = totalRows

    val triangleSize = (canvasWidth * 0.9f * 2) / (maxRowWidth + 1)
    val triangleHeight = triangleSize * sqrt(3f) / 2f
    val rowVerticalSpacing = triangleHeight

    val totalHeight = totalRows * rowVerticalSpacing
    val startY = (canvasHeight - totalHeight) / 2 + triangleHeight / 2

    for (row in gameState.grid.indices) {
        val rowCells = gameState.grid[row]
        val y = startY + row * rowVerticalSpacing

        val rowWidth = (rowCells.size - 1) * triangleSize / 2
        val startX = centerX - rowWidth / 2

        for (col in rowCells.indices) {
            val x = startX + col * triangleSize / 2
            val isUpward = gameState.isTriangleUpward(row, col)
            val center = Offset(x, y)

            val path = createTrianglePath(center, triangleSize, triangleHeight, isUpward)

            positions.add(
                TriangleInfo(
                    row = row,
                    col = col,
                    center = center,
                    path = path,
                    isUpward = isUpward
                )
            )
        }
    }

    return positions
}

// Create a triangle path
private fun createTrianglePath(
    center: Offset,
    size: Float,
    height: Float,
    isUpward: Boolean
): Path {
    val halfSize = size / 2f

    return Path().apply {
        if (isUpward) {
            moveTo(center.x, center.y - height / 2)
            lineTo(center.x - halfSize, center.y + height / 2)
            lineTo(center.x + halfSize, center.y + height / 2)
        } else {
            moveTo(center.x, center.y + height / 2)
            lineTo(center.x - halfSize, center.y - height / 2)
            lineTo(center.x + halfSize, center.y - height / 2)
        }
        close()
    }
}

// Draw the static grid structure
private fun DrawScope.drawStaticGrid(positions: List<TriangleInfo>) {
    // First pass: Draw all triangle backgrounds
    positions.forEach { triangleInfo ->
        // Light background for empty cells
        drawPath(
            path = triangleInfo.path,
            color = Color(0xFFCDC1B4) // Light beige color for empty cells
        )
    }

    // Second pass: Draw all borders
    positions.forEach { triangleInfo ->
        drawPath(
            path = triangleInfo.path,
            color = Color(0xFF000000), // Darker border color
            style = Stroke(width = 3.dp.toPx())
        )
    }
}

// Draw individual tile with value
private fun DrawScope.drawTile(
    triangleInfo: TriangleInfo,
    value: Int,
    isNewTile: Boolean,
    highlightWidth: Float,
    glowAlpha: Float,
    scale: Float = 1f
) {
    // Apply scale transformation
    scale(scale, triangleInfo.center) {
        // Draw tile background
        val tileColor = TileColors.getTileColor(value)

        // Clip to triangle shape for clean tile rendering
        clipPath(triangleInfo.path) {
            drawPath(
                path = triangleInfo.path,
                color = tileColor
            )
        }

        // Draw special effects for new tile
        if (isNewTile) {
            // Glowing effect
            drawPath(
                path = triangleInfo.path,
                color = Color(0xFFFFD700).copy(alpha = glowAlpha),
                style = Stroke(width = highlightWidth.dp.toPx())
            )
        }
    }

    // Draw value text (not scaled)
    drawContext.canvas.nativeCanvas.apply {
        val textSize = when {
            value < 100 -> triangleInfo.path.getBounds().width * 0.3f
            value < 1000 -> triangleInfo.path.getBounds().width * 0.24f
            else -> triangleInfo.path.getBounds().width * 0.18f
        } * scale

        val paint = android.graphics.Paint().apply {
            color = TileColors.getTileTextColor(value).value.toInt()
            this.textSize = textSize
            textAlign = android.graphics.Paint.Align.CENTER
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            isAntiAlias = true
        }

        val textY = triangleInfo.center.y + textSize * 0.35f
        drawText(
            value.toString(),
            triangleInfo.center.x,
            textY,
            paint
        )
    }
}