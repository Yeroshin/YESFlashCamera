package com.yes.flashcamera.presentation.ui


import android.R.attr
import android.content.Context
import android.graphics.SurfaceTexture
import android.opengl.GLES11Ext.GL_TEXTURE_EXTERNAL_OES
import android.opengl.GLES20.GL_COLOR_BUFFER_BIT
import android.opengl.GLES20.glBindTexture
import android.opengl.GLES20.glClear
import android.opengl.GLES20.glClearColor
import android.opengl.GLES20.glGenTextures
import android.opengl.GLES20.glTexParameterf
import android.opengl.GLES20.glViewport
import android.opengl.GLSurfaceView
import android.opengl.Matrix.multiplyMM
import android.opengl.Matrix.perspectiveM
import android.opengl.Matrix.rotateM
import android.opengl.Matrix.setIdentityM
import android.opengl.Matrix.setLookAtM
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10


class MyRenderer(
    private val context: Context,
    private val callback: (surfaceTexture: SurfaceTexture) -> Unit
) : GLSurfaceView.Renderer {

    private val projectionMatrix = FloatArray(16)
    private val modelMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)
    private val viewProjectionMatrix = FloatArray(16)
    private val invertedViewProjectionMatrix = FloatArray(16)
    private val modelViewProjectionMatrix = FloatArray(16)


    private var surfaceTexture: SurfaceTexture? = null
    private val glScreen by lazy {
        GLScreen()
    }
    private val mallet by lazy {
        Mallet()
    }



    private var textureProgram: TextureShaderProgram? = null
    private var colorProgram: ColorShaderProgram? = null


    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f)
        createSurfaceTexture()
        textureProgram = TextureShaderProgram(context)
        colorProgram = ColorShaderProgram(context)
    }


    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        glViewport(0, 0, width, height)

        perspectiveM(
            projectionMatrix,0, 45f,
            attr.width.toFloat() / attr.height.toFloat(), 1f, 10f
        )

        setLookAtM(viewMatrix, 0, 0f, 1.2f, 2.2f, 0f, 0f, 0f, 0f, 1f, 0f)

    }
    private fun positionTableInScene() {
        // The table is defined in terms of X & Y coordinates, so we rotate it
        // 90 degrees to lie flat on the XZ plane.
        setIdentityM(modelMatrix, 0)
        rotateM(modelMatrix, 0, -90f, 1f, 0f, 0f)
        multiplyMM(
            modelViewProjectionMatrix, 0, viewProjectionMatrix,
            0, modelMatrix, 0
        )
    }

    private val transformMatrix = FloatArray(16)
    override fun onDrawFrame(gl: GL10?) {
        surfaceTexture?.updateTexImage()
        surfaceTexture?.getTransformMatrix(transformMatrix)
        glClearColor(1.0f, 0.0f, 0.0f, 0.0f)
        glClear(GL_COLOR_BUFFER_BIT)

        multiplyMM(viewProjectionMatrix, 0, projectionMatrix, 0,
            viewMatrix, 0);

        positionTableInScene()
        textureProgram?.useProgram()
        textureProgram?.setUniforms(modelViewProjectionMatrix, texture)
        glScreen.bindData(textureProgram!!)
        glScreen.draw()

      /*  mallet.bindData(colorProgram!!)
        colorProgram?.useProgram()
        colorProgram?.setUniforms(projectionMatrix)
        mallet.draw()*/


    }


    private var texture = 0
    private fun createSurfaceTexture() {

        texture = createOESTextureObject()
        surfaceTexture = SurfaceTexture(texture)
        callback(
            surfaceTexture!!
        )

    }

    private fun createOESTextureObject(): Int {
        val tex = IntArray(1)
        glGenTextures(1, tex, 0)
        glBindTexture(GL_TEXTURE_EXTERNAL_OES, tex[0])
        glTexParameterf(
            GL_TEXTURE_EXTERNAL_OES,
            GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST.toFloat()
        )
        glTexParameterf(
            GL_TEXTURE_EXTERNAL_OES,
            GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR.toFloat()
        )
        glTexParameterf(
            GL_TEXTURE_EXTERNAL_OES,
            GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE.toFloat()
        )
        glTexParameterf(
            GL_TEXTURE_EXTERNAL_OES,
            GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE.toFloat()
        )
        glBindTexture(GL_TEXTURE_EXTERNAL_OES, 0)
        return tex[0]
        //////////////////
      /*  val textureObjectIds = IntArray(1)
        glGenTextures(1, textureObjectIds, 0)
        val options = BitmapFactory.Options()
        options.inScaled = false


        // Read in the resource
        val bitmap = BitmapFactory.decodeResource(
            context.resources, R.drawable.test1, options
        )
        glBindTexture(GL_TEXTURE_2D, textureObjectIds[0]);

        // Set filtering: a default must be set, or the texture will be
        // black.
        glTexParameteri(GL_TEXTURE_2D,
            GL_TEXTURE_MIN_FILTER,
            GL_LINEAR_MIPMAP_LINEAR);
        glTexParameteri(GL_TEXTURE_2D,
            GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        // Load the bitmap into the bound texture.
        texImage2D(GL_TEXTURE_2D, 0, bitmap, 0);

        // Note: Following code may cause an error to be reported in the
        // ADB log as follows: E/IMGSRV(20095): :0: HardwareMipGen:
        // Failed to generate texture mipmap levels (error=3)
        // No OpenGL error will be encountered (glGetError() will return
        // 0). If this happens, just squash the source image to be
        // square. It will look the same because of texture coordinates,
        // and mipmap generation will work.

        glGenerateMipmap(GL_TEXTURE_2D);

        // Recycle the bitmap, since its data has been loaded into
        // OpenGL.
      //  bitmap.recycle();

        // Unbind from the texture.
        glBindTexture(GL_TEXTURE_2D, 0);

        return textureObjectIds[0];*/
    }

}