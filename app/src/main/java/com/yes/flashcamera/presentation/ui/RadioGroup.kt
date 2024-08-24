package com.yes.flashcamera.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Preview
@Composable
fun RadioGroup(
    radioOptions: List<String> = listOf("Calls", "Missed", "Friends"),
  //  onOptionSelected:((string:String?)->Unit)
) {

    val selectedOption = remember { mutableStateOf<String?>(null) }
    Row(
        Modifier
            .selectableGroup()
            .fillMaxWidth(),
         verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        radioOptions.forEach { text ->
            Column(
                Modifier

                    .height(56.dp)
                    .selectable(
                        selected = (
                                text == selectedOption.value
                                ),
                        onClick = {
                            if (text==selectedOption.value) {
                                selectedOption.value=null
                            } else {
                                selectedOption.value=text
                            }
                        },
                        role = Role.RadioButton
                    )
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                RadioButton(
                    selected = (text == selectedOption.value),
                    onClick = null
                )
                Text(
                    text = text,
                    // style = MaterialTheme.typography.body1.merge(),
                  //  modifier = Modifier.padding(start = 16.dp)
                )
            }
        }
    }
}