#extension GL_OES_EGL_image_external : require
precision mediump float;

varying vec2 vTexCoord1;
varying vec2 vTexCoord2;
varying vec2 vNormalizedPos;

uniform samplerExternalOES baseTexture;
uniform sampler2D overlayTexture;

void main()
{
    // Since the quad is square (vertexWidth == vertexHeight), length(vNormalizedPos) forms a perfect circle
    float dist = length(vNormalizedPos);

    float radius = 0.98;
    float edgeSmooth = 0.02;
    float alpha = 1.0 - smoothstep(radius - edgeSmooth, radius, dist);

    if (alpha <= 0.0) {
        discard;
    }

    vec4 baseColor = texture2D(baseTexture, vTexCoord1);
    vec4 overlayColor = texture2D(overlayTexture, vTexCoord2);
    vec4 color = mix(baseColor, overlayColor, overlayColor.a);

    gl_FragColor = vec4(color.rgb, color.a * alpha);
}
