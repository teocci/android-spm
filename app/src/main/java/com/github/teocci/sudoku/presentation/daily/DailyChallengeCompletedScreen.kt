package com.github.teocci.sudoku.presentation.daily

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.github.teocci.sudoku.data.RepositoryProvider
import com.github.teocci.sudoku.domain.model.Difficulty
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Screen shown when a daily challenge is completed.
 * Displays completion stats, comparison with averages, and reward collection.
 */
@Composable
fun DailyChallengeCompletedScreen(
    score: Int,
    timeSeconds: Long,
    difficulty: Difficulty,
    date: LocalDate,
    onCollectReward: () -> Unit,
    onSeeAllStats: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Load stats for comparison
    val statsRepository = remember { RepositoryProvider.getStatsRepository() }
    val averageTime = remember {
        val stats = statsRepository.getDifficultyStats(difficulty)
        stats.averageTime
    }
    val timeDifference = averageTime - timeSeconds
    val isFasterThanAverage = timeDifference > 0 && averageTime > 0

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        // Top Section: Title and Subtitle
        Text(
            text = "Congratulations!",
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "You have completed the daily challenge for ${formatDate(date)}!",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        Spacer(modifier = Modifier.height(40.dp))

        // Center: Reward Icon
        RewardIcon(
            difficulty = difficulty,
            modifier = Modifier.size(180.dp)
        )

        Spacer(modifier = Modifier.height(40.dp))

        // Statistics Section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        ) {
            // Stats Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Stats",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                OutlinedButton(
                    onClick = onSeeAllStats,
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "See All",
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Highlight Message Card
            if (isFasterThanAverage) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = buildAnnotatedString {
                                append("You've solved this puzzle ")
                                withStyle(
                                    SpanStyle(
                                        color = Color(0xFFFFD700), // Gold
                                        fontWeight = FontWeight.Bold
                                    )
                                ) {
                                    append(formatTime(timeDifference))
                                }
                                append(" faster than your average!")
                            },
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Stats Rows Container
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                )
            ) {
                Column {
                    // Row 1: Difficulty
                    StatRow(
                        icon = Icons.Default.BarChart,
                        label = "Difficulty",
                        value = difficulty.name.lowercase().replaceFirstChar { it.uppercase() }
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                    )

                    // Row 2: Time
                    StatRow(
                        icon = Icons.Default.Timer,
                        label = "Time",
                        value = formatTime(timeSeconds)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.height(24.dp))

        // Bottom Action Button
        Button(
            onClick = onCollectReward,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .shadow(8.dp, RoundedCornerShape(12.dp)),
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            Text(
                text = "Collect",
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

/**
 * Individual stat row with icon, label, and value.
 */
@Composable
private fun StatRow(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )
        }

        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

/**
 * Trophy/medal reward icon with difficulty-based coloring.
 */
@Composable
private fun RewardIcon(
    difficulty: Difficulty,
    modifier: Modifier = Modifier
) {
    // Trophy/medal color based on difficulty
    val color = when (difficulty) {
        Difficulty.EASY -> Color(0xFFCD7F32) // Bronze
        Difficulty.MEDIUM -> Color(0xFFC0C0C0) // Silver
        Difficulty.HARD -> Color(0xFFFFD700) // Gold
        Difficulty.EXPERT -> Color(0xFFE5E4E2) // Platinum
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.EmojiEvents, // Trophy icon
            contentDescription = "Reward",
            modifier = Modifier.fillMaxSize(),
            tint = color
        )
    }
}

/**
 * Format date for display (e.g., "January 9").
 */
private fun formatDate(date: LocalDate): String {
    val formatter = DateTimeFormatter.ofPattern("MMMM d")
    return formatter.format(date)
}

/**
 * Format time in seconds to MM:SS format.
 */
private fun formatTime(seconds: Long): String {
    val minutes = seconds / 60
    val secs = seconds % 60
    return String.format("%02d:%02d", minutes, secs)
}
