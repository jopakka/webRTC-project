package com.monitor.app.core.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.monitor.app.data.model.SensorInfo

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SensorListItem(sensorInfo: SensorInfo, onClick: (id: String) -> Unit) {
    Card(modifier = Modifier
        .padding(all = 8.dp)
        .fillMaxWidth(),
        onClick = { onClick(sensorInfo.id ?: "") }) {
        Column(modifier = Modifier.padding(all = 4.dp)) {
            Text(text = sensorInfo.name, style = MaterialTheme.typography.subtitle1)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = sensorInfo.info, style = MaterialTheme.typography.subtitle1)
        }
    }
}

@Preview
@Composable
fun PreviewListItem() {
    SensorListItem(SensorInfo("Testi", "Jee")) {}
}