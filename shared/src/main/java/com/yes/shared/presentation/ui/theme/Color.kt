package com.yes.shared.presentation.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

// Primary Palette
val GreenAccent = Color(0xFF00FF00)
val YellowAccent = Color(0xFFFFFF00)
val RedActive = Color(0xFFFF0000)
val BlackBackground = Color(0xFF000000)
val SurfaceDark = Color(0xFF121212)
val WhitePrimary = Color(0xFFFFFFFF)
val MutedText = Color(0xFFB0B0B0)
val DarkShadow = Color(0xFF424242)
val LightGrayUnselected = Color(0xFFD3D3D3)
val GraySecondary = Color(0xFF808080)
val SemiTransparentWhite = Color(0x80FFFFFF)
val SemiTransparentBlack = Color(0x80000000)

// Light Palette
val LightBackground = Color(0xFFF8F9FA)
val SurfaceLight = Color(0xFFFFFFFF)
val TextDarkPrimary = Color(0xFF1F1F22)
val TextDarkSecondary = Color(0xFF5F6368)
val LightShadow = Color(0x22000000)
val LightStroke = Color(0xFFDADCE0)
val LightUnselected = Color(0xFFBDC1C6)

@Immutable
data class AppColors(
    val primaryAccent: Color = GreenAccent,
    val secondaryAccent: Color = YellowAccent,
    val recordActive: Color = RedActive,
    val background: Color = BlackBackground,
    val surface: Color = SurfaceDark,
    val textPrimary: Color = WhitePrimary,
    val textSecondary: Color = MutedText,
    val iconPrimary: Color = WhitePrimary,
    val iconSecondary: Color = GraySecondary,
    val shadow: Color = DarkShadow,
    val stroke: Color = DarkShadow,
    val unselected: Color = LightGrayUnselected,
    val overlaySemiTransparent: Color = SemiTransparentWhite,
    val transparent: Color = Color.Transparent
) {
    companion object {
        fun dark(): AppColors = AppColors()

        fun amoled(): AppColors = AppColors(
            background = Color(0xFF000000),
            surface = Color(0xFF000000),
            stroke = Color(0xFF1F1F22)
        )

        fun highContrast(): AppColors = AppColors(
            primaryAccent = Color(0xFFFFEB3B),
            secondaryAccent = Color(0xFF00E676),
            textPrimary = Color(0xFFFFFFFF),
            surface = Color(0xFF1E1E1E),
            stroke = Color(0xFFFFFFFF)
        )

        fun light(): AppColors = AppColors(
            primaryAccent = Color(0xFF0061A4),
            secondaryAccent = Color(0xFF7D5260),
            recordActive = Color(0xFFBA1A1A),
            background = LightBackground,
            surface = SurfaceLight,
            textPrimary = TextDarkPrimary,
            textSecondary = TextDarkSecondary,
            iconPrimary = TextDarkPrimary,
            iconSecondary = TextDarkSecondary,
            shadow = LightShadow,
            stroke = LightStroke,
            unselected = LightUnselected,
            overlaySemiTransparent = Color(0x80FFFFFF),
            transparent = Color.Transparent
        )
    }
}
