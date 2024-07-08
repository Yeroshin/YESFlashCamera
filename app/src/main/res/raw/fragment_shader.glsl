#extension GL_OES_EGL_image_external : require
precision mediump float;
uniform samplerExternalOES usTextureOes;
varying vec2 vvTextureCoordinate;

void main() {
    vec2 scaledTextureCoordinate = vvTextureCoordinate * 0.5;


        vec4 vCameraColor = texture2D(usTextureOes, scaledTextureCoordinate);


        gl_FragColor = vCameraColor;
}
