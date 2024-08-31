package com.yes.flashcamera.presentation.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.focusModifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yes.flashcamera.R

class RadioItem(
    id:Int,
    private val title:String,
    var resId:Int,
):RadioButton(id) {
    @Composable
    override fun item() {

            /*  Image(
                  modifier = Modifier
                      .size(50.dp)
                      ,
                  painter = painterResource(
                      id = R.drawable.camera
                  ),
                  contentDescription = null,
              )*/
        Text(
            text = title,
            style = TextStyle(
                color = Color.White,
                fontSize = 14.sp,
                shadow = Shadow(
                    color = Color.DarkGray,
                    offset = Offset(5.0f, 5.0f),
                    blurRadius = 5f
                )
            )
            // style = MaterialTheme.typography.body1.merge(),
            //  modifier = Modifier.padding(start = 16.dp)
        )
            VectorShadow(
                Modifier
                    .size(24.dp)

                ,
                vectorColor = Color.White,
                shadowColor = Color.DarkGray,
                resId = resId
            )

        }

}