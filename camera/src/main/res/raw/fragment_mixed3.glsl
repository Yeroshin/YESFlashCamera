#version 310 es
#extension GL_OES_EGL_image_external : require
precision mediump float; // Устанавливаем точность для float

// Входящие переменные
in vec2 vTexCoord1; // Координаты текстурирования первой текстуры
in vec2 vTexCoord2; // Координаты текстурирования второй текстуры

// Унформы
uniform samplerExternalOES baseTexture; // Основная текстура
uniform sampler2D overlayTexture; // Наложенная текстура


// Выходной цвет фрагмента
out vec4 fragColor;

void main() {
    // Получаем цвет из основной текстуры
    vec4 baseColor = texture(baseTexture, vTexCoord1); // Для samplerExternalOES это может быть проблемой

    // Получаем цвет из наложенной текстуры
    vec4 overlayColor = texture(overlayTexture, vTexCoord2);

    // Смешиваем цвета с учетом альфа-канала
    fragColor = mix(baseColor, overlayColor, overlayColor.a);
}