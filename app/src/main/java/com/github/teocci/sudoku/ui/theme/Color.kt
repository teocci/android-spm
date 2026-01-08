package com.github.teocci.sudoku.ui.theme

import androidx.compose.ui.graphics.Color

// ==================== PRIMARY COLORS ====================

// Primary brand colors (blue accent)
val Primary = Color(0xFF4A90D9)
val PrimaryLight = Color(0xFF7BB5EA)
val PrimaryDark = Color(0xFF2D6AAF)
val PrimaryContainer = Color(0xFF1E3A5F)
val OnPrimary = Color(0xFFFFFFFF)
val OnPrimaryContainer = Color(0xFFD1E4FF)

// Secondary colors (teal accent)
val Secondary = Color(0xFF4DB6AC)
val SecondaryLight = Color(0xFF82E9DE)
val SecondaryDark = Color(0xFF00867D)
val SecondaryContainer = Color(0xFF1D4D4A)
val OnSecondary = Color(0xFF003731)
val OnSecondaryContainer = Color(0xFFB4F0EA)

// Tertiary colors (gold/amber for achievements)
val Tertiary = Color(0xFFFFB74D)
val TertiaryLight = Color(0xFFFFE97D)
val TertiaryDark = Color(0xFFC88719)
val TertiaryContainer = Color(0xFF4A3700)
val OnTertiary = Color(0xFF422C00)
val OnTertiaryContainer = Color(0xFFFFDEA6)

// ==================== BACKGROUND COLORS ====================

// Dark theme backgrounds
val BackgroundDark = Color(0xFF0D1B2A)
val BackgroundDarkSecondary = Color(0xFF1B2838)
val SurfaceDark = Color(0xFF1B2838)
val SurfaceDarkVariant = Color(0xFF243447)
val SurfaceDarkElevated = Color(0xFF2D3E50)

// Light theme backgrounds
val BackgroundLight = Color(0xFFF5F7FA)
val BackgroundLightSecondary = Color(0xFFE8EDF2)
val SurfaceLight = Color(0xFFFFFFFF)
val SurfaceLightVariant = Color(0xFFE8EDF2)
val SurfaceLightElevated = Color(0xFFFFFFFF)

// ==================== TEXT COLORS ====================

// Dark theme text
val OnBackgroundDark = Color(0xFFE1E8F0)
val OnSurfaceDark = Color(0xFFE1E8F0)
val OnSurfaceVariantDark = Color(0xFFB0BEC5)
val TextSecondaryDark = Color(0xFF8899A6)
val TextDisabledDark = Color(0xFF546E7A)

// Light theme text
val OnBackgroundLight = Color(0xFF1B2838)
val OnSurfaceLight = Color(0xFF1B2838)
val OnSurfaceVariantLight = Color(0xFF49596A)
val TextSecondaryLight = Color(0xFF6B7C8A)
val TextDisabledLight = Color(0xFF9EADBA)

// ==================== SUDOKU GRID COLORS ====================

// Cell backgrounds - Dark theme
val CellBackgroundDark = Color(0xFF1B2838)
val CellBackgroundAltDark = Color(0xFF243447)  // Alternate box background
val CellSelectedDark = Color(0xFF2D5A8A)       // Selected cell
val CellSelectedPressedDark = Color(0xFF234570) // Selected cell pressed state
val CellRelatedDark = Color(0xFF1E3A5F)        // Same row/col/box
val CellSameNumberDark = Color(0xFF2D4A6A)     // Same number highlight
val CellErrorDark = Color(0xFF5C2B2B)          // Error cell background
val CellCompletedDark = Color(0xFF1B4D3E)      // Completed row/col/box flash
val CellHintDark = Color(0xFF3D5A40)           // Hint cell highlight

// Cell backgrounds - Light theme
val CellBackgroundLight = Color(0xFFFFFFFF)
val CellBackgroundAltLight = Color(0xFFF0F4F8)
val CellSelectedLight = Color(0xFFBBDEFB)
val CellSelectedPressedLight = Color(0xFF90CAF9)
val CellRelatedLight = Color(0xFFE3F2FD)
val CellSameNumberLight = Color(0xFFCCE5FF)
val CellErrorLight = Color(0xFFFFCDD2)
val CellCompletedLight = Color(0xFFC8E6C9)
val CellHintLight = Color(0xFFDCEDC8)

// Grid lines
val GridLineThinDark = Color(0xFF3D4F61)
val GridLineThickDark = Color(0xFF5A7A9A)
val GridLineThinLight = Color(0xFFCFD8DC)
val GridLineThickLight = Color(0xFF90A4AE)

// ==================== NUMBER COLORS ====================

// Fixed numbers (clues) - high contrast
val NumberFixedDark = Color(0xFFE1E8F0)
val NumberFixedLight = Color(0xFF1B2838)

// User-entered numbers
val NumberUserDark = Color(0xFF7BB5EA)
val NumberUserLight = Color(0xFF2D6AAF)

// Error numbers
val NumberErrorDark = Color(0xFFEF5350)
val NumberErrorLight = Color(0xFFD32F2F)

// Notes (smaller numbers)
val NumberNoteDark = Color(0xFF8899A6)
val NumberNoteLight = Color(0xFF6B7C8A)

// Hint revealed numbers
val NumberHintDark = Color(0xFF81C784)
val NumberHintLight = Color(0xFF388E3C)

// Completed number (number pad when all 9 placed)
val NumberCompletedDark = Color(0xFF546E7A)
val NumberCompletedLight = Color(0xFF9EADBA)

// ==================== UI ELEMENT COLORS ====================

