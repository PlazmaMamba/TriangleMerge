package com.example.trianglemerge.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.trianglemerge.data.GamePreferences
import com.example.trianglemerge.model.GameState
import com.example.trianglemerge.ui.components.TriangularGrid
import com.example.trianglemerge.ui.components.GameOverScreen
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2

enum class SwipeDirection {
    LEFT, RIGHT, TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun GameScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val gamePrefs = remember { GamePreferences(context) }

    var gameState by remember {
        mutableStateOf(
            gamePrefs.loadGame() ?: GameState.initial()
        )
    }
    var spawnEnabled by remember { mutableStateOf(true) }
    var highScore by remember { mutableStateOf(gamePrefs.getHighScore()) }
    var showNewGameDialog by remember { mutableStateOf(false) }
    val isNewHighScore = gameState.isGameOver && gameState.score == highScore && gameState.score > 0

    // Auto-save game state whenever it changes
    LaunchedEffect(gameState) {
        if (!gameState.isGameOver) {
            gamePrefs.saveGame(gameState)
        } else {
            // Clear saved game when game is over
            gamePrefs.clearSavedGame()
        }
        // Update high score
        if (gameState.score > highScore) {
            highScore = gameState.score
            gamePrefs.updateHighScore(gameState.score)
        }
    }

    fun handleSwipe(direction: SwipeDirection) {
        if (gameState.isGameOver) return // Prevent moves when game is over

        val updated = when (direction) {
            SwipeDirection.LEFT -> gameState.slideLeft(spawnEnabled)
            SwipeDirection.RIGHT -> gameState.slideRight(spawnEnabled)
            SwipeDirection.TOP_LEFT -> gameState.slideTopLeft(spawnEnabled)
            SwipeDirection.TOP_RIGHT -> gameState.slideTopRight(spawnEnabled)
            SwipeDirection.BOTTOM_LEFT -> gameState.slideBottomLeft(spawnEnabled)
            SwipeDirection.BOTTOM_RIGHT -> gameState.slideBottomRight(spawnEnabled)
        }
        gameState = updated
    }

    fun startNewGame() {
        gameState = GameState.initial()
        gamePrefs.clearSavedGame()
        showNewGameDialog = false
    }

    val aspectRatio = 1.15f

