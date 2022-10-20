package com.monitor.app.ui.control.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.monitor.app.R
import com.monitor.app.core.components.SensorList
import com.monitor.app.core.constants.Constants

@Composable
fun ControlMainScreen(navigator: NavHostController, viewModel: ControlMainViewModel = viewModel()) {
    val sensors = viewModel.sensors

    Constants.isIntiatedNow = true
    Constants.isCallEnded = true

    Scaffold(
        content = {
            Column(
                modifier = Modifier
                    .background(color = MaterialTheme.colors.background)
                    .padding(it)
            ) {
                Text(text = stringResource(R.string.sensors), style = MaterialTheme.typography.h2)
                SensorList(sensors = sensors) { id ->
                    val user = "user-1"
                    navigator.navigate("sensorView/$user/$id")
                }
            }
        }
    )
}
