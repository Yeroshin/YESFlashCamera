package com.yes.camera.presentation.ui.views

import android.content.Context
import android.opengl.GLSurfaceView
import android.view.MotionEvent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.yes.camera.presentation.model.CharacteristicsUI
import com.yes.camera.presentation.model.RadioGroupItem
import com.yes.camera.presentation.ui.custom.compose.RadioUiItem
import com.yes.camera.presentation.ui.custom.compose.ShutterBox
import com.yes.camera.presentation.ui.custom.compose.TextRadioContent
import com.yes.camera.presentation.ui.custom.compose.UniversalRadioGroup
import com.yes.camera.presentation.ui.custom.compose.createRadioUiItems

import com.yes.camera.presentation.ui.custom.gles.AutoFitSurfaceView
import com.yes.camera.presentation.ui.custom.gles.GLRenderer
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first

@Composable
fun CameraScreenSuccess(
    context: Context,
    renderer: GLRenderer,
    characteristicsInit: CharacteristicsUI,
    onSettingsClick: () -> Unit,
    onStartVideoRecord: (enabled: Boolean) -> Unit,
    onSetCharacteristic: (characteristics: CharacteristicsUI) -> Unit,
){
    var characteristics by remember(characteristicsInit) {
        mutableStateOf(characteristicsInit)
    }
    var surfaceViewSize by remember { mutableStateOf(IntSize.Zero) }
    LaunchedEffect(surfaceViewSize ) {
        // Ждем, когда размеры станут известны (не 0)
        snapshotFlow { surfaceViewSize }
            .filter { it.width > 0 && it.height > 0 }
            .first() // Берем только самое первое валидное значение
            .let { size ->
                val normalizedX =
                    (size.width.toFloat() / 2f / size.width.toFloat()) * 2f - 1f
                val normalizedY =
                    -((size.height.toFloat() / 2f / size.height.toFloat()) * 2f - 1f)
                renderer.handleTouchPress(normalizedX, normalizedY)
                renderer.configureMagnifier(1f)
            }
    }
    var touchPoint by remember {
        mutableStateOf(FloatArray(0))
    }
    var shutterBoxIsOpen by remember { mutableStateOf(true) }
    val paramsRadioGroup = remember(characteristics.characteristicsItems) {
        characteristics.characteristicsItems.map { data ->
            RadioUiItem(id = data.id) { isSelected ->
                // Здесь мы решаем, как рисовать конкретный тип данных
                when (data) {
                    is RadioGroupItem.IconItem -> {
                        Icon(
                            painter = painterResource(data.iconRes),
                            contentDescription = null,
                            tint = if (isSelected) Color.Yellow else Color.White
                        )
                    }
                    is RadioGroupItem.TextItem -> {
                        TextRadioContent(
                            title = data.title,
                            value = data.currentValue,
                            selected = isSelected
                        )
                    }
                }
            }
        }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ){
        ///////////preview]
        Box() {
            AndroidView(
                modifier = Modifier
                    .padding(
                        top = if (characteristics.fullScreen) {
                            0.dp
                        } else {
                            84.dp
                        }
                    )
                    .onSizeChanged { size ->
                        surfaceViewSize = size
                        ///////////
                        //   surfaceViewSize = IntSize(width, height)
                        /*  val normalizedX =
                              (surfaceViewSize.width.toFloat() / 2f / surfaceViewSize.width.toFloat()) * 2f - 1f
                          val normalizedY =
                              -((surfaceViewSize.height.toFloat() / 2f / surfaceViewSize.height.toFloat()) * 2f - 1f)
                          renderer.handleTouchPress(normalizedX, normalizedY)*/
                        // renderer.configureMagnifier(1f)
                        /////////////
                    },
                factory = {
                    AutoFitSurfaceView(context, null).apply {
                        setEGLContextClientVersion(3)
                        setRenderer(renderer)
                        renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY
                        /* viewTreeObserver.addOnGlobalLayoutListener {
                             surfaceViewSize = IntSize(width, height)
                             val normalizedX =
                                 (surfaceViewSize.width.toFloat() / 2f / surfaceViewSize.width.toFloat()) * 2f - 1f
                             val normalizedY =
                                 -((surfaceViewSize.height.toFloat() / 2f / surfaceViewSize.height.toFloat()) * 2f - 1f)
                             renderer.handleTouchPress(normalizedX, normalizedY)
                             renderer.configureMagnifier(1f)
                         }*/
                        setOnTouchListener { v, event ->
                            v.performClick()
                            val normalizedX = (event.x / v.width.toFloat()) * 2f - 1f
                            val normalizedY = -((event.y / v.height.toFloat()) * 2f - 1f)
                            when (event.action) {
                                MotionEvent.ACTION_DOWN -> {
                                    renderer.handleTouchPress(normalizedX, normalizedY)
                                }

                                MotionEvent.ACTION_MOVE -> {
                                    renderer.handleTouchDrag(normalizedX, normalizedY)
                                }

                                MotionEvent.ACTION_UP -> {
                                    touchPoint = floatArrayOf(event.x / v.width, event.y / v.height)
                                }
                            }
                            true
                        }
                        renderer.glSurfaceView = this
                    }
                },
                update = { view ->
                    view.setFullscreen(characteristics.fullScreen)
                    view.setAspectRatio(
                        // 1280, 720
                        characteristics.aspectRatio?.width ?: 3,
                        characteristics.aspectRatio?.height ?: 2
                    )
                }
            )

            ShutterBox(
                isOpen = shutterBoxIsOpen,
                onToggle = { shutterBoxIsOpen = !shutterBoxIsOpen },
                modifier = Modifier.padding(
                    top = if (characteristics.fullScreen) 0.dp else 84.dp
                )
            ) {}
        }
    }
    var selectedItemParamsRadioGroup by remember {
        mutableStateOf(
             characteristics.characteristicsItems.first().id
        )
    }
    UniversalRadioGroup(
        items = paramsRadioGroup,
        selectedId = selectedItemParamsRadioGroup,
        onItemClick = { value ->
            selectedItemParamsRadioGroup = value
        }
    )
}