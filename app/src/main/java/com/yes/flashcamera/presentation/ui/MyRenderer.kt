package com.yes.flashcamera.presentation.ui


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
import android.opengl.Matrix.invertM
import android.opengl.Matrix.multiplyMM
import android.opengl.Matrix.multiplyMV
import android.opengl.Matrix.orthoM
import android.opengl.Matrix.setIdentityM
import android.opengl.Matrix.translateM
import com.yes.flashcamera.presentation.ui.Geometry.Ray
import com.yes.flashcamera.presentation.ui.Geometry.vectorBetween
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

    private var textureProgram: TextureShaderProgram? = null
    private var colorProgram: ColorShaderProgram? = null
    private var scaledTextureProgram: ScaledTextureProgram? = null


    private var surfaceTexture: SurfaceTexture? = null
    private val glScreen by lazy {
        GLScreen()
    }
    private val magnifier by lazy {
        Magnifier(0.5f, 270.3f, 32)
    }
    private var blueMalletPosition: Geometry.Point? = null
    private var previousBlueMalletPosition: Geometry.Point? = null
    private val leftBound = -1f
    private val rightBound = 1f
    private val farBound = -1f
    private val nearBound = 1f

    fun setDefaultBufferSize(){
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
        // We'll convert these normalized device coordinates into world-space
        // coordinates. We'll pick a point on the near and far planes, and draw a
        // line between them. To do this transform, we need to first multiply by
        // the inverse matrix, and then we need to undo the perspective divide.
        val nearPointNdc = floatArrayOf(normalizedX, normalizedY, -1f, 1f)
        val farPointNdc = floatArrayOf(normalizedX, normalizedY, 1f, 1f)

        val nearPointWorld = FloatArray(4)
        val farPointWorld = FloatArray(4)

        multiplyMV(
            nearPointWorld, 0, invertedViewProjectionMatrix, 0, nearPointNdc, 0
        )
        multiplyMV(
            farPointWorld, 0, invertedViewProjectionMatrix, 0, farPointNdc, 0
        )

        // Why are we dividing by W? We multiplied our vector by an inverse
        // matrix, so the W value that we end up is actually the *inverse* of
        // what the projection matrix would create. By dividing all 3 components
        // by W, we effectively undo the hardware perspective divide.
        divideByW(nearPointWorld)
        divideByW(farPointWorld)

        // We don't care about the W value anymore, because our points are now
        // in world coordinates.
        val nearPointRay =
            Geometry.Point(nearPointWorld[0], nearPointWorld[1], nearPointWorld[2])

        val farPointRay =
            Geometry.Point(farPointWorld[0], farPointWorld[1], farPointWorld[2])

        return Ray(
            nearPointRay,
            vectorBetween(nearPointRay, farPointRay)
        )
    }

    private var malletPressed = false
    fun handleTouchPress(normalizedX: Float, normalizedY: Float) {
        val ray: Ray = convertNormalized2DPointToRay(normalizedX, normalizedY)

        // Now test if this ray intersects with the mallet by creating a
        // bounding sphere that wraps the mallet.
        val malletBoundingSphere: Geometry.Sphere = Geometry.Sphere(
            Geometry.Point(
                blueMalletPosition!!.x,
                blueMalletPosition!!.y,
                blueMalletPosition!!.z
            ),
            magnifier.height
        )

        // If the ray intersects (if the user touched a part of the screen that
        // intersects the mallet's bounding sphere), then set malletPressed =
        // true.
        malletPressed = Geometry.intersects(malletBoundingSphere, ray)
    }

    fun handleTouchDrag(normalizedX: Float, normalizedY: Float) {

        if (malletPressed) {
            val ray: Ray = convertNormalized2DPointToRay(normalizedX, normalizedY)
            // Define a plane representing our air hockey table.
            val plane = Geometry.Plane(
                Geometry.Point(0f, 0f, 0f),
                Geometry.Vector(0f, 0f, 1f)
            )
            // Find out where the touched point intersects the plane
            // representing our table. We'll move the mallet along this plane.
            val touchedPoint: Geometry.Point = Geometry.intersectionPoint(ray, plane)

            // Clamp to bounds
            previousBlueMalletPosition = blueMalletPosition


            blueMalletPosition =
                Geometry.Point(touchedPoint.x, touchedPoint.y, 0f);
            val magnifierWidth =if(rotationPortrait){
                (width/2 ).toFloat()
            } else {
                (height/2 ).toFloat()
            }
            // val magnifierWidth = (height/2 ).toFloat()

            val magnification = 2.0f
            /*   blueMalletPosition = Geometry.Point(
                   clamp(
                       touchedPoint.x,
                       -height/2 + magnifierWidth/2,
                       height/2 - magnifierWidth/2
                   ),
                   clamp(
                       touchedPoint.y,
                       -width/2 + magnifierWidth/2,
                       width/2 - magnifierWidth/2
                   ),
                   0f// mallet.radius,
               )*/
            // val magnification=0.03125f
            val magnifierValueX = (1 / (width / magnifierWidth)) / (2*magnification)
            val magnifierValueY = (1 / (height / magnifierWidth)) / (2*magnification)
            //    val magnifierValue = ((1 / magnifierWidth) * magnification * 2)
            val position =if (rotationPortrait){
                mapVertexToTextureCoords(
                    blueMalletPosition!!.x / (height / 2),
                    blueMalletPosition!!.y / (width / 2)
                )
            }else{
                mapVertexToTextureCoords(
                    blueMalletPosition!!.x / (width / 2),
                    blueMalletPosition!!.y / (height / 2)
                )
            }
          /*  val position = mapVertexToTextureCoords(
                blueMalletPosition!!.x / (height / 2),
                blueMalletPosition!!.y / (width / 2)
            )*/

            magnifier.updateVertexBuffer(
                magnifierWidth
            )
            if(rotationPortrait){
                magnifier.updateTextureBuffer(position, magnifierValueY, magnifierValueX)
            }else{
                magnifier.updateTextureBuffer(position, magnifierValueX, magnifierValueY)
            }
        }


    }

    private fun mapVertexToTextureCoords(vertexX: Float, vertexY: Float): Pair<Float, Float> {
        val textureX = (vertexX + 1.0f) / 2.0f
        val textureY = 1.0f - (vertexY + 1.0f) / 2.0f

        return Pair(textureX, textureY)
    }


    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f)
        createSurfaceTexture()
        textureProgram = TextureShaderProgram(context)
        colorProgram = ColorShaderProgram(context)
        scaledTextureProgram = ScaledTextureProgram(context)

        blueMalletPosition = Geometry.Point(0f, 0f, 0f)
    }

    var width = 0
    var height = 0
    var rotationPortrait=false
  /*  override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        glViewport(0, 0, width, height)
        this.width = width
        this.height = height
        val tmp = maxOf(
            width.toFloat() * 2,
            height.toFloat() * 2
        )
        val windowManager: WindowManager = context
            .getSystemService(Context.WINDOW_SERVICE) as WindowManager

        var rotationX = 0f
        var rotationY = 0f
        var length=0f
        when (windowManager.defaultDisplay.rotation) {
            Surface.ROTATION_0 -> {
                rotationX = -1f
                rotationY = 0f
                length=sqrt(width.toFloat() * width.toFloat() + height.toFloat() * height.toFloat())*1.09f
                rotationPortrait=true
            }

            Surface.ROTATION_90 -> {
                rotationX = 0f
                rotationY = 1f
                length=sqrt(width.toFloat() * width + height * height)*0.44f
                rotationPortrait=false
            }

            Surface.ROTATION_180 -> {
                rotationX = 1f
                rotationY = 0f
                length=sqrt(width.toFloat() * width + height * height)*1.09f
                rotationPortrait=true
            }

            Surface.ROTATION_270 -> {
                rotationX = 0f
                rotationY = -1f
                length=sqrt(width.toFloat() * width + height * height)*0.44f
                rotationPortrait=false
            }

            else -> "Не понятно"
        }

        perspectiveM(
            projectionMatrix,
            0,
            45f,
            width.toFloat() / height.toFloat(),
            1f,
            maxOf(
                width.toFloat() * 4,
                height.toFloat() * 4
            )
            // width.toFloat() * 4
        )


        setLookAtM(
            viewMatrix,
            0,
            0f,
            0.0f,
           /*  maxOf(
                 width.toFloat() * 3,
                 height.toFloat() * 3
             ),*/
           // sqrt(width.toFloat() * width + height * height)*1.09f,
           // sqrt(width.toFloat() * width + height * height)*0.44f,
            length,
            //height.toFloat()+1000 ,
            0f,
            0f,
            0f,
            rotationX,
            rotationY,
            0f
        )
        /*  glScreen.updateVertexBuffer(
              height.toFloat(), width.toFloat(),
          )*/
        glScreen.updateVertexBuffer(
            maxOf(
                width.toFloat(),
                height.toFloat()
            ),
            minOf(
                width.toFloat(),
                height.toFloat()
            )
        )
        magnifier.updateVertexBuffer(
            600f
        )
        magnifier.updateTextureBuffer(Pair(0f, 0f), 0.5f, 0.5f)
    }*/
  public override fun onSurfaceChanged(glUnused: GL10?, width: Int, height: Int) {
      // Set the OpenGL viewport to fill the entire surface.
      glViewport(0, 0, width, height)

      val aspectRatio =
          if (width > height) width.toFloat() / height.toFloat() else height.toFloat() / width.toFloat()

      if (width > height) {
          // Landscape
          orthoM(projectionMatrix, 0, -aspectRatio, aspectRatio, -1f, 1f, -1f, 1f)
      } else {
          // Portrait or square
          orthoM(projectionMatrix, 0, -1f, 1f, -aspectRatio, aspectRatio, -1f, 1f)
      }
      val tmp=normalizeValue(width.toFloat(), -1f, 1f)
     /* glScreen.updateVertexBuffer(
          maxOf(
              normalizeValue(width.toFloat(), -1f, 1f),
                      normalizeValue(height.toFloat(), -1f, 1f)
             // width.toFloat(),
            //  height.toFloat()
          ),
          minOf(
              normalizeValue(width.toFloat(), -1f, 1f),
                      normalizeValue(height.toFloat(), -1f, 1f)
            //  width.toFloat(),
            //  height.toFloat()
          )
      )*/
      glScreen.updateVertexBuffer(
          1.0f*4,
          0.6f*4,

      )
  }
    fun normalizeValue(x: Float, minValue: Float, maxValue: Float):Float {
        return (x - minValue) / (maxValue - minValue)
    }

    // private val transformMatrix = FloatArray(16)
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

        multiplyMM(
            viewProjectionMatrix, 0, projectionMatrix, 0,
            viewMatrix, 0
        )
        invertM(invertedViewProjectionMatrix, 0, viewProjectionMatrix, 0)

        positionScreenInScene()
        glScreen.bindData(textureProgram!!)
        textureProgram?.useProgram()
        textureProgram?.setUniforms(projectionMatrix, texture)
       // textureProgram?.setUniforms(modelViewProjectionMatrix, texture)

        glScreen.draw()


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
        positionObjectInScene(
            blueMalletPosition!!.x,
            blueMalletPosition!!.y,
            0f
        )
        magnifier.bindData(scaledTextureProgram!!)
        scaledTextureProgram?.useProgram()
        scaledTextureProgram?.setUniforms(modelViewProjectionMatrix, texture)
        magnifier.draw()
    }

    private fun positionScreenInScene() {
        setIdentityM(modelMatrix, 0)
        //  rotateM(modelMatrix, 0, -45f, 1f, 0f, 0f)
        multiplyMM(
            modelViewProjectionMatrix, 0, viewProjectionMatrix,
            0, modelMatrix, 0
        )
    }

    private fun positionObjectInScene(x: Float, y: Float, z: Float) {
        setIdentityM(modelMatrix, 0)

        translateM(modelMatrix, 0, x, y, z)
        // rotateM(modelMatrix, 0, -90f, 0f, 0f, 1f)
        //   scaleM(modelMatrix,0,0.2f,0.2f,0.0f)
        multiplyMM(
            modelViewProjectionMatrix, 0, viewProjectionMatrix,
            0, modelMatrix, 0
        )
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