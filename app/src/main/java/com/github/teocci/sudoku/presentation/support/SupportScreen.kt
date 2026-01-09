package com.github.teocci.sudoku.presentation.support

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.teocci.sudoku.ui.theme.SudokuPuzzleMasterTheme
import com.github.teocci.sudoku.ui.theme.SudokuTheme

/**
 * Support/Donation screen.
 *
 * @param onNavigateBack Called when back button is clicked
 */
@Composable
fun SupportScreen(
    onNavigateBack: () -> Unit = {}
) {
    SupportScreenContent(
        onNavigateBack = onNavigateBack
    )
}

/**
 * Support screen content (stateless for preview).
 */
@Composable
private fun SupportScreenContent(
    onNavigateBack: () -> Unit
) {
    val colors = SudokuTheme.colors
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    fun openUrl(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Could not open browser", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = colors.controlButtonIcon
                )
            }

            Text(
                text = "Support Development",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = colors.numberFixed,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Hero Card (Appreciation)
            HeroCard()

            Spacer(modifier = Modifier.height(24.dp))

            // Support Options Section
            Text(
                text = "Ways to Support",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = colors.controlButtonBackground
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    SupportLinkItem(
                        icon = Icons.Default.LocalCafe,
                        title = "Buy Me a Coffee",
                        description = "One-time support",
                        onClick = { openUrl("https://buymeacoffee.com/teocci") }
                    )

                    SupportLinkItem(
                        icon = Icons.Default.Favorite,
                        title = "Become a Patron",
                        description = "Monthly support & perks",
                        onClick = { openUrl("https://www.patreon.com/teocci") }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // App Values Section
            Text(
                text = "What You're Supporting",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = colors.controlButtonBackground
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AppValueItem(text = "100% Ad-Free Experience")
                    AppValueItem(text = "No Data Collection")
                    AppValueItem(text = "Regular Updates & Features")
                    AppValueItem(text = "Community-Driven Development")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Footer Text
            Text(
                text = "Your support helps keep this app free and awesome for everyone",
                fontSize = 12.sp,
                color = colors.controlButtonIcon.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

/**
 * Hero card with appreciation message.
 */
@Composable
private fun HeroCard() {
    val colors = SudokuTheme.colors

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = colors.controlButtonBackground
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Heart Icon
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Headline
            Text(
                text = "Thank You for Playing!",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = colors.numberFixed,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Body Copy
            Text(
                text = "We're glad you're enjoying Sudoku Puzzle Master! This app is completely ad-free and respects your privacy—no data collection, no tracking, just pure puzzle fun.\n\n" +
                        "If you'd like to support continued development and help keep this app free for everyone, consider buying us a coffee or becoming a patron. Your support is greatly appreciated!",
                fontSize = 14.sp,
                color = colors.controlButtonIcon,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )
        }
    }
}

/**
 * Support link item (clickable donation link).
 */
@Composable
private fun SupportLinkItem(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = SudokuTheme.colors

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = colors.numberFixed
            )
            Text(
                text = description,
                fontSize = 13.sp,
                color = colors.controlButtonIcon
            )
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = colors.controlButtonIcon.copy(alpha = 0.5f),
            modifier = Modifier.size(20.dp)
        )
    }
}

/**
 * App value item (checkmark bullet point).
 */
@Composable
private fun AppValueItem(
    text: String,
    modifier: Modifier = Modifier
) {
    val colors = SudokuTheme.colors

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = colors.success,
            modifier = Modifier.size(20.dp)
        )

        Text(
            text = text,
            fontSize = 14.sp,
            color = colors.numberFixed,
            modifier = Modifier.padding(start = 12.dp)
        )
    }
}

// ==================== PREVIEWS ====================

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun PreviewSupportScreen() {
    SudokuPuzzleMasterTheme(darkTheme = true) {
        SupportScreenContent(
            onNavigateBack = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewHeroCard() {
    SudokuPuzzleMasterTheme(darkTheme = true) {
        Column(modifier = Modifier.padding(16.dp)) {
            HeroCard()
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewSupportLinkItem() {
    SudokuPuzzleMasterTheme(darkTheme = true) {
        val colors = SudokuTheme.colors
        Card(
            colors = CardDefaults.cardColors(
                containerColor = colors.controlButtonBackground
            )
        ) {
            Column {
                SupportLinkItem(
                    icon = Icons.Default.LocalCafe,
                    title = "Buy Me a Coffee",
                    description = "One-time support",
                    onClick = {}
                )
                SupportLinkItem(
                    icon = Icons.Default.Favorite,
                    title = "Become a Patron",
                    description = "Monthly support & perks",
                    onClick = {}
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewAppValueItem() {
    SudokuPuzzleMasterTheme(darkTheme = true) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AppValueItem(text = "100% Ad-Free Experience")
            AppValueItem(text = "No Data Collection")
            AppValueItem(text = "Regular Updates & Features")
            AppValueItem(text = "Community-Driven Development")
        }
    }
}
