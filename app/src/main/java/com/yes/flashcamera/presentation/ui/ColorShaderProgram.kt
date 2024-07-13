package com.yes.flashcamera.presentation.ui


import android.content.Context
import android.opengl.GLES20.glGetAttribLocation
import android.opengl.GLES20.glGetUniformLocation
import android.opengl.GLES20.glUniformMatrix4fv
import com.yes.flashcamera.R


class ColorShaderProgram(context: Context) : ShaderProgram(
    context, R.raw.simple_vertex,
    R.raw.simple_fragment
) {
    private  val A_COLOR: String = "a_Color"
    private val uMatrixLocation = glGetUniformLocation(program, U_MATRIX)

    val positionAttributeLocation: Int = glGetAttribLocation(program, A_POSITION)
    val colorAttributeLocation: Int = glGetAttribLocation(program, A_COLOR)
    fun setUniforms(matrix: FloatArray?) {

        glUniformMatrix4fv(uMatrixLocation, 1, false, matrix, 0)
    }
}