package com.yes.camera.presentation.ui.adapter

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yes.camera.presentation.model.ShutterItemUI

class ShutterValueItemAdapterDelegate : AdapterDelegate<ShutterItemUI> {
    @Composable
    override fun Content(
        item: ShutterItemUI
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier

               // .size(48.dp)
                .width(48.dp)
                  .wrapContentHeight()
                /* .background(
                     if (item.text.toInt() % 2 == 0) {
                         Color.LightGray
                     } else {
                         Color.DarkGray
                     }

                 )*/

               // .padding(4.dp),
        ) {
            Text(
                textAlign = TextAlign.Center,


                text = item.text,
                style = TextStyle(
                    fontSize = 14.sp,
                    color = if (item.passed) {
                        Color.Green
                    } else {
                        Color.White
                    },
                    shadow = Shadow(
                        color = Color.DarkGray,
                        offset = Offset(5.0f, 5.0f),
                        blurRadius = 5f
                    )
                )

            )
            Text(
                textAlign = TextAlign.Center,
              /*  modifier = Modifier
                    .background(
                        if (item.text.toInt() % 2 == 0) {
                            Color.LightGray
                        } else {
                            Color.DarkGray
                        }

                    ),*/

                text = "|",
                style = TextStyle(
                    fontSize = 14.sp,
                    color = if (item.passed) {
                        Color.Green
                    } else {
                        Color.White
                    },
                    shadow = Shadow(
                        color = Color.DarkGray,
                        offset = Offset(5.0f, 5.0f),
                        blurRadius = 5f
                    )
                )


            )

        }


    }
}