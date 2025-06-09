package com.example.trianglemerge.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trianglemerge.model.GameStats
import com.example.trianglemerge.ui.theme.TileColors

@Composable
fun GameOverScreen(
    finalScore: Int,
    highScore: Int,
    gameStats: GameStats,
    isNewHighScore: Boolean,
    onNewGame: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Game Over title with celebration if new high score
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(
                containerColor = if (isNewHighScore) Color(0xFFFFD700) else Color(0xFFEDC22E)
            )
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Game Over!",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                if (isNewHighScore) {
                    Text(
                        text = "🎉 New Best Score! 🎉",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }

        // Stats Grid
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFBBADA0))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Score row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatItem(
                        label = "Final Score",
                        value = finalScore.toString(),
                        modifier = Modifier.weight(1f)
                    )
                    StatItem(
                        label = "Best Score",
                        value = highScore.toString(),
                        isHighlight = isNewHighScore,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Gameplay stats row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatItem(
                        label = "Total Swipes",
                        value = gameStats.totalSwipes.toString(),
                        modifier = Modifier.weight(1f)
                    )
                    StatItem(
                        label = "Tiles Merged",
                        value = gameStats.totalMerges.toString(),
                        modifier = Modifier.weight(1f)
                    )
                }

                // Highest tile achieved - special display
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF8F7A66))
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Highest Tile",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFFEEE4DA),
                            fontWeight = FontWeight.Medium
                        )
                        Box(
                            modifier = Modifier
                                .padding(top = 8.dp)
                                .size(60.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(TileColors.getTileColor(gameStats.highestTile)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = gameStats.highestTile.toString(),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = TileColors.getTileTextColor(gameStats.highestTile),
                                fontSize = if (gameStats.highestTile >= 1000) 18.sp else 24.sp
                            )
                        }
                    }
                }

                // Performance metrics
                if (gameStats.totalSwipes > 0) {
                    val efficiency = (gameStats.totalMerges.toFloat() / gameStats.totalSwipes * 100).toInt()
                    val scorePerSwipe = finalScore / gameStats.totalSwipes

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatItem(
                            label = "Merge Rate",
                            value = "$efficiency%",
                            modifier = Modifier.weight(1f)
                        )
                        StatItem(
                            label = "Points/Swipe",
                            value = scorePerSwipe.toString(),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // New Game button
        Button(
            onClick = onNewGame,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF8F7A66)
            )
        ) {
            Text(
                text = "New Game",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String,
    isHighlight: Boolean = false,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFFEEE4DA),
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            color = if (isHighlight) Color(0xFFFFD700) else Color.White,
            fontWeight = FontWeight.Bold
        )
    }
}