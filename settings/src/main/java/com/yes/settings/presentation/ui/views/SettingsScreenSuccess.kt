package com.yes.settings.presentation.ui.views

import android.net.Uri
import android.widget.GridLayout
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yes.settings.R
import com.yes.settings.presentation.model.SettingsUI

@Stable
data class ImmutableCollection<T>(
    val list: List<T>
)

@Composable
fun RadioDialog(
    //   show: Boolean,
    options: ImmutableCollection<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {

    // if (show) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select option") },
        text = {
            Column {
                options.list.forEach { option ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOptionSelected(option) }
                    ) {
                        RadioButton(
                            selected = option == selectedOption,
                            onClick = {
                                onOptionSelected(option)
                            }
                        )
                        Text(
                            text = option,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("OK")
            }
        }
    )
    //  }
}

@Composable
fun SettingsScreenSuccess(
    settingsUI: SettingsUI,
    onBackClick: () -> Unit,
    onSettingsChanged: (SettingsUI) -> Unit
) {
    var settings by remember {
        mutableStateOf(settingsUI)
    }
    LaunchedEffect(settings) {
        onSettingsChanged(settings)
    }
    var showDialog by remember { mutableStateOf(false) }
    var resolutionItems by remember(settings.resolutionItems) {
        mutableStateOf(settings.resolutionItems)
    }
    var resolutionSelected by remember(
        settings.resolutionValue
    ) {
        mutableStateOf(
            settings.resolutionValue
        )
    }
    // Доступные варианты выбора
    /*var options : ImmutableCollection<String> =
        ImmutableCollection(
            listOf(
                "a",
                "b"
            )
        )*/

    var options by remember {
        mutableStateOf(
            ImmutableCollection(
                emptyList<String>()
            )

        )
    }
    // Текущий выбранный вариант
    var selectedOption by remember { mutableStateOf("") }
    var onConfirmAction by remember {
        mutableStateOf({ })
    }

    val context = LocalContext.current
    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }

    var directory by remember {
        mutableStateOf("Dcim/photo")
    }
    // Создаем launcher для выбора файла
    val dirPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree(),
        onResult = { uri ->
            uri?.let {
                directory = uri.lastPathSegment.toString()
            }
        }
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {
            IconButton(
                onClick = { onBackClick() },
                modifier = Modifier.size(48.dp),

                ) {
                Icon(

                    modifier = Modifier.size(48.dp),
                    tint = Color.White,
                    painter = painterResource(id = R.drawable.arrow_back),
                    contentDescription = "Назад" // Обязательно для accessibility
                )
            }
            Spacer(modifier = Modifier.height(48.dp))
            Text(
                text = "Settings",
                fontSize = 32.sp,
                color = Color.White,
            )
            Spacer(modifier = Modifier.height(48.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        options = settings.resolutionItems
                        selectedOption = settings.resolutionValue
                        showDialog = true
                        onConfirmAction = {
                            settings = settings.copy(
                                resolutionValue = selectedOption
                            )
                            //  settings.resolutionValue = selectedOption  // Сброс
                            showDialog = false
                        }
                    }
            ) {
                Text(
                    text = "Image resolution",
                    fontSize = 24.sp,
                    color = Color.White
                )
                Text(
                    text = resolutionSelected,//"1024 x 768",
                    fontSize = 18.sp,
                    color = Color.Green
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        dirPickerLauncher.launch(null)
                    }
            ) {
                Text(
                    text = "Directory",
                    fontSize = 24.sp,
                    color = Color.White
                )
                Text(
                    text = directory,
                    fontSize = 18.sp,
                    color = Color.Green
                )
            }
            Spacer(modifier = Modifier.height(32.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {}
            ) {
                Text(
                    text = "Image format",
                    fontSize = 24.sp,
                    color = Color.White
                )
                Text(
                    text = "Jpeg + RAW",
                    fontSize = 18.sp,
                    color = Color.Green
                )
            }
            Spacer(modifier = Modifier.height(32.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Fullscreen preview",
                    fontSize = 24.sp,
                    color = Color.White
                )
                Spacer(modifier = Modifier.weight(1f))
                var isChecked by remember { mutableStateOf(settings.fullScreen) }
                Checkbox(
                    checked = isChecked,
                    onCheckedChange = {
                        settings = settings.copy(
                            fullScreen = it
                        )
                        isChecked = it
                    },
                    colors = CheckboxDefaults.colors(checkedColor = Color.Green)
                )
            }
        }
    }
    if (showDialog) {
        RadioDialog(
            options = options,
            selectedOption = selectedOption,
            onOptionSelected = { option ->
                selectedOption = option
                // Можно сразу закрыть диалог при выборе:
                // showDialog = false
            },
            onDismiss = { showDialog = false },
            onConfirm = onConfirmAction
        )
    }
    /* RadioDialog(
         show = showDialog,
         options = options,
         selectedOption = selectedOption,
         onOptionSelected = { option ->
             selectedOption = option
             // Можно сразу закрыть диалог при выборе:
             // showDialog = false
         },
         onDismiss = { showDialog = false }
     )*/
}