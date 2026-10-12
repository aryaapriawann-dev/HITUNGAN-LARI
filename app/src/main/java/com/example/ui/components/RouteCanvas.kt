package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.data.model.RunPoint
import com.example.ui.theme.DarkOutline
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.SportCyan
import com.example.ui.theme.SportVolt

@Composable
fun RouteCanvas(
    points: List<RunPoint>,
    modifier: Modifier = Modifier,
    isLiveTracking: Boolean = false,
    accentColor: Color = SportVolt
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseRadius by infiniteTransition.animateFloat(
        initialValue = 6f,
        targetValue = 22f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseRadius"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseAlpha"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(DarkSurface)
            .border(1.dp, DarkOutline, RoundedCornerShape(20.dp))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // Background subtle grid lines
            val gridStep = 40.dp.toPx()
            var x = 0f
            while (x < width) {
                drawLine(
                    color = Color(0x15FFFFFF),
                    start = Offset(x, 0f),
                    end = Offset(x, height),
                    strokeWidth = 1f
                )
                x += gridStep
            }
            var y = 0f
            while (y < height) {
                drawLine(
                    color = Color(0x15FFFFFF),
                    start = Offset(0f, y),
                    end = Offset(width, y),
                    strokeWidth = 1f
                )
                y += gridStep
            }

            if (points.isEmpty()) {
                // Empty state: radar pulse in center
                val center = Offset(width / 2f, height / 2f)
                drawCircle(
                    color = accentColor.copy(alpha = 0.25f),
                    radius = 32.dp.toPx(),
                    center = center,
                    style = Stroke(width = 2f)
                )
                drawCircle(
                    color = accentColor.copy(alpha = 0.45f),
                    radius = 16.dp.toPx(),
                    center = center,
                    style = Stroke(width = 2f)
                )
                drawCircle(
                    color = accentColor,
                    radius = 5.dp.toPx(),
                    center = center
                )
                return@Canvas
            }

            // Calculate bounding box
            var minLat = points.minOf { it.latitude }
            var maxLat = points.maxOf { it.latitude }
            var minLng = points.minOf { it.longitude }
            var maxLng = points.maxOf { it.longitude }

            // Prevent divide by zero if user has only 1 point or stayed in place
            val latRange = (maxLat - minLat).coerceAtLeast(0.0005)
            val lngRange = (maxLng - minLng).coerceAtLeast(0.0005)

            val padding = 36.dp.toPx()
            val availableW = (width - padding * 2).coerceAtLeast(10f)
            val availableH = (height - padding * 2).coerceAtLeast(10f)

            fun toCanvasOffset(lat: Double, lng: Double): Offset {
                val normX = ((lng - minLng) / lngRange).toFloat().coerceIn(0f, 1f)
                // Invert latitude because higher latitude is North (top of screen)
                val normY = (1f - ((lat - minLat) / latRange).toFloat()).coerceIn(0f, 1f)
                return Offset(
                    x = padding + normX * availableW,
                    y = padding + normY * availableH
                )
            }

            if (points.size >= 2) {
                val path = Path()
                val firstOffset = toCanvasOffset(points.first().latitude, points.first().longitude)
                path.moveTo(firstOffset.x, firstOffset.y)

                for (i in 1 until points.size) {
                    val pt = toCanvasOffset(points[i].latitude, points[i].longitude)
                    path.lineTo(pt.x, pt.y)
                }

                // Glow path layer
                drawPath(
                    path = path,
                    color = SportCyan.copy(alpha = 0.35f),
                    style = Stroke(
                        width = 10.dp.toPx(),
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )

                // Crisp route line
                drawPath(
                    path = path,
                    color = SportVolt,
                    style = Stroke(
                        width = 4.5.dp.toPx(),
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )
            }

            // Draw Start Point Marker (Green Pin)
            val startOffset = toCanvasOffset(points.first().latitude, points.first().longitude)
            drawCircle(
                color = Color(0xFF22C55E),
                radius = 7.dp.toPx(),
                center = startOffset
            )
            drawCircle(
                color = Color.White,
                radius = 3.5.dp.toPx(),
                center = startOffset
            )

            // Draw Current / End Point Marker
            val currentOffset = toCanvasOffset(points.last().latitude, points.last().longitude)
            if (isLiveTracking) {
                // Expanding animated pulse ring
                drawCircle(
                    color = SportVolt.copy(alpha = pulseAlpha),
                    radius = pulseRadius.dp.toPx(),
                    center = currentOffset
                )
            }
            drawCircle(
                color = Color.White,
                radius = 8.dp.toPx(),
                center = currentOffset
            )
            drawCircle(
                color = SportVolt,
                radius = 6.dp.toPx(),
                center = currentOffset
            )
        }
    }
}
