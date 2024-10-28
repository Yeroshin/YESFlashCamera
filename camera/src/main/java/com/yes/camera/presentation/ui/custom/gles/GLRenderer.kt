package com.yes.camera.presentation.ui.custom.gles


import android.app.ActivityManager
import android.content.Context
import android.content.Context.ACTIVITY_SERVICE
import android.graphics.SurfaceTexture
import android.opengl.GLES11Ext.GL_TEXTURE_EXTERNAL_OES
import android.opengl.GLES20
import android.opengl.GLES20.GL_COLOR_BUFFER_BIT
import android.opengl.GLES20.GL_FLOAT
import android.opengl.GLES20.GL_NEAREST
import android.opengl.GLES20.GL_TEXTURE_MAG_FILTER
import android.opengl.GLES20.GL_TEXTURE_MIN_FILTER
import android.opengl.GLES20.GL_TEXTURE_WRAP_S
import android.opengl.GLES20.GL_TEXTURE_WRAP_T
import android.opengl.GLES20.glActiveTexture
import android.opengl.GLES20.glBindTexture
import android.opengl.GLES20.glClear
import android.opengl.GLES20.glClearColor
import android.opengl.GLES20.glEnableVertexAttribArray
import android.opengl.GLES20.glGenTextures
import android.opengl.GLES20.glGetAttribLocation
import android.opengl.GLES20.glGetUniformLocation
import android.opengl.GLES20.glTexParameterfv
import android.opengl.GLES20.glTexParameteri
import android.opengl.GLES20.glUniformMatrix4fv
import android.opengl.GLES20.glVertexAttribPointer
import android.opengl.GLES32.GL_CLAMP_TO_BORDER
import android.opengl.GLES32.GL_TEXTURE_BORDER_COLOR
import android.opengl.GLSurfaceView
import android.opengl.Matrix.invertM
import android.opengl.Matrix.multiplyMV

import com.yes.camera.R
import com.yes.camera.utils.Geometry
import com.yes.camera.utils.Geometry.Ray
import com.yes.camera.utils.Geometry.vectorBetween
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10


