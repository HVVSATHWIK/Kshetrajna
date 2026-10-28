package com.kshetrajna.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kshetrajna.app.ui.theme.ForestGreenDark
import com.kshetrajna.app.ui.theme.PlayfairFontFamily
import com.kshetrajna.app.ui.theme.WarmGold
import com.kshetrajna.app.ui.theme.WarmIvory
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private val RestrainedDecelEasing = CubicBezierEasing(0.2f, 0.0f, 0.2f, 1.0f)
private val SmoothEaseOut = CubicBezierEasing(0.25f, 1.0f, 0.5f, 1.0f)

/**
 * Single-run, cinematic startup animation for Kshetrajna.
 * Total duration: ~1.85 seconds with poise, intentional pacing, and instant tap-to-skip.
 */
@Composable
fun KshetrajnaAnimatedSplash(
    onSplashFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    var hasFinished by remember { mutableStateOf(false) }
    val finishSplash = {
        if (!hasFinished) {
            hasFinished = true
            onSplashFinished()
        }
    }

    // Stage Motion Progress Animators
    val sunProgress = remember { Animatable(0f) }      // 0 -> 1: Observer point expansion
    val archProgress = remember { Animatable(0f) }     // 0 -> 1: Arch of awareness sweep
    val furrowsProgress = remember { Animatable(0f) }  // 0 -> 1: Field furrows resolution
    val titleAlpha = remember { Animatable(0f) }       // 0 -> 1: Wordmark & Sanskrit fade
    val titleOffsetY = remember { Animatable(10f) }    // 10dp -> 0dp: Subtle upward settle
    val dividerScale = remember { Animatable(0f) }     // 0 -> 1: Hairline expansion
    val taglineAlpha = remember { Animatable(0f) }     // 0 -> 1: Tagline fade

    LaunchedEffect(Unit) {
        // Stage 1: The Observer (Knower) awakens (0ms - 450ms)
        launch {
            sunProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 450, easing = SmoothEaseOut)
            )
        }

        // Stage 2: Arch draws gracefully across the horizon (250ms - 800ms)
        launch {
            delay(250)
            archProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 550, easing = RestrainedDecelEasing)
            )
        }

        // Stage 3: Field lines resolve into place (450ms - 950ms)
        launch {
            delay(450)
            furrowsProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 500, easing = RestrainedDecelEasing)
            )
        }

        // Stage 4: Typography reveals with editorial poise (800ms - 1300ms)
        launch {
            delay(800)
            launch {
                titleAlpha.animateTo(1f, animationSpec = tween(450, easing = LinearEasing))
            }
            launch {
                titleOffsetY.animateTo(0f, animationSpec = tween(450, easing = SmoothEaseOut))
            }
            launch {
                delay(120)
                dividerScale.animateTo(1f, animationSpec = tween(400, easing = SmoothEaseOut))
            }
            launch {
                delay(240)
                taglineAlpha.animateTo(1f, animationSpec = tween(380, easing = LinearEasing))
            }
        }

        // Stage 5: Poised stillness hold (350ms), then seamless transition
        delay(1850)
        finishSplash()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ForestGreenDark)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                // Instant tap-to-skip for immediate camera access
                finishSplash()
            }
            .testTag("animated_splash_screen"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        ) {
            // Authentic Emblem Canvas
            ResolvingInsigniaCanvas(
                sunProgress = sunProgress.value,
                archProgress = archProgress.value,
                furrowProgress = furrowsProgress.value,
                size = 140.dp
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Typographic Wordmark & Motto
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .offset { IntOffset(0, titleOffsetY.value.dp.roundToPx()) }
                    .alpha(titleAlpha.value)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Kshetrajna",
                        color = WarmIvory,
                        fontFamily = PlayfairFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 32.sp,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "क्षेत्रज्ञ",
                        color = WarmGold,
                        fontFamily = PlayfairFontFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 20.sp,
                        letterSpacing = 1.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Restrained Hairline Divider with Center Diamond
                QuietInsigniaDivider(
                    scale = dividerScale.value,
                    modifier = Modifier.padding(vertical = 2.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Motto with authoritative tracking
                Text(
                    text = "CAPTURE WHAT MATTERS.",
                    color = WarmGold.copy(alpha = 0.92f),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 10.5.sp,
                    letterSpacing = 2.8.sp,
                    modifier = Modifier.alpha(taglineAlpha.value)
                )
            }
        }

        // Subtle skip hint
        Text(
            text = "Tap to skip",
            color = WarmIvory.copy(alpha = 0.35f),
            style = MaterialTheme.typography.labelSmall,
            letterSpacing = 1.sp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 24.dp)
                .alpha(taglineAlpha.value * 0.7f)
        )
    }
}

/**
 * Resolves the Kshetrajna insignia into its definitive vector proportions.
 */
