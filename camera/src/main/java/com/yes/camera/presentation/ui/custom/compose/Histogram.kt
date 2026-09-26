package com.yes.camera.presentation.ui.custom.compose

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import com.yes.shared.presentation.ui.theme.AppTheme

@Composable
fun Histogram(
    modifier: Modifier = Modifier,
    values: Map<Int, Int>?,
    widthDp: Dp = AppTheme.dimens.histogramWidth,
    heightDp: Dp = AppTheme.dimens.histogramHeight
) {
    val widthPx = with(LocalDensity.current) { widthDp.toPx() }
    val heightPx = with(LocalDensity.current) { heightDp.toPx() }
    val maxValue = values?.values?.maxOrNull() ?: 0
    val overlayColor = AppTheme.colors.overlaySemiTransparent

    values?.let {
        val barWidth = widthPx / values.size.toFloat()

        Canvas(
            modifier = modifier
                .width(widthDp)
                .height(heightDp)
        ) {
            drawRect(
                color = overlayColor,
                topLeft = Offset(0f, 0f),
                size = Size(widthPx, heightPx),
                style = Stroke(width = 5f)
            )
            values.forEach { (key, value) ->
                val barHeight = (value.toFloat() * heightPx / maxValue)
                drawRect(
                    color = overlayColor,
                    topLeft = Offset(key.toFloat() * barWidth, heightPx - barHeight),
                    size = Size(barWidth, barHeight)
                )
            }
        }
    }
}
