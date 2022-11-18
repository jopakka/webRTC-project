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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.monitor.app.R
import com.monitor.app.core.components.SensorList
import com.monitor.app.core.components.Title
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
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.app_name)) },
                contentColor = Color.White,
                elevation = 10.dp
            )
        },
        content = {
            Column(
                modifier = Modifier
                    .background(color = MaterialTheme.colors.background)
                    .padding(it),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                Title(stringResource(R.string.camera_devices))
                SensorList(sensors = sensors) { id ->
                    onSensorSelected(id)
                }
            }
        }
    )
}
