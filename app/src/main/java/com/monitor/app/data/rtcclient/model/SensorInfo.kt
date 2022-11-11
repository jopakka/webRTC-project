package com.monitor.app.data.rtcclient.model

import com.google.firebase.firestore.ServerTimestamp
import com.monitor.app.core.SensorStatuses
import java.util.*

data class SensorInfo(
    val name: String,
    val info: String,
    @ServerTimestamp
    val createdAt: Date? = null,
    val id: String? = null,
    val battery: Int? = null,
    val status: SensorStatuses = SensorStatuses.OFFLINE,
)
