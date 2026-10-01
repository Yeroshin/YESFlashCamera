package com.yes.settings.presentation.ui.views
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.yes.settings.R
import com.yes.settings.BuildConfig

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HtmlBody(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .requiredWidth(width = 390.dp)
            .requiredHeight(height = 1131.dp)
            .background(color = Color(0xff131316))
    ) {
        Column(
            modifier = Modifier
                .requiredWidth(width = 390.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = Color(0xff131316))
                    .padding(top = 64.dp)
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp,
                            end = 16.dp,
                            bottom = 48.dp)
                ) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(shape = RoundedCornerShape(32.dp))
                                        .background(color = Color(0xff1f1f22))
                                        .shadow(elevation = 2.dp,
                                            shape = RoundedCornerShape(32.dp))
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(all = 16.dp)
                                    ) {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.Start),
                                            modifier = Modifier
                                                .fillMaxWidth()
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
                                                    contentDescription = "Container",
                                                    colorFilter = ColorFilter.tint(Color(0xffc3c6d0)))
                                            }
                                            Column(
                                                verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.Top),
                                                modifier = Modifier
                                                    .weight(weight = 1f)
                                            ) {
                                                Column(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                ) {
                                                    Text(
                                                        text = "Photo resolution",
                                                        color = Color(0xffe4e1e6),
                                                        lineHeight = 1.5.em,
                                                        style = TextStyle(
                                                            fontSize = 16.sp,
                                                            fontWeight = FontWeight.Medium,
                                                            letterSpacing = 0.15.sp),
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .wrapContentHeight(align = Alignment.CenterVertically))
                                                }
                                                Column(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                ) {
                                                    Text(
                                                        text = "High resolution (50 MP) вЂў 8160 Г—\n6120",
                                                        color = Color(0xffc3c6d0),
                                                        lineHeight = 1.43.em,
                                                        style = TextStyle(
                                                            fontSize = 14.sp,
                                                            letterSpacing = 0.25.sp),
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .wrapContentHeight(align = Alignment.CenterVertically))
                                                }
                                            }
                                            Row(
                                                horizontalArrangement = Arrangement.Center,
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier
                                                    .requiredSize(size = 36.dp)
                                                    .clip(shape = RoundedCornerShape(9999.dp))
                                            ) {
                                                Image(
                                                    painter = painterResource(id = R.drawable.container),
                                                    contentDescription = "Container",
                                                    colorFilter = ColorFilter.tint(Color(0xffc3c6d0)))
                                            }
                                        }
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(top = 16.dp)
                                        ) {
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.Start),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(shape = RoundedCornerShape(9999.dp))
                                                    .background(color = Color(0xff0e0e11))
                                                    .padding(all = 4.dp)
                                            ) {
                                                Row(
                                                    horizontalArrangement = Arrangement.Center,
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier
                                                        .clip(shape = RoundedCornerShape(9999.dp))
                                                        .padding(start = 26.979999542236328.dp,
                                                            end = 27.dp,
                                                            top = 9.dp,
                                                            bottom = 9.dp)
                                                ) {
                                                    Column(
                                                        horizontalAlignment = Alignment.CenterHorizontally
                                                    ) {
                                                        Text(
                                                            text = "12 MP (Standard)",
                                                            color = Color(0xffc3c6d0),
                                                            textAlign = TextAlign.Center,
                                                            lineHeight = 1.33.em,
                                                            style = MaterialTheme.typography.labelMedium,
                                                            modifier = Modifier
                                                                .wrapContentHeight(align = Alignment.CenterVertically))
                                                    }
                                                }
                                                InputChip(
                                                    label = {
                                                        Text(
                                                            text = "50 MP (High-res)",
                                                            color = Color(0xff33537f),
                                                            textAlign = TextAlign.Center,
                                                            lineHeight = 1.33.em,
                                                            style = TextStyle(
                                                                fontSize = 12.sp,
                                                                letterSpacing = 0.5.sp),
                                                            modifier = Modifier
                                                                .wrapContentHeight(align = Alignment.CenterVertically))
                                                    },
                                                    leadingIcon = {
                                                        Image(
                                                            painter = painterResource(id = R.drawable.container),
                                                            contentDescription = "Container",
                                                            colorFilter = ColorFilter.tint(Color(0xff33537f)))
                                                    },
                                                    shape = RoundedCornerShape(9999.dp),
                                                    colors = FilterChipDefaults.filterChipColors(
                                                        containerColor = Color(0xffa8c7fa)
                                                    ),
                                                    selected = true,
                                                    onClick = { },
                                                    modifier = Modifier
                                                        .shadow(elevation = 2.dp))
                                            }
                                        }
                                    }
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .requiredHeight(height = 1.dp)
                                            .padding(start = 56.dp,
                                                end = 16.dp)
                                    ) {
                                        Divider(
                                            color = Color(0xff43474f).copy(alpha = 0.3f),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .requiredHeight(height = 1.dp))
                                    }
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(all = 16.dp)
                                    ) {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.Start),
                                            modifier = Modifier
                                                .fillMaxWidth()
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
                                                    contentDescription = "Container",
                                                    colorFilter = ColorFilter.tint(Color(0xffc3c6d0)))
                                            }
                                            Column(
                                                verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.Top),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                            ) {
                                                Row(
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.Start),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                ) {
                                                    Column() {
                                                        Text(
                                                            text = "Raw / JPEG control",
                                                            color = Color(0xffe4e1e6),
                                                            lineHeight = 1.5.em,
                                                            style = TextStyle(
                                                                fontSize = 16.sp,
                                                                fontWeight = FontWeight.Medium,
                                                                letterSpacing = 0.15.sp),
                                                            modifier = Modifier
                                                                .wrapContentHeight(align = Alignment.CenterVertically))
                                                    }
                                                    Row(
                                                        modifier = Modifier
                                                            .clip(shape = RoundedCornerShape(16.dp))
                                                            .background(color = Color(0xff353438))
                                                            .padding(horizontal = 6.dp,
                                                                vertical = 2.dp)
                                                    ) {
                                                        Text(
                                                            text = "DNG 14-bit",
                                                            color = Color(0xff80cfff),
                                                            lineHeight = 1.45.em,
                                                            style = TextStyle(
                                                                fontSize = 11.sp,
                                                                letterSpacing = 0.5.sp),
                                                            modifier = Modifier
                                                                .wrapContentHeight(align = Alignment.CenterVertically))
                                                    }
                                                }
                                                Column(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                ) {
                                                    Text(
                                                        text = "Save both sensor raw data and\ncompressed image",
                                                        color = Color(0xffc3c6d0),
                                                        lineHeight = 1.43.em,
                                                        style = TextStyle(
                                                            fontSize = 14.sp,
                                                            letterSpacing = 0.25.sp),
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .wrapContentHeight(align = Alignment.CenterVertically))
                                                }
                                            }
                                        }
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(top = 16.dp)
                                        ) {
                                            Column(
                                                verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.Top),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                            ) {
                                                Row(
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.Start),
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clip(shape = RoundedCornerShape(9999.dp))
                                                        .background(color = Color(0xff0e0e11))
                                                        .padding(all = 4.dp)
                                                ) {
                                                    Column(
                                                        verticalArrangement = Arrangement.Center,
                                                        horizontalAlignment = Alignment.CenterHorizontally,
                                                        modifier = Modifier
                                                            .weight(weight = 0.33f)
                                                            .clip(shape = RoundedCornerShape(9999.dp))
                                                            .padding(vertical = 8.dp)
                                                    ) {
                                                        Text(
                                                            text = "JPEG",
                                                            color = Color(0xffc3c6d0),
                                                            textAlign = TextAlign.Center,
                                                            lineHeight = 1.33.em,
                                                            style = MaterialTheme.typography.labelMedium,
                                                            modifier = Modifier
                                                                .wrapContentHeight(align = Alignment.CenterVertically))
                                                    }
                                                    Column(
                                                        verticalArrangement = Arrangement.Center,
                                                        horizontalAlignment = Alignment.CenterHorizontally,
                                                        modifier = Modifier
                                                            .weight(weight = 0.33f)
                                                            .clip(shape = RoundedCornerShape(9999.dp))
                                                            .padding(vertical = 8.dp)
                                                    ) {
                                                        Text(
                                                            text = "RAW",
                                                            color = Color(0xffc3c6d0),
                                                            textAlign = TextAlign.Center,
                                                            lineHeight = 1.33.em,
                                                            style = MaterialTheme.typography.labelMedium,
                                                            modifier = Modifier
                                                                .wrapContentHeight(align = Alignment.CenterVertically))
                                                    }
                                                    InputChip(
                                                        label = {
                                                            Text(
                                                                text = "JPEG + RAW",
                                                                color = Color(0xff33537f),
                                                                textAlign = TextAlign.Center,
                                                                lineHeight = 1.33.em,
                                                                style = TextStyle(
                                                                    fontSize = 12.sp,
                                                                    letterSpacing = 0.5.sp),
                                                                modifier = Modifier
                                                                    .wrapContentHeight(align = Alignment.CenterVertically))
                                                        },
                                                        leadingIcon = {
                                                            Image(
                                                                painter = painterResource(id = R.drawable.container),
                                                                contentDescription = "Container",
                                                                colorFilter = ColorFilter.tint(Color(0xff33537f)))
                                                        },
                                                        shape = RoundedCornerShape(9999.dp),
                                                        colors = FilterChipDefaults.filterChipColors(
                                                            containerColor = Color(0xffa8c7fa)
                                                        ),
                                                        selected = true,
                                                        onClick = { },
                                                        modifier = Modifier
                                                            .weight(weight = 0.33f))
                                                }
                                                Column(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(top = 4.dp)
                                                ) {
                                                    Row(
                                                        horizontalArrangement = Arrangement.spacedBy(68.67.dp, Alignment.Start),
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(horizontal = 4.dp)
                                                    ) {
                                                        Row(
                                                            horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.Start),
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            Image(
                                                                painter = painterResource(id = R.drawable.screen),
                                                                contentDescription = "Container",
                                                                colorFilter = ColorFilter.tint(Color(0xffd3e2ff)))
                                                            Text(
                                                                text = "Buffer size: ~45 MB/shot",
                                                                color = Color(0xffc3c6d0),
                                                                lineHeight = 1.45.em,
                                                                style = MaterialTheme.typography.labelSmall,
                                                                modifier = Modifier
                                                                    .wrapContentHeight(align = Alignment.CenterVertically))
                                                        }
                                                        Column() {
                                                            Text(
                                                                text = "Ultra HDR enabled",
                                                                color = Color(0xff80cfff),
                                                                lineHeight = 1.45.em,
                                                                style = MaterialTheme.typography.labelSmall,
                                                                modifier = Modifier
                                                                    .wrapContentHeight(align = Alignment.CenterVertically))
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(shape = RoundedCornerShape(32.dp))
                                        .background(color = Color(0xff1f1f22))
                                        .padding(all = 16.dp)
                                        .shadow(elevation = 2.dp,
                                            shape = RoundedCornerShape(32.dp))
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.Start),
                                        modifier = Modifier
                                            .fillMaxWidth()
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
                                                contentDescription = "Container",
                                                colorFilter = ColorFilter.tint(Color(0xffc3c6d0)))
                                        }
                                        Column(
                                            verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.Top),
                                            modifier = Modifier
                                                .weight(weight = 1f)
                                        ) {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                            ) {
                                                Text(
                                                    text = "Storage location",
                                                    color = Color(0xffe4e1e6),
                                                    lineHeight = 1.5.em,
                                                    style = TextStyle(
                                                        fontSize = 16.sp,
                                                        fontWeight = FontWeight.Medium,
                                                        letterSpacing = 0.15.sp),
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .wrapContentHeight(align = Alignment.CenterVertically))
                                            }
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                            ) {
                                                Text(
                                                    text = "/DCIM/Camera/ (Internal...)",
                                                    color = Color(0xffc3c6d0),
                                                    lineHeight = 1.43.em,
                                                    style = TextStyle(
                                                        fontSize = 14.sp,
                                                        letterSpacing = 0.25.sp),
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .wrapContentHeight(align = Alignment.CenterVertically))
                                            }
                                        }
                                        Column(
                                            verticalArrangement = Arrangement.Center,
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier
                                                .clip(shape = RoundedCornerShape(9999.dp))
                                                .background(color = Color(0xff353438))
                                                .padding(horizontal = 16.dp,
                                                    vertical = 6.dp)
                                        ) {
                                            Text(
                                                text = "Change",
                                                color = Color(0xffd3e2ff),
                                                textAlign = TextAlign.Center,
                                                lineHeight = 1.33.em,
                                                style = MaterialTheme.typography.labelMedium,
                                                modifier = Modifier
                                                    .wrapContentHeight(align = Alignment.CenterVertically))
                                        }
                                    }
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 16.dp)
                                    ) {
                                        Column(
                                            verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.Top),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(shape = RoundedCornerShape(48.dp))
                                                .background(color = Color(0xff0e0e11))
                                                .padding(all = 16.dp)
                                        ) {
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(123.51.dp, Alignment.Start),
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                            ) {
                                                Column() {
                                                    Text(
                                                        text = "142 GB available",
                                                        color = Color(0xffe4e1e6),
                                                        lineHeight = 1.33.em,
                                                        style = MaterialTheme.typography.labelMedium,
                                                        modifier = Modifier
                                                            .wrapContentHeight(align = Alignment.CenterVertically))
                                                }
                                                Column() {
                                                    Text(
                                                        text = "256 GB total",
                                                        color = Color(0xffc3c6d0),
                                                        lineHeight = 1.33.em,
                                                        style = MaterialTheme.typography.labelMedium,
                                                        modifier = Modifier
                                                            .wrapContentHeight(align = Alignment.CenterVertically))
                                                }
                                            }
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .requiredHeight(height = 10.dp)
                                                    .clip(shape = RoundedCornerShape(9999.dp))
                                                    .background(color = Color(0xff353438))
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxHeight()
                                                        .requiredWidth(width = 131.dp)
                                                        .clip(shape = RoundedCornerShape(9999.dp))
                                                        .background(color = Color(0xffd3e2ff)))
                                            }
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(top = 4.dp)
                                            ) {
                                                Row(
                                                    horizontalArrangement = Arrangement.spacedBy(33.17.dp, Alignment.Start),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                ) {
                                                    Column() {
                                                        Text(
                                                            text = "Approx. 3,155 shots in RAW+JPEG",
                                                            color = Color(0xffc3c6d0),
                                                            lineHeight = 1.45.em,
                                                            style = MaterialTheme.typography.labelSmall,
                                                            modifier = Modifier
                                                                .wrapContentHeight(align = Alignment.CenterVertically))
                                                    }
                                                    Column() {
                                                        Text(
                                                            text = "55% available",
                                                            color = Color(0xffd3e2ff),
                                                            lineHeight = 1.45.em,
                                                            style = MaterialTheme.typography.labelSmall,
                                                            modifier = Modifier
                                                                .wrapContentHeight(align = Alignment.CenterVertically))
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(shape = RoundedCornerShape(32.dp))
                                        .background(color = Color(0xff1f1f22))
                                        .shadow(elevation = 2.dp,
                                            shape = RoundedCornerShape(32.dp))
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.Start),
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .fillMaxWidth()
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
                                                contentDescription = "Container",
                                                colorFilter = ColorFilter.tint(Color(0xffc3c6d0)))
                                        }
                                        Column(
                                            verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.Top),
                                            modifier = Modifier
                                                .weight(weight = 1f)
                                                .padding(end = 4.dp)
                                        ) {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                            ) {
                                                Text(
                                                    text = "Full screen viewfinder",
                                                    color = Color(0xffe4e1e6),
                                                    lineHeight = 1.5.em,
                                                    style = TextStyle(
                                                        fontSize = 16.sp,
                                                        fontWeight = FontWeight.Medium,
                                                        letterSpacing = 0.15.sp),
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .wrapContentHeight(align = Alignment.CenterVertically))
                                            }
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                            ) {
                                                Text(
                                                    text = "19.5:9 crop preview instead of\nstandard 4:3",
                                                    color = Color(0xffc3c6d0),
                                                    lineHeight = 1.43.em,
                                                    style = TextStyle(
                                                        fontSize = 14.sp,
                                                        letterSpacing = 0.25.sp),
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .wrapContentHeight(align = Alignment.CenterVertically))
                                            }
                                        }
                                        Row(
                                            modifier = Modifier
                                                .requiredWidth(width = 52.dp)
                                                .requiredHeight(height = 32.dp)
                                                .clip(shape = RoundedCornerShape(9999.dp))
                                                .background(color = Color(0xffd3e2ff))
                                                .padding(start = 24.dp,
                                                    end = 4.dp,
                                                    top = 4.dp,
                                                    bottom = 4.dp)
                                        ) {
                                            Row(
                                                horizontalArrangement = Arrangement.Center,
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier
                                                    .requiredSize(size = 24.dp)
                                                    .clip(shape = RoundedCornerShape(9999.dp))
                                                    .background(color = Color(0xff0a315b))
                                                    .shadow(elevation = 2.dp,
                                                        shape = RoundedCornerShape(9999.dp))
                                            ) {
                                                Image(
                                                    painter = painterResource(id = R.drawable.grid),
                                                    contentDescription = "Container",
                                                    colorFilter = ColorFilter.tint(Color(0xffd3e2ff)))
                                            }
                                        }
                                    }
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.Start),
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .fillMaxWidth()
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
                                                painter = painterResource(id = R.drawable.container),
                                                contentDescription = "Container",
                                                colorFilter = ColorFilter.tint(Color(0xffc3c6d0)))
                                        }
                                        Column(
                                            verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.Top),
                                            modifier = Modifier
                                                .weight(weight = 1f)
                                                .padding(end = 4.dp)
                                        ) {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                            ) {
                                                Text(
                                                    text = "Grid and level",
                                                    color = Color(0xffe4e1e6),
                                                    lineHeight = 1.5.em,
                                                    style = TextStyle(
                                                        fontSize = 16.sp,
                                                        fontWeight = FontWeight.Medium,
                                                        letterSpacing = 0.15.sp),
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .wrapContentHeight(align = Alignment.CenterVertically))
                                            }
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                            ) {
                                                Text(
                                                    text = "Tilt level indicator and 3Г—3\ngolden ratio guide",
                                                    color = Color(0xffc3c6d0),
                                                    lineHeight = 1.43.em,
                                                    style = TextStyle(
                                                        fontSize = 14.sp,
                                                        letterSpacing = 0.25.sp),
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .wrapContentHeight(align = Alignment.CenterVertically))
                                            }
                                        }
                                        Row(
                                            modifier = Modifier
                                                .requiredWidth(width = 52.dp)
                                                .requiredHeight(height = 32.dp)
                                                .clip(shape = RoundedCornerShape(9999.dp))
                                                .background(color = Color(0xffd3e2ff))
                                                .padding(start = 24.dp,
                                                    end = 4.dp,
                                                    top = 4.dp,
                                                    bottom = 4.dp)
                                        ) {
                                            Row(
                                                horizontalArrangement = Arrangement.Center,
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier
                                                    .requiredSize(size = 24.dp)
                                                    .clip(shape = RoundedCornerShape(9999.dp))
                                                    .background(color = Color(0xff0a315b))
                                                    .shadow(elevation = 2.dp,
                                                        shape = RoundedCornerShape(9999.dp))
                                            ) {
                                                Image(
                                                    painter = painterResource(id = R.drawable.location),
                                                    contentDescription = "Container",
                                                    colorFilter = ColorFilter.tint(Color(0xffd3e2ff)))
                                            }
                                        }
                                    }
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.Start),
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .fillMaxWidth()
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
                                                painter = painterResource(id = R.drawable.container),
                                                contentDescription = "Container",
                                                colorFilter = ColorFilter.tint(Color(0xffc3c6d0)))
                                        }
                                        Column(
                                            verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.Top),
                                            modifier = Modifier
                                                .weight(weight = 1f)
                                                .padding(end = 4.dp)
                                        ) {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                            ) {
                                                Text(
                                                    text = "Save location",
                                                    color = Color(0xffe4e1e6),
                                                    lineHeight = 1.5.em,
                                                    style = TextStyle(
                                                        fontSize = 16.sp,
                                                        fontWeight = FontWeight.Medium,
                                                        letterSpacing = 0.15.sp),
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .wrapContentHeight(align = Alignment.CenterVertically))
                                            }
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                            ) {
                                                Text(
                                                    text = "Add GPS coordinates to photo\nEXIF metadata",
                                                    color = Color(0xffc3c6d0),
                                                    lineHeight = 1.43.em,
                                                    style = TextStyle(
                                                        fontSize = 14.sp,
                                                        letterSpacing = 0.25.sp),
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .wrapContentHeight(align = Alignment.CenterVertically))
                                            }
                                        }
                                        Row(
                                            modifier = Modifier
                                                .requiredWidth(width = 52.dp)
                                                .requiredHeight(height = 32.dp)
                                                .clip(shape = RoundedCornerShape(9999.dp))
                                                .background(color = Color(0xff353438))
                                                .padding(start = 8.dp,
                                                    end = 28.dp,
                                                    top = 8.dp,
                                                    bottom = 8.dp)
                                        ) {
                                            val checkedState = remember { mutableStateOf(true) }
                                            Checkbox(
                                                checked = checkedState.value,
                                                onCheckedChange = { checkedState.value = it },
                                                modifier = Modifier
                                                    .shadow(elevation = 2.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                        ) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.Top),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .fillMaxWidth()
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(shape = RoundedCornerShape(9999.dp))
                                        .background(color = Color(0xff1f1f22))
                                        .padding(horizontal = 16.dp,
                                            vertical = 14.dp)
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.container),
                                        contentDescription = "Container",
                                        colorFilter = ColorFilter.tint(Color(0xffffb4ab)))
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "Reset camera settings",
                                            color = Color(0xffffb4ab),
                                            textAlign = TextAlign.Center,
                                            lineHeight = 1.43.em,
                                            style = MaterialTheme.typography.labelLarge,
                                            modifier = Modifier
                                                .wrapContentHeight(align = Alignment.CenterVertically))
                                    }
                                }
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.Start),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column() {
                                        Text(
                                            text = "YES Flash Camera",
                                            color = Color(0xffc3c6d0).copy(alpha = 0.6f),
                                            lineHeight = 1.45.em,
                                            style = MaterialTheme.typography.labelSmall,
                                            modifier = Modifier
                                                .wrapContentHeight(align = Alignment.CenterVertically))
                                    }
                                    Column() {
                                        Text(
                                            text = "•",
                                            color = Color(0xffc3c6d0).copy(alpha = 0.6f),
                                            lineHeight = 1.45.em,
                                            style = MaterialTheme.typography.labelSmall,
                                            modifier = Modifier
                                                .wrapContentHeight(align = Alignment.CenterVertically))
                                    }
                                    Column() {
                                        val currentVersion = BuildConfig.VERSION_NAME
                                        Text(
                                            text = "Version $currentVersion",
                                            color = Color(0xffc3c6d0).copy(alpha = 0.6f),
                                            lineHeight = 1.45.em,
                                            style = MaterialTheme.typography.labelSmall,
                                            modifier = Modifier
                                                .wrapContentHeight(align = Alignment.CenterVertically))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        TopAppBar(
            title = {
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
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.container),
                                    contentDescription = "Container",
                                    colorFilter = ColorFilter.tint(Color(0xffe4e1e6)))
                            }
                            Column() {
                                Text(
                                    text = "Camera Settings",
                                    color = Color(0xffe4e1e6),
                                    lineHeight = 1.33.em,
                                    style = TextStyle(
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Medium),
                                    modifier = Modifier
                                        .wrapContentHeight(align = Alignment.CenterVertically))
                            }
                        }
                    })
            },
            modifier = Modifier
                .shadow(elevation = 8.dp))
    }
}

@Preview(widthDp = 390, heightDp = 1131)
@Composable
private fun HtmlBodyPreview() {
    HtmlBody(Modifier)
}