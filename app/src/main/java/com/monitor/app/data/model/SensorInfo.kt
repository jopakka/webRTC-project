package com.monitor.app.data.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.*

data class SensorInfo(
    val name: String,
    val info: String,
    @ServerTimestamp
    val createdAt: Date? = null,
    val id: String? = null,
)
