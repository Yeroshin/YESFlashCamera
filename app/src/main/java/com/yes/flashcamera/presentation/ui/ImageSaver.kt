package com.yes.flashcamera.presentation.ui

import android.media.ImageReader
import android.os.Environment
import android.util.Log
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.Random



class ImageSaver(private val mImageReader: ImageReader) : Runnable {
    override fun run() {
        Log.i("level", "ImageSaver--->run")
        val image = mImageReader.acquireLatestImage()
        checkParentDir()
        checkJpegDir()
        val file = createJpeg()
        try {
            val buffer = image.planes[0].buffer
            val bytes = ByteArray(buffer.remaining())
            buffer[bytes]
            try {
                save(bytes, file)
            } catch (e: IOException) {
                e.printStackTrace()
            }
            image.close()
        } catch (e: Exception) {
            e.stackTrace
        }
    }

    private fun checkParentDir() {
        val dir = File(Environment.getExternalStorageDirectory().toString() + "/Android_L_Test/")
        if (!dir.exists()) {
            dir.mkdir()
        }
    }

    private fun checkJpegDir() {
        val dir =
            File(Environment.getExternalStorageDirectory().toString() + "/Android_L_Test/jpeg/")
        if (!dir.exists()) {
            dir.mkdir()
        }
    }

    private fun createJpeg(): File {
        val time = System.currentTimeMillis()
        val random: Int = Random().nextInt(1000)
        val dir =
            File(Environment.getExternalStorageDirectory().toString() + "/Android_L_Test/jpeg/")
        Log.i("JpegSaver", time.toString() + "_" + random + ".jpg")
        return File(dir, time.toString() + "_" + random + ".jpg")
    }

    @Throws(IOException::class)
    private fun save(bytes: ByteArray, file: File) {
        Log.i("JpegSaver", "save")
        var os: OutputStream? = null
        try {
            os = FileOutputStream(file)
            os.write(bytes)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            os?.close()
        }
    }
}