// TriangularGrid.kt
package com.example.trianglemerge.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
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
    Canvas(modifier = modifier) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val centerX = canvasWidth / 2

        // Calculate triangle dimensions for tight fitting
        val totalRows = GameState.GRID_SIZE
        val maxRowWidth = totalRows // Maximum number of triangles in bottom row

        // Calculate triangle side length based on available width
        // For a proper tessellation, we need to account for the fact that
        // triangles share edges and the row width is not simply triangleSize * count
        // The actual width of n triangles in a row is: (n + 1) * triangleSize / 2
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
            // In a tessellated pattern, horizontal spacing between centers is triangleSize/2
            val rowWidth = (rowCells.size - 1) * triangleSize / 2
            val startX = centerX - rowWidth / 2

            // Draw each cell in the row
            for (col in rowCells.indices) {
                // Triangles are placed with centers triangleSize/2 apart horizontally
                val x = startX + col * triangleSize / 2
                val value = rowCells[col]
                val isUpward = gameState.isTriangleUpward(row, col)

                drawTriangleCell(
                    center = Offset(x, y),
                    size = triangleSize,
                    value = value,
                    isUpward = isUpward
                )
            }
        }
    }
}

fun DrawScope.drawTriangleCell(
    center: Offset,
    size: Float,
    value: Int,
    isUpward: Boolean
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
    drawPath(
        path = path,
        color = TileColors.getTileColor(value)
    )

    // Draw black border around triangle
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