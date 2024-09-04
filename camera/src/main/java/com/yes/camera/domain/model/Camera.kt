package com.yes.camera.domain.model


data class Camera(
    val iso: IntRange,
    val exposure: LongRange,
    val resolutions:List<Dimensions>
)