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
    private val mProjectionMatrix = FloatArray(16)
    private val mViewMatrix = FloatArray(16)
    private val mMatrix = FloatArray(16)
    private val mModelMatrix = FloatArray(16)
    private var uMatrixLocation = 0

    private var surfaceTexture:SurfaceTexture?=null
    private val glScreen by lazy {
        GLScreen()
    }
    private val mallet by lazy {
        Mallet()
    }
    private val projectionMatrix = FloatArray(16)
    private val modelMatrix = FloatArray(16)

var textureProgram:TextureShaderProgram?=null
var colorProgram:ColorShaderProgram?=null
  //  private lateinit var colorProgram:ColorShaderProgram
    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
      //  glClearColor(0.0f, 0.0f, 0.0f, 0.0f)
     //   textureProgram = TextureShaderProgram(context)
     //   colorProgram=ColorShaderProgram(context)

      //  createSurfaceTexture()
    //  textureProgram=TextureShaderProgram(context)
    //  textureProgram?.useProgram()

     // glScreen.bindData(textureProgram!!)
      createAndUseProgram()
      locations
      prepareData()

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
          val vertexHandle = createShader(context, GLES20.GL_VERTEX_SHADER, R.raw.vertex)
        val fragmentHandle= createShader(context, GLES20.GL_FRAGMENT_SHADER, R.raw.fragment)


        /////////////////
//val vertexHandle= createShader(context, GL_VERTEX_SHADER, R.raw.vertex_shader)
   //     val fragmentHandle = createShader(context, GL_FRAGMENT_SHADER, R.raw.fragment_shader)
        programId = createProgram(vertexHandle,fragmentHandle)
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


        ////////////////

      //  textureProgram?.setUniforms(transformMatrix,texture)
       glUniformMatrix4fv(vertexMatrixHandle, 1, false, transformMatrix, 0)
        glDrawArrays(GL_TRIANGLES, 0, 6)
      //  glScreen.draw()

////////////////
      /*  colorProgram?.useProgram()
        colorProgram?.setUniforms(projectionMatrix)
        mallet.bindData(colorProgram!!)
        mallet.draw()*/

       // glFlush()

    }
    private fun createViewMatrix() {
        // точка полоения камеры
        val eyeX = 0f
        val eyeY = 0f
        val eyeZ = 7f

        // точка направления камеры
        val centerX = 0f
        val centerY = 0f
        val centerZ = 0f

        // up-вектор
        val upX = -3f
        val upY = 0f
        val upZ = 0f

        Matrix.setLookAtM(
            mViewMatrix,
            0,
            eyeX,
            eyeY,
            eyeZ,
            centerX,
            centerY,
            centerZ,
            upX,
            upY,
            upZ
        )
    }
    private fun bindMatrix() {
        multiplyMM(mMatrix, 0, mViewMatrix, 0, mModelMatrix, 0)
        multiplyMM(mMatrix, 0, mProjectionMatrix, 0, mMatrix, 0)
        glUniformMatrix4fv(uMatrixLocation, 1, false, mMatrix, 0)
    }
    private fun createProjectionMatrix(width: Int, height: Int) {
        var ratio = 1f
        var left = -0.5f
        var right = 0.5f
        var bottom = -0.5f
        var top = 0.5f
        val near = 2f
        val far = 12f
        if (width > height) {
            ratio = width.toFloat() / height
            left *= ratio
            right *= ratio
        } else {
            ratio = height.toFloat() / width
            bottom *= ratio
            top *= ratio
        }

        Matrix.frustumM(mProjectionMatrix, 0, left, right, bottom, top, near, far)
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