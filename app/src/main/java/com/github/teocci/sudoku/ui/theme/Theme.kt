package com.github.teocci.sudoku.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Custom color scheme for Sudoku-specific UI elements.
 */
data class SudokuColors(
    // Cell backgrounds
    val cellBackground: Color,
    val cellBackgroundAlt: Color,
    val cellSelected: Color,
    val cellSelectedPressed: Color,
    val cellRelated: Color,
    val cellSameNumber: Color,
    val cellError: Color,
    val cellCompleted: Color,
    val cellHint: Color,

    // Grid lines
    val gridLineThin: Color,
    val gridLineThick: Color,

    // Numbers
    val numberFixed: Color,
    val numberUser: Color,
    val numberError: Color,
    val numberNote: Color,
    val numberHint: Color,
    val numberCompleted: Color,

    // Number pad
    val numberPadBackground: Color,
    val numberPadButton: Color,
    val numberPadButtonActive: Color,
    val numberPadText: Color,

    // Control buttons
    val controlButtonBackground: Color,
    val controlButtonIcon: Color,
    val controlButtonActiveBackground: Color,
    val controlButtonActiveIcon: Color,
    val controlButtonDisabled: Color,

    // Navigation
    val navBarBackground: Color,
    val navBarSelected: Color,
    val navBarUnselected: Color,

    // Status
    val success: Color,
    val successContainer: Color,
    val error: Color,
    val errorContainer: Color,
    val warning: Color,
    val warningContainer: Color,
    val info: Color,
    val infoContainer: Color,

    // Special
    val timerText: Color,
    val timerPaused: Color,
    val scoreText: Color,
    val scorePositive: Color,
    val scoreNegative: Color,
    val mistakesDotEmpty: Color,
    val mistakesDotFilled: Color,
    val starFilled: Color,
    val starEmpty: Color,

    // Calendar
    val calendarDayCompleted: Color,
    val calendarDayCurrent: Color,
    val calendarDayFuture: Color,

    // Streak indicator
    val streakFire: Color,

    // Animation
    val completionFlashStart: Color,
    val completionFlashEnd: Color,
    val confetti: List<Color>
)

/**
 * Dark theme Sudoku colors.
 */
private val DarkSudokuColors = SudokuColors(
    cellBackground = CellBackgroundDark,
    cellBackgroundAlt = CellBackgroundAltDark,
    cellSelected = CellSelectedDark,
    cellSelectedPressed = CellSelectedPressedDark,
    cellRelated = CellRelatedDark,
    cellSameNumber = CellSameNumberDark,
    cellError = CellErrorDark,
    cellCompleted = CellCompletedDark,
    cellHint = CellHintDark,

    gridLineThin = GridLineThinDark,
    gridLineThick = GridLineThickDark,

    numberFixed = NumberFixedDark,
    numberUser = NumberUserDark,
    numberError = NumberErrorDark,
    numberNote = NumberNoteDark,
    numberHint = NumberHintDark,
    numberCompleted = NumberCompletedDark,

    numberPadBackground = NumberPadBackgroundDark,
    numberPadButton = NumberPadButtonDark,
    numberPadButtonActive = NumberPadButtonActiveDark,
    numberPadText = NumberPadTextDark,

    controlButtonBackground = ControlButtonBackgroundDark,
    controlButtonIcon = ControlButtonIconDark,
    controlButtonActiveBackground = ControlButtonActiveBackgroundDark,
    controlButtonActiveIcon = ControlButtonActiveIconDark,
    controlButtonDisabled = ControlButtonDisabledDark,

    navBarBackground = NavBarBackgroundDark,
    navBarSelected = NavBarSelectedDark,
    navBarUnselected = NavBarUnselectedDark,

    success = SuccessDark,
    successContainer = SuccessContainerDark,
    error = ErrorDark,
    errorContainer = ErrorContainerDark,
    warning = WarningDark,
    warningContainer = WarningContainerDark,
    info = InfoDark,
    infoContainer = InfoContainerDark,

    timerText = TimerTextDark,
    timerPaused = TimerPausedDark,
    scoreText = ScoreTextDark,
    scorePositive = ScorePositiveDark,
    scoreNegative = ScoreNegativeDark,
    mistakesDotEmpty = MistakesDotEmptyDark,
    mistakesDotFilled = MistakesDotFilledDark,
    starFilled = StarFilledDark,
    starEmpty = StarEmptyDark,

    calendarDayCompleted = CalendarDayCompletedDark,
    calendarDayCurrent = CalendarDayCurrentDark,
    calendarDayFuture = CalendarDayFutureDark,

    streakFire = StreakFireDark,

    completionFlashStart = CompletionFlashStart,
    completionFlashEnd = CompletionFlashEnd,
    confetti = ConfettiColors
)

/**
 * Light theme Sudoku colors.
 */
