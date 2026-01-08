package com.github.teocci.sudoku.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.teocci.sudoku.core.Constants.ANIMATION_COMPLETION_DURATION
import com.github.teocci.sudoku.core.Constants.ANIMATION_CONFETTI_DURATION
import com.github.teocci.sudoku.core.Constants.ANIMATION_SCORE_FLOAT_DURATION
import com.github.teocci.sudoku.ui.theme.ConfettiColors
import com.github.teocci.sudoku.ui.theme.ScoreFloatingStyle
import com.github.teocci.sudoku.ui.theme.SudokuPuzzleMasterTheme
import com.github.teocci.sudoku.ui.theme.SudokuTheme
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Floating score indicator that animates upward and fades out.
 *
 * @param points Points to display (can be negative)
 * @param onAnimationComplete Called when animation finishes
 * @param modifier Modifier for positioning
 */
@Composable
fun FloatingScoreIndicator(
    points: Int,
    onAnimationComplete: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val colors = SudokuTheme.colors
    val density = LocalDensity.current

    var isVisible by remember { mutableStateOf(true) }
    val offsetY = remember { Animatable(0f) }
    val alpha = remember { Animatable(1f) }

    val isPositive = points >= 0
    val textColor = if (isPositive) colors.scorePositive else colors.scoreNegative
    val prefix = if (isPositive) "+" else ""

    LaunchedEffect(Unit) {
        // Animate upward
        offsetY.animateTo(
            targetValue = -80f,
            animationSpec = tween(
                durationMillis = ANIMATION_SCORE_FLOAT_DURATION,
                easing = FastOutSlowInEasing
            )
        )
    }

    LaunchedEffect(Unit) {
        // Fade out after delay
        delay(ANIMATION_SCORE_FLOAT_DURATION / 2L)
        alpha.animateTo(
            targetValue = 0f,
            animationSpec = tween(
                durationMillis = ANIMATION_SCORE_FLOAT_DURATION / 2,
                easing = LinearEasing
            )
        )
        isVisible = false
        onAnimationComplete()
    }

    if (isVisible) {
        Text(
            text = "$prefix$points",
            style = ScoreFloatingStyle,
            color = textColor.copy(alpha = alpha.value),
            fontWeight = FontWeight.Bold,
            modifier = modifier.offset {
                IntOffset(0, with(density) { offsetY.value.dp.roundToPx() })
            }
        )
    }
}

/**
 * Container for managing multiple floating score indicators.
 */
@Composable
fun FloatingScoreContainer(
    scores: List<FloatingScore>,
    onScoreAnimationComplete: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxWidth()) {
        scores.forEach { score ->
            FloatingScoreIndicator(
                points = score.points,
                onAnimationComplete = { onScoreAnimationComplete(score.id) },
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(x = score.offsetX.dp, y = score.offsetY.dp)
            )
        }
    }
}

/**
 * Data class for floating score animation.
 */
data class FloatingScore(
    val id: String = java.util.UUID.randomUUID().toString(),
    val points: Int,
    val offsetX: Float = 0f,
    val offsetY: Float = 0f
)

/**
 * Completion flash overlay for rows, columns, or boxes.
 *
 * @param isVisible Whether the flash is visible
 * @param onAnimationComplete Called when animation finishes
 */
@Composable
fun CompletionFlash(
    isVisible: Boolean,
    onAnimationComplete: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val colors = SudokuTheme.colors

    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 0.4f else 0f,
        animationSpec = tween(
            durationMillis = ANIMATION_COMPLETION_DURATION / 2,
            easing = FastOutSlowInEasing
        ),
        finishedListener = {
            if (!isVisible) {
                onAnimationComplete()
            }
        },
        label = "flashAlpha"
    )

    val scale by animateFloatAsState(
        targetValue = if (isVisible) 1.05f else 1f,
        animationSpec = tween(
            durationMillis = ANIMATION_COMPLETION_DURATION / 2,
            easing = FastOutSlowInEasing
        ),
        label = "flashScale"
    )

    if (alpha > 0f) {
        Box(
            modifier = modifier
                .fillMaxSize()
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawRect(
                    color = colors.completionFlashStart.copy(alpha = alpha),
                    size = size
                )
            }
        }
    }
}

