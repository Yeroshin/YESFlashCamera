package com.yes.flashcamera.presentation.ui

import android.content.Context
import android.graphics.Color
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.blue
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.green
import androidx.core.graphics.red
import com.yes.flashcamera.R
import java.nio.ByteBuffer

class ImageComparator(private val context: Context) {
    fun getImages(){
        val test1=ResourcesCompat.getDrawable(context.resources, R.drawable.test1,null)
        val test2=ResourcesCompat.getDrawable(context.resources, R.drawable.test2,null)
        val test5=ResourcesCompat.getDrawable(context.resources, R.drawable.test5,null)
        val bitmap5=test5!!.toBitmap()
        val width=bitmap5.width
        val height=bitmap5.height

        val imageSize = width*height*4
        val pixelsBuffer = ByteBuffer.allocateDirect(imageSize)
        bitmap5.copyPixelsToBuffer(pixelsBuffer)
        val pixelArray=pixelsBuffer.array()

        val pixel = bitmap5.getPixel(1,0)


        val redValue = Color.red(pixel)
        val greenValue = Color.green(pixel)
        val blueValue = Color.blue(pixel)

        val pixels= IntArray(imageSize)
        bitmap5.getPixels(pixels,0,width,0,0,width,height)

        val color=Color.red(pixels[0])
        val red1=pixels[0].red
        val green1=pixels[0].green
        val blue1=pixels[0].blue

        val red2=pixels[1].red
        val green2=pixels[1].green
        val blue2=pixels[1].blue

        val red3=pixels[2].red
        val green3=pixels[2].green
        val blue3=pixels[2].blue

        val red4=pixels[3].red
        val green4=pixels[3].green
        val blue4=pixels[3].blue


    }
}