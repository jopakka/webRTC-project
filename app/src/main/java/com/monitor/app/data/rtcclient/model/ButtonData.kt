package com.monitor.app.data.rtcclient.model

data class ButtonData(
    val text: String,
    val onPress: () -> Unit,
)
