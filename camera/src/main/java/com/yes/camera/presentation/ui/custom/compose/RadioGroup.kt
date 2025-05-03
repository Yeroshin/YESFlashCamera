package com.yes.camera.presentation.ui.custom.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box


import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width

import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.yes.camera.presentation.model.Item
import com.yes.camera.presentation.model.SettingsItem
import com.yes.camera.presentation.ui.views.ImmutableCollection

@Immutable
abstract class RadioButton(open val id: Item){

    @Composable
    abstract fun item(

    )
}

@Composable
fun RadioGroup(
    modifier: Modifier,
    onOptionSelected:((value:Item?)->Unit),
    items: ImmutableCollection<RadioButton>
  // items:RadioButton
) {
  /*  var items by remember{
        mutableStateOf(items)
    }*/
   val selectedOption = remember { mutableStateOf<Item?>(items.list[0].id) }

  //  val visibleStates = remember { items.map { mutableStateOf(false) } }

  /*  LaunchedEffect(Unit) {
        items.forEachIndexed { index, _ ->
            delay(index * 150L)
            visibleStates[index].value = true
        }
    }*/
    Row(
        modifier = modifier
            /* .background(
                Color.LightGray.copy(alpha = 0.5f)
            )*/

            .selectableGroup(),
           // .wrapContentHeight()
           // .padding(4.dp)
            // .height(120.dp)
           // .fillMaxWidth(),
         verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly
       // horizontalArrangement = Arrangement.Center,
               // horizontalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        items.list.forEachIndexed { index, item ->
            Box(

                modifier = Modifier
                     .wrapContentWidth()
                   // .width(72.dp)
                    // .background(Color.Red)
                    .wrapContentHeight(),
                contentAlignment = Alignment.Center

            ) {
              /*  this@Row.AnimatedVisibility(
                    visible = visibleStates[index].value,
                    enter = fadeIn() + scaleIn()
                ) {*/
                    Row(
                        Modifier
                            .alpha(
                                if (item.id == selectedOption.value) {
                                    1.0f
                                } else {
                                    0.5f
                                }
                            )
                            .selectable(
                                selected = (item.id == selectedOption.value),
                                onClick = {
                                    /* if (item.id == selectedOption.value) {
                                        selectedOption.value = null
                                        onOptionSelected(null)
                                    } else {
                                        selectedOption.value = item.id
                                        onOptionSelected(item.id)
                                    }*/
                                    selectedOption.value = item.id
                                    onOptionSelected(item.id)
                                },
                                role = Role.RadioButton
                            )
                            .padding(horizontal = 8.dp),
                       // horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        item.item()
                    }
                }
            }
        }

}