package com.monitor.app.core.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Checkbox
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun DeviceTypeCheckBox() {
    Column {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val isChecked = remember { mutableStateOf(false) }
            Checkbox(checked = isChecked.value, onCheckedChange = { isChecked.value = it })
            Text(text = "CAMERA DEVICE")
        }
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val isChecked = remember { mutableStateOf(false) }
            Checkbox(checked = isChecked.value, onCheckedChange = { isChecked.value = it })
            Text(text = "CONTROL DEVICE")
        }
    }
}

@Preview
@Composable
fun PreviewDeviceTypeCheckBox() {
    DeviceTypeCheckBox()
}