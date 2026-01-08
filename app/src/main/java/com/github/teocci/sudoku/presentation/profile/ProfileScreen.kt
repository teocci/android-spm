package com.github.teocci.sudoku.presentation.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.teocci.sudoku.core.formatTime
import com.github.teocci.sudoku.domain.model.Difficulty
import com.github.teocci.sudoku.ui.theme.DifficultyEasy
import com.github.teocci.sudoku.ui.theme.DifficultyExpert
import com.github.teocci.sudoku.ui.theme.DifficultyHard
import com.github.teocci.sudoku.ui.theme.DifficultyMedium
import com.github.teocci.sudoku.ui.theme.SudokuPuzzleMasterTheme
import com.github.teocci.sudoku.ui.theme.SudokuTheme
import com.github.teocci.sudoku.ui.theme.TrophyBronze
import com.github.teocci.sudoku.ui.theme.TrophyGold
import com.github.teocci.sudoku.ui.theme.TrophySilver

/**
 * Profile/Statistics screen.
 *
 * @param viewModel The profile ViewModel
 */
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    ProfileScreenContent(uiState = uiState)
}

/**
 * Profile screen content (stateless for preview).
 */
@Composable
private fun ProfileScreenContent(
    uiState: ProfileUiState
) {
    val colors = SudokuTheme.colors

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .padding(horizontal = 16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(16.dp))

            // Header
            Text(
                text = "Statistics",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = colors.numberFixed
            )

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Overall stats card
        item {
            OverallStatsCard(stats = uiState.overallStats)
            Spacer(modifier = Modifier.height(20.dp))
        }

        // Difficulty breakdown
        item {
            Text(
                text = "By Difficulty",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.numberFixed,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            DifficultyStatsRow(difficultyStats = uiState.difficultyStats)
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Achievements section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Achievements",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.numberFixed
                )
                Text(
                    text = "${uiState.unlockedAchievements}/${uiState.totalAchievements}",
                    fontSize = 14.sp,
                    color = colors.controlButtonIcon
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Achievement items
        items(uiState.achievements) { achievement ->
            AchievementCard(achievement = achievement)
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Recent games section
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Recent Games",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.numberFixed,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        // Recent game items
        items(uiState.recentGames) { game ->
            RecentGameCard(game = game)
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Bottom padding for navigation bar
        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

/**
 * Overall statistics card.
 */
@Composable
private fun OverallStatsCard(
    stats: OverallStatistics,
    modifier: Modifier = Modifier
) {
    val colors = SudokuTheme.colors

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = colors.controlButtonBackground
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Top row stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    value = stats.gamesPlayed.toString(),
                    label = "Played"
                )
                StatItem(
                    value = stats.winRateFormatted,
                    label = "Win Rate"
                )
                StatItem(
                    value = stats.currentStreak.toString(),
                    label = "Streak"
                )
                StatItem(
                    value = stats.bestStreak.toString(),
                    label = "Best"
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Divider
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(colors.controlButtonIcon.copy(alpha = 0.2f))
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Bottom row stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    value = formatTime(stats.averageTime),
                    label = "Avg Time"
                )
                StatItem(
                    value = formatTime(stats.bestTime),
                    label = "Best Time"
                )
                StatItem(
                    value = stats.perfectGames.toString(),
                    label = "Perfect"
                )
            }
        }
    }
}

/**
 * Individual statistic item.
 */
@Composable
private fun StatItem(
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    val colors = SudokuTheme.colors

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = colors.scoreText
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            color = colors.controlButtonIcon
        )
    }
}

/**
 * Horizontal scrolling difficulty stats row.
 */
@Composable
private fun DifficultyStatsRow(
    difficultyStats: Map<Difficulty, DifficultyStatistics>,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(Difficulty.entries) { difficulty ->
            val stats = difficultyStats[difficulty] ?: DifficultyStatistics()
            DifficultyStatCard(
                difficulty = difficulty,
                stats = stats
            )
        }
    }
}

/**
 * Difficulty statistics card.
 */
