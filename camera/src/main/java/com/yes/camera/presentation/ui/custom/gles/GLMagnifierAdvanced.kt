package com.yes.camera.presentation.ui.custom.gles

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.opengl.GLES10.glDrawArrays
import android.opengl.GLES11Ext.GL_TEXTURE_EXTERNAL_OES
import android.opengl.GLES20
import android.opengl.GLES20.GL_BLEND
import android.opengl.GLES20.GL_CLAMP_TO_EDGE
import android.opengl.GLES20.GL_FLOAT
import android.opengl.GLES20.GL_NEAREST
import android.opengl.GLES20.GL_NO_ERROR
import android.opengl.GLES20.GL_ONE_MINUS_SRC_ALPHA
import android.opengl.GLES20.GL_SRC_ALPHA
import android.opengl.GLES20.GL_TEXTURE0
import android.opengl.GLES20.GL_TEXTURE1
import android.opengl.GLES20.GL_TEXTURE_2D
import android.opengl.GLES20.GL_TEXTURE_MAG_FILTER
import android.opengl.GLES20.GL_TEXTURE_MIN_FILTER
import android.opengl.GLES20.GL_TEXTURE_WRAP_S
import android.opengl.GLES20.GL_TEXTURE_WRAP_T
import android.opengl.GLES20.GL_TRIANGLES
import android.opengl.GLES20.glActiveTexture
import android.opengl.GLES20.glBindTexture
import android.opengl.GLES20.glBlendFunc
import android.opengl.GLES20.glDisable
import android.opengl.GLES20.glEnable
import android.opengl.GLES20.glEnableVertexAttribArray
import android.opengl.GLES20.glGenTextures
import android.opengl.GLES20.glGetAttribLocation
import android.opengl.GLES20.glGetError
import android.opengl.GLES20.glGetUniformLocation
import android.opengl.GLES20.glTexParameterfv
import android.opengl.GLES20.glTexParameteri
import android.opengl.GLES20.glUniform1i
import android.opengl.GLES20.glUniformMatrix4fv
import android.opengl.GLES20.glVertexAttribPointer
import android.opengl.GLES32.GL_CLAMP_TO_BORDER
import android.opengl.GLES32.GL_TEXTURE_BORDER_COLOR
import android.opengl.GLUtils.texImage2D
import android.opengl.Matrix.setIdentityM
import android.opengl.Matrix.translateM
import android.util.Log
import androidx.core.math.MathUtils.clamp
import com.yes.camera.R
import com.yes.camera.utils.Geometry
import java.nio.ByteBuffer
import java.nio.ByteOrder

