package com.yes.camera.presentation.ui.custom.gles

import android.content.Context
import android.graphics.BitmapFactory
import android.opengl.GLES10.glDrawArrays
import android.opengl.GLES20
import android.opengl.GLES20.GL_NEAREST
import android.opengl.GLES20.GL_ONE_MINUS_SRC_ALPHA
import android.opengl.GLES20.GL_SRC_ALPHA
import android.opengl.GLES20.GL_TEXTURE_2D
import android.opengl.GLES20.GL_TEXTURE_MAG_FILTER
import android.opengl.GLES20.GL_TEXTURE_MIN_FILTER
import android.opengl.GLES20.GL_TEXTURE_WRAP_S
import android.opengl.GLES20.GL_TEXTURE_WRAP_T
import android.opengl.GLES20.GL_TRIANGLES
import android.opengl.GLES20.glActiveTexture
import android.opengl.GLES20.glBindTexture
import android.opengl.GLES20.glBlendFunc
import android.opengl.GLES20.glTexParameterfv
import android.opengl.GLES20.glTexParameteri
import android.opengl.GLES20.glUniform1i
import android.opengl.GLES20.glUniformMatrix4fv
import android.opengl.GLES32.GL_CLAMP_TO_BORDER
import android.opengl.GLES32.GL_TEXTURE_BORDER_COLOR
import android.opengl.GLUtils
import android.opengl.Matrix.setIdentityM
import android.opengl.Matrix.translateM
import androidx.core.math.MathUtils.clamp
import com.yes.camera.R
import com.yes.camera.utils.Geometry


class GlFocus(
    val glShaderProgram: ShaderProgram,
    val context: Context
) : GLRenderer.GLObject(glShaderProgram) {
    private var magnification = 4.0f
    private var magnifierSizeW = 0.5f
    private var magnifierSizeH = 0.5f
    private var ratio = 1f

    override val vertexData = FloatArray(12)
    override val textureData = FloatArray(12)

    private var posXcorrection = 0f
    private var posYcorrection = 0f

    init {
        loadTexture(context)

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
     /*   texturePosition = mapVertexToTextureCords(
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
        )*/
        ////////////////////
        setIdentityM(modelMatrix, 0)
        translateM(modelMatrix, 0, centerPosition.first, centerPosition.second, 0f)
        /////////////////////

    }




    private var textureWidth = 0f
    private var textureHeight = 0f

    fun configure(
        textureScale: Float,
        sizeW: Float,
        sizeH: Float,
    ) {
        this.magnification = textureScale
        this.magnifierSizeW = sizeW
        this.magnifierSizeH = sizeH

        vertexWidth = maxOf(width, height) * sizeW//1.0f/ratio// wid*magnifierSizeW
        vertexHeight = minOf(width, height) * sizeH//1.0f// he*magnifierSizeW
/*
        textureWidth = 1f * (magnifierSizeW / magnification) // 0.0625fratio
        textureHeight = 1f * (magnifierSizeH / magnification) // 0.0625f
*/
        textureWidth = 1f/textureScale
        textureHeight = 1f/textureScale

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
        setIdentityM(modelMatrix, 0)
        translateM(modelMatrix, 0, 0f, 0f, 0f)

    }
    override fun updateTextureBuffer(
        positionX: Float,
        positionY: Float,
        width: Float,
        height: Float
    ) {
        val textureData = floatArrayOf( // Order of coordinates: X, Y, S, T
            0.5f-width/2, 0.5f+height/2,
            0.5f+width/2, 0.5f+height/2,
            0.5f+width/2, 0.5f-height/2,
            0.5f+width/2, 0.5f-height/2,
            0.5f-width/2, 0.5f-height/2,
            0.5f-width/2, 0.5f+height/2,

        )
      /*  val textureData = floatArrayOf( // Order of coordinates: X, Y, S, T
            0.0f - width / 2, 0.0f + height / 2,
            0.0f + width / 2, 0.0f + height / 2,
            0.0f + width / 2, 0.0f - height / 2,
            0.0f + width / 2, 0.0f - height / 2,
            0.0f - width / 2, 0.0f - height / 2,
            0.0f - width / 2, 0.0f + height / 2
        )*/
        textureBuffer.position(0)
        textureBuffer.put(textureData, 0, vertexDataSize)
        textureBuffer.position(0)
    }

    private var width: Float = 0f
    private var height: Float = 0f
    override fun onRatioChanged(ratio: Float) {
        this.ratio = ratio
        this.height = 2f
        this.width = ratio * height

    }



    val textureHandle = 2
    fun loadTexture(context: Context) {


        val options = BitmapFactory.Options().apply {
          /*  inPreferredConfig = Bitmap.Config.RGBA_F16
            inScaled = true*/
        }
        // options.inScaled = false // No pre-scaling

        // Read in the resource
        val bitmap = BitmapFactory.decodeResource(
            context.resources,
            R.drawable.tmp,
            options
        )

        // Bind to the texture in OpenGL
        glActiveTexture(GLES20.GL_TEXTURE1)
        glBindTexture(GL_TEXTURE_2D, textureHandle)

        glTexParameteri(
            GL_TEXTURE_2D,
            GL_TEXTURE_WRAP_S,
            GL_CLAMP_TO_BORDER
        );
        glTexParameteri(
            GL_TEXTURE_2D,
            GL_TEXTURE_WRAP_T,
            GL_CLAMP_TO_BORDER
        );
        val borderColor = floatArrayOf(1.0f, 0.0f, 0.0f, 1.0f)
        glTexParameterfv(
            GL_TEXTURE_2D,
            GL_TEXTURE_BORDER_COLOR,
            borderColor,
            0)


        // Set filtering
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

        // Load the bitmap into the bound texture.
        GLUtils.texImage2D(GL_TEXTURE_2D, 0, bitmap, 0)
        glBindTexture(GL_TEXTURE_2D, 0)
        // Recycle the bitmap, since its data has been loaded into OpenGL.
        bitmap.recycle()
    }

    override fun draw(modelViewProjectionMatrix: FloatArray) {


        val mTextureUniformHandle =
            GLES20.glGetUniformLocation(shaderProgram.programId, "u_TextureUnit")
        glActiveTexture(GLES20.GL_TEXTURE1)
        glBindTexture(GL_TEXTURE_2D, textureHandle)

     //   glEnable(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
          glUniform1i(mTextureUniformHandle, 1)

        /* glEnable(GL_BLEND)
         glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)*/

        bindData()
        shaderProgram.useProgram()
        glUniformMatrix4fv(uMatrixLocation, 1, false, modelViewProjectionMatrix, 0)
      //  shaderProgram.setUniforms(modelViewProjectionMatrix)
        glDrawArrays(GL_TRIANGLES, 0, 6)
      //  glDisable(GL_BLEND)
        glBindTexture(GL_TEXTURE_2D, 0)

    }
}
