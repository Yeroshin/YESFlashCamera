package com.yes.flashcamera.presentation.ui


import android.content.Context
import android.graphics.SurfaceTexture
import android.opengl.GLES20.glClearColor
import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.opengl.GLES20.GL_COLOR_BUFFER_BIT
import android.opengl.GLES20.GL_FRAGMENT_SHADER
import android.opengl.GLES20.GL_TRIANGLES
import android.opengl.GLES20.GL_VERTEX_SHADER
import android.opengl.GLES20.glClear
import android.opengl.GLES20.glDrawArrays
import android.opengl.GLES20.glEnableVertexAttribArray
import android.opengl.GLES20.glFlush
import android.opengl.GLES20.glGetAttribLocation
import android.opengl.GLES20.glGetUniformLocation
import android.opengl.GLES20.glUniformMatrix4fv
import android.opengl.GLES20.glUseProgram
import android.opengl.GLES20.glVertexAttribPointer
import android.opengl.GLES20.glViewport
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.opengl.Matrix.multiplyMM
import android.opengl.Matrix.rotateM
import android.opengl.Matrix.setIdentityM
import android.opengl.Matrix.translateM
import com.yes.flashcamera.R
import com.yes.flashcamera.presentation.ui.OpenGLRenderer.ShaderUtils.createProgram
import com.yes.flashcamera.presentation.ui.OpenGLRenderer.ShaderUtils.createShader
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.math.tan


class MyRenderer (
    private val context: Context,
    private val callback: (surfaceTexture: SurfaceTexture) -> Unit
) : GLSurfaceView.Renderer {


    private var surfaceTexture:SurfaceTexture?=null
    private val glScreen by lazy {
        GLScreen()
    }
    private val mallet by lazy {
        Mallet()
    }
    private val projectionMatrix = FloatArray(16)


var textureProgram:TextureShaderProgram?=null
var colorProgram:ColorShaderProgram?=null


    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {

        createSurfaceTexture()
      textureProgram=TextureShaderProgram(context)


        colorProgram=ColorShaderProgram(context)



    }

    private var vertexBuffer:FloatBuffer?=null
    private var vertexOrederBuffer: FloatBuffer?=null
    val vertex_coords: FloatArray = floatArrayOf(
        1f, 1f,
        -1f, 1f,
        -1f, -1f,
        1f, 1f,
        -1f, -1f,
        1f, -1f
    )

    val vertex_coords_order: FloatArray = floatArrayOf(
        1f, 1f,
        0f, 1f,
        0f, 0f,
        1f, 1f,
        0f, 0f,
        1f, 0f
    )
    private fun prepareData() {

        vertexBuffer = ByteBuffer.allocateDirect(vertex_coords.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
        (vertexBuffer as FloatBuffer).put(vertex_coords).position(0);

        vertexOrederBuffer = ByteBuffer.allocateDirect(vertex_coords_order.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .put(vertex_coords_order)
        (vertexOrederBuffer as FloatBuffer).position(0)
        ////////////////////
        glVertexAttribPointer(vertexPositionHandle, 2, GLES20.GL_FLOAT, false, 8, vertexBuffer);
        glVertexAttribPointer(vertexCoordinateHandle, 2, GLES20.GL_FLOAT, false, 8, vertexOrederBuffer);
        glEnableVertexAttribArray(vertexPositionHandle);
        glEnableVertexAttribArray(vertexCoordinateHandle)
        createSurfaceTexture()

    }
    private var vertexPositionHandle = 0
    private var vertexMatrixHandle = 0
    private var texureOESHandle = 0
    private var vertexCoordinateHandle = 0
    private val locations: Unit
        get() {
            vertexPositionHandle = glGetAttribLocation(programId, "a_Position");
            vertexCoordinateHandle = glGetAttribLocation(programId,  "a_TextureCoordinates");
            vertexMatrixHandle = glGetUniformLocation(programId, "u_Matrix");
            texureOESHandle = glGetUniformLocation(programId, "u_TextureUnit");
        }
 /*   private val locations: Unit
        get() {
            vertexPositionHandle = glGetAttribLocation(programId, "avVertex");
            vertexCoordinateHandle = glGetAttribLocation(programId, "avVertexCoordinate");
            vertexMatrixHandle = glGetUniformLocation(programId, "umTransformMatrix");
            texureOESHandle = glGetUniformLocation(programId, "usTextureOes");
        }*/
    private var programId = 0
    private fun createAndUseProgram() {
          val vertexHandle = ShaderProgram.ShaderHelper.createShader(context, GL_VERTEX_SHADER, R.raw.vertex)
        val fragmentHandle= ShaderProgram.ShaderHelper.createShader(context, GL_FRAGMENT_SHADER, R.raw.fragment)


        /////////////////
//val vertexHandle= createShader(context, GL_VERTEX_SHADER, R.raw.vertex_shader)
   //     val fragmentHandle = createShader(context, GL_FRAGMENT_SHADER, R.raw.fragment_shader)
        programId = ShaderProgram.ShaderHelper.buildProgram(vertexHandle,fragmentHandle)
        /////////////////

        //  programId = createProgram(vertexShaderId, fragmentShaderId)
        glUseProgram(programId)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        glViewport(0, 0, width, height)

      /*  perspectiveM(
            projectionMatrix, 45f,
            width.toFloat() / height.toFloat(), 1f, 10f
        )

        setIdentityM(modelMatrix, 0)
        translateM(modelMatrix, 0, 0f, 0f, -2.5f)
        rotateM(modelMatrix, 0, -60f, 1f, 0f, 0f)

        val temp = FloatArray(16)
        multiplyMM(temp, 0, projectionMatrix, 0, modelMatrix, 0)
        System.arraycopy(temp, 0, projectionMatrix, 0, temp.size)*/
    }
    private val transformMatrix = FloatArray(16)
    override fun onDrawFrame(gl: GL10?) {
        surfaceTexture?.updateTexImage()
        surfaceTexture?.getTransformMatrix(transformMatrix)
        glClearColor(1.0f, 0.0f, 0.0f, 0.0f)
        glClear(GL_COLOR_BUFFER_BIT)


        glScreen.bindData(textureProgram!!)
        textureProgram?.useProgram()
        textureProgram?.setUniforms(transformMatrix,texture)
        glScreen.draw()

        mallet.bindData(colorProgram!!)
        colorProgram?.useProgram()
        colorProgram?.setUniforms(projectionMatrix)
        mallet.draw()


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