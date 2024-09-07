package com.yes.camera.domain.model


data class Characteristics(
    val isoValue:Int,
    val isoRange: IntRange,
    var shutterValue:Int=0,
    val shutterRange: LongRange,
    val resolutions:List<Dimensions>
)