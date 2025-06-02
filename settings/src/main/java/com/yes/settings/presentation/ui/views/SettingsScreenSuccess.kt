package com.yes.settings.presentation.ui.views

import android.net.Uri
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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

@Composable
fun RadioDialog(
    show: Boolean,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    if (show) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Select option") },
            text = {
                Column {
                    options.forEach { option ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onOptionSelected(option) }
                        ) {
                            RadioButton(
                                selected = option == selectedOption,
                                onClick = { onOptionSelected(option) }
                            )
                            Text(
                                text = option,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = onDismiss) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
fun SettingsScreen(
    onButtonClick: () -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }

    // Доступные варианты выбора
    val options = listOf("Option 1", "Option 2", "Option 3")

    // Текущий выбранный вариант
    var selectedOption by remember { mutableStateOf(options[0]) }

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
                directory=uri.lastPathSegment.toString()
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
                onClick = { onButtonClick() },
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
                        showDialog = true
                    }
            ) {
                Text(
                    text = "Image resolution",
                    fontSize = 24.sp,
                    color = Color.White
                )
                Text(
                    text = "1024 x 768",
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
        }
    }
    RadioDialog(
        show = showDialog,
        options = options,
        selectedOption = selectedOption,
        onOptionSelected = { option ->
            selectedOption = option
            // Можно сразу закрыть диалог при выборе:
            // showDialog = false
        },
        onDismiss = { showDialog = false }
    )
}