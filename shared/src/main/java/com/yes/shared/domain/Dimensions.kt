package com.yes.shared.domain

data class Dimensions(val width: Int, val height: Int)
enum class ImgFormat{
    JPEG,
    RAW,
    JPEGRAW
}