class GlMagnifierAdvanced(
    glShaderProgram: ShaderProgram,
    val context: Context
) :
    GLRenderer.GLObject(glShaderProgram) {
    private var magnification = 4.0f
    private var magnifierSizeW = 0.5f
    private var magnifierSizeH = 0.5f
    private var ratio = 1f

    override val vertexData = FloatArray(12)
    override val textureData = FloatArray(12)
     val textureData1 = FloatArray(12)

    private var posXcorrection = 0f
    private var posYcorrection = 0f

    protected val textureBuffer2 by lazy {
        ByteBuffer
            .allocateDirect(vertexDataSize * BYTES_PER_FLOAT)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .put(textureData1)
    }
    protected val vertexBuffer2 by lazy {
        ByteBuffer
            .allocateDirect(vertexDataSize * BYTES_PER_FLOAT)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .put(vertexData)
    }

    private val textureHandle = IntArray(2)
    init {
        loadTexture()

    }

    override fun setSelected(pressed: Boolean, touchedPointX: Float, touchedPointY: Float) {
        this.selected = pressed
        posXcorrection = touchedPointX - centerPosition.first
        posYcorrection = touchedPointY - centerPosition.second
    }

    private fun mapVertexToTextureCords(
        vertexPositionX: Float,
        vertexPositionY: Float
    ): Pair<Float, Float> {
        val textureX = (vertexPositionX + 1.0f) / 2.0f
        val textureY = 1.0f - (vertexPositionY + 1.0f) / 2.0f

        return Pair(textureX, textureY)
    }


    private var texturePosition = Pair(0f, 0f)
   override fun bindData() {

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
        ////////////////////////tex1
       val tmp1=  glGetAttribLocation(shaderProgram.programId,"a_Position")
       val tmp2=  glGetAttribLocation(shaderProgram.programId,"aTexCord2")
       val tmp3=  glGetAttribLocation(shaderProgram.programId,"a_TextureCoordinates")

       textureBuffer.position(0)
        glVertexAttribPointer(
            glGetAttribLocation(shaderProgram.programId,"a_TextureCoordinates"),
            2,
            GL_FLOAT,
            false,
            8,
            textureBuffer
        )
        glEnableVertexAttribArray(
            glGetAttribLocation(shaderProgram.programId, "a_TextureCoordinates")
        )
       /////////////////////////////////tex2

       textureBuffer2.position(0)
       glVertexAttribPointer(
           glGetAttribLocation(shaderProgram.programId, "aTexCoord2"),
           2,
           GL_FLOAT,
           false,
           8,
           textureBuffer2
       )
       glEnableVertexAttribArray(
           glGetAttribLocation(shaderProgram.programId, "aTexCoord2")
       )
    }

    override fun translate(draggedPointX: Float, draggedPointY: Float) {
        val touchedPoint = Geometry.Point(
            draggedPointX - posXcorrection,
            draggedPointY - posYcorrection,
            0f
        )

        centerPosition = Triple(
            clamp(
                touchedPoint.x,
                -1 * ratio + vertexWidth / 2,
                1 * ratio - vertexWidth / 2
            ),
            clamp(
                touchedPoint.y,
                -1 + vertexHeight / 2,
                1 - vertexHeight / 2
            ),
            0f// mallet.radius,
        )
        texturePosition = mapVertexToTextureCords(
            centerPosition.first / ratio,
            centerPosition.second
        )
        updateVertexBuffer(
            vertexWidth,
            vertexHeight
        )

        updateTextureBuffer(
            texturePosition.first,
            texturePosition.second,
            textureWidth,
            textureHeight,
        )
        ////////////////////
        setIdentityM(modelMatrix, 0)
        translateM(modelMatrix, 0, centerPosition.first, centerPosition.second, 0f)
        /////////////////////
    }




    private var textureWidth = 0f
    private var textureHeight = 0f

    fun configure(
        magnification: Float,
        magnifierSizeW: Float,
        magnifierSizeH: Float,
    ) {
        this.magnification = magnification
        this.magnifierSizeW = magnifierSizeW
        this.magnifierSizeH = magnifierSizeH

        vertexWidth = maxOf(width, height) * magnifierSizeW//1.0f/ratio// wid*magnifierSizeW
        vertexHeight = minOf(width, height) * magnifierSizeH//1.0f// he*magnifierSizeW

        textureWidth = 1f * (magnifierSizeW / magnification) // 0.0625fratio
        textureHeight = 1f * (magnifierSizeH / magnification) // 0.0625f


        updateVertexBuffer(
            vertexWidth,
            vertexHeight
        )
        texturePosition = mapVertexToTextureCords(
            centerPosition.first / ratio,
            centerPosition.second
        )
        updateTextureBuffer(
            texturePosition.first,
            texturePosition.second,
            textureWidth,
            textureHeight,
        )
        /*  setIdentityM(modelMatrix, 0)
          translateM(modelMatrix, 0, 0f, 0f, 0f)*/

    }
    override  fun updateVertexBuffer(width: Float, height: Float) {
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
        ////////////////
        val vertexData2 = floatArrayOf( // Order of coordinates: X, Y, S, T
            0.0f - width / 2, 0.0f + height / 2,
            0.0f + width / 2, 0.0f + height / 2,
            0.0f + width / 2, 0.0f - height / 2,
            0.0f + width / 2, 0.0f - height / 2,
            0.0f - width / 2, 0.0f - height / 2,
            0.0f - width / 2, 0.0f + height / 2
        )
        vertexBuffer2.position(0)
        vertexBuffer2.put(vertexData2, 0, vertexDataSize)
        vertexBuffer2.position(0)
    }
    override fun updateTextureBuffer(
        positionX: Float,
        positionY: Float,
        width: Float,
        height: Float
    ) {
        //////////////////////////////
        val textureData1 = floatArrayOf( // Order of coordinates: X, Y, S, T
            positionX - width / 2, positionY - height / 2,
            positionX + width / 2, positionY - height / 2,
            positionX + width / 2, positionY + height / 2,
            positionX + width / 2, positionY + height / 2,
            positionX - width / 2, positionY + height / 2,
            positionX - width / 2, positionY - height / 2
        )
        textureBuffer.position(0)
        textureBuffer.put(textureData1, 0, vertexDataSize)
        textureBuffer.position(0)
        /////////////////////////////////
        // 6,1 -- 2
        //   5 -- 4,3
        val textureData2 = floatArrayOf(
            positionX ,positionY+height,
            positionX + width,positionY+height,
            positionX + width, positionY ,
            positionX + width, positionY ,
            positionX , positionY ,
            positionX , positionY+height,

            )
        textureBuffer2.position(0)
        textureBuffer2.put(textureData2, 0, vertexDataSize)
        textureBuffer2.position(0)
    }

    private var width: Float = 0f
    private var height: Float = 0f
    override fun onRatioChanged(ratio: Float) {
        this.ratio = ratio
        this.height = 2f
        this.width = ratio * height
    }
    override fun draw(modelViewProjectionMatrix: FloatArray) {

        shaderProgram.useProgram()
        glActiveTexture(GL_TEXTURE0)
        glBindTexture(GL_TEXTURE_EXTERNAL_OES, 1)
        glUniform1i(
            glGetUniformLocation(shaderProgram.programId, "baseTexture"),
            0
        )
        glActiveTexture(GL_TEXTURE1)
        glBindTexture(GL_TEXTURE_2D, 3)
        glUniform1i(
            glGetUniformLocation(shaderProgram.programId, "overlayTexture"),
            1
        )



        /*  glEnable(GL_BLEND)
          glBlendFunc(GL_ONE, GL_ONE_MINUS_SRC_ALPHA)*/


       //  glEnable(GL_BLEND)
         glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)

        bindData()

        glUniformMatrix4fv(uMatrixLocation, 1, false, modelViewProjectionMatrix, 0)

        // shaderProgram.setUniforms(modelViewProjectionMatrix)
        glDrawArrays(GL_TRIANGLES, 0, 6)
     //    glDisable(GL_BLEND)
        glBindTexture(GL_TEXTURE_EXTERNAL_OES, 0)
        glBindTexture(GL_TEXTURE_2D, 0)
///////////////////
        val error1 = glGetError()
        if (error1 != GL_NO_ERROR) {
            Log.e("TextureLoad", "OpenGL Error: $error1")
        } else {
            Log.d("TextureLoad", "Texture loaded successfully")
        }
        /////////////////////////
    }

    private fun loadTexture() {




        glGenTextures(2, textureHandle, 0)
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
        glTexParameteri(
            GL_TEXTURE_EXTERNAL_OES,
            GL_TEXTURE_WRAP_S,
            GL_CLAMP_TO_EDGE
        );
        glTexParameteri(
            GL_TEXTURE_EXTERNAL_OES,
            GL_TEXTURE_WRAP_T,
            GL_CLAMP_TO_EDGE
        );

        glBindTexture(GL_TEXTURE_EXTERNAL_OES, 0)

        //////////////////
        val options = BitmapFactory.Options().apply {
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }

        val bitmap = BitmapFactory.decodeResource(
            context.resources,
            R.drawable.focus_test,
            options
        )

        glActiveTexture(GL_TEXTURE1)
        glBindTexture(GL_TEXTURE_2D, textureHandle[1])

        glTexParameteri(
            GL_TEXTURE_2D,
            GL_TEXTURE_WRAP_S,
            GL_CLAMP_TO_EDGE
        )
        glTexParameteri(
            GL_TEXTURE_2D,
            GL_TEXTURE_WRAP_T,
            GL_CLAMP_TO_EDGE
        );
        glTexParameteri(
            GL_TEXTURE_2D,
            GL_TEXTURE_MIN_FILTER,
            GL_NEAREST
        )
        glTexParameteri(
            GL_TEXTURE_2D,
            GL_TEXTURE_MAG_FILTER,
            GL_NEAREST
        )

        texImage2D(GL_TEXTURE_2D, 0, bitmap, 0);
        bitmap.recycle()

        glBindTexture(GL_TEXTURE_2D, 0)
    }

}