// Number pad
val NumberPadBackgroundDark = Color(0xFF243447)
val NumberPadBackgroundLight = Color(0xFFE8EDF2)
val NumberPadButtonDark = Color(0xFF2D3E50)
val NumberPadButtonLight = Color(0xFFFFFFFF)
val NumberPadButtonActiveDark = Color(0xFF2D5A8A)
val NumberPadButtonActiveLight = Color(0xFFBBDEFB)
val NumberPadTextDark = Color(0xFFE1E8F0)
val NumberPadTextLight = Color(0xFF1B2838)

// Control buttons (Undo, Erase, Notes, Hint)
val ControlButtonBackgroundDark = Color(0xFF2D3E50)
val ControlButtonBackgroundLight = Color(0xFFE8EDF2)
val ControlButtonIconDark = Color(0xFFB0BEC5)
val ControlButtonIconLight = Color(0xFF49596A)
val ControlButtonActiveBackgroundDark = Color(0xFF1E3A5F)
val ControlButtonActiveBackgroundLight = Color(0xFFBBDEFB)
val ControlButtonActiveIconDark = Color(0xFF7BB5EA)
val ControlButtonActiveIconLight = Color(0xFF2D6AAF)
val ControlButtonDisabledDark = Color(0xFF1B2838)
val ControlButtonDisabledLight = Color(0xFFF5F7FA)

// Navigation bar
val NavBarBackgroundDark = Color(0xFF1B2838)
val NavBarBackgroundLight = Color(0xFFFFFFFF)
val NavBarSelectedDark = Color(0xFF4A90D9)
val NavBarSelectedLight = Color(0xFF4A90D9)
val NavBarUnselectedDark = Color(0xFF546E7A)
val NavBarUnselectedLight = Color(0xFF9EADBA)

// ==================== STATUS COLORS ====================

// Success/Correct
val SuccessDark = Color(0xFF81C784)
val SuccessLight = Color(0xFF4CAF50)
val SuccessContainerDark = Color(0xFF1B4D3E)
val SuccessContainerLight = Color(0xFFC8E6C9)

// Error/Mistake
val ErrorDark = Color(0xFFEF5350)
val ErrorLight = Color(0xFFD32F2F)
val ErrorContainerDark = Color(0xFF5C2B2B)
val ErrorContainerLight = Color(0xFFFFCDD2)

// Warning
val WarningDark = Color(0xFFFFB74D)
val WarningLight = Color(0xFFFF9800)
val WarningContainerDark = Color(0xFF4A3700)
val WarningContainerLight = Color(0xFFFFE0B2)

// Info
val InfoDark = Color(0xFF4FC3F7)
val InfoLight = Color(0xFF03A9F4)
val InfoContainerDark = Color(0xFF0D3D56)
val InfoContainerLight = Color(0xFFB3E5FC)

// ==================== SPECIAL COLORS ====================

// Timer colors
val TimerTextDark = Color(0xFFE1E8F0)
val TimerTextLight = Color(0xFF1B2838)
val TimerPausedDark = Color(0xFFFFB74D)
val TimerPausedLight = Color(0xFFFF9800)

// Score colors
val ScoreTextDark = Color(0xFFFFD700)
val ScoreTextLight = Color(0xFFFFB300)
val ScorePositiveDark = Color(0xFF81C784)
val ScorePositiveLight = Color(0xFF4CAF50)
val ScoreNegativeDark = Color(0xFFEF5350)
val ScoreNegativeLight = Color(0xFFD32F2F)

// Mistakes counter
val MistakesDotEmptyDark = Color(0xFF3D4F61)
val MistakesDotEmptyLight = Color(0xFFCFD8DC)
val MistakesDotFilledDark = Color(0xFFEF5350)
val MistakesDotFilledLight = Color(0xFFD32F2F)

// Stars (rating)
val StarFilledDark = Color(0xFFFFD700)
val StarFilledLight = Color(0xFFFFB300)
val StarEmptyDark = Color(0xFF3D4F61)
val StarEmptyLight = Color(0xFFCFD8DC)

// Streak indicator (fire icon)
val StreakFireDark = Color(0xFFFF6F00)      // Deep vibrant orange
val StreakFireLight = Color(0xFFE65100)     // Darker orange for contrast

// Trophy colors
val TrophyGold = Color(0xFFFFD700)
val TrophySilver = Color(0xFFC0C0C0)
val TrophyBronze = Color(0xFFCD7F32)

// Calendar (daily challenges)
val CalendarDayCompletedDark = Color(0xFF4A90D9)
val CalendarDayCompletedLight = Color(0xFF4A90D9)
val CalendarDayCurrentDark = Color(0xFF2D5A8A)
val CalendarDayCurrentLight = Color(0xFFBBDEFB)
val CalendarDayFutureDark = Color(0xFF243447)
val CalendarDayFutureLight = Color(0xFFE8EDF2)

// ==================== ANIMATION COLORS ====================

// Completion animation
val CompletionFlashStart = Color(0xFF4A90D9)
val CompletionFlashEnd = Color(0xFF81C784)
val ConfettiColors = listOf(
    Color(0xFFFF6B6B),  // Red
    Color(0xFF4ECDC4),  // Teal
    Color(0xFFFFE66D),  // Yellow
    Color(0xFF95E1D3),  // Mint
    Color(0xFFF38181),  // Coral
    Color(0xFFAA96DA),  // Purple
    Color(0xFFFCBAD3),  // Pink
    Color(0xFFA8D8EA)   // Light blue
)

// ==================== DIFFICULTY COLORS ====================

val DifficultyEasy = Color(0xFF81C784)
val DifficultyMedium = Color(0xFF4A90D9)
val DifficultyHard = Color(0xFFFFB74D)
val DifficultyExpert = Color(0xFFEF5350)

// ==================== LEGACY COLORS (for compatibility) ====================

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)