/**
 * Confetti particle for celebration animation.
 */
private data class ConfettiParticle(
    var x: Float,
    var y: Float,
    val velocityX: Float,
    var velocityY: Float,
    val rotation: Float,
    val rotationSpeed: Float,
    val color: Color,
    val size: Float,
    val shape: ConfettiShape
)

private enum class ConfettiShape {
    SQUARE, CIRCLE, RECTANGLE
}

/**
 * Game completion celebration with confetti animation.
 *
 * @param isVisible Whether the celebration is visible
 * @param onAnimationComplete Called when animation finishes
 */
@Composable
fun GameCompletionCelebration(
    isVisible: Boolean,
    onAnimationComplete: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(tween(300)) + scaleIn(tween(300)),
        exit = fadeOut(tween(500)) + scaleOut(tween(500)),
        modifier = modifier
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            ConfettiAnimation(
                onAnimationComplete = onAnimationComplete
            )
        }
    }
}

/**
 * Confetti particle animation.
 */
@Composable
private fun ConfettiAnimation(
    particleCount: Int = 100,
    onAnimationComplete: () -> Unit = {}
) {
    val density = LocalDensity.current
    val particles = remember {
        mutableStateListOf<ConfettiParticle>().apply {
            repeat(particleCount) {
                add(createConfettiParticle())
            }
        }
    }

    var animationProgress by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(Unit) {
        val startTime = System.currentTimeMillis()
        val duration = ANIMATION_CONFETTI_DURATION.toLong()

        while (System.currentTimeMillis() - startTime < duration) {
            val elapsed = System.currentTimeMillis() - startTime
            animationProgress = elapsed.toFloat() / duration

            // Update particle positions
            particles.forEachIndexed { index, particle ->
                particles[index] = particle.copy(
                    x = particle.x + particle.velocityX,
                    y = particle.y + particle.velocityY,
                    velocityY = particle.velocityY + 0.3f // Gravity
                )
            }

            delay(16) // ~60 FPS
        }

        onAnimationComplete()
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        particles.forEach { particle ->
            // Calculate actual position based on canvas size
            val x = (particle.x / 100f) * canvasWidth
            val y = (particle.y / 100f) * canvasHeight

            // Only draw if within bounds
            if (y < canvasHeight + 50) {
                rotate(
                    degrees = particle.rotation + (animationProgress * particle.rotationSpeed * 360),
                    pivot = Offset(x, y)
                ) {
                    when (particle.shape) {
                        ConfettiShape.SQUARE -> {
                            drawRect(
                                color = particle.color,
                                topLeft = Offset(x - particle.size / 2, y - particle.size / 2),
                                size = androidx.compose.ui.geometry.Size(particle.size, particle.size)
                            )
                        }
                        ConfettiShape.CIRCLE -> {
                            drawCircle(
                                color = particle.color,
                                radius = particle.size / 2,
                                center = Offset(x, y)
                            )
                        }
                        ConfettiShape.RECTANGLE -> {
                            drawRect(
                                color = particle.color,
                                topLeft = Offset(x - particle.size / 2, y - particle.size / 4),
                                size = androidx.compose.ui.geometry.Size(particle.size, particle.size / 2)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Create a random confetti particle.
 */
private fun createConfettiParticle(): ConfettiParticle {
    val angle = Random.nextFloat() * Math.PI.toFloat() // 0 to 180 degrees (downward spread)
    val speed = Random.nextFloat() * 3f + 1f

    return ConfettiParticle(
        x = Random.nextFloat() * 100f, // Percentage of width
        y = -Random.nextFloat() * 20f, // Start above screen
        velocityX = cos(angle) * speed * (if (Random.nextBoolean()) 1 else -1),
        velocityY = sin(angle) * speed + 1f,
        rotation = Random.nextFloat() * 360f,
        rotationSpeed = Random.nextFloat() * 2f - 1f,
        color = ConfettiColors.random(),
        size = Random.nextFloat() * 12f + 6f,
        shape = ConfettiShape.entries.random()
    )
}

/**
 * Pulsing glow effect for highlighted elements.
 */
@Composable
fun PulsingGlow(
    color: Color,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulsingGlow")

    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowScale"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        drawCircle(
            color = color.copy(alpha = alpha),
            radius = size.minDimension / 2 * scale
        )
    }
}

/**
 * Star rating display with animation.
 *
 * @param rating Number of filled stars (1-5)
 * @param maxStars Maximum number of stars
 * @param animated Whether to animate the stars appearing
 */
@Composable
fun StarRating(
    rating: Int,
    maxStars: Int = 5,
    animated: Boolean = true,
    modifier: Modifier = Modifier
) {
    val colors = SudokuTheme.colors

    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val starSize = size.width / maxStars * 0.8f
            val spacing = size.width / maxStars

            for (i in 0 until maxStars) {
                val centerX = spacing * i + spacing / 2
                val centerY = size.height / 2
                val isFilled = i < rating

                // Draw star shape
                drawStar(
                    center = Offset(centerX, centerY),
                    outerRadius = starSize / 2,
                    innerRadius = starSize / 4,
                    color = if (isFilled) colors.starFilled else colors.starEmpty
                )
            }
        }
    }
}

/**
 * Draw a 5-pointed star.
 */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawStar(
    center: Offset,
    outerRadius: Float,
    innerRadius: Float,
    color: Color
) {
    val path = androidx.compose.ui.graphics.Path()
    val points = 5
    val angleStep = (2 * Math.PI / points).toFloat()
    val startAngle = (-Math.PI / 2).toFloat() // Start at top

    for (i in 0 until points * 2) {
        val radius = if (i % 2 == 0) outerRadius else innerRadius
        val angle = startAngle + angleStep * i / 2
        val x = center.x + radius * cos(angle)
        val y = center.y + radius * sin(angle)

        if (i == 0) {
            path.moveTo(x, y)
        } else {
            path.lineTo(x, y)
        }
    }
    path.close()

    drawPath(path, color)
}

// ==================== PREVIEWS ====================

@Preview(showBackground = true, backgroundColor = 0xFF0D1B2A)
@Composable
private fun PreviewFloatingScorePositive() {
    SudokuPuzzleMasterTheme(darkTheme = true) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(100.dp),
            contentAlignment = Alignment.Center
        ) {
            FloatingScoreIndicator(points = 100)
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0D1B2A)
@Composable
private fun PreviewFloatingScoreNegative() {
    SudokuPuzzleMasterTheme(darkTheme = true) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(100.dp),
            contentAlignment = Alignment.Center
        ) {
            FloatingScoreIndicator(points = -20)
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0D1B2A)
@Composable
private fun PreviewCompletionFlash() {
    SudokuPuzzleMasterTheme(darkTheme = true) {
        Box(modifier = Modifier.fillMaxSize()) {
            CompletionFlash(isVisible = true)
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0D1B2A)
@Composable
private fun PreviewConfetti() {
    SudokuPuzzleMasterTheme(darkTheme = true) {
        Box(modifier = Modifier.fillMaxSize()) {
            GameCompletionCelebration(isVisible = true)
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0D1B2A)
@Composable
private fun PreviewStarRating() {
    SudokuPuzzleMasterTheme(darkTheme = true) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(50.dp)
        ) {
            StarRating(rating = 4, maxStars = 5)
        }
    }
}
