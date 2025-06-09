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
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import com.example.trianglemerge.model.GameState
import com.example.trianglemerge.ui.theme.TileColors
import kotlinx.coroutines.launch
import kotlin.math.sqrt

@Composable
fun AnimatedTriangularGrid(
    gameState: GameState,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()

    // Track animation states
    var previousGrid by remember { mutableStateOf(gameState.grid) }
    var isAnimating by remember { mutableStateOf(false) }
    var animationStartTime by remember { mutableStateOf(0L) }

    // Animation configuration
    val slideAnimationDuration = 200
    val scaleAnimationDuration = 300
    val mergeAnimationDuration = 250

    // Current time for animation
    var currentTime by remember { mutableStateOf(System.currentTimeMillis()) }

    // Animation progress (0 to 1)
    val slideProgress = remember { Animatable(1f) }
    val newTileScale = remember { Animatable(0f) }
    val mergeScale = remember { Animatable(1f) }

    // Update animation when grid changes
    LaunchedEffect(gameState.grid) {
        if (previousGrid != gameState.grid) {
            isAnimating = true
            animationStartTime = System.currentTimeMillis()

            // Start slide animation
            scope.launch {
                slideProgress.snapTo(0f)
                slideProgress.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(
                        durationMillis = slideAnimationDuration,
                        easing = FastOutSlowInEasing
                    )
                )
            }

            // Animate new tile appearance
            if (gameState.lastSpawnedPosition != null) {
                scope.launch {
                    kotlinx.coroutines.delay(slideAnimationDuration.toLong())
                    newTileScale.snapTo(0f)
                    newTileScale.animateTo(
                        targetValue = 1f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    )
                }
            }

            previousGrid = gameState.grid

            scope.launch {
                kotlinx.coroutines.delay((slideAnimationDuration + 100).toLong())
                isAnimating = false
            }
        }
    }

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

        // Calculate triangle dimensions
        val totalRows = GameState.GRID_SIZE
        val maxRowWidth = totalRows
        val triangleSize = (canvasWidth * 0.9f * 2) / (maxRowWidth + 1)
        val triangleHeight = triangleSize * sqrt(3f) / 2f
        val rowVerticalSpacing = triangleHeight
        val totalHeight = totalRows * rowVerticalSpacing
        val startY = (canvasHeight - totalHeight) / 2 + triangleHeight / 2

        // Draw grid background first (empty triangles)
        for (row in 0 until totalRows) {
            val rowSize = 2 * row + 1
            val y = startY + row * rowVerticalSpacing
            val rowWidth = (rowSize - 1) * triangleSize / 2
            val startX = centerX - rowWidth / 2

            for (col in 0 until rowSize) {
                val x = startX + col * triangleSize / 2
                val isUpward = col % 2 == 0

                // Draw empty cell background
                drawEmptyTriangle(
                    center = Offset(x, y),
                    size = triangleSize,
                    isUpward = isUpward
                )
            }
        }

        // Draw tiles with animations
        for (row in gameState.grid.indices) {
            val rowCells = gameState.grid[row]
            val y = startY + row * rowVerticalSpacing
            val rowWidth = (rowCells.size - 1) * triangleSize / 2
            val startX = centerX - rowWidth / 2

            for (col in rowCells.indices) {
                val value = rowCells[col]
                if (value == 0) continue

                val x = startX + col * triangleSize / 2
                val isUpward = gameState.isTriangleUpward(row, col)
                val isNewTile = gameState.lastSpawnedPosition == (row to col)

                // Calculate position with animation
                val basePosition = Offset(x, y)
                var animatedPosition = basePosition
                var scale = 1f
                var alpha = 1f

                if (isAnimating) {
                    val progress = slideProgress.value

                    if (isNewTile) {
                        // New tile animation
                        scale = newTileScale.value
                        alpha = newTileScale.value
                    } else {
                        // Slide animation - simple directional slide
                        val slideDistance = triangleSize * 0.5f * (1f - progress)

                        // Determine slide direction based on position
                        val slideOffset = when {
                            // For simplicity, slide from edges toward center
                            col == 0 -> Offset(slideDistance, 0f) // From left
                            col == rowCells.size - 1 -> Offset(-slideDistance, 0f) // From right
                            row == 0 -> Offset(0f, -slideDistance) // From top
                            row == gameState.grid.size - 1 -> Offset(0f, slideDistance) // From bottom
                            else -> {
                                // Interior tiles slide diagonally
                                val diagSlide = slideDistance * 0.7f
                                when {
                                    col < rowCells.size / 2 -> Offset(diagSlide, -diagSlide)
                                    else -> Offset(-diagSlide, -diagSlide)
                                }
                            }
                        }

                        animatedPosition = basePosition + slideOffset
                    }
                }

                // Draw the tile
                scale(scale, basePosition) {
                    drawAnimatedTriangleCell(
                        center = animatedPosition,
                        size = triangleSize,
                        value = value,
                        isUpward = isUpward,
                        isNewTile = isNewTile,
                        highlightWidth = highlightAnimation,
                        glowAlpha = glowAnimation,
                        alpha = alpha
                    )
                }
            }
        }
    }
}

