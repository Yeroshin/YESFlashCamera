package com.yes.camera.presentation.ui.custom.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box


import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding

import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.yes.camera.presentation.model.Item
import com.yes.camera.presentation.model.WbItem
import com.yes.camera.presentation.ui.views.ImmutableCollection

@Immutable
abstract class RadioButton(open val id: Item) {

    @Composable
    abstract fun item(

    )
}

@Composable
fun RadioGroup(
    modifier: Modifier,
    onOptionSelected: ((value: Item?) -> Unit),
    items: ImmutableCollection<RadioButton>? = null,
    selectedOption: Item? = items?.list?.get(0)?.id,

    ) {
    /*  var items by remember{
          mutableStateOf(items)
      }*/

    var selected by remember(selectedOption) {
        mutableStateOf(selectedOption)
    }

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

        items?.list?.forEachIndexed { index, item ->
            key(item.id) {
                Box(

                    modifier = Modifier
                        .alpha(
                            if (item.id == selected) {
                                1.0f
                            } else {
                                0.5f
                            }
                        )
                        .selectable(
                            selected = (item.id == selected),
                            onClick = {
                               /*  if (item.id == selected) {
                                    selected = null
                                    onOptionSelected(null)
                                } else {
                                    selected = item.id
                                    onOptionSelected(item.id)
                                }*/
                                selected = item.id
                                onOptionSelected(item.id)
                            },
                            role = Role.RadioButton
                        )
                        .padding(horizontal = 8.dp),
                    // horizontalAlignment = Alignment.CenterHorizontally

                 //   . wrapContentWidth ()
                    // .width(72.dp)
                    // .background(Color.Red)
                //    .wrapContentHeight(),
            //    contentAlignment = Alignment.Center

                ){
                item.item()
            }
            }
        }
    }
}

/*   {
       /*  this@Row.AnimatedVisibility(
       visible = visibleStates[index].value,
       enter = fadeIn() + scaleIn()
   ) {*/
       Row(
           Modifier
                .alpha(
                   if (item.id == selected) {
                       1.0f
                   } else {
                       0.5f
                   }
               )
               .selectable(
                   selected = (item.id == selected),
                   onClick = {
                       /* if (item.id == selectedOption.value) {
                           selectedOption.value = null
                           onOptionSelected(null)
                       } else {
                           selectedOption.value = item.id
                           onOptionSelected(item.id)
                       }*/
                       selected = item.id
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
}*/