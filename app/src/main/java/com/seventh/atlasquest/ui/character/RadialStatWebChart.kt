package com.seventh.atlasquest.ui.character

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.seventh.atlasquest.domain.model.StatAxis
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun RadialStatWebChart(
    statMap: Map<StatAxis, Int>,
    modifier: Modifier = Modifier,
    size: Dp = 260.dp,
    maxStatValue: Float = 10f
) {
    val axes = listOf(
        StatAxis.STR to "STR",
        StatAxis.VIT to "VIT",
        StatAxis.INT to "INT",
        StatAxis.FOC to "FOC",
        StatAxis.DIS to "DIS",
        StatAxis.EXP to "EXP"
    )

    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceVariant = MaterialTheme.colorScheme.outlineVariant
    val onSurface = MaterialTheme.colorScheme.onSurface

    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.toPx() / 2f, size.toPx() / 2f)
            val radius = (size.toPx() / 2f) * 0.75f
            val numAxes = axes.size
            val angleStep = (2 * Math.PI / numAxes).toFloat()

            // Draw 3 concentric rings (0-10 scale)
            val rings = 5 // representing rings at 2, 4, 6, 8, 10
            for (i in 1..rings) {
                val ringRadius = radius * (i.toFloat() / rings.toFloat())
                val ringPath = Path().apply {
                    for (j in 0 until numAxes) {
                        val angle = j * angleStep - Math.PI.toFloat() / 2f
                        val x = center.x + ringRadius * cos(angle)
                        val y = center.y + ringRadius * sin(angle)
                        if (j == 0) moveTo(x, y) else lineTo(x, y)
                    }
                    close()
                }
                drawPath(
                    path = ringPath,
                    color = surfaceVariant.copy(alpha = 0.4f),
                    style = Stroke(width = 1.dp.toPx())
                )
            }

            // Draw axis lines from center to outer ring
            axes.forEachIndexed { index, _ ->
                val angle = index * angleStep - Math.PI.toFloat() / 2f
                val endX = center.x + radius * cos(angle)
                val endY = center.y + radius * sin(angle)
                drawLine(
                    color = surfaceVariant.copy(alpha = 0.6f),
                    start = center,
                    end = Offset(endX, endY),
                    strokeWidth = 1.dp.toPx()
                )
            }

            // Draw stat polygon
            val polygonPath = Path().apply {
                axes.forEachIndexed { index, (axis, _) ->
                    val rawValue = statMap[axis]?.toFloat() ?: 0f
                    val normalizedValue = (rawValue / maxStatValue).coerceIn(0f, 1f)
                    val currentRadius = radius * normalizedValue
                    val angle = index * angleStep - Math.PI.toFloat() / 2f
                    val x = center.x + currentRadius * cos(angle)
                    val y = center.y + currentRadius * sin(angle)

                    if (index == 0) moveTo(x, y) else lineTo(x, y)
                }
                close()
            }

            // Fill polygon with semi-transparent primary color
            drawPath(
                path = polygonPath,
                color = primaryColor.copy(alpha = 0.35f)
            )
            // Stroke polygon border
            drawPath(
                path = polygonPath,
                color = primaryColor,
                style = Stroke(width = 2.5.dp.toPx())
            )

            // Draw labels
            axes.forEachIndexed { index, (_, label) ->
                val angle = index * angleStep - Math.PI.toFloat() / 2f
                val labelRadius = radius * 1.2f
                val x = center.x + labelRadius * cos(angle)
                val y = center.y + labelRadius * sin(angle)

                drawContext.canvas.nativeCanvas.apply {
                    val paint = android.graphics.Paint().apply {
                        textSize = 12.dp.toPx()
                        color = android.graphics.Color.WHITE
                        textAlign = android.graphics.Paint.Align.CENTER
                        isAntiAlias = true
                    }
                    // Adjust y alignment for native canvas text
                    drawText(label, x, y + 4.dp.toPx(), paint)
                }
            }
        }
    }
}