fun DrawScope.drawEmptyTriangle(
    center: Offset,
    size: Float,
    isUpward: Boolean
) {
    val height = size * sqrt(3f) / 2f
    val halfSize = size / 2f

    val path = Path().apply {
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

    // Draw light background
    drawPath(
        path = path,
        color = Color(0xFFCDC1B4)
    )

    // Draw border
    drawPath(
        path = path,
        color = Color(0xFFBBADA0),
        style = Stroke(width = 1.dp.toPx())
    )
}

fun DrawScope.drawAnimatedTriangleCell(
    center: Offset,
    size: Float,
    value: Int,
    isUpward: Boolean,
    isNewTile: Boolean = false,
    highlightWidth: Float = 3f,
    glowAlpha: Float = 0.5f,
    alpha: Float = 1f
) {
    val height = size * sqrt(3f) / 2f
    val halfSize = size / 2f

    val path = Path().apply {
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

    // Draw triangle background with alpha
    val tileColor = TileColors.getTileColor(value)
    drawPath(
        path = path,
        color = tileColor.copy(alpha = alpha)
    )

    // Draw special effects for new tile
    if (isNewTile && value > 0 && alpha >= 0.8f) {
        // Glow effect
        for (i in 3 downTo 1) {
            drawPath(
                path = path,
                color = Color(0xFFFFD700).copy(alpha = glowAlpha * alpha * 0.3f),
                style = Stroke(width = (highlightWidth + i * 2).dp.toPx())
            )
        }

        // Main highlight border
        drawPath(
            path = path,
            color = Color(0xFFFFD700).copy(alpha = alpha),
            style = Stroke(width = highlightWidth.dp.toPx())
        )
    }

    // Draw standard black border
    drawPath(
        path = path,
        color = Color.Black.copy(alpha = alpha),
        style = Stroke(width = 2.dp.toPx())
    )

    // Draw value text
    if (alpha > 0.5f) {
        drawContext.canvas.nativeCanvas.apply {
            val textSize = when {
                value < 100 -> size * 0.3f
                value < 1000 -> size * 0.24f
                else -> size * 0.18f
            }

            val paint = android.graphics.Paint().apply {
                color = android.graphics.Color.argb(
                    (255 * alpha).toInt(),
                    android.graphics.Color.red(TileColors.getTileTextColor(value).value.toInt()),
                    android.graphics.Color.green(TileColors.getTileTextColor(value).value.toInt()),
                    android.graphics.Color.blue(TileColors.getTileTextColor(value).value.toInt())
                )
                this.textSize = textSize
                textAlign = android.graphics.Paint.Align.CENTER
                typeface = android.graphics.Typeface.DEFAULT_BOLD
                isAntiAlias = true
            }

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