private val LightSudokuColors = SudokuColors(
    cellBackground = CellBackgroundLight,
    cellBackgroundAlt = CellBackgroundAltLight,
    cellSelected = CellSelectedLight,
    cellSelectedPressed = CellSelectedPressedLight,
    cellRelated = CellRelatedLight,
    cellSameNumber = CellSameNumberLight,
    cellError = CellErrorLight,
    cellCompleted = CellCompletedLight,
    cellHint = CellHintLight,

    gridLineThin = GridLineThinLight,
    gridLineThick = GridLineThickLight,

    numberFixed = NumberFixedLight,
    numberUser = NumberUserLight,
    numberError = NumberErrorLight,
    numberNote = NumberNoteLight,
    numberHint = NumberHintLight,
    numberCompleted = NumberCompletedLight,

    numberPadBackground = NumberPadBackgroundLight,
    numberPadButton = NumberPadButtonLight,
    numberPadButtonActive = NumberPadButtonActiveLight,
    numberPadText = NumberPadTextLight,

    controlButtonBackground = ControlButtonBackgroundLight,
    controlButtonIcon = ControlButtonIconLight,
    controlButtonActiveBackground = ControlButtonActiveBackgroundLight,
    controlButtonActiveIcon = ControlButtonActiveIconLight,
    controlButtonDisabled = ControlButtonDisabledLight,

    navBarBackground = NavBarBackgroundLight,
    navBarSelected = NavBarSelectedLight,
    navBarUnselected = NavBarUnselectedLight,

    success = SuccessLight,
    successContainer = SuccessContainerLight,
    error = ErrorLight,
    errorContainer = ErrorContainerLight,
    warning = WarningLight,
    warningContainer = WarningContainerLight,
    info = InfoLight,
    infoContainer = InfoContainerLight,

    timerText = TimerTextLight,
    timerPaused = TimerPausedLight,
    scoreText = ScoreTextLight,
    scorePositive = ScorePositiveLight,
    scoreNegative = ScoreNegativeLight,
    mistakesDotEmpty = MistakesDotEmptyLight,
    mistakesDotFilled = MistakesDotFilledLight,
    starFilled = StarFilledLight,
    starEmpty = StarEmptyLight,

    calendarDayCompleted = CalendarDayCompletedLight,
    calendarDayCurrent = CalendarDayCurrentLight,
    calendarDayFuture = CalendarDayFutureLight,

    streakFire = StreakFireLight,

    completionFlashStart = CompletionFlashStart,
    completionFlashEnd = CompletionFlashEnd,
    confetti = ConfettiColors
)

/**
 * CompositionLocal for Sudoku colors.
 */
val LocalSudokuColors = staticCompositionLocalOf { DarkSudokuColors }

/**
 * Material3 dark color scheme for Sudoku.
 */
private val DarkColorScheme = darkColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryContainer,
    onPrimaryContainer = OnPrimaryContainer,
    secondary = Secondary,
    onSecondary = OnSecondary,
    secondaryContainer = SecondaryContainer,
    onSecondaryContainer = OnSecondaryContainer,
    tertiary = Tertiary,
    onTertiary = OnTertiary,
    tertiaryContainer = TertiaryContainer,
    onTertiaryContainer = OnTertiaryContainer,
    background = BackgroundDark,
    onBackground = OnBackgroundDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceDarkVariant,
    onSurfaceVariant = OnSurfaceVariantDark,
    error = ErrorDark,
    onError = Color.White,
    errorContainer = ErrorContainerDark,
    onErrorContainer = Color.White
)

/**
 * Material3 light color scheme for Sudoku.
 */
private val LightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryContainer,
    onPrimaryContainer = OnPrimaryContainer,
    secondary = Secondary,
    onSecondary = OnSecondary,
    secondaryContainer = SecondaryContainer,
    onSecondaryContainer = OnSecondaryContainer,
    tertiary = Tertiary,
    onTertiary = OnTertiary,
    tertiaryContainer = TertiaryContainer,
    onTertiaryContainer = OnTertiaryContainer,
    background = BackgroundLight,
    onBackground = OnBackgroundLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceLightVariant,
    onSurfaceVariant = OnSurfaceVariantLight,
    error = ErrorLight,
    onError = Color.White,
    errorContainer = ErrorContainerLight,
    onErrorContainer = Color.Black
)

/**
 * Main theme composable for Sudoku Puzzle Master.
 *
 * @param darkTheme Whether to use dark theme
 * @param dynamicColor Whether to use dynamic colors (Android 12+)
 * @param content The content to display
 */
@Composable
fun SudokuPuzzleMasterTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Disabled by default for consistent game look
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val sudokuColors = if (darkTheme) DarkSudokuColors else LightSudokuColors

    // Set status bar color
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    CompositionLocalProvider(LocalSudokuColors provides sudokuColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}

/**
 * Access Sudoku-specific colors from any composable.
 */
object SudokuTheme {
    val colors: SudokuColors
        @Composable
        @ReadOnlyComposable
        get() = LocalSudokuColors.current
}
