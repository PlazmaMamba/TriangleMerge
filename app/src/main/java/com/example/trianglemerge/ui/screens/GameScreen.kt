package com.example.trianglemerge.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.trianglemerge.model.GameState
import com.example.trianglemerge.ui.components.TriangularGrid
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2

enum class SwipeDirection {
    LEFT, RIGHT, TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT
}

@Composable
fun GameScreen(modifier: Modifier = Modifier) {
    var gameState by remember { mutableStateOf(GameState.initial()) }

    fun handleSwipe(direction: SwipeDirection) {
        gameState = when (direction) {
            SwipeDirection.LEFT -> gameState.slideLeft()
            SwipeDirection.RIGHT -> gameState.slideRight()
            SwipeDirection.TOP_LEFT -> gameState.slideTopLeft()
            SwipeDirection.TOP_RIGHT -> gameState.slideTopRight()
            SwipeDirection.BOTTOM_LEFT -> gameState.slideBottomLeft()
            SwipeDirection.BOTTOM_RIGHT -> gameState.slideBottomRight()
        }
    }

    // Calculate the aspect ratio for the triangular grid
    // The grid forms a roughly hexagonal shape when fully populated
    // For equilateral triangles in a tessellation pattern with GRID_SIZE rows
    // The aspect ratio should be close to 1.0 but slightly wider
    val aspectRatio = 1.15f // Slightly wider than square to accommodate the hexagonal shape

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFAF8EF))
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Title
        Text(
            text = "Triangle 2048",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF776E65),
            modifier = Modifier.padding(vertical = 8.dp)
        )

        // Score
        Card(
            modifier = Modifier
                .padding(vertical = 4.dp)
                .clip(RoundedCornerShape(8.dp)),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFBBADA0))
        ) {
            Text(
                text = "Score: ${gameState.score}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
            )
        }

        // Game Grid with gesture detection - Takes up maximum available space
        Box(
            modifier = Modifier
                .weight(0.5f)
                .fillMaxWidth(0.75f)
                .padding(vertical = 8.dp, horizontal = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            TriangularGrid(
                gameState = gameState,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(aspectRatio)
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragEnd = {
                                // Handle drag end if needed
                            }
                        ) { change, dragAmount ->
                            val (dx, dy) = dragAmount
                            val threshold = 50f

                            if (abs(dx) > threshold || abs(dy) > threshold) {
                                val direction = getSwipeDirection(dx, dy)
                                direction?.let { handleSwipe(it) }
                            }
                        }
                    }
            )
        }

        // Bottom controls
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            // Direction buttons for testing/accessibility
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(4.dp)
            ) {
                Button(
                    onClick = { handleSwipe(SwipeDirection.TOP_LEFT) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8F7A66)),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text("↖", color = Color.White)
                }
                Button(
                    onClick = { handleSwipe(SwipeDirection.LEFT) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8F7A66)),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text("←", color = Color.White)
                }
                Button(
                    onClick = { handleSwipe(SwipeDirection.TOP_RIGHT) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8F7A66)),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text("↗", color = Color.White)
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(4.dp)
            ) {
                Button(
                    onClick = { handleSwipe(SwipeDirection.BOTTOM_LEFT) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8F7A66)),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text("↙", color = Color.White)
                }
                Button(
                    onClick = { handleSwipe(SwipeDirection.RIGHT) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8F7A66)),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text("→", color = Color.White)
                }
                Button(
                    onClick = { handleSwipe(SwipeDirection.BOTTOM_RIGHT) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8F7A66)),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text("↘", color = Color.White)
                }
            }

            // New Game Button
            Button(
                onClick = { gameState = GameState.initial() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8F7A66)),
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Text(
                    text = "New Game",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }

            // Game Over Message
            if (gameState.isGameOver) {
                Card(
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFEDC22E))
                ) {
                    Text(
                        text = "Game Over!",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}

fun getSwipeDirection(dx: Float, dy: Float): SwipeDirection? {
    val angle = atan2(dy, dx) * 180 / PI
    val normalizedAngle = if (angle < 0) angle + 360 else angle

    return when (normalizedAngle) {
        in 0.0..30.0, in 330.0..360.0 -> SwipeDirection.RIGHT
        in 30.0..90.0 -> SwipeDirection.BOTTOM_RIGHT
        in 90.0..150.0 -> SwipeDirection.BOTTOM_LEFT
        in 150.0..210.0 -> SwipeDirection.LEFT
        in 210.0..270.0 -> SwipeDirection.TOP_LEFT
        in 270.0..330.0 -> SwipeDirection.TOP_RIGHT
        else -> null
    }
}