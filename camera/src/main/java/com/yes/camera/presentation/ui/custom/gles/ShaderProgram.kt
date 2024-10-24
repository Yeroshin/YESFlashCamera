package com.yes.camera.presentation.ui.custom.gles

import android.content.Context
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
import android.opengl.GLES20.glGetError
import android.opengl.GLES20.glGetProgramiv
import android.opengl.GLES20.glGetShaderInfoLog
import android.opengl.GLES20.glGetShaderiv
import android.opengl.GLES20.glLinkProgram
import android.opengl.GLES20.glShaderSource
import android.opengl.GLES20.glUseProgram
import android.opengl.GLES30
import android.opengl.GLES30.GL_VERSION
import android.opengl.GLES30.glGetString
import android.util.Log
import com.yes.camera.presentation.ui.custom.gles.ShaderProgram.ShaderHelper.createShader
import com.yes.camera.utils.FileUtils


class ShaderProgram (
    context: Context, vertexShaderResourceId: Int,
    fragmentShaderResourceId: Int
) {


     val programId: Int = ShaderHelper.buildProgram(
        createShader(
            context, GL_VERTEX_SHADER, vertexShaderResourceId
        ),
        createShader(
            context, GL_FRAGMENT_SHADER, fragmentShaderResourceId
        )
    )

    fun useProgram() {
        val error1 = glGetError()
        if (error1 != GLES30.GL_NO_ERROR) {
            Log.e("TextureLoad", "OpenGL Error: $error1")
        } else {
            Log.d("TextureLoad", "Texture loaded successfully")
        }
        glUseProgram(programId)
    }

    object ShaderHelper {
        fun buildProgram(vertexShaderId: Int, fragmentShaderId: Int): Int {
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

        fun createShader(context: Context, type: Int, shaderRawId: Int): Int {
            val shaderText = FileUtils.readTextFromRaw(context, shaderRawId)
            return createShader(type, shaderText)
        }

        private fun createShader(type: Int, shaderText: String?): Int {
            val version = glGetString(GL_VERSION)
            Log.d("OpenGL Version", "OpenGL ES version: $version")

            val shaderId = glCreateShader(type)
            if (shaderId == 0) {
                return 0
            }
            glShaderSource(shaderId, shaderText)
            glCompileShader(shaderId)
            val compileStatus = IntArray(1)
            glGetShaderiv(shaderId, GL_COMPILE_STATUS, compileStatus, 0)
            if (compileStatus[0] == 0) {
                val error = glGetShaderInfoLog(shaderId)
                Log.e("ShaderError", "Error compiling shader: $error")
                glDeleteShader(shaderId)
                return 0
            }
            val shaderLog = glGetShaderInfoLog(shaderId)
            if (shaderLog.isNotEmpty()) {
                Log.e("Shader Error", shaderLog)
            }
            return shaderId
        }
    }
}