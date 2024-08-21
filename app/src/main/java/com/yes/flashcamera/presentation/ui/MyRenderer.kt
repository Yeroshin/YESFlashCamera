package com.yes.flashcamera.presentation.ui


import android.content.Context
import android.graphics.SurfaceTexture
import android.opengl.GLES10.glDrawArrays
import android.opengl.GLES11Ext.GL_TEXTURE_EXTERNAL_OES
import android.opengl.GLES20.GL_COLOR_BUFFER_BIT
import android.opengl.GLES20.GL_FLOAT
import android.opengl.GLES20.GL_TRIANGLES
import android.opengl.GLES20.glBindTexture
import android.opengl.GLES20.glClear
import android.opengl.GLES20.glClearColor
import android.opengl.GLES20.glEnableVertexAttribArray
import android.opengl.GLES20.glGenTextures
import android.opengl.GLES20.glGetAttribLocation
import android.opengl.GLES20.glGetUniformLocation
import android.opengl.GLES20.glTexParameterf
import android.opengl.GLES20.glUniformMatrix4fv
import android.opengl.GLES20.glVertexAttribPointer
import android.opengl.GLSurfaceView
import android.opengl.Matrix.invertM
import android.opengl.Matrix.multiplyMV
import android.opengl.Matrix.setIdentityM
import android.opengl.Matrix.translateM
import com.yes.flashcamera.presentation.ui.Geometry.Ray
import com.yes.flashcamera.presentation.ui.Geometry.vectorBetween
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10


