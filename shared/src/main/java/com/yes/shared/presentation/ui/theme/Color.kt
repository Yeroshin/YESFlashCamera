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
)
