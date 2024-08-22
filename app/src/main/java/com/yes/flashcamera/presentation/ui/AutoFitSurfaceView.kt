package com.yes.flashcamera.presentation.ui


import android.content.Context
import android.opengl.GLSurfaceView
import android.util.AttributeSet


class AutoFitSurfaceView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : GLSurfaceView(context, attrs) {

    private var aspectRatio = 1.0f
    private var fullscreen = true

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    fun setAspectRatio(width: Int, height: Int) {
        require(width > 0 && height > 0) { "Size cannot be negative" }
      //  aspectRatio = width.toFloat() / height.toFloat()
        aspectRatio = if (width > height) width.toFloat() / height.toFloat() else height.toFloat() / width.toFloat()

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
            if((height*aspectRatio).toInt()<height){
                setMeasuredDimension(
                    (height*aspectRatio).toInt(),
                    height
                )

            }else{
                setMeasuredDimension(
                    width,
                    (height*aspectRatio).toInt(),
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