package com.github.teocci.sudoku.presentation.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Highlight
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.teocci.sudoku.ui.theme.SudokuPuzzleMasterTheme
import com.github.teocci.sudoku.ui.theme.SudokuTheme
import kotlinx.coroutines.flow.collectLatest

/**
 * Settings screen.
 *
 * @param viewModel The settings ViewModel
 * @param onNavigateBack Called when back button is clicked
 * @param onThemeChanged Called when theme is changed
 */
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = viewModel(),
    onNavigateBack: () -> Unit = {},
    onNavigateToSupport: () -> Unit = {},
    onThemeChanged: (Boolean) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    // Handle events
    LaunchedEffect(Unit) {
        viewModel.event.collectLatest { event ->
            when (event) {
                is SettingsEvent.ThemeChanged -> onThemeChanged(event.darkTheme)
                is SettingsEvent.StatisticsReset -> { /* Show toast */ }
                is SettingsEvent.SavedGamesCleared -> { /* Show toast */ }
            }
        }
    }

    SettingsScreenContent(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onNavigateToSupport = onNavigateToSupport,
        onToggleSound = viewModel::toggleSound,
        onToggleHaptic = viewModel::toggleHaptic,
        onToggleAutoRemoveNotes = viewModel::toggleAutoRemoveNotes,
        onToggleHighlightRelated = viewModel::toggleHighlightRelatedCells,
        onToggleHighlightSame = viewModel::toggleHighlightSameNumbers,
        onToggleShowTimer = viewModel::toggleShowTimer,
        onToggleShowMistakes = viewModel::toggleShowMistakes,
        onToggleDarkTheme = viewModel::toggleDarkTheme,
        onToggleAutoSave = viewModel::toggleAutoSave,
        onResetStatistics = viewModel::showResetConfirmation,
        onClearSavedGames = viewModel::showClearGamesConfirmation,
        onDismissResetDialog = viewModel::hideResetConfirmation,
        onConfirmReset = viewModel::confirmResetStatistics,
        onDismissClearDialog = viewModel::hideClearGamesConfirmation,
        onConfirmClear = viewModel::confirmClearSavedGames,
        appVersion = viewModel.getAppVersion()
    )
}

/**
 * Settings screen content (stateless for preview).
 */
