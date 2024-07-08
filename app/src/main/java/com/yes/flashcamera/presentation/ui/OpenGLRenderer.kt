package com.yes.flashcamera.presentation.ui

import android.content.Context
import android.graphics.SurfaceTexture
import android.opengl.GLES10.GL_COLOR_BUFFER_BIT
import android.opengl.GLES10.GL_FLOAT
import android.opengl.GLES10.GL_TRIANGLES
import android.opengl.GLES10.glClear
import android.opengl.GLES10.glClearColor
import android.opengl.GLES10.glDrawArrays
import android.opengl.GLES10.glViewport
import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.opengl.GLES20.GL_COMPILE_STATUS
import android.opengl.GLES20.GL_FRAGMENT_SHADER
import android.opengl.GLES20.GL_LINK_STATUS
import android.opengl.GLES20.GL_VERTEX_SHADER
import android.opengl.GLES20.glAttachShader
import android.opengl.GLES20.glCompileShader
import android.opengl.GLES20.glCreateProgram
import android.opengl.GLES20.glCreateShader
import android.opengl.GLES20.glDeleteProgram
import android.opengl.GLES20.glDeleteShader
import android.opengl.GLES20.glEnableVertexAttribArray
import android.opengl.GLES20.glGetAttribLocation
import android.opengl.GLES20.glGetProgramiv
import android.opengl.GLES20.glGetShaderiv
import android.opengl.GLES20.glGetUniformLocation
import android.opengl.GLES20.glLinkProgram
import android.opengl.GLES20.glShaderSource
import android.opengl.GLES20.glUniform4f
import android.opengl.GLES20.glUseProgram
import android.opengl.GLES20.glVertexAttribPointer
import android.opengl.GLSurfaceView
import android.opengl.GLSurfaceView.Renderer
import com.yes.flashcamera.R
import com.yes.flashcamera.presentation.ui.MainActivity.FileUtils
import com.yes.flashcamera.presentation.ui.OpenGLRenderer.ShaderUtils.createProgram
import com.yes.flashcamera.presentation.ui.OpenGLRenderer.ShaderUtils.createShader
import com.yes.flashcamera.presentation.ui.Utils.readShaderFromResource
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10


