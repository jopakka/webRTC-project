package com.monitor.app.data.model

data class SensorInfo(
    val name: String,
    val info: String,
    val createdAt: Long = System.currentTimeMillis(),
    val id: String? = null,
)
