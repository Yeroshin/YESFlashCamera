package com.yes.camera.presentation.ui.views

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yes.camera.R
import com.yes.camera.presentation.ui.custom.compose.VectorShadow
import com.yes.shared.presentation.ui.theme.AppTheme
import com.yes.shared.presentation.ui.theme.FlashCameraTheme

/**
 * Screen that displays a captured photo/frame with gesture scaling (zoom/pan) and a back button.
 * Image saving is handled automatically upon capture.
 *
 * @param modifier Layout modifier.
 * @param bitmap Optional captured Bitmap of the shot.
 * @param onBackClick Callback when the user clicks the back button.
 */
@Composable
fun CapturedImageScreen(
    modifier: Modifier = Modifier,
    bitmap: Bitmap? = null,
    onBackClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppTheme.colors.background)
    ) {
        // Captured Image display in center/fullscreen
        Box(
            modifier = Modifier
                .fillMaxSize()
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

                                offset = if (newScale > 1f) {
                                    val maxOffsetX = (containerSize.width * (newScale - 1f)) / 2f
                                    val maxOffsetY = (containerSize.height * (newScale - 1f)) / 2f
                                    val newOffset = offset + pan
                                    Offset(
                                        x = newOffset.x.coerceIn(-maxOffsetX, maxOffsetX),
                                        y = newOffset.y.coerceIn(-maxOffsetY, maxOffsetY)
                                    )
                                } else {
                                    Offset.Zero
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
                        .background(AppTheme.colors.surface),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No Captured Image Available",
                        color = AppTheme.colors.textSecondary,
                        fontSize = 16.sp
                    )
                }
            }
        }

        // Top-left Back Navigation Button
        VectorShadow(
            modifier = Modifier
                .padding(top = 28.dp, start = 20.dp)
                .size(AppTheme.dimens.iconLarge)
                .align(Alignment.TopStart),
            resId = R.drawable.arrow_back, // Back button icon (user can customize drawable)
            vectorColor = AppTheme.colors.iconPrimary,
            shadowColor = AppTheme.colors.shadow,
            onClick = onBackClick
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF131316)
@Composable
fun CapturedImageScreenPreview() {
    FlashCameraTheme {
        CapturedImageScreen(
            bitmap = null
        ) {}
    }
}