    if (showNewGameDialog && !gameState.isGameOver) {
        AlertDialog(
            onDismissRequest = { showNewGameDialog = false },
            title = { Text("Start New Game?") },
            text = { Text("You have a game in progress. Starting a new game will lose your current progress.") },
            confirmButton = {
                TextButton(onClick = { startNewGame() }) {
                    Text("New Game")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewGameDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFAF8EF))
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Triangle Merge",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF776E65),
            modifier = Modifier.padding(vertical = 8.dp)
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(vertical = 4.dp)
        ) {
            Card(
                modifier = Modifier.clip(RoundedCornerShape(8.dp)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFBBADA0))
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "SCORE",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFEEE4DA)
                    )
                    Text(
                        text = "${gameState.score}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Card(
                modifier = Modifier.clip(RoundedCornerShape(8.dp)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFBBADA0))
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "BEST",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFEEE4DA)
                    )
                    Text(
                        text = "$highScore",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }

        // Animated size for the triangle grid
        val gridSizeAnimation by animateFloatAsState(
            targetValue = if (gameState.isGameOver) 0.3f else 0.5f,
            animationSpec = tween(
                durationMillis = 600,
                easing = FastOutSlowInEasing
            )
        )

        val gridWidthAnimation by animateFloatAsState(
            targetValue = if (gameState.isGameOver) 0.5f else 0.75f,
            animationSpec = tween(
                durationMillis = 600,
                easing = FastOutSlowInEasing
            )
        )

        Box(
            modifier = Modifier
                .weight(gridSizeAnimation)
                .fillMaxWidth(gridWidthAnimation)
                .padding(vertical = 8.dp, horizontal = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            TriangularGrid(
                gameState = gameState,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(aspectRatio)
                    .pointerInput(Unit) {
                        detectDragGestures(onDragEnd = {}) { _, dragAmount ->
                            val (dx, dy) = dragAmount
                            val threshold = 50f
                            if (abs(dx) > threshold || abs(dy) > threshold) {
                                getSwipeDirection(dx, dy)?.let { handleSwipe(it) }
                            }
                        }
                    }
            )
        }

        // Controls/Game Over section with animation
        Box(
            modifier = Modifier.weight(1f - gridSizeAnimation)
        ) {
            AnimatedContent(
                targetState = gameState.isGameOver,
                transitionSpec = {
                    fadeIn(animationSpec = tween(300, delayMillis = 300)) +
                            slideInVertically(animationSpec = tween(300, delayMillis = 300)) with
                            fadeOut(animationSpec = tween(300)) +
                            slideOutVertically(animationSpec = tween(300))
                }
            ) { isGameOver ->
                if (isGameOver) {
                    // Game Over screen with stats
                    GameOverScreen(
                        finalScore = gameState.score,
                        highScore = highScore,
                        gameStats = gameState.stats,
                        isNewHighScore = isNewHighScore,
                        onNewGame = { startNewGame() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    )
                } else {
                    // Normal controls
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(vertical = 8.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(bottom = 4.dp)
                        ) {
                            Spacer(modifier = Modifier.width(32.dp))
                            Button(
                                onClick = { handleSwipe(SwipeDirection.TOP_LEFT) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8F7A66)),
                                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                                modifier = Modifier.size(56.dp)
                            ) {
                                Text("↖", color = Color.White, style = MaterialTheme.typography.titleLarge)
                            }
                            Button(
                                onClick = { handleSwipe(SwipeDirection.TOP_RIGHT) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8F7A66)),
                                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                                modifier = Modifier.size(56.dp)
                            ) {
                                Text("↗", color = Color.White, style = MaterialTheme.typography.titleLarge)
                            }
                            Spacer(modifier = Modifier.width(32.dp))
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(vertical = 0.dp)
                        ) {
                            Button(
                                onClick = { handleSwipe(SwipeDirection.LEFT) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8F7A66)),
                                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                                modifier = Modifier.size(56.dp)
                            ) {
                                Text("←", color = Color.White, style = MaterialTheme.typography.titleLarge)
                            }
                            Spacer(modifier = Modifier.size(56.dp))
                            Button(
                                onClick = { handleSwipe(SwipeDirection.RIGHT) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8F7A66)),
                                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                                modifier = Modifier.size(56.dp)
                            ) {
                                Text("→", color = Color.White, style = MaterialTheme.typography.titleLarge)
                            }
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Spacer(modifier = Modifier.width(32.dp))
                            Button(
                                onClick = { handleSwipe(SwipeDirection.BOTTOM_LEFT) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8F7A66)),
                                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                                modifier = Modifier.size(56.dp)
                            ) {
                                Text("↙", color = Color.White, style = MaterialTheme.typography.titleLarge)
                            }
                            Button(
                                onClick = { handleSwipe(SwipeDirection.BOTTOM_RIGHT) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8F7A66)),
                                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                                modifier = Modifier.size(56.dp)
                            ) {
                                Text("↘", color = Color.White, style = MaterialTheme.typography.titleLarge)
                            }
                            Spacer(modifier = Modifier.width(32.dp))
                        }

                        Button(
                            onClick = { showNewGameDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8F7A66)),
                            modifier = Modifier.padding(top = 12.dp, bottom = 8.dp)
                        ) {
                            Text(
                                text = "New Game",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                            Text("Spawn Tiles", color = Color.DarkGray, modifier = Modifier.padding(end = 8.dp))
                            Switch(checked = spawnEnabled, onCheckedChange = { spawnEnabled = it })
                        }
                    }
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

