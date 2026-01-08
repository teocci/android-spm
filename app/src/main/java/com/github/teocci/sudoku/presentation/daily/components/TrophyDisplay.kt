package com.github.teocci.sudoku.presentation.daily.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.teocci.sudoku.presentation.daily.Trophy
import com.github.teocci.sudoku.ui.theme.SudokuPuzzleMasterTheme
import com.github.teocci.sudoku.ui.theme.SudokuTheme
import com.github.teocci.sudoku.ui.theme.TrophyBronze
import com.github.teocci.sudoku.ui.theme.TrophyGold
import com.github.teocci.sudoku.ui.theme.TrophySilver

/**
 * Horizontal scrolling trophy display.
 *
 * @param trophies List of trophies to display
 * @param modifier Modifier for the display
 */
@Composable
fun TrophyDisplay(
    trophies: List<Trophy>,
    modifier: Modifier = Modifier
) {
    val colors = SudokuTheme.colors

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Achievements",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = colors.numberFixed,
            modifier = Modifier.padding(horizontal = 4.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(trophies) { trophy ->
                TrophyCard(trophy = trophy)
            }
        }
    }
}

/**
 * Individual trophy card.
 */
@Composable
fun TrophyCard(
    trophy: Trophy,
    modifier: Modifier = Modifier
) {
    val colors = SudokuTheme.colors

    val alpha by animateFloatAsState(
        targetValue = if (trophy.isUnlocked) 1f else 0.6f,
        animationSpec = tween(300),
        label = "trophyAlpha"
    )

    val trophyColor = if (trophy.isUnlocked) {
        getTrophyColor(trophy.id)
    } else {
        colors.controlButtonIcon.copy(alpha = 0.5f)
    }

    Card(
        modifier = modifier
            .width(140.dp)
            .alpha(alpha),
        colors = CardDefaults.cardColors(
            containerColor = colors.controlButtonBackground
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Trophy icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = trophyColor.copy(alpha = 0.2f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (trophy.isUnlocked) {
                        getTrophyIcon(trophy.id)
                    } else {
                        Icons.Default.Lock
                    },
                    contentDescription = trophy.name,
                    tint = trophyColor,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Trophy name
            Text(
                text = trophy.name,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = colors.numberFixed,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Description
            Text(
                text = trophy.description,
                fontSize = 11.sp,
                color = colors.controlButtonIcon,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 14.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Progress bar
            if (!trophy.isUnlocked) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    LinearProgressIndicator(
                        progress = { trophy.progressPercentage },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = trophyColor,
                        trackColor = colors.controlButtonIcon.copy(alpha = 0.2f),
                        strokeCap = StrokeCap.Round
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "${trophy.progress}/${trophy.maxProgress}",
                        fontSize = 10.sp,
                        color = colors.controlButtonIcon
                    )
                }
            } else {
                // Unlocked indicator
                Text(
                    text = "Unlocked!",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = trophyColor
                )
            }
        }
    }
}

/**
 * Compact trophy row showing just icons.
 */
@Composable
fun TrophyRow(
    trophies: List<Trophy>,
    modifier: Modifier = Modifier
) {
    val colors = SudokuTheme.colors

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        trophies.take(5).forEach { trophy ->
            TrophyIcon(
                trophy = trophy,
                modifier = Modifier.size(40.dp)
            )
        }

        if (trophies.size > 5) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(colors.controlButtonBackground, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "+${trophies.size - 5}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = colors.controlButtonIcon
                )
            }
        }
    }
}

/**
 * Small trophy icon.
 */
@Composable
private fun TrophyIcon(
    trophy: Trophy,
    modifier: Modifier = Modifier
) {
    val colors = SudokuTheme.colors

    val trophyColor = if (trophy.isUnlocked) {
        getTrophyColor(trophy.id)
    } else {
        colors.controlButtonIcon.copy(alpha = 0.3f)
    }

    Box(
        modifier = modifier
            .background(
                color = trophyColor.copy(alpha = 0.2f),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = if (trophy.isUnlocked) {
                getTrophyIcon(trophy.id)
            } else {
                Icons.Default.Lock
            },
            contentDescription = trophy.name,
            tint = trophyColor,
            modifier = Modifier.size(24.dp)
        )
    }
}

/**
 * Large trophy display for achievement detail view.
 */
