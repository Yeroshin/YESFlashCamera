package com.yes.shared.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FileNameGenerator {
    fun generateFileName(): String {
        val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        val currentTime = System.currentTimeMillis()
        return "${dateFormat.format(Date(currentTime))}"
    }

    fun generateFileName(captureTimeMillis: Long): String {
        val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        return "${dateFormat.format(Date(captureTimeMillis))}"
    }
}