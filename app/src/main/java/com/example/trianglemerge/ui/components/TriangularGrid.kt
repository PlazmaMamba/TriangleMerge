package com.example.trianglemerge.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.example.trianglemerge.model.GameState
import com.example.trianglemerge.ui.theme.TileColors
import kotlin.math.sqrt

@Composable
fun TriangularGrid(
    gameState: GameState,
    modifier: Modifier = Modifier
) {
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

    Canvas(modifier = modifier) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val centerX = canvasWidth / 2

        // Calculate triangle dimensions for tight fitting
        val totalRows = GameState.GRID_SIZE
        val maxRowWidth = totalRows // Maximum number of triangles in bottom row

        // Calculate triangle side length based on available width
        val triangleSize = (canvasWidth * 0.9f * 2) / (maxRowWidth + 1)

        // Triangle height for equilateral triangles
        val triangleHeight = triangleSize * sqrt(3f) / 2f

        // Vertical spacing between rows - triangles should touch perfectly
        val rowVerticalSpacing = triangleHeight

        // Calculate starting Y position to center grid vertically
        val totalHeight = totalRows * rowVerticalSpacing
        val startY = (canvasHeight - totalHeight) / 2 + triangleHeight / 2

        // Draw each row
        for (row in gameState.grid.indices) {
            val rowCells = gameState.grid[row]
            val y = startY + row * rowVerticalSpacing

            // Calculate starting X position for this row to center it
            val rowWidth = (rowCells.size - 1) * triangleSize / 2
            val startX = centerX - rowWidth / 2

            // Draw each cell in the row
            for (col in rowCells.indices) {
                // Triangles are placed with centers triangleSize/2 apart horizontally
                val x = startX + col * triangleSize / 2
                val value = rowCells[col]
                val isUpward = gameState.isTriangleUpward(row, col)
                val isNewTile = gameState.lastSpawnedPosition == (row to col)

                drawTriangleCell(
                    center = Offset(x, y),
                    size = triangleSize,
                    value = value,
                    isUpward = isUpward,
                    isNewTile = isNewTile,
                    highlightWidth = highlightAnimation,
                    glowAlpha = glowAnimation
                )
            }
        }
    }
}

fun DrawScope.drawTriangleCell(
    center: Offset,
    size: Float,
    value: Int,
    isUpward: Boolean,
    isNewTile: Boolean = false,
    highlightWidth: Float = 3f,
    glowAlpha: Float = 0.5f
) {
    val height = size * sqrt(3f) / 2f
    val halfSize = size / 2f

    // Create triangle path based on orientation
    val path = Path().apply {
        if (isUpward) {
            // Upward triangle - point at top
            moveTo(center.x, center.y - height / 2) // Top vertex
            lineTo(center.x - halfSize, center.y + height / 2) // Bottom left
            lineTo(center.x + halfSize, center.y + height / 2) // Bottom right
        } else {
            // Downward triangle - point at bottom
            moveTo(center.x, center.y + height / 2) // Bottom vertex
            lineTo(center.x - halfSize, center.y - height / 2) // Top left
            lineTo(center.x + halfSize, center.y - height / 2) // Top right
        }
        close()
    }

    // Draw triangle background (filled)
    val tileColor = TileColors.getTileColor(value)
    drawPath(
        path = path,
        color = tileColor
    )

    // Draw special effects for new tile
    if (isNewTile && value > 0) {
        // Draw glowing effect
        drawPath(
            path = path,
            color = Color(0xFFFFD700).copy(alpha = glowAlpha),
            style = Stroke(width = highlightWidth.dp.toPx())
        )

        // Draw animated border
        drawPath(
            path = path,
            color = Color(0xFFFFD700),
            style = Stroke(width = highlightWidth.dp.toPx())
        )
    }

    // Draw standard black border
    drawPath(
        path = path,
        color = Color.Black,
        style = Stroke(width = 2.dp.toPx())
    )

    // Draw value text if not empty
    if (value > 0) {
        drawContext.canvas.nativeCanvas.apply {
            val textSize = when {
                value < 100 -> size * 0.3f
                value < 1000 -> size * 0.24f
                else -> size * 0.18f
            }

            val paint = android.graphics.Paint().apply {
                color = TileColors.getTileTextColor(value).value.toInt()
                this.textSize = textSize
                textAlign = android.graphics.Paint.Align.CENTER
                typeface = android.graphics.Typeface.DEFAULT_BOLD
                isAntiAlias = true
            }

            // Center text vertically in triangle
            val textY = center.y + textSize * 0.35f
            drawText(
                value.toString(),
                center.x,
                textY,
                paint
            )
        }
    }
}