@Composable
fun TrophyDetailCard(
    trophy: Trophy,
    modifier: Modifier = Modifier
) {
    val colors = SudokuTheme.colors
    val trophyColor = if (trophy.isUnlocked) getTrophyColor(trophy.id) else colors.controlButtonIcon

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = colors.controlButtonBackground
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Large trophy icon
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(
                        color = trophyColor.copy(alpha = 0.2f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (trophy.isUnlocked) {
                        getTrophyIcon(trophy.id)
                    } else {
                        Icons.Default.Lock
                    },
                    contentDescription = trophy.name,
                    tint = trophyColor,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = trophy.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.numberFixed
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = trophy.description,
                    fontSize = 14.sp,
                    color = colors.controlButtonIcon
                )

                if (!trophy.isUnlocked) {
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        LinearProgressIndicator(
                            progress = { trophy.progressPercentage },
                            modifier = Modifier
                                .weight(1f)
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = trophyColor,
                            trackColor = colors.controlButtonIcon.copy(alpha = 0.2f),
                            strokeCap = StrokeCap.Round
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = "${trophy.progress}/${trophy.maxProgress}",
                            fontSize = 12.sp,
                            color = colors.controlButtonIcon
                        )
                    }
                }
            }
        }
    }
}

/**
 * Get color for trophy based on its ID/tier.
 */
private fun getTrophyColor(trophyId: String): Color {
    return when {
        trophyId.contains("gold") || trophyId.contains("master") -> TrophyGold
        trophyId.contains("silver") || trophyId.contains("week") -> TrophySilver
        else -> TrophyBronze
    }
}

/**
 * Get icon for trophy based on its ID.
 */
private fun getTrophyIcon(trophyId: String): ImageVector {
    return when {
        trophyId.contains("streak") || trophyId.contains("week") -> Icons.Default.Whatshot
        trophyId.contains("speed") || trophyId.contains("time") -> Icons.Default.Timer
        trophyId.contains("star") || trophyId.contains("perfect") -> Icons.Default.Star
        else -> Icons.Default.EmojiEvents
    }
}

// ==================== PREVIEWS ====================

@Preview(showBackground = true, backgroundColor = 0xFF0D1B2A)
@Composable
private fun PreviewTrophyDisplay() {
    SudokuPuzzleMasterTheme(darkTheme = true) {
        TrophyDisplay(
            trophies = previewTrophies(),
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0D1B2A)
@Composable
private fun PreviewTrophyCard_Unlocked() {
    SudokuPuzzleMasterTheme(darkTheme = true) {
        TrophyCard(
            trophy = Trophy(
                id = "week_streak",
                name = "Week Warrior",
                description = "Complete 7 days in a row",
                isUnlocked = true,
                progress = 7,
                maxProgress = 7
            ),
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0D1B2A)
@Composable
private fun PreviewTrophyCard_Locked() {
    SudokuPuzzleMasterTheme(darkTheme = true) {
        TrophyCard(
            trophy = Trophy(
                id = "month_master",
                name = "Month Master",
                description = "Complete all days in a month",
                isUnlocked = false,
                progress = 15,
                maxProgress = 31
            ),
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0D1B2A)
@Composable
private fun PreviewTrophyRow() {
    SudokuPuzzleMasterTheme(darkTheme = true) {
        TrophyRow(
            trophies = previewTrophies(),
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0D1B2A)
@Composable
private fun PreviewTrophyDetailCard() {
    SudokuPuzzleMasterTheme(darkTheme = true) {
        TrophyDetailCard(
            trophy = Trophy(
                id = "month_master",
                name = "Month Master",
                description = "Complete all days in a month",
                isUnlocked = false,
                progress = 15,
                maxProgress = 31
            ),
            modifier = Modifier.padding(16.dp)
        )
    }
}

private fun previewTrophies() = listOf(
    Trophy(
        id = "week_streak",
        name = "Week Warrior",
        description = "Complete 7 days in a row",
        isUnlocked = true,
        progress = 7,
        maxProgress = 7
    ),
    Trophy(
        id = "month_master",
        name = "Month Master",
        description = "Complete all days in a month",
        isUnlocked = false,
        progress = 15,
        maxProgress = 31
    ),
    Trophy(
        id = "speed_demon",
        name = "Speed Demon",
        description = "Complete in under 3 minutes",
        isUnlocked = true,
        progress = 1,
        maxProgress = 1
    ),
    Trophy(
        id = "perfect_gold",
        name = "Perfectionist",
        description = "No mistakes, no hints",
        isUnlocked = false,
        progress = 3,
        maxProgress = 10
    )
)
