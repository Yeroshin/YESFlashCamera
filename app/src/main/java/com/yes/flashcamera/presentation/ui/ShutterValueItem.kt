package com.yes.flashcamera.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yes.flashcamera.presentation.model.ShutterItemUI

class ShutterValueItemAdapterDelegate: AdapterDelegate<ShutterItemUI> {
    @Composable
    override fun Content(
        item:  ShutterItemUI
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier

                .size(48.dp)
              //  .wrapContentHeight()
                .background(
                    if (item.text.toInt() % 2 == 0) {
                        Color.LightGray
                    } else {
                        Color.DarkGray
                    }

                )
                .padding(4.dp),
        ){
            Text(
                textAlign = TextAlign.Center,


                text = item.text,
                style = TextStyle(
                    fontSize = 14.sp,
                    color = if (item.passed){
                        Color.Green
                    }else{
                        Color.White
                    }
                )

            )
            Text(
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .background(
                        if (item.text.toInt() % 2 == 0) {
                            Color.LightGray
                        } else {
                            Color.DarkGray
                        }

                    ),

                text = "|",
                style = TextStyle(
                    fontSize = 14.sp,
                    color = if (item.passed){
                        Color.Green
                    }else{
                        Color.White
                    }
                )

            )
        }



    }
}