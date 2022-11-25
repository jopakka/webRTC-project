package com.monitor.app.core.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.monitor.app.data.rtcclient.model.ButtonData

@Composable
fun AlertDialogMultipleButtons(
    buttons: List<ButtonData>,
    title: String = "",
    text: String = "",
    onClose: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = { onClose() },
        buttons = {
            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                buttons.map {
                    Button(
                        { it.onPress() }, modifier = Modifier
                            .padding(8.dp)
                    ) {
                        Text(it.text)
                    }
                }
            }
        },
        title = { Text(title) },
        text = { Text(text) },
        modifier = Modifier.padding(4.dp)
    )
}