class GLRenderer(
    private val context: Context,
    private val callback: (surfaceTexture: SurfaceTexture) -> Unit
) : GLSurfaceView.Renderer {

    private var surfaceTexture: SurfaceTexture? = null
    private val plainShaderProgram by lazy {
            ShaderProgram(
                context,
                R.raw.vertex,
                R.raw.fragment
            )
    }
    private val stateShaderProgram by lazy {
        ShaderProgram(
            context,
            R.raw.vertex,
            R.raw.state_fragment
        )
    }
    private val scaledShaderProgram by lazy {
        ShaderProgram(
            context,
            R.raw.vertex,
            R.raw.scaled_fragment
        )
    }
    private val glScreen by lazy {
        GLScreen(
            plainShaderProgram
        )
    }
    private val glMagnifier by lazy {
        GlMagnifier(
            plainShaderProgram
        )
    }
    private val glFocus by lazy {
        GlFocus(
            stateShaderProgram,
            context
        )
    }

    private val glObjects = mutableListOf<GLObject>()
    fun addGlObjects(objects: List<GLObject>) {
        glObjects.addAll(objects)
    }

    private val glCamera by lazy {
        GlCamera(context)
    }

    private fun divideByW(vector: FloatArray) {
        vector[0] /= vector[3]
        vector[1] /= vector[3]
        vector[2] /= vector[3]
    }

    private fun convertNormalized2DPointToRay(
        normalizedX: Float, normalizedY: Float
    ): Ray {

        val nearPointNdc = floatArrayOf(normalizedX, normalizedY, -1f, 1f)
        val farPointNdc = floatArrayOf(normalizedX, normalizedY, 1f, 1f)

        val nearPointWorld = FloatArray(4)
        val farPointWorld = FloatArray(4)
        val invertedViewProjectionMatrix = FloatArray(16)
        invertM(invertedViewProjectionMatrix, 0, glCamera.viewProjectionMatrix, 0)
        multiplyMV(
            nearPointWorld, 0, invertedViewProjectionMatrix, 0, nearPointNdc, 0
        )
        multiplyMV(
            farPointWorld, 0, invertedViewProjectionMatrix, 0, farPointNdc, 0
        )

        divideByW(nearPointWorld)
        divideByW(farPointWorld)

        val nearPointRay =
            Geometry.Point(nearPointWorld[0], nearPointWorld[1], nearPointWorld[2])

        val farPointRay =
            Geometry.Point(farPointWorld[0], farPointWorld[1], farPointWorld[2])

        return Ray(
            nearPointRay,
            vectorBetween(nearPointRay, farPointRay)
        )
    }

    private var touchedPoint = Geometry.Point(0f, 0f, 0f)
    fun handleTouchPress(normalizedX: Float, normalizedY: Float) {
        val ray: Ray = convertNormalized2DPointToRay(normalizedX, normalizedY)
        val plane = Geometry.Plane(
            Geometry.Point(0f, 0f, 0f),
            Geometry.Vector(0f, 0f, 1f)
        )
        touchedPoint = Geometry.intersectionPoint(ray, plane)

        glObjects.forEach {
            val objectBoundingRectangle = Geometry.Rectangle(
                Geometry.Point(
                    it.centerPosition.first,
                    it.centerPosition.second,
                    it.centerPosition.third,
                ),
                it.vertexWidth,
                it.vertexHeight
            )
            it.setSelected(
                Geometry.intersects(
                    objectBoundingRectangle, ray
                ),
                touchedPoint.x,
                touchedPoint.y
            )
        }
    }

    fun configureMagnifier(
        magnification: Float,
        magnifierSizeW: Float,
        magnifierSizeH: Float,
    ) {
       // val foundDog: GlMagnifier? = glObjects.fi{ glObjects is GlMagnifier  } as Dog?
        glObjects.find { it is GlMagnifier  }?.let { it as GlMagnifier
            it.configure(
                magnification,
                magnifierSizeW,
                magnifierSizeH,
            )
        }
       /* (glObjects.find { it is GlMagnifier  } as GlMagnifier)?.let {
            it.configure(
                magnification,
                magnifierSizeW,
                magnifierSizeH,
            )
        }*/

      /*  val dogs: List<GlMagnifier> = glObjects.filterIsInstance<GlMagnifier>()
        if(glObjects.size>1){
           /* (glObjects[1] as GlMagnifier).configure(
                magnification,
                magnifierSizeW,
                magnifierSizeH,
            )*/
        }*/

    }

    fun handleTouchDrag(normalizedX: Float, normalizedY: Float) {
        glObjects.forEach {
            if (it.selected) {
                val ray: Ray = convertNormalized2DPointToRay(normalizedX, normalizedY)
                val plane = Geometry.Plane(
                    Geometry.Point(0f, 0f, 0f),
                    Geometry.Vector(0f, 0f, 1f)
                )
                val draggedPoint: Geometry.Point = Geometry.intersectionPoint(ray, plane)
                it.translate(draggedPoint.x, draggedPoint.y)
            }
        }
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f)
        if (checkSupport()) {
            createSurfaceTexture()
            addGlObjects(
                listOf(
                    glScreen,
                   glMagnifier,
                    glFocus,
                )
            )
        }


    }

    private fun checkSupport(): Boolean {
        val activityManager = context.getSystemService(ACTIVITY_SERVICE) as ActivityManager
        val configurationInfo = activityManager.deviceConfigurationInfo
        val supportsEs2 = configurationInfo.reqGlEsVersion >= 0x20000
        return if (supportsEs2) {
            true
            /* binding.viewFinder.setEGLContextClientVersion(2)
             binding.viewFinder.setRenderer(
                 renderer
             )*/

            /*  binding.glSurfaceView.setEGLContextClientVersion(2)
              binding.glSurfaceView.setRenderer(
                 renderer
              )*/

        } else {
            /* Toast.makeText(this, "This device does not support OpenGL ES 2.0.", Toast.LENGTH_LONG)
                 .show()*/
            false
        }
    }

    override fun onSurfaceChanged(glUnused: GL10?, width: Int, height: Int) {

        glCamera.setProjection(width, height)

        val ratio =
            if (width > height) width.toFloat() / height.toFloat() else height.toFloat() / width.toFloat()
        glObjects.forEach {
            it.onRatioChanged(ratio)
        }
        glObjects.find { it is GlMagnifier  }?.let { it as GlMagnifier
            it.configure(
                2f, 0.5f, 0.5f
            )
        }
        glObjects.find { it is GlFocus  }?.let { it as GlFocus
            it.configure(
                1f, 0.5f, 0.5f
            )
        }

    }

    override fun onDrawFrame(gl: GL10?) {
        surfaceTexture?.updateTexImage()
        //   surfaceTexture?.getTransformMatrix(transformMatrix)

        glClearColor(1.0f, 0.0f, 0.0f, 0.0f)
        glClear(GL_COLOR_BUFFER_BIT)

        glObjects.forEach {
            it.draw(
                glCamera.translateObjectInScene(it.modelMatrix)
            )
        }
    }

    private fun createSurfaceTexture() {
        surfaceTexture = SurfaceTexture(createOESTextureObject()).apply {
            callback(this)
        }
    }

    private fun createOESTextureObject(): Int {
        val textureHandle = IntArray(1)



        glGenTextures(1, textureHandle, 0)
        glActiveTexture(GLES20.GL_TEXTURE0)
        glBindTexture(GL_TEXTURE_EXTERNAL_OES, textureHandle[0])
        glTexParameteri(
            GL_TEXTURE_EXTERNAL_OES,
            GL_TEXTURE_MIN_FILTER, GL_NEAREST
        )
        glTexParameteri(
            GL_TEXTURE_EXTERNAL_OES,
            GL_TEXTURE_MAG_FILTER, GL_NEAREST
        )
       /* glTexParameterf(
            GL_TEXTURE_EXTERNAL_OES,
            GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE.toFloat()
        )
        glTexParameterf(
            GL_TEXTURE_EXTERNAL_OES,
            GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE.toFloat()
        )*/
        glTexParameteri(
            GL_TEXTURE_EXTERNAL_OES,
            GL_TEXTURE_WRAP_S,
            GL_CLAMP_TO_BORDER
        );
        glTexParameteri(
            GL_TEXTURE_EXTERNAL_OES,
            GL_TEXTURE_WRAP_T,
            GL_CLAMP_TO_BORDER
        );
        val borderColor = floatArrayOf(1.0f, 0.0f, 0.0f, 1.0f)
        glTexParameterfv(
            GL_TEXTURE_EXTERNAL_OES,
            GL_TEXTURE_BORDER_COLOR,
            borderColor,
            0)
        glBindTexture(GL_TEXTURE_EXTERNAL_OES, 0)
        return textureHandle[0]
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

    abstract class GLObject(protected val shaderProgram: ShaderProgram) {
        var selected = false
        private val BYTES_PER_FLOAT: Int = 4
        protected val vertexDataSize: Int = 12
        private val POSITION_COMPONENT_COUNT = 2
        private val TEXTURE_COORDINATES_COMPONENT_COUNT = 2
        private val STRIDE: Int = (POSITION_COMPONENT_COUNT
                + TEXTURE_COORDINATES_COMPONENT_COUNT) * BYTES_PER_FLOAT
        abstract val vertexData: FloatArray
        abstract val textureData: FloatArray
        private val vertexBuffer by lazy {
            ByteBuffer
                .allocateDirect(vertexData.size * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(vertexData)
        }
        protected val textureBuffer by lazy {
            ByteBuffer
                .allocateDirect(vertexDataSize * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(textureData)
        }
        val modelMatrix = FloatArray(16)
        var vertexWidth = 0f
        var vertexHeight = 0f
        private val U_TEXTURE_UNIT: String = "u_TextureUnit"
        private val A_TEXTURE_COORDINATES: String = "a_TextureCoordinates"

        private val A_POSITION: String = "a_Position"
        private val U_MATRIX: String = "u_Matrix"
        protected val uMatrixLocation = glGetUniformLocation(shaderProgram.programId, U_MATRIX)
        private val uTextureUnitLocation = glGetUniformLocation(shaderProgram.programId, U_TEXTURE_UNIT)
        val positionAttributeLocation = glGetAttribLocation(shaderProgram.programId, A_POSITION)
        val textureCoordinatesAttributeLocation = glGetAttribLocation(shaderProgram.programId, A_TEXTURE_COORDINATES)

        protected  fun updateVertexBuffer(width: Float, height: Float) {
            val vertexData = floatArrayOf( // Order of coordinates: X, Y, S, T
                0.0f - width / 2, 0.0f + height / 2,
                0.0f + width / 2, 0.0f + height / 2,
                0.0f + width / 2, 0.0f - height / 2,
                0.0f + width / 2, 0.0f - height / 2,
                0.0f - width / 2, 0.0f - height / 2,
                0.0f - width / 2, 0.0f + height / 2
            )
            vertexBuffer.position(0)
            vertexBuffer.put(vertexData, 0, vertexDataSize)
            vertexBuffer.position(0)
        }

        protected open fun updateTextureBuffer(
            positionX: Float,
            positionY: Float,
            width: Float,
            height: Float
        ) {
            val textureData = floatArrayOf( // Order of coordinates: X, Y, S, T
                positionX - width / 2, positionY - height / 2,
                positionX + width / 2, positionY - height / 2,
                positionX + width / 2, positionY + height / 2,
                positionX + width / 2, positionY + height / 2,
                positionX - width / 2, positionY + height / 2,
                positionX - width / 2, positionY - height / 2
            )
            textureBuffer.position(0)
            textureBuffer.put(textureData, 0, vertexDataSize)
            textureBuffer.position(0)
        }

        abstract fun setSelected(pressed: Boolean, touchedPointX: Float, touchedPointY: Float)
        protected fun bindData() {

            vertexBuffer.position(0)
            glVertexAttribPointer(
                positionAttributeLocation,
                2,
                GL_FLOAT,
                false,
                8,
                vertexBuffer
            )
            glEnableVertexAttribArray(
                positionAttributeLocation
            )
            ////////////////////////
            textureBuffer.position(0)
            glVertexAttribPointer(
                textureCoordinatesAttributeLocation,
                2,
                GL_FLOAT,
                false,
                8,
                textureBuffer
            )
            glEnableVertexAttribArray(
                textureCoordinatesAttributeLocation
            )
        }

        var centerPosition = Triple(0f, 0f, 0f)
        abstract fun translate(draggedPointX: Float, draggedPointY: Float)

        abstract fun draw(modelViewProjectionMatrix: FloatArray)

        abstract fun onRatioChanged(ratio: Float)

    }
}