@Composable
private fun DifficultyStatCard(
    difficulty: Difficulty,
    stats: DifficultyStatistics,
    modifier: Modifier = Modifier
) {
    val colors = SudokuTheme.colors
    val difficultyColor = getDifficultyColor(difficulty)

    Card(
        modifier = modifier.width(140.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.controlButtonBackground
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Difficulty header
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(difficultyColor, CircleShape)
                )
                Text(
                    text = difficulty.displayName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = colors.numberFixed,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Stats
            Text(
                text = "${stats.gamesPlayed} games",
                fontSize = 12.sp,
                color = colors.controlButtonIcon
            )

            Text(
                text = "${String.format("%.0f", stats.winRate)}% wins",
                fontSize = 12.sp,
                color = colors.controlButtonIcon
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Best time
            if (stats.bestTime > 0) {
                Text(
                    text = "Best: ${formatTime(stats.bestTime)}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = difficultyColor
                )
            }
        }
    }
}

/**
 * Achievement card.
 */
@Composable
private fun AchievementCard(
    achievement: Achievement,
    modifier: Modifier = Modifier
) {
    val colors = SudokuTheme.colors
    val iconColor = if (achievement.isUnlocked) {
        getAchievementColor(achievement.iconType)
    } else {
        colors.controlButtonIcon.copy(alpha = 0.5f)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .alpha(if (achievement.isUnlocked) 1f else 0.7f),
        colors = CardDefaults.cardColors(
            containerColor = colors.controlButtonBackground
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Achievement icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(iconColor.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (achievement.isUnlocked) {
                        getAchievementIcon(achievement.iconType)
                    } else {
                        Icons.Default.Lock
                    },
                    contentDescription = achievement.name,
                    tint = iconColor,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Achievement info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = achievement.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = colors.numberFixed
                )

                Text(
                    text = achievement.description,
                    fontSize = 13.sp,
                    color = colors.controlButtonIcon
                )

                // Progress bar for locked achievements
                if (!achievement.isUnlocked && achievement.maxProgress > 1) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        LinearProgressIndicator(
                            progress = { achievement.progressPercentage },
                            modifier = Modifier
                                .weight(1f)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = iconColor,
                            trackColor = colors.controlButtonIcon.copy(alpha = 0.2f),
                            strokeCap = StrokeCap.Round
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${achievement.progress}/${achievement.maxProgress}",
                            fontSize = 11.sp,
                            color = colors.controlButtonIcon
                        )
                    }
                }

                // Unlocked date
                if (achievement.isUnlocked && achievement.unlockedDate != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Unlocked ${achievement.unlockedDate}",
                        fontSize = 11.sp,
                        color = iconColor
                    )
                }
            }
        }
    }
}

/**
 * Recent game card.
 */
@Composable
private fun RecentGameCard(
    game: RecentGame,
    modifier: Modifier = Modifier
) {
    val colors = SudokuTheme.colors
    val difficultyColor = getDifficultyColor(game.difficulty)

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = colors.controlButtonBackground
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Status icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        if (game.isCompleted) {
                            colors.calendarDayCompleted.copy(alpha = 0.2f)
                        } else {
                            colors.error.copy(alpha = 0.2f)
                        },
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (game.isCompleted) {
                        Icons.Default.CheckCircle
                    } else {
                        Icons.Default.Close
                    },
                    contentDescription = null,
                    tint = if (game.isCompleted) {
                        colors.calendarDayCompleted
                    } else {
                        colors.error
                    },
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Game info
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(difficultyColor, CircleShape)
                    )
                    Text(
                        text = game.difficulty.displayName,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = colors.numberFixed,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                Text(
                    text = game.date,
                    fontSize = 12.sp,
                    color = colors.controlButtonIcon
                )
            }

            // Time and score
            Column(
                horizontalAlignment = Alignment.End
            ) {
                if (game.isCompleted) {
                    Text(
                        text = formatTime(game.time),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = colors.numberFixed
                    )
                    Text(
                        text = "${game.score} pts",
                        fontSize = 12.sp,
                        color = colors.controlButtonIcon
                    )
                } else {
                    Text(
                        text = "Failed",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = colors.error
                    )
                    Text(
                        text = "${game.mistakes} mistakes",
                        fontSize = 12.sp,
                        color = colors.controlButtonIcon
                    )
                }
            }
        }
    }
}

/**
 * Get color for difficulty.
 */
private fun getDifficultyColor(difficulty: Difficulty): Color {
    return when (difficulty) {
        Difficulty.EASY -> DifficultyEasy
        Difficulty.MEDIUM -> DifficultyMedium
        Difficulty.HARD -> DifficultyHard
        Difficulty.EXPERT -> DifficultyExpert
    }
}

/**
 * Get icon for achievement type.
 */
