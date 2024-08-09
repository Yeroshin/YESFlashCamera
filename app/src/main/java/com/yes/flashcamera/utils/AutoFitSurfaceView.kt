package com.yes.flashcamera.utils


import android.content.Context
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import android.util.Log
import android.view.SurfaceView
import kotlin.math.roundToInt


class AutoFitSurfaceView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : GLSurfaceView(context, attrs) {

    private var aspectRatio = 0.8f
    private var fullscreen = true

    fun setAspectRatio(width: Int, height: Int) {
        require(width > 0 && height > 0) { "Size cannot be negative" }
        aspectRatio = width.toFloat() / height.toFloat()
        //  holder.setFixedSize(320,400)
        requestLayout()
    }

    fun setFullscreen(fullScreen: Boolean) {
        this.fullscreen = fullScreen
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)

        if (fullscreen){
            if((width*aspectRatio).toInt()>height){
                setMeasuredDimension(
                    width,
                    (width*aspectRatio).toInt()
                )

            }else{
                setMeasuredDimension(
                    (height*aspectRatio).toInt(),
                    height
                )
            }
        }else{
            if((width*aspectRatio).toInt()>height){
                setMeasuredDimension(
                    (height*aspectRatio).toInt(),
                    height
                )
            }else{
                setMeasuredDimension(
                    width,
                    (width*aspectRatio).toInt()
                )
            }

        }

        /* if (aspectRatio == 0f) {
           //  setMeasuredDimension(width, height)
         } else {

             // Performs center-crop transformation of the camera frames
             val newWidth: Int
             val newHeight: Int
             val actualRatio = if (width > height) aspectRatio else 1f / aspectRatio
             if (width < height * actualRatio) {
                 newHeight = height
                 newWidth = (height * actualRatio).roundToInt()
             } else {
                 newWidth = width
                 newHeight = (width / actualRatio).roundToInt()
             }

             Log.d(TAG, "Measured dimensions set: $newWidth x $newHeight")
            // setMeasuredDimension(newWidth, newHeight)
             setMeasuredDimension(640, 1280)
         }*/
    }

    companion object {
        private val TAG = AutoFitSurfaceView::class.java.simpleName
    }
}