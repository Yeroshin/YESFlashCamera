package com.yes.camera.domain.model


data class Characteristics(
    val isoRange: IntRange,
    var shutterValueSelected:Int=0,
    val shutterRange: LongRange,
    val resolutions:List<Dimensions>
)