package com.monitor.app.classes

data class SensorInfo(
    val name: String,
    val info: String,
    val createdAt: Long = System.currentTimeMillis(),
)
