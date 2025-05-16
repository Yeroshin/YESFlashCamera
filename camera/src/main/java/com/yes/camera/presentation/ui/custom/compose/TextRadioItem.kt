package com.yes.camera.presentation.ui.custom.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.yes.camera.presentation.model.SettingsRadioGroupItem

class TextRadioItem (
    id: SettingsRadioGroupItem,
    var value: String?,
    private val title: String,
    ) : RadioButton(id) {
        @Composable
        override fun item() {
            value?.let {
                Column(
                    //  modifier = Modifier
                    horizontalAlignment = Alignment.Start

                ) {
                    Text(
                        textAlign = TextAlign.Start,
                        text = title,
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
                    val minFontSize: TextUnit = 8.sp
                    val initialFontSize=16.sp
                    var fontSize by remember { mutableStateOf(initialFontSize) }

                    Text(
                        maxLines = 1,
                        text = it,
                        style = TextStyle(
                            color = Color.White,
                            fontSize = fontSize,
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
                }
            }




        }

    }