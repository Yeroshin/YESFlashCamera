package com.yes.camera.domain.model

import androidx.compose.material3.RangeSlider


data class Characteristics(
    val isoValue:Int?=0,
    val isoRange: IntRange= IntRange(0,0),
    val shutterValue:Long?=0L,
    val shutterRange: LongRange=LongRange(0L,0L),
    val focusValue:Float=0f,
    val minFocusValue:Float=0f,
    val resolutions:List<Dimensions> = emptyList(),
    val touchPoint: FloatArray = FloatArray(2)
)