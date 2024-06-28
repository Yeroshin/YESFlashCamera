package com.yes.flashcamera.presentation.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.graphics.Rect
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.blue
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.green
import androidx.core.graphics.red
import com.yes.flashcamera.R
import java.lang.Long.max
import java.nio.ByteBuffer
import kotlin.math.min


class ImageComparator(private val context: Context) {
    fun getImages(){
        val test1=ResourcesCompat.getDrawable(context.resources, R.drawable.test1,null)
        val test2=ResourcesCompat.getDrawable(context.resources, R.drawable.test2,null)
        val test5=ResourcesCompat.getDrawable(context.resources, R.drawable.test5,null)
        val bitmap1=test1!!.toBitmap()
        val bitmap2=test2!!.toBitmap()
        val test1Value=getImageValue(
            bitmap1
        )
        val test2Value=getImageValue(
            bitmap2
        )
        val dif=compareValues(test1Value,test2Value)

    }
    private fun compareValues(firstValue:Long,secondValue:Long):Int{
        val a = max(firstValue, secondValue).toFloat()
        val b = min(firstValue, secondValue).toFloat()
        return (((a-b)/b)*100).toInt()
    }
    private fun getImageValue(bitmap:Bitmap):Long{
        val width=bitmap.width
        val height=bitmap.height
        val imageSize = width*height*4

        val pixels= IntArray(imageSize)
        val grayscaleBitmap=toGrayscale(bitmap)
        grayscaleBitmap.getPixels(pixels,0,width,0,0,width,height)

        var imageValue:Long=0
        for (i in pixels){
            imageValue+=i.green
        }
        return imageValue
    }
    private fun toGrayscale(bmpOriginal: Bitmap): Bitmap {

        val bmpGrayscale = Bitmap.createBitmap(bmpOriginal.width, bmpOriginal.height, Bitmap.Config.RGB_565)
        val c = Canvas(bmpGrayscale)
        val paint = Paint()
        val cm = ColorMatrix()
        cm.setSaturation(0F)
        val f = ColorMatrixColorFilter(cm)
        paint.setColorFilter(f)
        c.drawBitmap(bmpOriginal, null, Rect(0,0,bmpOriginal.width, bmpOriginal.height), paint)
        return bmpGrayscale
    }
}