class OpenGLRenderer(
    private val context: Context,
    private val callback: (surfaceTexture:SurfaceTexture) -> Unit,
    private val glSurfaceView : GLSurfaceView
) : Renderer {
    private var programId = 0
    private var vertexData: FloatBuffer? = null
    private var uColorLocation = 0
    private var aPositionLocation = 0

    private var surfaceTexture:SurfaceTexture?=null
    private val transformMatrix = FloatArray(16)
    private var surfaceTextureId:Int?=null

    private var programHandle = 0
    private var vertexHandle = 0
    private var fragmentHandle = 0
    private var vertexBuffer:FloatBuffer?=null
    private var vertexOrederBuffer: FloatBuffer?=null

    private var vertexPositionHandle = 0
    private var vertexMatrixHandle = 0
    private var texureOESHandle = 0
    private var vertexCoordinateHandle = 0

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

    init {
        prepareData()
    }

    override fun onSurfaceCreated(arg0: GL10, arg1: EGLConfig) {
        createSurfaceTexture()
         /* glClearColor(0f, 0f, 0f, 1f)
          val vertexShaderId = createShader(context, GL_VERTEX_SHADER, R.raw.vertex_shader)
          val fragmentShaderId = createShader(context, GL_FRAGMENT_SHADER, R.raw.fragment_shader)
          programId = createProgram(vertexShaderId, fragmentShaderId)
          glUseProgram(programId)
          bindData()*/

        vertexBuffer = ByteBuffer.allocateDirect(vertex_coords.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
        (vertexBuffer as FloatBuffer).put(vertex_coords).position(0);

        vertexOrederBuffer = ByteBuffer.allocateDirect(vertex_coords_order.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .put(vertex_coords_order)
        (vertexOrederBuffer as FloatBuffer).position(0)

    //    programHandle = glCreateProgram()

      //  vertexHandle = glCreateShader(GL_VERTEX_SHADER)

        vertexHandle= createShader(context, GL_VERTEX_SHADER, R.raw.vertex_shader)

     //   val vertexShader = readShaderFromResource(context, com.yes.flashcamera.R.raw.vertex_shader)
    /*    glShaderSource(vertexHandle, vertexShader)
        glCompileShader(vertexHandle)
        glAttachShader(programHandle, vertexHandle)*/

     //   fragmentHandle = glCreateShader(GL_FRAGMENT_SHADER)

        fragmentHandle = createShader(context, GL_FRAGMENT_SHADER, R.raw.fragment_shader)
        programHandle = createProgram(vertexHandle,fragmentHandle)
      //  val fragmentShader = readShaderFromResource(context, com.yes.flashcamera.R.raw.fragment_shader)
      /*  glShaderSource(fragmentHandle, fragmentShader)
        glCompileShader(fragmentHandle)
        glAttachShader(programHandle, fragmentHandle)*/

     //   glLinkProgram(programHandle)

    }

    override fun onSurfaceChanged(arg0: GL10, width: Int, height: Int) {
        glViewport(0, 0, width, height)
    }



    private fun prepareData() {
       /* val vertices = floatArrayOf(
            1f, 1f,
            -1f, 1f,
            -1f, -1f,
            1f, 1f,
            -1f, -1f,
            1f, -1f
        )*/
      /*  val vertices = floatArrayOf(-0.5f, -0.2f, 0.0f, 0.2f, 0.5f, -0.2f)
        vertexData = ByteBuffer.allocateDirect(vertices.size * 4).order(ByteOrder.nativeOrder())
            .asFloatBuffer()
        vertexData?.put(vertices)*/


    }

    private fun bindData() {
        uColorLocation = glGetUniformLocation(programId, "u_Color")
        glUniform4f(uColorLocation, 0.0f, 0.0f, 1.0f, 1.0f)
        aPositionLocation = glGetAttribLocation(programId, "a_Position")
        vertexData!!.position(0)
        glVertexAttribPointer(aPositionLocation, 2, GL_FLOAT, false, 0, vertexData)
        glEnableVertexAttribArray(aPositionLocation)
    }

    override fun onDrawFrame(arg0: GL10) {

            surfaceTexture?.updateTexImage();
            surfaceTexture?.getTransformMatrix(transformMatrix);

        glClearColor(1.0f, 0.0f, 0.0f, 0.0f)
        glClear(GL_COLOR_BUFFER_BIT)
      //  glDrawArrays(GL_TRIANGLES, 0, 3)

        glUseProgram(programHandle);

        vertexPositionHandle = glGetAttribLocation(programHandle, "avVertex");
        vertexCoordinateHandle = glGetAttribLocation(programHandle, "avVertexCoordinate");


        vertexMatrixHandle = glGetUniformLocation(programHandle, "umTransformMatrix");
        texureOESHandle = glGetUniformLocation(programHandle, "usTextureOes");


        glVertexAttribPointer(vertexPositionHandle, 2, GLES20.GL_FLOAT, false, 8, vertexBuffer);
        glVertexAttribPointer(vertexCoordinateHandle, 2, GLES20.GL_FLOAT, false, 8, vertexOrederBuffer);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, surfaceTextureId!!)
        GLES20.glUniform1i(texureOESHandle, 0);

        GLES20.glUniformMatrix4fv(vertexMatrixHandle, 1, false, transformMatrix, 0);

        glEnableVertexAttribArray(vertexPositionHandle);
        glEnableVertexAttribArray(vertexCoordinateHandle)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 6)

        GLES20.glDisableVertexAttribArray(vertexPositionHandle);
        GLES20.glDisableVertexAttribArray(vertexCoordinateHandle);
    }
    private fun createSurfaceTexture() {

        surfaceTextureId = createOESTextureObject()
        surfaceTexture = SurfaceTexture(surfaceTextureId!!)
        callback(surfaceTexture!!)


        //当camera有数据返回时，刷新
       surfaceTexture?.setOnFrameAvailableListener{
           glSurfaceView.requestRender()
       }
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
    object ShaderUtils {
        fun createProgram(vertexShaderId: Int, fragmentShaderId: Int): Int {
            val programId = glCreateProgram()
            if (programId == 0) {
                return 0
            }
            glAttachShader(programId, vertexShaderId)
            glAttachShader(programId, fragmentShaderId)
            glLinkProgram(programId)
            val linkStatus = IntArray(1)
            glGetProgramiv(programId, GL_LINK_STATUS, linkStatus, 0)
            if (linkStatus[0] == 0) {
                glDeleteProgram(programId)
                return 0
            }
            return programId
        }

        fun createShader(context: Context?, type: Int, shaderRawId: Int): Int {
            val shaderText = FileUtils.readTextFromRaw(context!!, shaderRawId)
            return createShader(type, shaderText)
        }

        private fun createShader(type: Int, shaderText: String?): Int {

            val shaderId = glCreateShader(type)
            if (shaderId == 0) {
                return 0
            }
            glShaderSource(shaderId, shaderText)
            glCompileShader(shaderId)
            val compileStatus = IntArray(1)
            glGetShaderiv(shaderId, GL_COMPILE_STATUS, compileStatus, 0)
            if (compileStatus[0] == 0) {
                glDeleteShader(shaderId)
                return 0
            }
            return shaderId
        }
    }
}