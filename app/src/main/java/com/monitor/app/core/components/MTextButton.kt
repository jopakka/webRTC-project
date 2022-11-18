package com.monitor.app.core.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun MTextButton(text: String, onClick: () -> Unit = {}) {
    TextButton(onClick) {
        Text(
            text = text,
            modifier = Modifier.padding(end = 8.dp)
        )
    }
}