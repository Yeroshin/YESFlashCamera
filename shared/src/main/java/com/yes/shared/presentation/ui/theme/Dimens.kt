package com.yes.shared.presentation.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Immutable
data class AppDimens(
    // Spacing & Margins
    val none: Dp = 0.dp,
    val extraSmall: Dp = 2.dp,
    val small: Dp = 4.dp,
    val medium: Dp = 8.dp,
    val large: Dp = 16.dp,
    val extraLarge: Dp = 24.dp,
    val huge: Dp = 32.dp,
    val giant: Dp = 48.dp,

    // Top Paddings
    val shutterTopPadding: Dp = 64.dp,
    val cameraTopPadding: Dp = 84.dp,
    val histogramTopPadding: Dp = 98.dp,

    // Component & Icon Sizes
    val iconSmall: Dp = 14.dp,
    val iconMedium: Dp = 20.dp,
    val iconDefault: Dp = 24.dp,
    val iconLarge: Dp = 32.dp,
    val iconHuge: Dp = 48.dp,
    val recordButtonSize: Dp = 96.dp,
    val recordInnerCircle: Dp = 60.dp,
    val selectorItemWidth: Dp = 48.dp,
    val selectorItemHeight: Dp = 18.dp,
    val controlBarHeight: Dp = 50.dp,
    val histogramWidth: Dp = 150.dp,
    val histogramHeight: Dp = 80.dp,
    val radioGroupSpacing: Dp = 56.dp,

    // Borders & Strokes
    val borderThin: Dp = 2.dp,
    val borderMedium: Dp = 5.dp,
    val borderThick: Dp = 8.dp,

    // Text Sizes
    val textCaption: TextUnit = 11.sp,
    val textSmall: TextUnit = 12.sp,
    val textBody: TextUnit = 14.sp,
    val textMedium: TextUnit = 16.sp,
    val textLarge: TextUnit = 18.sp,
    val textTitle: TextUnit = 24.sp,
    val textHeader: TextUnit = 32.sp
)
