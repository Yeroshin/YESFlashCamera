package com.yes.camera.presentation.ui.custom.compose

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

abstract class RadioButton(val id:Int){

    @Composable
    abstract fun item(

    )
}

@Composable
fun RadioGroup(
    modifier: Modifier,
    onOptionSelected:((value:Int?)->Unit),
    items:List<RadioButton>
) {

    val selectedOption = remember { mutableStateOf<Int?>(null) }

    val visibleStates = remember { items.map { mutableStateOf(false) } }

    LaunchedEffect(Unit) {
        items.forEachIndexed { index, _ ->
            delay(index * 150L)
            visibleStates[index].value = true
        }
    }
    Row(
        modifier
            .background(
                Color.LightGray.copy(alpha = 0.5f)
            )
            .selectableGroup()
            .wrapContentHeight()
            .padding(4.dp)
            // .height(120.dp)
            .fillMaxWidth(),
         verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        items.forEachIndexed { index, item ->
            Box(
                modifier = Modifier
                    .width(100.dp)
                    .wrapContentHeight(),
                contentAlignment = Alignment.Center

            ) {
                this@Row.AnimatedVisibility(
                    visible = visibleStates[index].value,
                    enter = fadeIn() + scaleIn()
                ) {
                    Column(
                        Modifier
                            .alpha(
                                if (item.id == selectedOption.value) {
                                    1.0f
                                } else {
                                    0.5f
                                }
                            )

                            //.width(80.dp)
                            // .fillMaxHeight()

                          //  .wrapContentWidth()
                         //   .wrapContentHeight()
                            //  .height(60.dp)
                            .selectable(
                                selected = (item.id == selectedOption.value),
                                onClick = {
                                    if (item.id == selectedOption.value) {
                                        selectedOption.value = null
                                        onOptionSelected(null)
                                    } else {
                                        selectedOption.value = item.id
                                        onOptionSelected(item.id)
                                    }
                                },
                                role = Role.RadioButton
                            )
                            .padding(horizontal = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        /*  RadioButton(
                                  selected = (text == selectedOption.value),
                                  onClick = null
                              )*/
                        item.item(

                        )
                    }
                }
            }
        }
      /*  radioOptions.forEach { value ->
            Column(
                Modifier
                    .fillMaxHeight()
                    // .height(120.dp)
                    .selectable(
                        selected = (value == selectedOption.value),
                        onClick = {
                            if (value == selectedOption.value) {
                                selectedOption.value = null
                                onOptionSelected(null)
                            } else {
                                selectedOption.value = value
                                onOptionSelected(value)
                            }
                        },
                        role = Role.RadioButton
                    )
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
              /*  RadioButton(
                    selected = (text == selectedOption.value),
                    onClick = null
                )*/
                items[0].item( )
                Column {
                  /*  Image(
                        modifier = Modifier
                            .size(50.dp)
                            ,
                        painter = painterResource(
                            id = R.drawable.camera
                        ),
                        contentDescription = null,
                    )*/
                    VectorShadow(R.drawable.camera)
                    Text(
                        text = value.toString(),
                        // style = MaterialTheme.typography.body1.merge(),
                        //  modifier = Modifier.padding(start = 16.dp)
                    )
                }

            }
        }*/
    }
}