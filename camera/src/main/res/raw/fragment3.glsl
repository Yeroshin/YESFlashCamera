#version 320 es
#extension GL_OES_EGL_image_external : require
precision mediump float;
uniform samplerExternalOES u_TextureUnit;
in vec2 v_TextureCoordinates;
out vec4 fragColor; // Используйте 'out' для задания цвета фрагмента
void main()
{
 vec3 texCoord = vec3(v_TextureCoordinates, float(0));
fragColor = texture(u_TextureUnit,  texCoord);

}