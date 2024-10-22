#extension GL_OES_EGL_image_external : require
precision mediump float;
uniform samplerExternalOES u_TextureUnit;
uniform vec4 u_Color;
varying vec2 v_TextureCoordinates;

void main()
{
    vec4 texColor = texture2D(u_TextureUnit, v_TextureCoordinates);
    gl_FragColor = vec4(u_Color.rgb, texColor.a);
}