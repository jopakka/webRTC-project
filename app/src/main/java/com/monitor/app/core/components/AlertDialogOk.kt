package com.monitor.app.core.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AlertDialogOk(
    btnText: String = "OK",
    title: String = "",
    text: String = "",
    onClose: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = { onClose() },
        buttons = {
            Button({ onClose() }, modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth()) {
                Text(btnText)
            }
        },
        title = { Text(title) },
        text = { Text(text) },
        modifier = Modifier.padding(4.dp)
    )
}