@Composable
private fun SettingsScreenContent(
    uiState: SettingsUiState,
    onNavigateBack: () -> Unit,
    onNavigateToSupport: () -> Unit,
    onToggleSound: () -> Unit,
    onToggleHaptic: () -> Unit,
    onToggleAutoRemoveNotes: () -> Unit,
    onToggleHighlightRelated: () -> Unit,
    onToggleHighlightSame: () -> Unit,
    onToggleShowTimer: () -> Unit,
    onToggleShowMistakes: () -> Unit,
    onToggleDarkTheme: () -> Unit,
    onToggleAutoSave: () -> Unit,
    onResetStatistics: () -> Unit,
    onClearSavedGames: () -> Unit,
    onDismissResetDialog: () -> Unit,
    onConfirmReset: () -> Unit,
    onDismissClearDialog: () -> Unit,
    onConfirmClear: () -> Unit,
    appVersion: String
) {
    val colors = SudokuTheme.colors
    val scrollState = rememberScrollState()

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
                text = "Settings",
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

            // Gameplay Section
            SettingsSection(title = "Gameplay") {
                SettingsToggleItem(
                    icon = Icons.Default.VolumeUp,
                    title = "Sound Effects",
                    description = "Play sounds for actions",
                    isEnabled = uiState.soundEnabled,
                    onToggle = onToggleSound
                )

                SettingsToggleItem(
                    icon = Icons.Default.Vibration,
                    title = "Haptic Feedback",
                    description = "Vibrate on actions",
                    isEnabled = uiState.hapticEnabled,
                    onToggle = onToggleHaptic
                )

                SettingsToggleItem(
                    icon = Icons.Default.GridOn,
                    title = "Auto-Remove Notes",
                    description = "Remove notes when placing numbers",
                    isEnabled = uiState.autoRemoveNotes,
                    onToggle = onToggleAutoRemoveNotes
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Highlighting Section
            SettingsSection(title = "Highlighting") {
                SettingsToggleItem(
                    icon = Icons.Default.Highlight,
                    title = "Highlight Related Cells",
                    description = "Highlight row, column, and box",
                    isEnabled = uiState.highlightRelatedCells,
                    onToggle = onToggleHighlightRelated
                )

                SettingsToggleItem(
                    icon = Icons.Default.Numbers,
                    title = "Highlight Same Numbers",
                    description = "Highlight matching numbers",
                    isEnabled = uiState.highlightSameNumbers,
                    onToggle = onToggleHighlightSame
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Display Section
            SettingsSection(title = "Display") {
                SettingsToggleItem(
                    icon = Icons.Default.Timer,
                    title = "Show Timer",
                    description = "Display game timer",
                    isEnabled = uiState.showTimer,
                    onToggle = onToggleShowTimer
                )

                SettingsToggleItem(
                    icon = Icons.Default.Warning,
                    title = "Show Mistakes",
                    description = "Display mistakes counter",
                    isEnabled = uiState.showMistakes,
                    onToggle = onToggleShowMistakes
                )

                SettingsToggleItem(
                    icon = Icons.Default.DarkMode,
                    title = "Dark Theme",
                    description = "Use dark color scheme",
                    isEnabled = uiState.darkTheme,
                    onToggle = onToggleDarkTheme
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Data Section
            SettingsSection(title = "Data") {
                SettingsToggleItem(
                    icon = Icons.Default.Save,
                    title = "Auto-Save",
                    description = "Automatically save game progress",
                    isEnabled = uiState.autoSave,
                    onToggle = onToggleAutoSave
                )

                SettingsActionItem(
                    icon = Icons.Default.Refresh,
                    title = "Reset Statistics",
                    description = "Clear all game statistics",
                    onClick = onResetStatistics,
                    isDangerous = true
                )

                SettingsActionItem(
                    icon = Icons.Default.Delete,
                    title = "Clear Saved Games",
                    description = "Delete all saved game data",
                    onClick = onClearSavedGames,
                    isDangerous = true
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // About Section
            SettingsSection(title = "About") {
                SettingsActionItem(
                    icon = Icons.Default.Favorite,
                    title = "Support Development",
                    description = "Help keep this app free & ad-free",
                    onClick = onNavigateToSupport,
                    isDangerous = false
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // App version
            Text(
                text = "Sudoku Puzzle Master",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = colors.controlButtonIcon,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                text = "Version $appVersion",
                fontSize = 12.sp,
                color = colors.controlButtonIcon.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Reset Statistics Dialog
    if (uiState.showResetDialog) {
        ConfirmationDialog(
            title = "Reset Statistics?",
            message = "This will permanently delete all your game statistics. This action cannot be undone.",
            confirmText = "Reset",
            onConfirm = onConfirmReset,
            onDismiss = onDismissResetDialog
        )
    }

    // Clear Saved Games Dialog
    if (uiState.showClearGamesDialog) {
        ConfirmationDialog(
            title = "Clear Saved Games?",
            message = "This will delete all saved game progress. Any unfinished games will be lost.",
            confirmText = "Clear",
            onConfirm = onConfirmClear,
            onDismiss = onDismissClearDialog
        )
    }
}

/**
 * Settings section with title.
 */
@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    val colors = SudokuTheme.colors

    Column {
        Text(
            text = title,
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
                content()
            }
        }
    }
}

/**
 * Toggle settings item.
 */
@Composable
private fun SettingsToggleItem(
    icon: ImageVector,
    title: String,
    description: String,
    isEnabled: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = SudokuTheme.colors

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = colors.controlButtonIcon,
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

        Switch(
            checked = isEnabled,
            onCheckedChange = { onToggle() },
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.primary,
                checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                uncheckedThumbColor = colors.controlButtonIcon,
                uncheckedTrackColor = colors.controlButtonIcon.copy(alpha = 0.3f)
            )
        )
    }
}

/**
 * Action settings item (non-toggle).
 */
@Composable
private fun SettingsActionItem(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit,
    isDangerous: Boolean = false,
    modifier: Modifier = Modifier
) {
    val colors = SudokuTheme.colors
    val textColor = if (isDangerous) colors.error else colors.numberFixed

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
            tint = if (isDangerous) colors.error else colors.controlButtonIcon,
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
                color = textColor
            )
            Text(
                text = description,
                fontSize = 13.sp,
                color = colors.controlButtonIcon
            )
        }
    }
}

/**
 * Confirmation dialog.
 */
@Composable
private fun ConfirmationDialog(
    title: String,
    message: String,
    confirmText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val colors = SudokuTheme.colors

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                fontWeight = FontWeight.SemiBold,
                color = colors.numberFixed
            )
        },
        text = {
            Text(
                text = message,
                color = colors.controlButtonIcon
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = confirmText,
                    color = colors.error,
                    fontWeight = FontWeight.Medium
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Cancel",
                    color = colors.controlButtonIcon
                )
            }
        },
        containerColor = colors.controlButtonBackground,
        shape = RoundedCornerShape(16.dp)
    )
}

// ==================== PREVIEWS ====================

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun PreviewSettingsScreen() {
    SudokuPuzzleMasterTheme(darkTheme = true) {
        SettingsScreenContent(
            uiState = SettingsUiState(),
            onNavigateBack = {},
            onNavigateToSupport = {},
            onToggleSound = {},
            onToggleHaptic = {},
            onToggleAutoRemoveNotes = {},
            onToggleHighlightRelated = {},
            onToggleHighlightSame = {},
            onToggleShowTimer = {},
            onToggleShowMistakes = {},
            onToggleDarkTheme = {},
            onToggleAutoSave = {},
            onResetStatistics = {},
            onClearSavedGames = {},
            onDismissResetDialog = {},
            onConfirmReset = {},
            onDismissClearDialog = {},
            onConfirmClear = {},
            appVersion = "1.0.0"
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewSettingsSection() {
    SudokuPuzzleMasterTheme(darkTheme = true) {
        Column(modifier = Modifier.padding(16.dp)) {
            SettingsSection(title = "Gameplay") {
                SettingsToggleItem(
                    icon = Icons.Default.VolumeUp,
                    title = "Sound Effects",
                    description = "Play sounds for actions",
                    isEnabled = true,
                    onToggle = {}
                )
                SettingsToggleItem(
                    icon = Icons.Default.Vibration,
                    title = "Haptic Feedback",
                    description = "Vibrate on actions",
                    isEnabled = false,
                    onToggle = {}
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewConfirmationDialog() {
    SudokuPuzzleMasterTheme(darkTheme = true) {
        ConfirmationDialog(
            title = "Reset Statistics?",
            message = "This will permanently delete all your game statistics. This action cannot be undone.",
            confirmText = "Reset",
            onConfirm = {},
            onDismiss = {}
        )
    }
}
