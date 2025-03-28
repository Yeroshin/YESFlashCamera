package com.yes.camera.presentation.ui.custom.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yes.camera.presentation.model.Item
import org.intellij.lang.annotations.JdkConstants.HorizontalAlignment

class TextRadioItem (
    id: Item,
    var value: String,
    private val title: String,
    ) : RadioButton(id) {
        @Composable
        override fun item() {
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

                Text(
                    text = value,
                    style = TextStyle(
                        color = Color.White,
                        fontSize = 16.sp,
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