private fun getAchievementIcon(iconType: AchievementIcon): ImageVector {
    return when (iconType) {
        AchievementIcon.TROPHY -> Icons.Default.EmojiEvents
        AchievementIcon.STAR -> Icons.Default.Star
        AchievementIcon.TIMER -> Icons.Default.Timer
        AchievementIcon.STREAK -> Icons.Default.Whatshot
        AchievementIcon.BRAIN -> Icons.Default.Psychology
        AchievementIcon.EXPERT -> Icons.Default.WorkspacePremium
        AchievementIcon.CALENDAR -> Icons.Default.CalendarMonth
    }
}

/**
 * Get color for achievement type.
 */
private fun getAchievementColor(iconType: AchievementIcon): Color {
    return when (iconType) {
        AchievementIcon.TROPHY -> TrophyGold
        AchievementIcon.STAR -> TrophyGold
        AchievementIcon.TIMER -> TrophySilver
        AchievementIcon.STREAK -> Color(0xFFFF6B35)
        AchievementIcon.BRAIN -> TrophySilver
        AchievementIcon.EXPERT -> TrophyGold
        AchievementIcon.CALENDAR -> TrophyBronze
    }
}

// ==================== PREVIEWS ====================

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun PreviewProfileScreen() {
    SudokuPuzzleMasterTheme(darkTheme = true) {
        ProfileScreenContent(
            uiState = ProfileUiState(
                overallStats = OverallStatistics(
                    gamesPlayed = 156,
                    gamesWon = 142,
                    currentStreak = 7,
                    bestStreak = 23,
                    averageTime = 485L,
                    bestTime = 178L,
                    perfectGames = 45
                ),
                difficultyStats = mapOf(
                    Difficulty.EASY to DifficultyStatistics(45, 45, 245L, 120L),
                    Difficulty.MEDIUM to DifficultyStatistics(52, 50, 380L, 195L),
                    Difficulty.HARD to DifficultyStatistics(38, 32, 620L, 310L),
                    Difficulty.EXPERT to DifficultyStatistics(21, 15, 890L, 445L)
                ),
                achievements = listOf(
                    Achievement(
                        id = "first_win",
                        name = "First Victory",
                        description = "Complete your first puzzle",
                        iconType = AchievementIcon.TROPHY,
                        isUnlocked = true,
                        unlockedDate = "Jan 1, 2026"
                    ),
                    Achievement(
                        id = "perfectionist",
                        name = "Perfectionist",
                        description = "Complete 10 puzzles without mistakes",
                        iconType = AchievementIcon.STAR,
                        isUnlocked = false,
                        progress = 4,
                        maxProgress = 10
                    )
                ),
                recentGames = listOf(
                    RecentGame("Today", Difficulty.MEDIUM, 425L, 1850, true, 1),
                    RecentGame("Yesterday", Difficulty.HARD, 680L, 2400, true, 0),
                    RecentGame("Jan 5", Difficulty.EXPERT, 0L, 0, false, 3)
                )
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewOverallStatsCard() {
    SudokuPuzzleMasterTheme(darkTheme = true) {
        OverallStatsCard(
            stats = OverallStatistics(
                gamesPlayed = 156,
                gamesWon = 142,
                currentStreak = 7,
                bestStreak = 23,
                averageTime = 485L,
                bestTime = 178L,
                perfectGames = 45
            ),
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewAchievementCard_Unlocked() {
    SudokuPuzzleMasterTheme(darkTheme = true) {
        AchievementCard(
            achievement = Achievement(
                id = "first_win",
                name = "First Victory",
                description = "Complete your first puzzle",
                iconType = AchievementIcon.TROPHY,
                isUnlocked = true,
                unlockedDate = "Jan 1, 2026"
            ),
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewAchievementCard_Locked() {
    SudokuPuzzleMasterTheme(darkTheme = true) {
        AchievementCard(
            achievement = Achievement(
                id = "perfectionist",
                name = "Perfectionist",
                description = "Complete 10 puzzles without mistakes",
                iconType = AchievementIcon.STAR,
                isUnlocked = false,
                progress = 4,
                maxProgress = 10
            ),
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewRecentGameCard() {
    SudokuPuzzleMasterTheme(darkTheme = true) {
        Column(modifier = Modifier.padding(16.dp)) {
            RecentGameCard(
                game = RecentGame("Today", Difficulty.MEDIUM, 425L, 1850, true, 1)
            )
            Spacer(modifier = Modifier.height(8.dp))
            RecentGameCard(
                game = RecentGame("Jan 5", Difficulty.EXPERT, 0L, 0, false, 3)
            )
        }
    }
}
