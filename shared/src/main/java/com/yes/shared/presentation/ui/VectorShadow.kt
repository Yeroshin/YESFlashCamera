package com.yes.shared.presentation.ui

import android.content.Context
import android.graphics.BlurMaskFilter

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.platform.LocalContext
import com.yes.shared.utils.XmlParser
import org.xmlpull.v1.XmlPullParser

fun parseVector(
    context: Context,
    resId: Int
): String {
    val parser = context.resources.getXml(resId)
    var pathData = ""
    var eventType = parser.eventType
    while (eventType != XmlPullParser.END_DOCUMENT) {
        if (eventType == XmlPullParser.START_TAG && parser.name == "path") {
            for (i in 0 until parser.attributeCount) {
                if (parser.getAttributeName(i) == "pathData") {
                    pathData += parser.getAttributeValue(i)
                }
            }
        }
        eventType = parser.next()
    }
    return pathData
}


@Composable
fun VectorShadow(
    modifier: Modifier,
    resId: Int,
    vectorColor: Color = Color.Red,
    shadowColor: Color = Color.Black,
    shadowBlur: Float = 10f,
    shadowOffsetXPct: Float = 0.95f,
    shadowOffsetYPct: Float = 0.95f,
    scale: Float = 0.85f
) {
    val pathData = XmlParser(LocalContext.current, resId)
        .parse("path", "pathData")
    val pathShadow = PathParser().parsePathString(pathData).toPath()
    val pathVector = PathParser().parsePathString(pathData).toPath()
    Canvas(
        modifier = modifier
            .fillMaxSize()
        /* .size(resSize)
          .background(color = Color.Green)*/

    ) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        drawIntoCanvas { canvas ->


            val matrix = Matrix()
            /////Shadow
            val shadowPaint = Paint().apply {
                color = shadowColor// Color.Black.copy(alpha = 1.0f)
                asFrameworkPaint().apply {
                    maskFilter =
                        BlurMaskFilter(shadowBlur, BlurMaskFilter.Blur.NORMAL)
                }
            }
            val pathShadowWidth = pathShadow.getBounds().width
            val pathShadowHeight = pathShadow.getBounds().height
            val scaleShadowX = (canvasWidth / pathShadowWidth) * scale
            val scaleShadowY = (canvasHeight / pathShadowHeight) * scale

            matrix.scale(scaleShadowX, scaleShadowY)
            matrix.translate(pathShadow.getBounds().left * -1, pathShadow.getBounds().top * -1)
            pathShadow.transform(matrix)

            val offsetX = (1 - shadowOffsetXPct) * canvasWidth
            val offsetY = (1 - shadowOffsetYPct) * canvasHeight
            val sdx = ((canvasWidth - pathShadowWidth * scaleShadowX) / 2) + offsetX
            val sdy = ((canvasHeight - pathShadowHeight * scaleShadowY) / 2) + offsetY

            matrix.reset()
            matrix.translate(sdx, sdy)
            pathShadow.transform(matrix)
            canvas.drawPath(pathShadow, shadowPaint)
            ////////////////////////
            ////Vector
            val paintVector = Paint().apply {
                color = vectorColor
            }
            val pathVectorWidth = pathVector.getBounds().width
            val pathVectorHeight = pathVector.getBounds().height
            val scaleVectorX = (canvasWidth / pathVectorWidth) * scale
            val scaleVectorY = (canvasHeight / pathVectorHeight) * scale

            matrix.reset()
            matrix.scale(scaleVectorX, scaleVectorY)
            matrix.translate(pathVector.getBounds().left * -1, pathVector.getBounds().top * -1)
            pathVector.transform(matrix)

            val vdx = (canvasWidth - pathVector.getBounds().width) / 2
            val vdy = (canvasHeight - pathVector.getBounds().height) / 2

            matrix.reset()
            matrix.translate(vdx, vdy)
            pathVector.transform(matrix)

            canvas.drawPath(pathVector, paintVector)
        }
    }

}