package com.monitor.app.core.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.monitor.app.data.model.SampleData
import com.monitor.app.data.model.SensorInfo

@Composable
fun SensorList(sensors: List<SensorInfo>, itemOnClick: (id: String) -> Unit) {
    Surface(modifier = Modifier.fillMaxSize()) {
        LazyColumn {
            items(sensors) { sensor ->
                SensorListItem(sensor, itemOnClick)
            }
        }
    }
}

@Preview
@Composable
fun PreviewSensorList() {
    SensorList(SampleData.sensors) {}
}
