package com.yes.camera.presentation.ui.custom.compose

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.yes.shared.presentation.ui.theme.AppTheme
import kotlin.math.abs
import kotlin.math.atan2

private const val GOLDEN_RATIO_FRACTION = 0.38196601125f // 1 / (1 + phi) = 0.382

@Composable
fun GoldenRatioGridOverlay(
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    if (!enabled) return

    val context = LocalContext.current
    var tiltAngle by remember { mutableFloatStateOf(0f) }

    DisposableEffect(context, enabled) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        val accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        val listener = object : SensorEventListener {
            private var lastTilt = 0f

            override fun onSensorChanged(event: SensorEvent?) {
                if (event == null || event.sensor.type != Sensor.TYPE_ACCELEROMETER) return
                val ax = event.values[0]
                val ay = event.values[1]

                val rawDegrees = Math.toDegrees(atan2(ax.toDouble(), ay.toDouble())).toFloat()

                var normalized = rawDegrees
                while (normalized > 45f) normalized -= 90f
                while (normalized < -45f) normalized += 90f

                // Low-pass filter for smooth motion
                val smoothed = lastTilt * 0.75f + normalized * 0.25f
                lastTilt = smoothed
                tiltAngle = smoothed
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        if (accelerometer != null) {
            sensorManager.registerListener(listener, accelerometer, SensorManager.SENSOR_DELAY_GAME)
        }

        onDispose {
            sensorManager?.unregisterListener(listener)
        }
    }

    val lineColor = Color.White.copy(alpha = 0.38f)
    val shadowColor = Color.Black.copy(alpha = 0.18f)
    val intersectionColor = Color.White.copy(alpha = 0.85f)
    val levelGreen = Color(0xFF81C784)
    val levelDefaultColor = Color.White.copy(alpha = 0.55f)

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        if (w <= 0f || h <= 0f) return@Canvas

        val x1 = w * GOLDEN_RATIO_FRACTION
        val x2 = w * (1f - GOLDEN_RATIO_FRACTION)
        val y1 = h * GOLDEN_RATIO_FRACTION
        val y2 = h * (1f - GOLDEN_RATIO_FRACTION)

        val strokeWidthPx = 1.dp.toPx()
        val shadowOffsetPx = 1.dp.toPx()

        // 1. Draw 3x3 Golden Ratio Grid Lines with subtle drop shadows
        listOf(
            Pair(Offset(x1, 0f), Offset(x1, h)),
            Pair(Offset(x2, 0f), Offset(x2, h)),
            Pair(Offset(0f, y1), Offset(w, y1)),
            Pair(Offset(0f, y2), Offset(w, y2))
        ).forEach { (start, end) ->
            // Shadow line for high contrast over light backgrounds
            drawLine(
                color = shadowColor,
                start = Offset(start.x + shadowOffsetPx, start.y + shadowOffsetPx),
                end = Offset(end.x + shadowOffsetPx, end.y + shadowOffsetPx),
                strokeWidth = strokeWidthPx
            )
            // Main grid line
            drawLine(
                color = lineColor,
                start = start,
                end = end,
                strokeWidth = strokeWidthPx
            )
        }

        // 2. Draw Golden Ratio Power Points (Intersections)
        val intersections = listOf(
            Offset(x1, y1),
            Offset(x2, y1),
            Offset(x1, y2),
            Offset(x2, y2)
        )

        val tickLen = 6.dp.toPx()
        val dotRadius = 2.5.dp.toPx()

        intersections.forEach { pt ->
            // Crosshair ticks
            drawLine(
                color = intersectionColor,
                start = Offset(pt.x - tickLen, pt.y),
                end = Offset(pt.x + tickLen, pt.y),
                strokeWidth = 1.5.dp.toPx()
            )
            drawLine(
                color = intersectionColor,
                start = Offset(pt.x, pt.y - tickLen),
                end = Offset(pt.x, pt.y + tickLen),
                strokeWidth = 1.5.dp.toPx()
            )
            // Central power point dot
            drawCircle(
                color = intersectionColor,
                radius = dotRadius,
                center = pt
            )
        }

        // 3. Draw Tilt Level Indicator (Horizon / Level Line)
        val cx = w / 2f
        val cy = h / 2f
        val isLevel = abs(tiltAngle) <= 1.2f
        val activeLevelColor = if (isLevel) levelGreen else levelDefaultColor

        val fixedTickWidth = 16.dp.toPx()
        val fixedTickOffset = 32.dp.toPx()
        val levelLineLength = 40.dp.toPx()

        // Fixed reference ticks on left and right
        drawLine(
            color = Color.White.copy(alpha = 0.4f),
            start = Offset(cx - fixedTickOffset - fixedTickWidth, cy),
            end = Offset(cx - fixedTickOffset, cy),
            strokeWidth = 1.5.dp.toPx()
        )
        drawLine(
            color = Color.White.copy(alpha = 0.4f),
            start = Offset(cx + fixedTickOffset, cy),
            end = Offset(cx + fixedTickOffset + fixedTickWidth, cy),
            strokeWidth = 1.5.dp.toPx()
        )

        // Center rotating level indicator line
        rotate(degrees = -tiltAngle, pivot = Offset(cx, cy)) {
            drawLine(
                color = activeLevelColor,
                start = Offset(cx - levelLineLength / 2f, cy),
                end = Offset(cx + levelLineLength / 2f, cy),
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round
            )
            drawCircle(
                color = activeLevelColor,
                radius = 3.dp.toPx(),
                center = Offset(cx, cy)
            )
        }
    }
}
