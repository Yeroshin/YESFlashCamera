package com.yes.camera.presentation.ui.adapter

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
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
import com.yes.camera.presentation.model.SelectorItem
import com.yes.camera.presentation.model.TextItem

/*class TextSelectorItemUI : CompositeAdapter.AdapterDelegate<TextItem> {

    @Composable
    override fun Content(
        item: TextItem,
        modifier: Modifier,
    ) {
        var itemText by remember(item.text) {
            mutableStateOf(item.text)
        }
      //  var rowWidthPx by remember { mutableIntStateOf(0) }
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
            Text(
                modifier = Modifier
                    .padding(2.dp),
                maxLines = 1,
                textAlign = TextAlign.Center,
              //  text = item.text,
                text = itemText,
                style = TextStyle(
                    fontSize = fontSize,
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
                ),
                onTextLayout = { layoutResult ->
                    if (layoutResult.hasVisualOverflow) {
                        val newSize = fontSize.value * 0.95f
                        fontSize = if (newSize.sp >= minFontSize) newSize.sp else minFontSize
                    }
                },

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


}*/

///////////////////////
class TextSelectorItemUI : CompositeAdapter.AdapterDelegate<TextItem> {

    @Composable
    override fun Content(
        item: TextItem,
        isPassed: Boolean,
        modifier: Modifier,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = modifier.wrapContentHeight()
        ) {
            val minFontSize = 8.sp
            val initialFontSize = 16.sp

            // Шрифт сбрасывается только если изменился ID или текст
            var fontSize by remember(item.id, item.text) { mutableStateOf(initialFontSize) }

            val commonColor = if (isPassed) Color.Green else Color.White
            val commonShadow = remember {
                Shadow(
                    color = Color.DarkGray,
                    offset = Offset(5.0f, 5.0f),
                    blurRadius = 5f
                )
            }

            Text(
                modifier = Modifier.padding(2.dp),
                maxLines = 1,
                textAlign = TextAlign.Center,
                text = item.text,
                style = TextStyle(
                    fontSize = fontSize,
                    color = commonColor,
                    shadow = commonShadow
                ),
                onTextLayout = { layoutResult ->
                    if (layoutResult.hasVisualOverflow && fontSize > minFontSize) {
                        val scaledSize = (fontSize.value * 0.9f).sp
                        fontSize = if (scaledSize < minFontSize) minFontSize else scaledSize
                    }
                },
                softWrap = false
            )

            Text(
                text = "|",
                textAlign = TextAlign.Center,
                style = TextStyle(
                    fontSize = 14.sp,
                    color = commonColor,
                    shadow = commonShadow
                )
            )
        }
    }
}

