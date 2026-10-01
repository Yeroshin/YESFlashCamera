package com.yes.camera.presentation.ui.custom.gles

import android.opengl.GLES11Ext.GL_TEXTURE_EXTERNAL_OES
import android.opengl.GLES20
import android.opengl.GLES20.GL_TRIANGLES
import android.opengl.GLES20.glActiveTexture
import android.opengl.GLES20.glBindTexture
import android.opengl.GLES20.glUniformMatrix4fv
import android.opengl.Matrix.setIdentityM
import android.opengl.Matrix.translateM

class GLScreen(glShaderProgram: ShaderProgram) :
    GLRenderer.GLObject(glShaderProgram) {

    override val vertexData = FloatArray(12)
    override val textureData = floatArrayOf(
        0f, 0f,
        1f, 0f,
        1f, 1f,
        1f, 1f,
        0f, 1f,
        0f, 0f
    )

    override fun setSelected(pressed: Boolean, touchedPointX: Float, touchedPointY: Float) {}

    override fun translate(draggedPointX: Float, draggedPointY: Float) {
        setIdentityM(modelMatrix, 0)
        translateM(modelMatrix, 0, draggedPointX, draggedPointY, 0f)
    }

    override fun onRatioChanged(ratio: Float) {
        updateVertexBuffer(
            ratio * 2,
            2f,
        )
        translate(0f, 0f)
    }

    override fun draw(modelViewProjectionMatrix: FloatArray, oesTextureId: Int) {
        shaderProgram.useProgram()
        glActiveTexture(GLES20.GL_TEXTURE0)
        glBindTexture(GL_TEXTURE_EXTERNAL_OES, if (oesTextureId != 0) oesTextureId else 1)

        bindData()
        glUniformMatrix4fv(uMatrixLocation, 1, false, modelViewProjectionMatrix, 0)

        GLES20.glDrawArrays(GL_TRIANGLES, 0, 6)
        glBindTexture(GL_TEXTURE_EXTERNAL_OES, 0)
    }
}
