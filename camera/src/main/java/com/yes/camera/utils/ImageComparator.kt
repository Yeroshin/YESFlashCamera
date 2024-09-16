package com.yes.camera.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.ImageFormat.NV21
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.YuvImage
import android.media.Image
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.green
import java.io.ByteArrayOutputStream

import java.lang.Long.max
import kotlin.math.max
import kotlin.math.min


class ImageComparator() {
    fun getImages(context: Context){
      /*  val test1=ResourcesCompat.getDrawable(context.resources, R.drawable.test1,null)
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
        val dif=compareValues(test1Value,test2Value)*/

    }
    fun compareImageValues(firstBitmap:Bitmap,secondBitmap:Bitmap):Int{
       /* val firstBitmapValue=getImageValue(
            firstBitmap
        )
        val secondBitmapValue=getImageValue(
            secondBitmap
        )
        return compareValues(firstBitmapValue,secondBitmapValue)*/
        val firstBitmapValue=calculateAverageBrightness(firstBitmap)
        val secondBitmapValue=calculateAverageBrightness(secondBitmap)
        return compareValues(firstBitmapValue,secondBitmapValue)
    }
    fun compareImageValues(firstBitmap:YuvImage,secondBitmap:YuvImage):Int{
        /* val firstBitmapValue=getImageValue(
             firstBitmap
         )
         val secondBitmapValue=getImageValue(
             secondBitmap
         )
         return compareValues(firstBitmapValue,secondBitmapValue)*/
        val firstBitmapValue=calculateAverageBrightness(firstBitmap)
        val secondBitmapValue=calculateAverageBrightness(secondBitmap)
        return compareValues(firstBitmapValue,secondBitmapValue)
    }

    private val Image.nv21ByteArray
        get() = ByteArray(width * height * 3 / 2).also {
            val vPlane = planes[2]
            val y = planes[0].buffer.apply { rewind() }
            val u = planes[1].buffer.apply { rewind() }
            val v = vPlane.buffer.apply { rewind() }
            y.get(it, 0, y.capacity()) // copy Y components
            if (vPlane.pixelStride == 2) {
                // Both of U and V are interleaved data, so copying V makes VU series but last U
                v.get(it, y.capacity(), v.capacity())
                it[it.size - 1] = u.get(u.capacity() - 1) // put last U
            } else { // vPlane.pixelStride == 1
                var offset = it.size - 1
                var i = v.capacity()
                while (i-- != 0) { // make VU interleaved data into ByteArray
                    it[offset - 0] = u[i]
                    it[offset - 1] = v[i]
                    offset -= 2
                }
            }
        }
    fun yuv420_888imageToBitmap(image: Image): Bitmap {

        val bytes = jpegByteArrayFrom(image)
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }
    private fun YuvImage.getJpegDataWithQuality(quality: Int) =
        ByteArrayOutputStream().also {
            compressToJpeg(Rect(0, 0, width, height), quality, it)
        }.toByteArray()
    fun jpegByteArrayFrom(yuv420_888: Image): ByteArray {
        return yuv420_888.nv21ByteArray
            .let { YuvImage(it, NV21, yuv420_888.width, yuv420_888.height, null) }
            .getJpegDataWithQuality(100)
    }
    fun imageToBitmap(image: Image): Bitmap {
        val buffer = image.planes[0].buffer
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }
    fun compareValues(firstValue:Double,secondValue:Double):Int{
        val a = max(firstValue, secondValue)
        val b = min(firstValue, secondValue)
        return (((a-b)/b)*100).toInt()
    }
  /*  fun compareValues(firstValue:Long,secondValue:Long):Int{
        val a = max(firstValue, secondValue).toFloat()
        val b = min(firstValue, secondValue).toFloat()
        return (((a-b)/b)*100).toInt()
    }*/
  fun calculateAverageBrightness(yuvImage: YuvImage): Double {
      val yuvBytes = ByteArrayOutputStream()
      yuvImage.compressToJpeg(android.graphics.Rect(0, 0, yuvImage.width, yuvImage.height), 100, yuvBytes)
      val jpegBytes = yuvBytes.toByteArray()

      var totalBrightness = 0.0
      var pixelCount = 0

      for (i in jpegBytes.indices step 3) {
          val y = jpegBytes[i].toInt() and 0xFF
          totalBrightness += y
          pixelCount++
      }

      return totalBrightness / pixelCount
  }
    fun calculateAverageBrightness(bitmap: Bitmap): Double {
      var totalBrightness = 0.0
      val width = bitmap.width
      val height = bitmap.height
      val pixelCount = width * height

      // Уменьшаем разрешение изображения
      val scaledBitmap = Bitmap.createScaledBitmap(bitmap, width / 30, height / 30, true)
      val scaledWidth = scaledBitmap.width
      val scaledHeight = scaledBitmap.height

      for (x in 0 until scaledWidth) {
          for (y in 0 until scaledHeight) {
              val pixel = scaledBitmap.getPixel(x, y)
              val brightness = (Color.red(pixel) + Color.green(pixel) + Color.blue(pixel)) / 3.0
              totalBrightness += brightness
          }
      }

        return totalBrightness / pixelCount
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