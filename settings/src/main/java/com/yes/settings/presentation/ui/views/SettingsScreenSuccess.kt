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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import com.yes.settings.R
import com.yes.settings.presentation.model.SettingsUI
import com.yes.shared.presentation.ui.theme.AppTheme
import kotlinx.coroutines.flow.drop

@Stable
data class ImmutableCollection<T>(
    val list: List<T>
)

@Composable
fun RadioDialog(
    options: ImmutableCollection<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val scrollState = rememberScrollState()
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select option") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(scrollState)
            ) {
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
                            modifier = Modifier.padding(start = AppTheme.dimens.medium)
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

    LaunchedEffect(Unit) {
        snapshotFlow { settings }
            .drop(1)
            .collect { newSettings ->
                onSettingsChanged(newSettings)
            }
    }
    var showDialog by remember { mutableStateOf(false) }

    var options by remember {
        mutableStateOf(
            ImmutableCollection(
                emptyList<String>()
            )
        )
    }

    var selectedOption by remember { mutableStateOf("") }
    var onConfirmAction by remember {
        mutableStateOf({ })
    }

    val context = LocalContext.current
    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }

    var directory by remember {
        mutableStateOf("Dcim/photo")
    }

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
            .background(AppTheme.colors.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(AppTheme.dimens.huge),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {
            IconButton(
                onClick = { onBackClick() },
                modifier = Modifier.size(AppTheme.dimens.iconHuge),
            ) {
                Icon(
                    modifier = Modifier.size(AppTheme.dimens.iconHuge),
                    tint = AppTheme.colors.iconPrimary,
                    painter = painterResource(id = R.drawable.arrow_back),
                    contentDescription = "Назад"
                )
            }
            Spacer(modifier = Modifier.height(AppTheme.dimens.giant))
            Text(
                text = "Settings",
                fontSize = AppTheme.dimens.textHeader,
                color = AppTheme.colors.textPrimary,
            )
            Spacer(modifier = Modifier.height(AppTheme.dimens.giant))
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
                            showDialog = false
                        }
                    }
            ) {
                Text(
                    text = "Image resolution",
                    fontSize = AppTheme.dimens.textTitle,
                    color = AppTheme.colors.textPrimary
                )
                Text(
                    text = settings.resolutionValue,
                    fontSize = AppTheme.dimens.textLarge,
                    color = AppTheme.colors.primaryAccent
                )
            }

            Spacer(modifier = Modifier.height(AppTheme.dimens.huge))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        dirPickerLauncher.launch(null)
                    }
            ) {
                Text(
                    text = "Directory",
                    fontSize = AppTheme.dimens.textTitle,
                    color = AppTheme.colors.textPrimary
                )
                Text(
                    text = directory,
                    fontSize = AppTheme.dimens.textLarge,
                    color = AppTheme.colors.primaryAccent
                )
            }
            Spacer(modifier = Modifier.height(AppTheme.dimens.huge))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        options = settings.imgFormatItems
                        selectedOption = settings.imgFormatValue
                        showDialog = true
                        onConfirmAction = {
                            settings = settings.copy(
                                imgFormatValue = selectedOption
                            )
                            showDialog = false
                        }
                    }
            ) {
                Text(
                    text = "Image format",
                    fontSize = AppTheme.dimens.textTitle,
                    color = AppTheme.colors.textPrimary
                )
                Text(
                    text = settings.imgFormatValue,
                    fontSize = AppTheme.dimens.textLarge,
                    color = AppTheme.colors.primaryAccent
                )
            }
            Spacer(modifier = Modifier.height(AppTheme.dimens.huge))
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Fullscreen preview",
                    fontSize = AppTheme.dimens.textTitle,
                    color = AppTheme.colors.textPrimary
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
                    colors = CheckboxDefaults.colors(checkedColor = AppTheme.colors.primaryAccent)
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
            },
            onDismiss = { showDialog = false },
            onConfirm = onConfirmAction
        )
    }
}
