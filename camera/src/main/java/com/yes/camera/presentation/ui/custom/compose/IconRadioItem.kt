package com.yes.camera.presentation.ui.custom.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
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

import com.yes.camera.presentation.model.RadioGroupItem

@Composable
fun createIconRadioItem(
    id: RadioGroupItem,
     resId: Int?,
) : RadioUiItem<RadioGroupItem> {
   // var resId by mutableStateOf(resId)

     /*  resId?.let {
           Text(
               textAlign = TextAlign.Start,
               text ="hell",
               style = TextStyle(
                   color = Color.White,
                   fontSize = 8.sp,
                   shadow = Shadow(
                       color = Color.DarkGray,
                       offset = Offset(5.0f, 5.0f),
                       blurRadius = 5f
                   )
               )
           )
           VectorShadow(
               Modifier
                   // .alpha(0.3f)
                   .size(24.dp),
               vectorColor = Color.White,
               shadowColor = Color.DarkGray,
               resId = it
           )
       }*/
     return RadioUiItem(id = id) { selected ->
         resId?.let {
           VectorShadow(
               Modifier
                   // .alpha(0.3f)
                   .size(24.dp),
               vectorColor = if(selected)
                   Color.Green
               else
                   Color.White,
               shadowColor = Color.DarkGray,
               resId = it
           )
       }
       /////////////
      /*  Column(
            //  modifier = Modifier
            horizontalAlignment = Alignment.CenterHorizontally

        ) {
            Text(
                textAlign = TextAlign.Start,
                text ="hell",
                style = TextStyle(
                    color = Color.White,
                    fontSize = 8.sp,
                    shadow = Shadow(
                        color = Color.DarkGray,
                        offset = Offset(5.0f, 5.0f),
                        blurRadius = 5f
                    )
                )
            )
           /* Box(
                modifier = Modifier
                    .size(20.dp)
            ){*/
              /*  resId?.let {
                    VectorShadow(
                        Modifier
                           // .alpha(0.3f)
                            .size(24.dp),
                        vectorColor = Color.White,
                        shadowColor = Color.DarkGray,
                        resId = it
                    )
                }*/
           // }

        }*/
    }
}
@Composable
fun temp(){
    Column(
        //  modifier = Modifier
        horizontalAlignment = Alignment.CenterHorizontally

    ) {
    Text(
        textAlign = TextAlign.Start,
        text = "title",
        style = TextStyle(
            color = Color.White,
            fontSize = 8.sp,
            shadow = Shadow(
                color = Color.DarkGray,
                offset = Offset(5.0f, 5.0f),
                blurRadius = 5f
            )
        )
    )
        }
}