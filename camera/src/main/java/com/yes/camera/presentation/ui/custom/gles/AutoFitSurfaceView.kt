package com.yes.camera.presentation.ui.custom.gles

import android.content.Context
import android.opengl.GLSurfaceView
import android.util.AttributeSet

class AutoFitSurfaceView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : GLSurfaceView(context, attrs) {

    private var aspectRatio = 0.75f // Default 3:4 aspect ratio (min/max)
    private var fullscreen = true

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    fun setAspectRatio(width: Int, height: Int) {
        require(width > 0 && height > 0) { "Size cannot be negative" }
        aspectRatio = if (width < height) width.toFloat() / height.toFloat() else height.toFloat() / width.toFloat()
        requestLayout()
    }

    fun setFullscreen(fullScreen: Boolean) {
        this.fullscreen = fullScreen
        requestLayout()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)

        if (width <= 0 || height <= 0 || aspectRatio <= 0f) {
            setMeasuredDimension(width, height)
            return
        }

        if (fullscreen) {
            if (width > height) {
                // Landscape FullScreen (Center Crop)
                setMeasuredDimension(
                    width,
                    (width * aspectRatio).toInt()
                )
            } else {
                // Portrait FullScreen (Center Crop)
                setMeasuredDimension(
                    (height * aspectRatio).toInt(),
                    height
                )
            }
        } else {
            if (width > height) {
                // Landscape Non-FullScreen (Fit Inside / Letterbox)
                setMeasuredDimension(
                    (height / aspectRatio).toInt(),
                    height
                )
            } else {
                // Portrait Non-FullScreen (Fit Inside / Letterbox)
                setMeasuredDimension(
                    width,
                    (width / aspectRatio).toInt()
                )
            }
        }
    }

    companion object {
        private val TAG = AutoFitSurfaceView::class.java.simpleName
    }
}
