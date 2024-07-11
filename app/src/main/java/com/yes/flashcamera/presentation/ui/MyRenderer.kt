package com.yes.flashcamera.presentation.ui

import android.R.attr
import android.content.Context
import android.graphics.SurfaceTexture
import android.opengl.GLES20.glClearColor
import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.opengl.GLES20.GL_COLOR_BUFFER_BIT
import android.opengl.GLES20.glClear
import android.opengl.GLES20.glViewport
import android.opengl.GLSurfaceView

import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.math.tan


class MyRenderer (
    private val context: Context,
    private val callback: (surfaceTexture: SurfaceTexture) -> Unit
) : GLSurfaceView.Renderer {
    private val glScreen by lazy {
        GLScreen()
    }
    private val mallet by lazy {
        Mallet()
    }
  /*  private val projectionMatrix = FloatArray(16)
    private val modelMatrix = FloatArray(16)
    private lateinit var textureProgram:TextureShaderProgram*/
  //  private lateinit var colorProgram:ColorShaderProgram
    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f)
     //   textureProgram = TextureShaderProgram(context)
        val colorProgram=ColorShaderProgram(context)
        colorProgram.useProgram()
        //  colorProgram.setUniforms(projectionMatrix)
        mallet.bindData(colorProgram)
       // createSurfaceTexture()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        glViewport(0, 0, attr.width, attr.height)

     /*   perspectiveM(
            projectionMatrix, 45f,
            attr.width.toFloat() / attr.height.toFloat(), 1f, 10f
        )

        setIdentityM(modelMatrix, 0)
        translateM(modelMatrix, 0, 0f, 0f, -2.5f)
        rotateM(modelMatrix, 0, -60f, 1f, 0f, 0f)

        val temp = FloatArray(16)
        multiplyMM(temp, 0, projectionMatrix, 0, modelMatrix, 0)
        System.arraycopy(temp, 0, projectionMatrix, 0, temp.size)*/
    }
    override fun onDrawFrame(gl: GL10?) {
        glClearColor(1.0f, 0.0f, 0.0f, 0.0f)
        glClear(GL_COLOR_BUFFER_BIT)
        mallet.draw()
/*textureProgram.useProgram();
        textureProgram.setUniforms(projectionMatrix, texture);
        glScreen.bindData(textureProgram);
        glScreen.draw()*/

    }
    private fun perspectiveM(
        m: FloatArray, yFovInDegrees: Float, aspect: Float,
        n: Float, f: Float
    ) {
        val angleInRadians = (yFovInDegrees * Math.PI / 180.0).toFloat()
        val a = (1.0 / tan(angleInRadians / 2.0)).toFloat()

        m[0] = a / aspect
        m[1] = 0f
        m[2] = 0f
        m[3] = 0f

        m[4] = 0f
        m[5] = a
        m[6] = 0f
        m[7] = 0f

        m[8] = 0f
        m[9] = 0f
        m[10] = -((f + n) / (f - n))
        m[11] = -1f

        m[12] = 0f
        m[13] = 0f
        m[14] = -((2f * f * n) / (f - n))
        m[15] = 0f
    }


    private var texture = 0
    private fun createSurfaceTexture() {

        texture = createOESTextureObject()
        callback(
            SurfaceTexture(texture)
        )

    }
    private fun createOESTextureObject(): Int {
        val tex = IntArray(1)
        GLES20.glGenTextures(1, tex, 0)
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, tex[0])
        GLES20.glTexParameterf(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
            GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST.toFloat()
        )
        GLES20.glTexParameterf(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
            GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR.toFloat()
        )
        GLES20.glTexParameterf(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
            GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE.toFloat()
        )
        GLES20.glTexParameterf(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
            GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE.toFloat()
        )
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0)
        return tex[0]
    }

}