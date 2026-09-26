package com.yes.shared.presentation.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf

val LocalAppColors = staticCompositionLocalOf { AppColors() }
val LocalAppDimens = staticCompositionLocalOf { AppDimens() }

private val DarkColorScheme = darkColorScheme(
    primary = GreenAccent,
    secondary = YellowAccent,
    tertiary = RedActive,
    background = BlackBackground,
    surface = SurfaceDark,
    onBackground = WhitePrimary,
    onSurface = WhitePrimary
)

object AppTheme {
    val colors: AppColors
        @Composable
        @ReadOnlyComposable
        get() = LocalAppColors.current

    val dimens: AppDimens
        @Composable
        @ReadOnlyComposable
        get() = LocalAppDimens.current

    val typography: Typography
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.typography
}

@Composable
fun FlashCameraTheme(
    colors: AppColors = AppColors(),
    dimens: AppDimens = AppDimens(),
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalAppColors provides colors,
        LocalAppDimens provides dimens
    ) {
        MaterialTheme(
            colorScheme = DarkColorScheme,
            typography = AppTypography,
            content = content
        )
    }
}
