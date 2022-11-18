package com.monitor.app.core.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun Title(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.h4,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(top = 8.dp)
    )
}