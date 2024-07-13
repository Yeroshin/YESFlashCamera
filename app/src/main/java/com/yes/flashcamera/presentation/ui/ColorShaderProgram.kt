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
    protected  val A_COLOR: String = "a_Color"
    // Uniform locations
    // Retrieve uniform locations for the shader program.
    private val uMatrixLocation = glGetUniformLocation(program, U_MATRIX)

    // Attribute locations
    // Retrieve attribute locations for the shader program.
    val positionAttributeLocation: Int = glGetAttribLocation(program, A_POSITION)
    val colorAttributeLocation: Int = glGetAttribLocation(program, A_COLOR)
    fun setUniforms(matrix: FloatArray?) {
        // Pass the matrix into the shader program.
        glUniformMatrix4fv(uMatrixLocation, 1, false, matrix, 0)
    }
}