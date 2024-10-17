package com.yes.camera.presentation.ui.custom.compose

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/*@Composable
@Preview
fun hist(){
    val v= listOf(1,2,3,4,1,2,3,4)
    Histogram(
        Modifier,
        v,
    300.dp,
    100.dp
    )
}*/

@Composable
fun Histogram(
    modifier: Modifier,
    values: MutableMap<Int, Int>?,
    widthDp: Dp,
    heightDp: Dp
) {
    val widthPx = with(LocalDensity.current) { widthDp.toPx() }
    val heightPx = with(LocalDensity.current) { heightDp.toPx() }
    val maxValu = values?.values
    val maxValue = values?.values?.maxOrNull() ?: 0
    values?.let {
        val barWidth = widthPx / values.size.toFloat()

        Canvas(modifier = modifier) {
            drawRect(
                color = Color.Green,
                topLeft = Offset(0f, 0f),
                size = Size(widthPx, heightPx),
                style = Stroke(width = 10f)
            )
            values.forEach { (key, value) ->
                val barHeight = (value.toFloat() * heightPx / maxValue)
                drawRect(
                    color = Color.Red,
                    topLeft = Offset(key.toFloat() * barWidth, heightPx - barHeight),
                    size = Size(barWidth, barHeight)
                )
            }
        }
    }
}