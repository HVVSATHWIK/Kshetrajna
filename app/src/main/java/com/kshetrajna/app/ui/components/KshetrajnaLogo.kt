package com.kshetrajna.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kshetrajna.app.ui.theme.ForestGreen
import com.kshetrajna.app.ui.theme.PlayfairFontFamily
import com.kshetrajna.app.ui.theme.WarmGold
import com.kshetrajna.app.ui.theme.WarmIvory

@Composable
fun KshetrajnaInsignia(
    modifier: Modifier = Modifier,
    size: Dp = 32.dp,
    archColor: Color = WarmGold,
    sunColor: Color = WarmGold,
    horizonColor: Color = WarmIvory
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height

        // Arch outline
        val strokeWidth = (w * 0.075f).coerceAtLeast(1.5f)
        val archSize = Size(w * 0.74f, h * 0.74f)
        val archTopLeft = Offset((w - archSize.width) / 2f, h * 0.12f)
        drawArc(
            color = archColor,
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = archTopLeft,
            size = archSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        // Central sun / observer disc
        val sunCenter = Offset(w * 0.5f, h * 0.38f)
        val sunRadius = w * 0.11f
        drawCircle(
            color = sunColor,
            radius = sunRadius,
            center = sunCenter
        )

        // Horizon field furrows
        val lineStroke = (w * 0.055f).coerceAtLeast(1.2f)
        // Furrow 1
        drawLine(
            color = horizonColor,
            start = Offset(w * 0.22f, h * 0.64f),
            end = Offset(w * 0.78f, h * 0.64f),
            strokeWidth = lineStroke,
            cap = StrokeCap.Round
        )
        // Furrow 2
        drawLine(
            color = archColor,
            start = Offset(w * 0.30f, h * 0.76f),
            end = Offset(w * 0.70f, h * 0.76f),
            strokeWidth = lineStroke * 0.85f,
            cap = StrokeCap.Round
        )
    }
}

@Composable
fun KshetrajnaBrandHeader(
    modifier: Modifier = Modifier,
    insigniaSize: Dp = 32.dp,
    textColor: Color = WarmIvory,
    goldColor: Color = WarmGold,
    showTagline: Boolean = false
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        KshetrajnaInsignia(
            size = insigniaSize,
            archColor = goldColor,
            sunColor = goldColor,
            horizonColor = textColor
        )

        Column {
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "Kshetrajna",
                    color = textColor,
                    fontFamily = PlayfairFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 19.sp,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "क्षेत्रज्ञ",
                    color = goldColor,
                    fontFamily = PlayfairFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 13.sp,
                    letterSpacing = 0.5.sp
                )
            }
            if (showTagline) {
                Text(
                    text = "CAPTURE WHAT MATTERS.",
                    color = goldColor,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 8.5.sp,
                    letterSpacing = 1.2.sp
                )
            }
        }
    }
}
