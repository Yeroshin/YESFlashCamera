package com.yes.camera.domain.model


data class Characteristics(
    val isoValue:Int?=0,
    val isoRange: IntRange= IntRange(0,0),
    val shutterValue:Long?=0L,
    val shutterRange: LongRange=LongRange(0L,0L),
    val wbManualValue:Int?=null,
    val wbSustemValue:Int?=null,
  //  val wbManualItems: List<Int> = emptyList(),
    val wbItems: IntArray?=null,
    val focusValue:Float?=null,
    val minFocusValue:Float=0f,
    val resolutions:List<Dimensions> = emptyList(),
    val touchPoint: FloatArray? = null
)