@Composable
private fun ResolvingInsigniaCanvas(
    sunProgress: Float,
    archProgress: Float,
    furrowProgress: Float,
    size: Dp
) {
    Canvas(modifier = Modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height

        // 1. Central Observer Point / Rising Sun
        // Exact geometry matching KshetrajnaInsignia: center at (w * 0.5f, h * 0.38f), radius w * 0.11f
        if (sunProgress > 0f) {
            val sunCenter = Offset(w * 0.5f, h * 0.38f)
            val currentRadius = w * 0.11f * sunProgress

            // Subtle soft outer halo during the initial bloom
            if (sunProgress < 0.95f) {
                drawCircle(
                    color = WarmGold.copy(alpha = 0.25f * (1f - sunProgress)),
                    radius = currentRadius * 1.5f,
                    center = sunCenter
                )
            }

            // Solid Warm Gold Core
            drawCircle(
                color = WarmGold,
                radius = currentRadius,
                center = sunCenter
            )
        }

        // 2. Arch of Awareness
        // Exact geometry matching KshetrajnaInsignia:
        // strokeWidth = w * 0.075f, archSize = Size(w * 0.74f, h * 0.74f), topLeft = Offset((w - archSize.width) / 2f, h * 0.12f)
        if (archProgress > 0f) {
            val strokeWidth = (w * 0.075f).coerceAtLeast(1.5f)
            val archSize = Size(w * 0.74f, h * 0.74f)
            val archTopLeft = Offset((w - archSize.width) / 2f, h * 0.12f)

            drawArc(
                color = WarmGold,
                startAngle = 180f,
                sweepAngle = 180f * archProgress,
                useCenter = false,
                topLeft = archTopLeft,
                size = archSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }

        // 3. Horizon Field Furrows
        // Exact geometry matching KshetrajnaInsignia:
        // lineStroke = w * 0.055f
        // Furrow 1: Warm Ivory, from w * 0.22f to w * 0.78f, y = h * 0.64f
        // Furrow 2: Warm Gold, from w * 0.30f to w * 0.70f, y = h * 0.76f
        if (furrowProgress > 0f) {
            val lineStroke = (w * 0.055f).coerceAtLeast(1.2f)

            // Upper Furrow (Warm Ivory) - Expands symmetrically from center
            val halfSpan1 = (w * 0.28f) * furrowProgress
            drawLine(
                color = WarmIvory.copy(alpha = furrowProgress.coerceIn(0f, 1f)),
                start = Offset(w * 0.5f - halfSpan1, h * 0.64f),
                end = Offset(w * 0.5f + halfSpan1, h * 0.64f),
                strokeWidth = lineStroke,
                cap = StrokeCap.Round
            )

            // Lower Furrow (Warm Gold) - Expands symmetrically from center
            val halfSpan2 = (w * 0.20f) * furrowProgress
            drawLine(
                color = WarmGold.copy(alpha = furrowProgress.coerceIn(0f, 1f)),
                start = Offset(w * 0.5f - halfSpan2, h * 0.76f),
                end = Offset(w * 0.5f + halfSpan2, h * 0.76f),
                strokeWidth = lineStroke * 0.85f,
                cap = StrokeCap.Round
            )
        }
    }
}

/**
 * Restrained hairline divider with a central diamond pip.
 */
@Composable
private fun QuietInsigniaDivider(
    scale: Float,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .width(160.dp)
            .height(10.dp)
    ) {
        if (scale > 0f) {
            val w = size.width
            val h = size.height
            val halfWidth = (w * 0.42f) * scale
            val centerY = h * 0.5f

            // Left hairline
            drawLine(
                color = WarmGold.copy(alpha = 0.65f * scale),
                start = Offset(w * 0.5f - halfWidth, centerY),
                end = Offset(w * 0.5f - 8.dp.toPx(), centerY),
                strokeWidth = 1.dp.toPx(),
                cap = StrokeCap.Round
            )

            // Center Diamond Pip
            val pipSize = 3.dp.toPx() * scale
            val path = Path().apply {
                moveTo(w * 0.5f, centerY - pipSize)
                lineTo(w * 0.5f + pipSize, centerY)
                lineTo(w * 0.5f, centerY + pipSize)
                lineTo(w * 0.5f - pipSize, centerY)
                close()
            }
            drawPath(path = path, color = WarmGold.copy(alpha = 0.85f * scale))

            // Right hairline
            drawLine(
                color = WarmGold.copy(alpha = 0.65f * scale),
                start = Offset(w * 0.5f + 8.dp.toPx(), centerY),
                end = Offset(w * 0.5f + halfWidth, centerY),
                strokeWidth = 1.dp.toPx(),
                cap = StrokeCap.Round
            )
        }
    }
}
