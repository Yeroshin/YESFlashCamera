package com.yes.camera.presentation.ui.adapter

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yes.camera.presentation.model.IconItem
import com.yes.camera.presentation.model.SelectorItem
import com.yes.camera.presentation.model.TextSelectorItemUI
import com.yes.camera.presentation.ui.custom.compose.VectorShadow

class IconSelectorItemUI: CompositeAdapter.AdapterDelegate<IconItem> {
    @Composable
    override fun Content(
        item: IconItem,
        modifier: Modifier,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = modifier

                // .size(48.dp)
                // .width(48.dp)
                .wrapContentHeight()
            /*   .onGloballyPositioned { coordinates ->
                   rowWidthPx = coordinates.size.width
               },*/
            /* .background(
                 if (item.text.toInt() % 2 == 0) {
                     Color.LightGray
                 } else {
                     Color.DarkGray
                 }

             )*/

            // .padding(4.dp),
        ) {
            val minFontSize: TextUnit = 8.sp
            val initialFontSize=16.sp
            var fontSize by remember { mutableStateOf(initialFontSize) }
           VectorShadow(
               vectorColor = if (item.passed) {
                   Color.Green
               } else {
                   Color.White
               },
               modifier = Modifier
                   .height(18.dp)
                   .width(24.dp),
               resId =item.icon
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

