package com.yes.camera.domain.model

import com.yes.shared.domain.Dimensions


data class Characteristics(
    val backCamera:Boolean?=null,
    val isoValue: Int? = null,
    val isoRange: IntRange = IntRange(0, 0),
    val shutterValue: Long? = null,
    val shutterRange: LongRange = LongRange(0L, 0L),
    val wbValue: Int? = null,
    val wbMode: Int? = null,
    val wbItems: IntArray? = null,
    val focusValue: Float? = null,
    val focusMode: Int? = null,
    val minFocusValue: Float = 0f,
    val maxFocusValue: Float = 0f,
    val resolution: Dimensions? = null,
    val resolutionItems: List<Dimensions> = emptyList(),
    val touchPoint: FloatArray? = null,
    val fullscreen:Boolean? =null
)