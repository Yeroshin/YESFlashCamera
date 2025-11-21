package com.yes.camera.presentation.ui.custom.compose

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp

@Composable
fun RecordButton(
    modifier: Modifier,
    isChecked: Boolean,
    onClick: (Boolean) -> Unit ={}
) {

    var checked by remember {
        mutableStateOf(isChecked)
    }



  /*  Button(
        modifier = modifier
        // .size(96.dp)
        /* .toggleable(
             value = isChecked,
             onValueChange = toggleHandler,
             role = Role.Checkbox
         )*/,
        border = BorderStroke(8.dp, Color.White),
        shape = CircleShape,
        onClick = {
            checked=!checked
            onClick(checked)
        }, // Переиспользуем тот же обработчик
        colors = ButtonDefaults.buttonColors(
            containerColor = if (checked) Color.Red else Color.Green
        )
    ) {
        // Ваш контент кнопки
    }*/
   /* Button(
        modifier = Modifier.size(96.dp),
        shape = CircleShape,
        border = BorderStroke(8.dp, Color.White),
        onClick = {
            checked = !checked
            onClick(checked)
        },
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent
        )
    ) {
        // Промежуточная черная окружность с внутренней красной/зеленой окружностью внутри
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // Черная промежуточная окружность (диаметр ~70-80.dp в зависимости от внутренней)
            Surface(
                shape = CircleShape,
                color = Color.Black,
                modifier = Modifier.size(60.dp)  // Пример размера; подгоните под нужную толщину
            ) {
                // Здесь ничего, next будет внутри
            }
            // Внутренняя окружность (красная если checked, зеленая иначе)
            Box(
                modifier = Modifier
                    .size(60.dp)  // Меньше, чем черная, чтобы была видна разница; подгоните
                    .background(
                        color = if (checked) Color.Red else Color.Green,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Ваш контент кнопки (например, Text или Icon)
                // Например: Text("Контент")
            }
        }
    }*/
 /* @Composable
  private fun CustomThreeCircleButton(
      checked: Boolean,
      onClick: (Boolean) -> Unit,
      outerSize: Float = 96f,  // Total diameter in dp, can be any value
      borderWidth: Float = 8f,  // Border width in dp
      middleFraction: Float = 0.75f,  // Fraction of the outer size for the middle circle (0.0 to 1.0)
      innerFraction: Float = 0.5f    // Fraction of the outer size for the inner circle (0.0 to middleFraction)
  ) {*/
    Button(
        modifier = Modifier.size(96.dp),  // Square size for perfect circles
        onClick = {
            checked=!checked
            onClick(checked)
                  },
        colors = ButtonDefaults.buttonColors(containerColor = Color.White)  // Transparent content
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(x = this.size.width / 2f, y = this.size.height / 2f)
            val radiusOuter = size.width / 2f  // Full radius
            val radiusMiddle = radiusOuter * 1.75f  // 75% for middle black circle
            val radiusInner = radiusOuter * 1.55f    // 50% for inner colored circle

            // Outer circle (white border-like, but actually outer edge; since border is handled by implicit shape, we draw transparent or adjust)
            // Note: Button already has transparent color; we draw the circles directly

            // 1. Draw middle circle (black, behind inner)
            drawCircle(color = Color.Black, radius = radiusMiddle, center = center)

            // 2. Draw inner circle (colored based on checked state, on top)
            drawCircle(
                color = if (checked) Color.Red else Color.Green,
                radius = radiusInner,
                center = center
            )

            // 3. Optionally, if you want a visible outer border, add another circle:
            // drawCircle(color = Color.White, radius = radiusOuter, center = center, style = Stroke(width = 8f))

            // The outer "border" is simulated by the button's shape (CircleShape) with transparent background.
            // If you need a thicker outer ring, uncomment and adjust.
        }
    }

}