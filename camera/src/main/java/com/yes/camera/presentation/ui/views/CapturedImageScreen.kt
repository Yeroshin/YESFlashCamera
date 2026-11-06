package com.yes.camera.presentation.ui.views

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yes.shared.presentation.ui.theme.FlashCameraTheme

/**
 * Screen that displays a captured photo/frame with gesture scaling (zoom/pan) and two actions: Save and Cancel.
 *
 * @param bitmap Optional captured Bitmap of the shot.
 * @param modifier Layout modifier.
 * @param onSaveClick Callback when the user clicks Save.
 * @param onCancelClick Callback when the user clicks Cancel.
 */
@Composable
fun CapturedImageScreen(
    bitmap: Bitmap? = null,
    modifier: Modifier = Modifier,
    onSaveClick: () -> Unit,
    onCancelClick: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xff131316))
    ) {
        // Captured Image display in center/fullscreen
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 100.dp)
                .clipToBounds(),
            contentAlignment = Alignment.Center
        ) {
            if (bitmap != null) {
                var scale by remember { mutableFloatStateOf(1f) }
                var offset by remember { mutableStateOf(Offset.Zero) }
                var containerSize by remember { mutableStateOf(IntSize.Zero) }

                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Captured Photo",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .onSizeChanged { containerSize = it }
                        .pointerInput(Unit) {
                            detectTransformGestures { _, pan, zoom, _ ->
                                val newScale = (scale * zoom).coerceIn(1f, 5f)
                                scale = newScale

                                if (newScale > 1f) {
                                    val maxOffsetX = (containerSize.width * (newScale - 1f)) / 2f
                                    val maxOffsetY = (containerSize.height * (newScale - 1f)) / 2f
                                    val newOffset = offset + pan
                                    offset = Offset(
                                        x = newOffset.x.coerceIn(-maxOffsetX, maxOffsetX),
                                        y = newOffset.y.coerceIn(-maxOffsetY, maxOffsetY)
                                    )
                                } else {
                                    offset = Offset.Zero
                                }
                            }
                        }
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onDoubleTap = {
                                    if (scale > 1.05f) {
                                        scale = 1f
                                        offset = Offset.Zero
                                    } else {
                                        scale = 2.5f
                                    }
                                }
                            )
                        }
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale,
                            translationX = offset.x,
                            translationY = offset.y
                        ),
                    contentScale = ContentScale.Fit
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xff1f1f22)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No Captured Image Available",
                        color = Color(0xffc3c6d0),
                        fontSize = 16.sp
                    )
                }
            }
        }

        // Bottom Action Bar with Save and Cancel buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(Color(0xff1f1f22).copy(alpha = 0.95f))
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Cancel Button
            Button(
                onClick = onCancelClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xff353438),
                    contentColor = Color(0xffe4e1e6)
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
            ) {
                Text(
                    text = "Отмена / Cancel",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Save Button
            Button(
                onClick = onSaveClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xffd3e2ff),
                    contentColor = Color(0xff0e0e11)
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
            ) {
                Text(
                    text = "Сохранить / Save",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF131316)
@Composable
fun CapturedImageScreenPreview() {
    FlashCameraTheme {
        CapturedImageScreen(
            bitmap = null,
            onSaveClick = {},
            onCancelClick = {}
        )
    }
}
