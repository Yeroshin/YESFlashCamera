package com.yes.camera.presentation.ui.custom.compose

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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



    Button(
        modifier = modifier
        // .size(96.dp)
        /* .toggleable(
             value = isChecked,
             onValueChange = toggleHandler,
             role = Role.Checkbox
         )*/,
        border = BorderStroke(5.dp, Color.Green),
        shape = CircleShape,
        onClick = {
            checked=!checked
            onClick(checked)
        }, // Переиспользуем тот же обработчик
        colors = ButtonDefaults.buttonColors(
            containerColor = if (checked) Color.Red else Color.White
        )
    ) {
        // Ваш контент кнопки
    }
}