class MyRenderer(
    private val context: Context,
    private val callback: (surfaceTexture: SurfaceTexture) -> Unit
) : GLSurfaceView.Renderer {

    private val projectionMatrix = FloatArray(16)

    //  private val modelMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)
    private val viewProjectionMatrix = FloatArray(16)
    private val invertedViewProjectionMatrix = FloatArray(16)
    private val modelViewProjectionMatrix = FloatArray(16)

    //private var textureProgram: TextureShaderProgram? = null
    private val textureProgram by lazy {
        TextureShaderProgram(context)
    }

    //   private var colorProgram: ColorShaderProgram? = null
    // private var scaledTextureProgram: ScaledTextureProgram? = null
    private val scaledTextureProgram by lazy {
        ScaledTextureProgram(context)
    }


    private var surfaceTexture: SurfaceTexture? = null
    private val glScreen by lazy {
        GLScreen()
    }
    private val glMagnifier by lazy {
        GlMagnifier()
    }
    private val glCamera by lazy {
        GlCamera(context)
    }

    // private var magnifierPosition: Geometry.Point? = null
    //  private var previousMagnifierPosition: Geometry.Point? = null
    private val leftBound = -1f
    private val rightBound = 1f
    private val farBound = -1f
    private val nearBound = 1f

    fun setDefaultBufferSize() {
        surfaceTexture?.setDefaultBufferSize(160, 120)//(3072x4096)//(1280, 720)//(1920,1080)

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

    // private var magnifierPressed = false
    var touchedPoint = Geometry.Point(0f, 0f, 0f)
    fun handleTouchPress(normalizedX: Float, normalizedY: Float) {
        val ray: Ray = convertNormalized2DPointToRay(normalizedX, normalizedY)
        val plane = Geometry.Plane(
            Geometry.Point(0f, 0f, 0f),
            Geometry.Vector(0f, 0f, 1f)
        )
        touchedPoint = Geometry.intersectionPoint(ray, plane)
        /*  val objectBoundingSphere: Geometry.Sphere = Geometry.Sphere(
              Geometry.Point(
                  glMagnifier.vertexPosition.x,
                  glMagnifier.vertexPosition.y,
                  glMagnifier.vertexPosition.z,
              ),
             /* Geometry.Point(
                  magnifierPosition!!.x,
                  magnifierPosition!!.y,
                  magnifierPosition!!.z
              ),*/
              0.5f
          )*/
        val objectBoundingRectangle = Geometry.Rectangle(
            Geometry.Point(
                glMagnifier.vertexPosition.x,
                glMagnifier.vertexPosition.y,
                glMagnifier.vertexPosition.z,
            ),
            glMagnifier.vertexWidth,
            glMagnifier.vertexHeight
        )
        glMagnifier.setSelected(
            Geometry.intersects(
                objectBoundingRectangle, ray
            ),
            touchedPoint.x,
            touchedPoint.y
        )
        /*  glMagnifier.pressed = Geometry.intersects(
              objectBoundingRectangle, ray
          )*/


        //  magnifierPressed = Geometry.intersects(objectBoundingSphere, ray)
    }

    fun handleTouchDrag(normalizedX: Float, normalizedY: Float) {

        if (glMagnifier.selected) {
            val ray: Ray = convertNormalized2DPointToRay(normalizedX, normalizedY)
            val plane = Geometry.Plane(
                Geometry.Point(0f, 0f, 0f),
                Geometry.Vector(0f, 0f, 1f)
            )
            val draggedPoint: Geometry.Point = Geometry.intersectionPoint(ray, plane)

            //  previousMagnifierPosition = magnifierPosition


            // val ratio = if (width > height) width.toFloat() / height.toFloat() else height.toFloat() / width.toFloat()
            //////////////////////////////

            glMagnifier.translate(draggedPoint)
            /////////////////////////////
            /*     val magnification = 2.0f
                 val magnifierSizeW = 0.5f
                 val magnifierSizeH = 0.5f
                 ///////////////////////////
                 val he = 2f
                 val wid = ratio * he


                 val magnifierVertexWidth =
                     maxOf(wid, he) * magnifierSizeW//1.0f/ratio// wid*magnifierSizeW
                 val magnifierVertexHeight = minOf(wid, he) * magnifierSizeH//1.0f// he*magnifierSizeW


                 val magnifierTextureWidth = 1f * (magnifierSizeW / magnification) // 0.0625fratio
                 val magnifierTextureHeight = 1f * (magnifierSizeH / magnification) // 0.0625f

                 magnifierPosition = Geometry.Point(
                     clamp(
                         touchedPoint.x,
                         -1 * ratio + magnifierVertexWidth / 2,
                         1 * ratio - magnifierVertexWidth / 2
                     ),
                     clamp(
                         touchedPoint.y,
                         -1 + magnifierVertexHeight / 2,
                         1 - magnifierVertexHeight / 2
                     ),
                     0f// mallet.radius,
                 )
                  val position = mapVertexToTextureCords(
                     magnifierPosition!!.x / ratio,
                     magnifierPosition!!.y
                 )

                 glMagnifier.updateVertexBuffer(
                     magnifierVertexWidth,
                     magnifierVertexHeight
                 )

                 glMagnifier.updateTextureBuffer(
                     position,
                     magnifierTextureWidth,
                     magnifierTextureHeight,
                 )*/

        }


    }

    private fun mapVertexToTextureCords(vertexX: Float, vertexY: Float): Pair<Float, Float> {
        val textureX = (vertexX + 1.0f) / 2.0f
        val textureY = 1.0f - (vertexY + 1.0f) / 2.0f

        return Pair(textureX, textureY)
    }


    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f)
        createSurfaceTexture()
        //  textureProgram = TextureShaderProgram(context)
        //   colorProgram = ColorShaderProgram(context)
        // scaledTextureProgram = ScaledTextureProgram(context)

        //magnifierPosition = Geometry.Point(0f, 0f, 0f)
    }

    //  var width = 0
    //  var height = 0


    //  private var ratio=1f
    override fun onSurfaceChanged(glUnused: GL10?, width: Int, height: Int) {
        ///////////tmp
        //   this.width = width
        //   this.height = height
        glCamera.setProjection(width, height)
        /*  var rotationX = 0f
          var rotationY = 0f
          val windowManager: WindowManager = context
              .getSystemService(Context.WINDOW_SERVICE) as WindowManager
          when (windowManager.defaultDisplay.rotation) {
              Surface.ROTATION_0 -> {
                  rotationX = -1f
                  rotationY = 0f
              }

              Surface.ROTATION_90 -> {
                  rotationX = 0f
                  rotationY = 1f
              }

              Surface.ROTATION_180 -> {
                  rotationX = 1f
                  rotationY = 0f
              }

              Surface.ROTATION_270 -> {
                  rotationX = 0f
                  rotationY = -1f
              }

              else -> "Не понятно"
          }*/
        val ratio =
            if (width > height) width.toFloat() / height.toFloat() else height.toFloat() / width.toFloat()

        // val ratio: Float = width.toFloat() / height.toFloat()

        ////////////////////////////////
        //   glViewport(0, 0, width, height)
        //  setLookAtM(viewMatrix, 0, 0f, 0f, 1f, 0f, 0f, 0f, rotationX, rotationY, 0.0f)
        /*  glScreen.updateVertexBuffer(
              ratio * 2,
              2f,
          )*/
        glScreen.onRatioChanged(ratio)
        ////////////////////////////////

        glMagnifier.onRatioChanged(ratio)
        glMagnifier.configure(2f, 0.5f, 0.5f)
        /*   glMagnifier.updateVertexBuffer(
               1f,
               1f
           )
           glMagnifier.updateTextureBuffer(Geometry.Point(0f, 0f,0f), 0.5f, 0.5f)
         */
        /*  if (width > height) {
            // Landscape

            orthoM(projectionMatrix, 0, -ratio, ratio, -1f, 1f, -1f, 1f)
        } else {
            // Portrait or square
            orthoM(projectionMatrix, 0, -1f, 1f, -ratio, ratio, -1f, 1f)
        }
        multiplyMM(
            viewProjectionMatrix, 0, projectionMatrix, 0,
            viewMatrix, 0
        )*/

        // create a projection matrix from device screen geometry
        // frustumM(projectionMatrix, 0,  -1f, 1f, -ratio, ratio,1f, 2f)
    }

    override fun onDrawFrame(gl: GL10?) {
        surfaceTexture?.updateTexImage()
        //   surfaceTexture?.getTransformMatrix(transformMatrix)
        /////////////////////
        /*   val buffer =
               ByteBuffer.allocateDirect(width * height * 4) // 4 байта на пиксель (ARGB)
           glReadPixels(0, 0, width, height, GL10.GL_RGBA, GL10.GL_UNSIGNED_BYTE, buffer)
           buffer.rewind()

           val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
           bitmap.copyPixelsFromBuffer(buffer)*/
        /////////////////////
        glClearColor(1.0f, 0.0f, 0.0f, 0.0f)
        glClear(GL_COLOR_BUFFER_BIT)

        /*  multiplyMM(
              viewProjectionMatrix, 0, projectionMatrix, 0,
              viewMatrix, 0
          )*/

        //  positionScreenInScene()
        //  multiplyMM(viewProjectionMatrix, 0, projectionMatrix, 0, viewMatrix, 0)
        // glScreen.bindData(textureProgram)
        //  textureProgram?.useProgram()
        // textureProgram?.setUniforms(modelViewProjectionMatrix)
        glScreen.translate(0f, 0f)
        val c = glCamera.translateObjectInScene(glScreen.modelMatrix)
        glScreen.draw(textureProgram, c)
        /*  positionObjectInScene(
              1f,
              2f,
             0f
          )*/
        /* mallet.bindData(colorProgram!!)
         colorProgram?.useProgram()
         colorProgram?.setUniforms(modelViewProjectionMatrix)
         mallet.draw()*/
        ///////////////////////////////////////////
        /* positionObjectInScene(
             magnifierPosition!!.x,
             magnifierPosition!!.y,
             0f
         )*/
        /* glMagnifier.translate(
             magnifierPosition!!.x,
             magnifierPosition!!.y
         )*/
        val m = glCamera.translateObjectInScene(glMagnifier.modelMatrix)
        glMagnifier.draw(scaledTextureProgram, m)
    }

    /*  private fun positionScreenInScene() {
          setIdentityM(modelMatrix, 0)
          //  rotateM(modelMatrix, 0, -45f, 1f, 0f, 0f)
          multiplyMM(
              modelViewProjectionMatrix, 0, viewProjectionMatrix,
              0, modelMatrix, 0
          )
      }*/

    /*  private fun positionObjectInScene(x: Float, y: Float, z: Float) {
          setIdentityM(modelMatrix, 0)

          translateM(modelMatrix, 0, x, y, z)
          // rotateM(modelMatrix, 0, -90f, 0f, 0f, 1f)
          //   scaleM(modelMatrix,0,0.2f,0.2f,0.0f)
          multiplyMM(
              modelViewProjectionMatrix, 0, viewProjectionMatrix,
              0, modelMatrix, 0
          )
      }*/


    // private var texture = 0
    private fun createSurfaceTexture() {

        //  texture = createOESTextureObject()
        surfaceTexture = SurfaceTexture(createOESTextureObject()).apply {
            callback(this)
        }

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

    abstract class GLObject(private val textureProgram:GlShaderProgram) {
        var selected = false
        private val BYTES_PER_FLOAT: Int = 4
        private val vertexDataSize: Int = 12
        private val POSITION_COMPONENT_COUNT = 2
        private val TEXTURE_COORDINATES_COMPONENT_COUNT = 2
        private val STRIDE: Int = (POSITION_COMPONENT_COUNT
                + TEXTURE_COORDINATES_COMPONENT_COUNT) * BYTES_PER_FLOAT
        abstract val vertexData: FloatArray
        abstract val textureData: FloatArray
        private val vertexBuffer: FloatBuffer = ByteBuffer
            .allocateDirect(vertexData.size * BYTES_PER_FLOAT)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .put(vertexData)
        private val textureBuffer: FloatBuffer = ByteBuffer
            .allocateDirect(vertexDataSize * BYTES_PER_FLOAT)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .put(textureData)
        val modelMatrix = FloatArray(16)
        private fun updateVertexBuffer(width: Float, height: Float) {
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

        private fun bindData() {

            vertexBuffer.position(0)
            glVertexAttribPointer(
                textureProgram.positionAttributeLocation,
                2,
                GL_FLOAT,
                false,
                8,
                vertexBuffer
            )
            glEnableVertexAttribArray(
                textureProgram.positionAttributeLocation
            )
            ////////////////////////
            textureBuffer.position(0)
            glVertexAttribPointer(
                textureProgram.textureCoordinatesAttributeLocation,
                2,
                GL_FLOAT,
                false,
                8,
                textureBuffer
            )
            glEnableVertexAttribArray(
                textureProgram.textureCoordinatesAttributeLocation
            )
        }

        fun translate(x: Float, y: Float) {
            setIdentityM(modelMatrix, 0)
            translateM(modelMatrix, 0, x, y, 0f)
        }

        fun draw( modelViewProjectionMatrix: FloatArray) {
            bindData()
            textureProgram.useProgram()
            textureProgram.setUniforms(modelViewProjectionMatrix)
            glDrawArrays(GL_TRIANGLES, 0, 6)
        }

        fun onRatioChanged(ratio: Float) {
            updateVertexBuffer(
                ratio * 2,
                2f,
            )
        }

    }

    class GlShaderProgram(
        val context: Context,
        val vertexShaderResourceId: Int,
        val fragmentShaderResourceId: Int
    ) : ShaderProgram(
        context,
        vertexShaderResourceId,
        fragmentShaderResourceId
    ){
        private val U_TEXTURE_UNIT: String = "u_TextureUnit"
        private  val A_TEXTURE_COORDINATES: String = "a_TextureCoordinates"

        val positionAttributeLocation = glGetAttribLocation(program, A_POSITION)
        val textureCoordinatesAttributeLocation = glGetAttribLocation(program, A_TEXTURE_COORDINATES)
        private val uMatrixLocation = glGetUniformLocation(program, U_MATRIX)
        private val uTextureUnitLocation = glGetUniformLocation(program, U_TEXTURE_UNIT)



        fun setUniforms(matrix: FloatArray?) {
            glUniformMatrix4fv(uMatrixLocation, 1, false, matrix, 0)
        }
    }

}