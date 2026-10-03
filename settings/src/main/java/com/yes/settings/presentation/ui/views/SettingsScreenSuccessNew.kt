package com.yes.settings.presentation.ui.views

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.yes.settings.BuildConfig
import com.yes.settings.R
import com.yes.settings.presentation.model.SettingsUI

/**
 * Оптимизированный оберточный класс списка для предотвращения лишних рекомпозиций в Compose.
 */
@Stable
data class ImmutableCollection<T>(
    val list: List<T> = emptyList()
)

/**
 * Полный функциональный экран настроек с категориями, сеткой, геометками и MVI-архитектурой.
 *
 * @param settingsUI MVI модель настроек [SettingsUI].
 * @param onBackClick Колбэк для возврата на предыдущий экран.
 * @param onSettingsChanged Колбэк обновления настроек в ViewModel.
 * @param modifier Модификатор макета.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreenSuccessNew(
    settingsUI: SettingsUI,
    onBackClick: () -> Unit,
    onSettingsChanged: (SettingsUI) -> Unit,
    modifier: Modifier = Modifier
) {
    var gridEnabled by remember {
        mutableStateOf(true)
    }

    var saveLocationEnabled by remember {
        mutableStateOf(false)
    }

    val dirPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree(),
        onResult = { uri: Uri? ->
            uri?.let {
                val path = it.toString()
                onSettingsChanged(settingsUI.copy(storagePath = path))
            }
        }
    )

    val formatUpper = settingsUI.imgFormatValue.uppercase()
    val formatBadgeText = when {
        formatUpper.contains("JPEG+RAW") || formatUpper.contains("JPEG + RAW") -> stringResource(R.string.format_badge_jpeg_dng)
        formatUpper.contains("RAW") -> stringResource(R.string.format_badge_dng_14bit)
        else -> stringResource(R.string.format_badge_jpeg_8bit)
    }
    val formatDescText = when {
        formatUpper.contains("JPEG+RAW") || formatUpper.contains("JPEG + RAW") -> stringResource(R.string.format_desc_jpeg_dng)
        formatUpper.contains("RAW") -> stringResource(R.string.format_desc_dng)
        else -> stringResource(R.string.format_desc_jpeg)
    }
    val bufferSizeText = settingsUI.bufferSizeText

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(color = Color(0xff131316))
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Верхний заголовок экрана с кнопкой возврата назад
            TopAppBar(
                title = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.Start),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .requiredSize(size = 44.dp)
                                .clip(shape = RoundedCornerShape(9999.dp))
                                .clickable { onBackClick() }
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.arrow_back),
                                contentDescription = stringResource(R.string.cd_back),
                                colorFilter = ColorFilter.tint(Color(0xffe4e1e6))
                            )
                        }
                        Text(
                            text = stringResource(R.string.camera_settings_title),
                            color = Color(0xffe4e1e6),
                            lineHeight = 1.33.em,
                            style = TextStyle(
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            modifier = Modifier.wrapContentHeight(align = Alignment.CenterVertically)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xff131316)
                ),
                modifier = Modifier.shadow(elevation = 8.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = Color(0xff131316))
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 48.dp)
                ) {
                    // КАРТОЧКА 1: Качество съемки и формат файлов (Photo Resolution & Raw/JPEG Control)
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(shape = RoundedCornerShape(32.dp))
                                    .background(color = Color(0xff1f1f22))
                            ) {
                                // Категория 1.1: Разрешение снимков (Photo resolution)
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(all = 16.dp)
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.Start),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            horizontalArrangement = Arrangement.Center,
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier
                                                .requiredSize(size = 40.dp)
                                                .clip(shape = RoundedCornerShape(9999.dp))
                                                .background(color = Color(0xff2a2a2d))
                                        ) {
                                            Image(
                                                painter = painterResource(id = R.drawable.camera),
                                                contentDescription = stringResource(R.string.cd_resolution),
                                                colorFilter = ColorFilter.tint(Color(0xffc3c6d0))
                                            )
                                        }
                                        Column(
                                            verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.Top),
                                            modifier = Modifier.weight(weight = 1f)
                                        ) {
                                            Text(
                                                text = stringResource(R.string.photo_resolution_title),
                                                color = Color(0xffe4e1e6),
                                                lineHeight = 1.5.em,
                                                style = TextStyle(
                                                    fontSize = 16.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    letterSpacing = 0.15.sp
                                                ),
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                            Text(
                                                text = if (settingsUI.resolutionValue.isNotEmpty()) settingsUI.resolutionValue else stringResource(R.string.standard_resolution),
                                                color = Color(0xffc3c6d0),
                                                lineHeight = 1.43.em,
                                                style = TextStyle(
                                                    fontSize = 14.sp,
                                                    letterSpacing = 0.25.sp
                                                ),
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        }
                                    }

                                    // Выпадающий список выбора разрешения фотографий (ExposedDropdownMenuBox)
                                    var resolutionExpanded by remember { mutableStateOf(false) }
                                    ExposedDropdownMenuBox(
                                        expanded = resolutionExpanded,
                                        onExpandedChange = { resolutionExpanded = it },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 16.dp)
                                    ) {
                                        Row(
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier
                                                .menuAnchor()
                                                .fillMaxWidth()
                                                .clip(shape = RoundedCornerShape(16.dp))
                                                .background(color = Color(0xff0e0e11))
                                                .padding(horizontal = 16.dp, vertical = 14.dp)
                                        ) {
                                            Text(
                                                text = if (settingsUI.resolutionValue.isNotEmpty()) settingsUI.resolutionValue else (settingsUI.resolutionItems.list.firstOrNull() ?: stringResource(R.string.select_resolution)),
                                                color = Color(0xffd3e2ff),
                                                style = TextStyle(
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    letterSpacing = 0.25.sp
                                                )
                                            )
                                            Image(
                                                painter = painterResource(id = R.drawable.arrow_back),
                                                contentDescription = stringResource(R.string.cd_expand),
                                                colorFilter = ColorFilter.tint(Color(0xffc3c6d0)),
                                                modifier = Modifier
                                                    .requiredSize(20.dp)
                                                    .graphicsLayer(rotationZ = 270f)
                                            )
                                        }

                                        ExposedDropdownMenu(
                                            expanded = resolutionExpanded,
                                            onDismissRequest = { resolutionExpanded = false },
                                            modifier = Modifier
                                                .background(color = Color(0xff1f1f22))
                                        ) {
                                            val resList = settingsUI.resolutionItems.list.ifEmpty {
                                                listOf(
                                                    "12 MP (4000x3000)",
                                                    "16 MP (4608x3456)",
                                                    "24 MP (5632x4224)",
                                                    "32 MP (6560x4928)",
                                                    "48 MP (8000x6000)",
                                                    "50 MP (8192x6144)",
                                                    "64 MP (9248x6936)",
                                                    "108 MP (12000x9000)",
                                                    "12 MP",
                                                    "50 MP"
                                                )
                                            }
                                            resList.forEach { resItem ->
                                                DropdownMenuItem(
                                                    text = {
                                                        Text(
                                                            text = resItem,
                                                            color = if (resItem == settingsUI.resolutionValue) Color(0xffd3e2ff) else Color(0xffc3c6d0),
                                                            style = TextStyle(fontSize = 14.sp)
                                                        )
                                                    },
                                                    onClick = {
                                                        onSettingsChanged(settingsUI.copy(resolutionValue = resItem))
                                                        resolutionExpanded = false
                                                    },
                                                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                                                )
                                            }
                                        }
                                    }
                                }

                                HorizontalDivider(
                                    color = Color(0xff43474f).copy(alpha = 0.3f),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 56.dp, end = 16.dp)
                                )

                                // Категория 1.2: Формат сырых данных (Raw / JPEG control)
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(all = 16.dp)
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.Start),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            horizontalArrangement = Arrangement.Center,
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier
                                                .requiredSize(size = 40.dp)
                                                .clip(shape = RoundedCornerShape(9999.dp))
                                                .background(color = Color(0xff2a2a2d))
                                        ) {
                                            Image(
                                                painter = painterResource(id = R.drawable.film),
                                                contentDescription = stringResource(R.string.cd_format),
                                                colorFilter = ColorFilter.tint(Color(0xffc3c6d0))
                                            )
                                        }
                                        Column(
                                            verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.Top),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.Start),
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text(
                                                    text = stringResource(R.string.raw_jpeg_control_title),
                                                    color = Color(0xffe4e1e6),
                                                    lineHeight = 1.5.em,
                                                    style = TextStyle(
                                                        fontSize = 16.sp,
                                                        fontWeight = FontWeight.Medium,
                                                        letterSpacing = 0.15.sp
                                                    )
                                                )
                                                Row(
                                                    modifier = Modifier
                                                        .clip(shape = RoundedCornerShape(16.dp))
                                                        .background(color = Color(0xff353438))
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    Text(
                                                        text = formatBadgeText,
                                                        color = Color(0xff80cfff),
                                                        lineHeight = 1.45.em,
                                                        style = TextStyle(
                                                            fontSize = 11.sp,
                                                            letterSpacing = 0.5.sp
                                                        )
                                                    )
                                                }
                                            }
                                            Text(
                                                text = formatDescText,
                                                color = Color(0xffc3c6d0),
                                                lineHeight = 1.43.em,
                                                style = TextStyle(
                                                    fontSize = 14.sp,
                                                    letterSpacing = 0.25.sp
                                                )
                                            )
                                        }
                                    }

                                    // Выпадающий список выбора формата изображения (ExposedDropdownMenuBox)
                                    var formatExpanded by remember { mutableStateOf(false) }
                                    ExposedDropdownMenuBox(
                                        expanded = formatExpanded,
                                        onExpandedChange = { formatExpanded = it },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 16.dp)
                                    ) {
                                        Row(
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier
                                                .menuAnchor()
                                                .fillMaxWidth()
                                                .clip(shape = RoundedCornerShape(16.dp))
                                                .background(color = Color(0xff0e0e11))
                                                .padding(horizontal = 16.dp, vertical = 14.dp)
                                        ) {
                                            Text(
                                                text = if (settingsUI.imgFormatValue.isNotEmpty()) settingsUI.imgFormatValue else (settingsUI.imgFormatItems.list.firstOrNull() ?: "JPEG"),
                                                color = Color(0xffd3e2ff),
                                                style = TextStyle(
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    letterSpacing = 0.25.sp
                                                )
                                            )
                                            Image(
                                                painter = painterResource(id = R.drawable.arrow_back),
                                                contentDescription = stringResource(R.string.cd_expand),
                                                colorFilter = ColorFilter.tint(Color(0xffc3c6d0)),
                                                modifier = Modifier
                                                    .requiredSize(20.dp)
                                                    .graphicsLayer(rotationZ = 270f)
                                            )
                                        }

                                        ExposedDropdownMenu(
                                            expanded = formatExpanded,
                                            onDismissRequest = { formatExpanded = false },
                                            modifier = Modifier
                                                .background(color = Color(0xff1f1f22))
                                        ) {
                                            val formatList = settingsUI.imgFormatItems.list.ifEmpty {
                                                listOf("JPEG", "RAW", "JPEG+RAW")
                                            }
                                            formatList.forEach { fmtItem ->
                                                DropdownMenuItem(
                                                    text = {
                                                        Text(
                                                            text = fmtItem,
                                                            color = if (fmtItem == settingsUI.imgFormatValue) Color(0xffd3e2ff) else Color(0xffc3c6d0),
                                                            style = TextStyle(fontSize = 14.sp)
                                                        )
                                                    },
                                                    onClick = {
                                                        onSettingsChanged(settingsUI.copy(imgFormatValue = fmtItem))
                                                        formatExpanded = false
                                                    },
                                                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                                                )
                                            }
                                        }
                                    }

                                    // Дополнительная инфо-строка
                                    Row(
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 8.dp, start = 4.dp, end = 4.dp)
                                    ) {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.Start),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Image(
                                                painter = painterResource(id = R.drawable.info),
                                                contentDescription = stringResource(R.string.cd_info),
                                                colorFilter = ColorFilter.tint(Color(0xffd3e2ff))
                                            )
                                            Text(
                                                text = bufferSizeText,
                                                color = Color(0xffc3c6d0),
                                                style = MaterialTheme.typography.labelSmall
                                            )
                                        }
                                        Text(
                                            text = stringResource(R.string.ultra_hdr_enabled),
                                            color = Color(0xff80cfff),
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // КАРТОЧКА 2: Место хранения (Storage location)
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(shape = RoundedCornerShape(32.dp))
                                    .background(color = Color(0xff1f1f22))
                                    .padding(all = 16.dp)
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.Start),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .requiredSize(size = 40.dp)
                                            .clip(shape = RoundedCornerShape(9999.dp))
                                            .background(color = Color(0xff2a2a2d))
                                    ) {
                                        Image(
                                            painter = painterResource(id = R.drawable.card),
                                            contentDescription = stringResource(R.string.cd_storage),
                                            colorFilter = ColorFilter.tint(Color(0xffc3c6d0))
                                        )
                                    }
                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.Top),
                                        modifier = Modifier.weight(weight = 1f)
                                    ) {
                                        Text(
                                            text = stringResource(R.string.storage_location_title),
                                            color = Color(0xffe4e1e6),
                                            lineHeight = 1.5.em,
                                            style = TextStyle(
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Medium,
                                                letterSpacing = 0.15.sp
                                            )
                                        )
                                        Text(
                                            text = settingsUI.storagePath,
                                            color = Color(0xffc3c6d0),
                                            lineHeight = 1.43.em,
                                            style = TextStyle(
                                                fontSize = 14.sp,
                                                letterSpacing = 0.25.sp
                                            )
                                        )
                                    }
                                    // Кнопка изменения папки хранения
                                    Column(
                                        verticalArrangement = Arrangement.Center,
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier
                                            .clip(shape = RoundedCornerShape(9999.dp))
                                            .background(color = Color(0xff353438))
                                            .clickable { dirPickerLauncher.launch(null) }
                                            .padding(horizontal = 16.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = stringResource(R.string.change_button),
                                            color = Color(0xffd3e2ff),
                                            textAlign = TextAlign.Center,
                                            style = MaterialTheme.typography.labelMedium
                                        )
                                    }
                                }

                                // Индикатор доступной памяти
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 16.dp)
                                        .clip(shape = RoundedCornerShape(24.dp))
                                        .background(color = Color(0xff0e0e11))
                                        .padding(all = 16.dp)
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = settingsUI.availableStorageText,
                                            color = Color(0xffe4e1e6),
                                            style = MaterialTheme.typography.labelMedium
                                        )
                                        Text(
                                            text = settingsUI.totalStorageText,
                                            color = Color(0xffc3c6d0),
                                            style = MaterialTheme.typography.labelMedium
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 8.dp)
                                            .requiredHeight(height = 10.dp)
                                            .clip(shape = RoundedCornerShape(9999.dp))
                                            .background(color = Color(0xff353438))
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxHeight()
                                                .fillMaxWidth(settingsUI.storageProgress.coerceIn(0f, 1f))
                                                .clip(shape = RoundedCornerShape(9999.dp))
                                                .background(color = Color(0xffd3e2ff))
                                        )
                                    }
                                    Row(
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 6.dp)
                                    ) {
                                        Text(
                                            text = settingsUI.remainingShotsText,
                                            color = Color(0xffc3c6d0),
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                        Text(
                                            text = stringResource(R.string.storage_percent_used, (settingsUI.storageProgress * 100).toInt()),
                                            color = Color(0xffd3e2ff),
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // КАРТОЧКА 3: Видоискатель, Сетка и Геометки (Viewfinder, Grid & Location)
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(shape = RoundedCornerShape(32.dp))
                                    .background(color = Color(0xff1f1f22))
                            ) {
                                // Категория 3.1: Полноэкранный видоискатель (Full screen viewfinder)
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.Start),
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            onSettingsChanged(settingsUI.copy(fullScreen = !settingsUI.fullScreen))
                                        }
                                        .padding(all = 16.dp)
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .requiredSize(size = 40.dp)
                                            .clip(shape = RoundedCornerShape(9999.dp))
                                            .background(color = Color(0xff2a2a2d))
                                    ) {
                                        Image(
                                            painter = painterResource(id = R.drawable.screen),
                                            contentDescription = stringResource(R.string.cd_viewfinder),
                                            colorFilter = ColorFilter.tint(Color(0xffc3c6d0))
                                        )
                                    }
                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.Top),
                                        modifier = Modifier.weight(weight = 1f)
                                    ) {
                                        Text(
                                            text = stringResource(R.string.full_screen_viewfinder_title),
                                            color = Color(0xffe4e1e6),
                                            lineHeight = 1.5.em,
                                            style = TextStyle(
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Medium,
                                                letterSpacing = 0.15.sp
                                            )
                                        )
                                        Text(
                                            text = stringResource(R.string.full_screen_viewfinder_desc),
                                            color = Color(0xffc3c6d0),
                                            lineHeight = 1.43.em,
                                            style = TextStyle(
                                                fontSize = 14.sp,
                                                letterSpacing = 0.25.sp
                                            )
                                        )
                                    }
                                    Checkbox(
                                        checked = settingsUI.fullScreen,
                                        onCheckedChange = { isChecked ->
                                            onSettingsChanged(settingsUI.copy(fullScreen = isChecked))
                                        }
                                    )
                                }

                                HorizontalDivider(
                                    color = Color(0xff43474f).copy(alpha = 0.3f),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 56.dp, end = 16.dp)
                                )

                                // Категория 3.2: Сетка и уровень (Grid and level)
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.Start),
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            gridEnabled = !gridEnabled
                                        }
                                        .padding(all = 16.dp)
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .requiredSize(size = 40.dp)
                                            .clip(shape = RoundedCornerShape(9999.dp))
                                            .background(color = Color(0xff2a2a2d))
                                    ) {
                                        Image(
                                            painter = painterResource(id = R.drawable.grid),
                                            contentDescription = stringResource(R.string.cd_grid),
                                            colorFilter = ColorFilter.tint(Color(0xffc3c6d0))
                                        )
                                    }
                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.Top),
                                        modifier = Modifier.weight(weight = 1f)
                                    ) {
                                        Text(
                                            text = stringResource(R.string.grid_and_level_title),
                                            color = Color(0xffe4e1e6),
                                            lineHeight = 1.5.em,
                                            style = TextStyle(
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Medium,
                                                letterSpacing = 0.15.sp
                                            )
                                        )
                                        Text(
                                            text = stringResource(R.string.grid_and_level_desc),
                                            color = Color(0xffc3c6d0),
                                            lineHeight = 1.43.em,
                                            style = TextStyle(
                                                fontSize = 14.sp,
                                                letterSpacing = 0.25.sp
                                            )
                                        )
                                    }
                                    Checkbox(
                                        checked = gridEnabled,
                                        onCheckedChange = { isChecked ->
                                            gridEnabled = isChecked
                                        }
                                    )
                                }

                                HorizontalDivider(
                                    color = Color(0xff43474f).copy(alpha = 0.3f),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 56.dp, end = 16.dp)
                                )

                                // Категория 3.3: Геометки (Save location)
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.Start),
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            saveLocationEnabled = !saveLocationEnabled
                                        }
                                        .padding(all = 16.dp)
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .requiredSize(size = 40.dp)
                                            .clip(shape = RoundedCornerShape(9999.dp))
                                            .background(color = Color(0xff2a2a2d))
                                    ) {
                                        Image(
                                            painter = painterResource(id = R.drawable.location),
                                            contentDescription = stringResource(R.string.cd_location),
                                            colorFilter = ColorFilter.tint(Color(0xffc3c6d0))
                                        )
                                    }
                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.Top),
                                        modifier = Modifier.weight(weight = 1f)
                                    ) {
                                        Text(
                                            text = stringResource(R.string.save_location_title),
                                            color = Color(0xffe4e1e6),
                                            lineHeight = 1.5.em,
                                            style = TextStyle(
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Medium,
                                                letterSpacing = 0.15.sp
                                            )
                                        )
                                        Text(
                                            text = stringResource(R.string.save_location_desc),
                                            color = Color(0xffc3c6d0),
                                            lineHeight = 1.43.em,
                                            style = TextStyle(
                                                fontSize = 14.sp,
                                                letterSpacing = 0.25.sp
                                            )
                                        )
                                    }
                                    Checkbox(
                                        checked = saveLocationEnabled,
                                        onCheckedChange = { isChecked ->
                                            saveLocationEnabled = isChecked
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // КАРТОЧКА 3.5: Тема приложения (App Theme) - расширяемый список
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(shape = RoundedCornerShape(32.dp))
                                    .background(color = Color(0xff1f1f22))
                                    .padding(all = 16.dp)
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.Start),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .requiredSize(size = 40.dp)
                                            .clip(shape = RoundedCornerShape(9999.dp))
                                            .background(color = Color(0xff2a2a2d))
                                    ) {
                                        Image(
                                            painter = painterResource(id = R.drawable.screen),
                                            contentDescription = stringResource(R.string.cd_theme),
                                            colorFilter = ColorFilter.tint(Color(0xffc3c6d0))
                                        )
                                    }
                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.Top),
                                        modifier = Modifier.weight(weight = 1f)
                                    ) {
                                        Text(
                                            text = stringResource(R.string.app_theme_title),
                                            color = Color(0xffe4e1e6),
                                            lineHeight = 1.5.em,
                                            style = TextStyle(
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Medium,
                                                letterSpacing = 0.15.sp
                                            )
                                        )
                                        Text(
                                            text = stringResource(R.string.app_theme_desc),
                                            color = Color(0xffc3c6d0),
                                            lineHeight = 1.43.em,
                                            style = TextStyle(
                                                fontSize = 14.sp,
                                                letterSpacing = 0.25.sp
                                            )
                                        )
                                    }
                                }

                                var themeExpanded by remember { mutableStateOf(false) }
                                ExposedDropdownMenuBox(
                                    expanded = themeExpanded,
                                    onExpandedChange = { themeExpanded = it },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 16.dp)
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .menuAnchor()
                                            .fillMaxWidth()
                                            .clip(shape = RoundedCornerShape(16.dp))
                                            .background(color = Color(0xff0e0e11))
                                            .padding(horizontal = 16.dp, vertical = 14.dp)
                                    ) {
                                        Text(
                                            text = if (settingsUI.themeValue.isNotEmpty()) settingsUI.themeValue else stringResource(R.string.theme_dark_default),
                                            color = Color(0xffd3e2ff),
                                            style = TextStyle(
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Medium,
                                                letterSpacing = 0.25.sp
                                            )
                                        )
                                        Image(
                                            painter = painterResource(id = R.drawable.arrow_back),
                                            contentDescription = stringResource(R.string.cd_expand),
                                            colorFilter = ColorFilter.tint(Color(0xffc3c6d0)),
                                            modifier = Modifier
                                                .requiredSize(20.dp)
                                                .graphicsLayer(rotationZ = 270f)
                                        )
                                    }

                                    ExposedDropdownMenu(
                                        expanded = themeExpanded,
                                        onDismissRequest = { themeExpanded = false },
                                        modifier = Modifier
                                            .background(color = Color(0xff1f1f22))
                                    ) {
                                        val themeList = settingsUI.themeItems.list.ifEmpty {
                                            listOf(
                                                stringResource(R.string.theme_dark_default),
                                                stringResource(R.string.theme_amoled_black),
                                                stringResource(R.string.theme_system_dark),
                                                stringResource(R.string.theme_high_contrast)
                                            )
                                        }
                                        themeList.forEach { themeItem ->
                                            DropdownMenuItem(
                                                text = {
                                                    Text(
                                                        text = themeItem,
                                                        color = if (themeItem == settingsUI.themeValue) Color(0xffd3e2ff) else Color(0xffc3c6d0),
                                                        style = TextStyle(fontSize = 14.sp)
                                                    )
                                                },
                                                onClick = {
                                                    onSettingsChanged(settingsUI.copy(themeValue = themeItem))
                                                    themeExpanded = false
                                                },
                                                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // КАРТОЧКА 4: Сброс настроек и версия приложения
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                        ) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.Top),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(shape = RoundedCornerShape(9999.dp))
                                        .background(color = Color(0xff1f1f22))
                                        .clickable {
                                            onSettingsChanged(
                                                SettingsUI(
                                                    resolutionValue = settingsUI.resolutionItems.list.firstOrNull() ?: "12 MP",
                                                    resolutionItems = settingsUI.resolutionItems,
                                                    fullScreen = true,
                                                    imgFormatValue = settingsUI.imgFormatItems.list.firstOrNull() ?: "JPEG",
                                                    imgFormatItems = settingsUI.imgFormatItems
                                                )
                                            )
                                            gridEnabled = true
                                            saveLocationEnabled = false
                                        }
                                        .padding(horizontal = 16.dp, vertical = 14.dp)
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.reset),
                                        contentDescription = stringResource(R.string.cd_reset),
                                        colorFilter = ColorFilter.tint(Color(0xffffb4ab))
                                    )
                                    Text(
                                        text = stringResource(R.string.reset_camera_settings),
                                        color = Color(0xffffb4ab),
                                        textAlign = TextAlign.Center,
                                        lineHeight = 1.43.em,
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                }

                                // Динамический вывод наименования и версии приложения
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.Start),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = stringResource(R.string.yes_flash_camera),
                                        color = Color(0xffc3c6d0).copy(alpha = 0.6f),
                                        lineHeight = 1.45.em,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                    Text(
                                        text = "•",
                                        color = Color(0xffc3c6d0).copy(alpha = 0.6f),
                                        lineHeight = 1.45.em,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                    Text(
                                        text = stringResource(R.string.app_version_format, BuildConfig.VERSION_NAME),
                                        color = Color(0xffc3c6d0).copy(alpha = 0.6f),
                                        lineHeight = 1.45.em,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(widthDp = 390, heightDp = 1131)
@Composable
private fun SettingsScreenSuccessNewPreview() {
    SettingsScreenSuccessNew(
        settingsUI = SettingsUI(
            resolutionValue = "50 MP",
            resolutionItems = ImmutableCollection(listOf("12 MP", "50 MP")),
            fullScreen = true,
            imgFormatValue = "JPEG + RAW",
            imgFormatItems = ImmutableCollection(listOf("JPEG", "RAW", "JPEG + RAW"))
        ),
        onBackClick = {},
        onSettingsChanged = {}
    )
}
