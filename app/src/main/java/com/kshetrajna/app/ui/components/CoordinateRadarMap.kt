package com.kshetrajna.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kshetrajna.app.ui.theme.ForestGreenDark
import com.kshetrajna.app.ui.theme.OutlineBorder
import com.kshetrajna.app.ui.theme.PrecisionBlueLight
import com.kshetrajna.app.ui.theme.TechnicalWhite
import com.kshetrajna.app.ui.theme.WarmGold
import com.kshetrajna.app.ui.theme.WarmIvory
import kotlin.math.min

/**
 * A zero-cost, offline-resilient, canvas-rendered coordinate radar and topological
 * compass reticle. Visualizes GPS position, cardinal orientation, scale rings,
 * and reported horizontal accuracy without any external paid map SDK or billing risk.
 */
@Composable
fun CoordinateRadarMap(
    latitude: Double,
    longitude: Double,
    accuracyMeters: Float,
    modifier: Modifier = Modifier,
    height: Dp = 140.dp
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF092017))
            .border(1.dp, OutlineBorder.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
            .testTag("coordinate_radar_map")
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val centerX = w / 2f
            val centerY = h / 2f
            val maxRadius = min(centerX, centerY) * 0.82f

            // Background subtle grid
            val gridColor = Color(0x1A8DAA91)
            val step = 20.dp.toPx()
            var x = step
            while (x < w) {
                drawLine(gridColor, Offset(x, 0f), Offset(x, h), strokeWidth = 1f)
                x += step
            }
            var y = step
            while (y < h) {
                drawLine(gridColor, Offset(0f, y), Offset(w, y), strokeWidth = 1f)
                y += step
            }

            // Concentric range rings (inner, mid, outer)
            val ringColor = Color(0x33D4A574)
            val rings = listOf(0.33f, 0.66f, 1.0f)
            rings.forEach { ratio ->
                drawCircle(
                    color = ringColor,
                    radius = maxRadius * ratio,
                    center = Offset(centerX, centerY),
                    style = Stroke(width = 1.2f)
                )
            }

            // Crosshair lines
            val axisColor = Color(0x448DAA91)
            drawLine(axisColor, Offset(centerX - maxRadius, centerY), Offset(centerX + maxRadius, centerY), strokeWidth = 1.2f)
            drawLine(axisColor, Offset(centerX, centerY - maxRadius), Offset(centerX, centerY + maxRadius), strokeWidth = 1.2f)

            // Cardinal direction marks
            // North indicator arrow in WarmGold
            val northPath = Path().apply {
                moveTo(centerX, centerY - maxRadius - 2f)
                lineTo(centerX - 4.dp.toPx(), centerY - maxRadius + 8.dp.toPx())
                lineTo(centerX + 4.dp.toPx(), centerY - maxRadius + 8.dp.toPx())
                close()
            }
            drawPath(northPath, color = WarmGold)

            // Reported Accuracy Circle (scaled relative to radar field)
            // Clamp visual radius between 8dp and maxRadius * 0.6f
            val accuracyDisplayRadius = (accuracyMeters * 1.5f).coerceIn(8.dp.toPx(), maxRadius * 0.55f)
            drawCircle(
                color = PrecisionBlueLight.copy(alpha = 0.15f),
                radius = accuracyDisplayRadius,
                center = Offset(centerX, centerY)
            )
            drawCircle(
                color = PrecisionBlueLight.copy(alpha = 0.6f),
                radius = accuracyDisplayRadius,
                center = Offset(centerX, centerY),
                style = Stroke(width = 1.2f)
            )

            // Center Observer Point (Kshetrajna focal point)
            drawCircle(
                color = WarmGold,
                radius = 3.5.dp.toPx(),
                center = Offset(centerX, centerY)
            )
            drawCircle(
                color = ForestGreenDark,
                radius = 1.5.dp.toPx(),
                center = Offset(centerX, centerY)
            )
        }

        // Overlay Labels: Cardinal headings and coordinate telemetry
        Text(
            text = "N",
            color = WarmGold,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 4.dp)
        )
        Text(
            text = "S",
            color = TechnicalWhite.copy(alpha = 0.7f),
            fontSize = 8.sp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 4.dp)
        )
        Text(
            text = "W",
            color = TechnicalWhite.copy(alpha = 0.7f),
            fontSize = 8.sp,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 6.dp)
        )
        Text(
            text = "E",
            color = TechnicalWhite.copy(alpha = 0.7f),
            fontSize = 8.sp,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 6.dp)
        )

        // Telemetry badge in bottom left (Technical White and Blue mixing)
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(6.dp)
                .background(Color(0xDD0A2233), RoundedCornerShape(4.dp))
                .border(0.5.dp, PrecisionBlueLight.copy(alpha = 0.45f), RoundedCornerShape(4.dp))
                .padding(horizontal = 5.dp, vertical = 2.dp)
        ) {
            Text(
                text = "GPS RADAR · ±${accuracyMeters.toInt()}m",
                color = TechnicalWhite,
                fontSize = 8.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
