package com.yes.flashcamera.presentation.ui

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yes.flashcamera.R


@Composable
fun RadioGroup(
    radioOptions: List<Int> = listOf(1),
    onOptionSelected:((value:Int?)->Unit),
) {

    val selectedOption = remember { mutableStateOf<Int?>(null) }
    Row(
        Modifier
            .selectableGroup()
           // .height(120.dp)
            .fillMaxWidth(),
         verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        radioOptions.forEach { value ->
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
        }
    }
}