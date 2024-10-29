precision mediump float; // Устанавливаем точность для float

varying vec2 vTexCoord1; // Получаем координаты текстурирования первой текстуры
varying vec2 vTexCoord2; // Получаем координаты текстурирования второй текстуры

uniform samplerExternalOES baseTexture;    // Основная текстура
uniform sampler2D overlayTexture;  // Наложенная текстура
uniform float overlayAlpha;         // Альфа-канал наложенной текстуры

void main()
{
    // Получаем цвет из основной текстуры
    vec4 baseColor = texture2D(baseTexture, vTexCoord1);

    // Получаем цвет из наложенной текстуры
    vec4 overlayColor = texture2D(overlayTexture, vTexCoord1);

    // Смешиваем цвета с учетом альфа-канала
    //gl_FragColor = mix(baseColor, overlayColor, overlayColor.a * overlayAlpha);
    gl_FragColor = mix(baseColor,overlayColor,  overlayColor.a);
 // gl_FragColor =texture2D(baseTexture, vTexCoord1);
 // gl_FragColor =texture2D(overlayTexture, vTexCoord1);
}