package com.yes.camera.presentation.ui.custom.compose

import android.opengl.GLSurfaceView
import android.view.MotionEvent
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.viewinterop.AndroidView
import com.yes.camera.presentation.ui.custom.gles.AutoFitSurfaceView
import com.yes.camera.presentation.ui.custom.gles.GLRenderer
import com.yes.shared.domain.Dimensions

@Stable
@Composable
fun CameraPreviewContainer(
    modifier: Modifier = Modifier,
    renderer: GLRenderer,
    fullScreen: Boolean,
    aspectRatio: Dimensions?,
    gridEnabled: Boolean = true,
    onSizeChanged: (IntSize) -> Unit,
    onTouchPoint: (Offset) -> Unit
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            modifier = Modifier.onSizeChanged { onSizeChanged(it) },
            factory = { context ->
                AutoFitSurfaceView(context, null).apply {
                    setEGLContextClientVersion(3)
                    setRenderer(renderer)
                    renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY

                    setOnTouchListener { v, event ->
                        val nx = (event.x / v.width) * 2f - 1f
                        val ny = -((event.y / v.height) * 2f - 1f)
                        when (event.action) {
                            MotionEvent.ACTION_DOWN -> renderer.handleTouchPress(nx, ny)
                            MotionEvent.ACTION_MOVE -> renderer.handleTouchDrag(nx, ny)
                            MotionEvent.ACTION_UP -> {
                                v.performClick()
                                onTouchPoint(Offset(event.x / v.width, event.y / v.height))
                            }
                        }
                        true
                    }
                    renderer.glSurfaceView = this
                }
            },
            update = { view ->
                view.setFullscreen(fullScreen)
                view.setAspectRatio(
                    aspectRatio?.width ?: 3,
                    aspectRatio?.height ?: 2
                )
            }
        )

        GoldenRatioGridOverlay(
            modifier = Modifier.matchParentSize(),
            enabled = gridEnabled
        )
    }
}
