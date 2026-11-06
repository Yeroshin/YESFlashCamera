package com.yes.settings.domain.model

data class StorageInfo(
    val totalBytes: Long,
    val availableBytes: Long,
    val totalFormatted: String,
    val availableFormatted: String,
    val usagePercentage: Float,
    val storagePath: String,
    val avgBytesPerShot: Long,
    val avgShotSizeFormatted: String,
    val remainingShots: Long
)
