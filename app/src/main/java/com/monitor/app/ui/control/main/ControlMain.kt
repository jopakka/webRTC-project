package com.monitor.app.ui.control.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.monitor.app.core.components.SensorList
import com.monitor.app.core.constants.Constants

@Composable
fun ControlMainScreen(
    userId: String,
    viewModel: ControlMainViewModel = viewModel(
        factory = ControlMainViewModelFactory(userId)
    ),
    onSensorSelected: (id: String) -> Unit
) {
    val sensors = viewModel.sensors

    Constants.isIntiatedNow = true
    Constants.isCallEnded = true

    Scaffold(
        topBar = { TopAppBar(title = { Text(text = "House Monitor System") }) },
        content = {
            Column(
                modifier = Modifier
                    .background(color = MaterialTheme.colors.background)
                    .padding(it),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                Text(
                    text = "Camera devices",
                    modifier = Modifier.padding(vertical = 30.dp),
                    style = MaterialTheme.typography.h4
                )
                SensorList(sensors = sensors) { id ->
                    onSensorSelected(id)
                }
            }
        }
    )
}
