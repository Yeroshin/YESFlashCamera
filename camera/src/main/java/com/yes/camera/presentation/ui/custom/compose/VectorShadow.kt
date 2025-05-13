package com.yes.camera.presentation.ui.custom.compose

import android.content.Context
import android.graphics.BlurMaskFilter

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.yes.camera.R
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
fun VectorShadowT(
    modifier: Modifier,
    resId: Int,
    vectorColor: Color = Color.Red,
    shadowColor: Color = Color.Black,
    shadowBlur: Float = 10f,
    shadowOffsetX: Float = 0.95f,
    shadowOffsetY: Float = 0.95f,
    scale: Float = 0.85f
) {
   // val modifier by remember { mutableStateOf(Modifier.size(32.dp)) }
   // val resId by remember { mutableStateOf(R.drawable.auto) }
   // val vectorColor by remember { mutableStateOf(Color.Red) }
   // val shadowColor by remember { mutableStateOf(Color.Black) }
  /*  val shadowBlur by remember { mutableStateOf(10f) }
    val shadowOffsetX by remember { mutableStateOf(0.95f) }
    val shadowOffsetY by remember { mutableStateOf(0.95f) }
    val scale by remember { mutableStateOf(0.85f) }

    val pathData = remember(resId) { XmlParser(context, resId).parse("path", "pathData") }
    */
    val context = LocalContext.current
    val pathData = remember {
        XmlParser(context, resId)
            .parse("path", "pathData")
    }
    val (pathShadow, pathVector) = remember(pathData) {
        val parsed = PathParser().parsePathString(pathData)
        parsed.toPath() to parsed.toPath()
    }
    /* val pathShadow = remember(pathData) { PathParser().parsePathString(pathData).toPath() }
     val pathVector = remember(pathData){PathParser().parsePathString(pathData).toPath()}*/
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
            val pathShadowBounds = pathShadow.getBounds()
            /* val scaleShadowX = (canvasWidth / pathShadowBounds.width) * scale
             val scaleShadowY = (canvasHeight / pathShadowBounds.height) * scale*/
            var scaleShadowX = 0f
            var scaleShadowY = 0f
            if (pathShadow.getBounds().width>=pathShadow.getBounds().height){
                scaleShadowX = (canvasWidth / pathShadowBounds.width) * scale
                scaleShadowY=scaleShadowX
            }else{
                scaleShadowY=(canvasHeight / pathShadowBounds.height) * scale
                scaleShadowX = scaleShadowY

            }

            matrix.scale(scaleShadowX, scaleShadowY)
            matrix.translate(pathShadow.getBounds().left * -1, pathShadow.getBounds().top * -1)
            pathShadow.transform(matrix)

            val offsetX = (1 - shadowOffsetX) * canvasWidth
            val offsetY = (1 - shadowOffsetY) * canvasHeight
            val sdx = ((canvasWidth - pathShadowBounds.width * scaleShadowX) / 2) + offsetX
            val sdy = ((canvasHeight - pathShadowBounds.height * scaleShadowY) / 2) + offsetY

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
            var scaleVectorX = 0f
            var scaleVectorY = 0f
            if (pathVector.getBounds().width>=pathVector.getBounds().height){
                scaleVectorX = (canvasWidth / pathVectorWidth) * scale
                scaleVectorY=scaleVectorX
            }else{
                scaleVectorY=(canvasHeight / pathVectorHeight) * scale
                scaleVectorX = scaleVectorY

            }
            /* val scaleVectorX = (canvasWidth / pathVectorWidth) * scale
             val scaleVectorY = (canvasHeight / pathVectorHeight) * scale*/

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

@Composable
fun VectorShadow(
    modifier: Modifier,
    resId: Int,
    vectorColor: Color = Color.Red,
    shadowColor: Color = Color.Black,
    shadowBlur: Float = 10f,
    shadowOffsetX: Float = 0.95f,
    shadowOffsetY: Float = 0.95f,
    scale: Float = 0.85f
) {
    val context = LocalContext.current
    val pathData = remember(resId) {
        XmlParser(context, resId).parse("path", "pathData")
    }
    /*val pathData = remember {
        XmlParser(context, resId)
            .parse("path", "pathData")
    }*/
    val (pathShadow, pathVector) = remember(pathData) {
        val parsed = PathParser().parsePathString(pathData)
        parsed.toPath() to parsed.toPath()
    }
   /* val pathShadow = remember(pathData) { PathParser().parsePathString(pathData).toPath() }
    val pathVector = remember(pathData){PathParser().parsePathString(pathData).toPath()}*/
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
            val pathShadowBounds = pathShadow.getBounds()
           /* val scaleShadowX = (canvasWidth / pathShadowBounds.width) * scale
            val scaleShadowY = (canvasHeight / pathShadowBounds.height) * scale*/
            var scaleShadowX = 0f
            var scaleShadowY = 0f
            if (pathShadow.getBounds().width>=pathShadow.getBounds().height){
                scaleShadowX = (canvasWidth / pathShadowBounds.width) * scale
                scaleShadowY=scaleShadowX
            }else{
                scaleShadowY=(canvasHeight / pathShadowBounds.height) * scale
                scaleShadowX = scaleShadowY

            }

            matrix.scale(scaleShadowX, scaleShadowY)
            matrix.translate(pathShadow.getBounds().left * -1, pathShadow.getBounds().top * -1)
            pathShadow.transform(matrix)

            val offsetX = (1 - shadowOffsetX) * canvasWidth
            val offsetY = (1 - shadowOffsetY) * canvasHeight
            val sdx = ((canvasWidth - pathShadowBounds.width * scaleShadowX) / 2) + offsetX
            val sdy = ((canvasHeight - pathShadowBounds.height * scaleShadowY) / 2) + offsetY

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
            var scaleVectorX = 0f
            var scaleVectorY = 0f
            if (pathVector.getBounds().width>=pathVector.getBounds().height){
                scaleVectorX = (canvasWidth / pathVectorWidth) * scale
                scaleVectorY=scaleVectorX
            }else{
                scaleVectorY=(canvasHeight / pathVectorHeight) * scale
                scaleVectorX = scaleVectorY

            }
           /* val scaleVectorX = (canvasWidth / pathVectorWidth) * scale
            val scaleVectorY = (canvasHeight / pathVectorHeight) * scale*/

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