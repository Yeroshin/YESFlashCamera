package com.yes.camera.domain.model


data class Characteristics(
    val isoValue:Int,
    val isoRange: IntRange,
    val shutterValue:Long,
    val focusValue:Float,
    val minFocusValue:Float,
    val shutterRange: LongRange,
    val resolutions:List<Dimensions>
)