package com.yes.camera.presentation.ui.custom.gles

import android.content.Context
import android.graphics.Bitmap
import android.graphics.SurfaceTexture
import android.opengl.GLES20
import android.opengl.GLES20.glReadPixels
import android.opengl.GLSurfaceView
import android.os.Environment
import android.util.Log
import android.view.MotionEvent
import android.view.SurfaceHolder
import java.io.FileOutputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10


class GLESFrameCapture(
    private val width: Int,
    private val height: Int,

) :
    GLSurfaceView.Renderer {
        init {

        }
     var surfaceTexture: SurfaceTexture? = null




    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        // Инициализация OpenGL ES
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
        createSurfaceTexture()
    }

    override fun onSurfaceChanged(gl: GL10, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
    }

    override fun onDrawFrame(gl: GL10) {
        // Рендеринг кадра
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        // Захват кадра
        captureFrame()
    }
    private var lastFrameTime: Long = 0
    private fun captureFrame() {
        val size = width * height
        val buffer = ByteBuffer.allocateDirect(size * 4)
        buffer.order(ByteOrder.nativeOrder())
        GLES20.glReadPixels(0, 0, width, height, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, buffer)

        // Сохранение кадра в файл
       // saveFrame(buffer)
        /////////////////
        val currentTime = System.currentTimeMillis()
        if (lastFrameTime != 0L) {
            val fps = 1000.0 / (currentTime - lastFrameTime)
            Log.e("","FPS: $fps")
            //  println("FPS: $fps")
        }
        lastFrameTime = currentTime
        /////////////////////////
        if (width>0&&height>0){
            val buffer =
                ByteBuffer.allocateDirect(width * height * 4) // 4 байта на пиксель (ARGB)
            glReadPixels(0, 0, width, height, GL10.GL_RGBA, GL10.GL_UNSIGNED_BYTE, buffer)
            buffer.rewind()

            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            bitmap.copyPixelsFromBuffer(buffer)
        }
    }

    private fun saveFrame(buffer: ByteBuffer) {
        val filePath = Environment.getExternalStorageDirectory().toString() + "/frame.png"
        try {
            FileOutputStream(filePath).use { fos ->
                val bytes = ByteArray(buffer.remaining())
                buffer[bytes]
                fos.write(bytes)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }


    fun loadTexture(): Int {
        // Создание текстуры
        val textureIds = IntArray(2)
        GLES20.glGenTextures(2, textureIds, 0)

        if (textureIds[0] == 0) {
            throw RuntimeException("Ошибка при создании текстуры")
        }

        // Настройка параметров текстуры
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureIds[0])
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_WRAP_S,
            GLES20.GL_CLAMP_TO_EDGE
        )
        GLES20.glTexParameteri(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_WRAP_T,
            GLES20.GL_CLAMP_TO_EDGE
        )

        // Возвращаем идентификатор текстуры
        return textureIds[0]
    }
    fun createSurfaceTexture() {
        surfaceTexture = SurfaceTexture(loadTexture())
    }

}
class SurfacePanel(context: Context?) : GLSurfaceView(context) {

    val renderer= GLESFrameCapture(1024, 768)
    init {
        Log.i("making", "panel")
        setEGLContextClientVersion(2)
        setRenderer(renderer)
        //setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }


    override fun surfaceCreated(holder: SurfaceHolder) {
        super.surfaceCreated(holder)
        Log.v("surface","created");
    }
}
