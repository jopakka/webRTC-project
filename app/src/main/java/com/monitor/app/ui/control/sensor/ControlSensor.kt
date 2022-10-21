package com.monitor.app.ui.control.sensor

import android.app.Application
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.monitor.app.R
import com.monitor.app.core.components.KeepScreenOn
import com.monitor.app.core.components.WebRTCVideoView

@Composable
fun ControlSensorScreen(
    userId: String,
    sensorId: String,
    viewModel: ControlSensorViewModel = viewModel(
        factory = ControlSensorViewModelFactory(userId, sensorId)
    ),
    onNavigateBack: () -> Unit
) {
    KeepScreenOn()
    val application = LocalContext.current.applicationContext as Application

    Scaffold(floatingActionButton = {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            FloatingActionButton(onClick = {
                viewModel.sendData("camera")
            }) {
                Icon(Icons.Filled.Camera, stringResource(R.string.description_change_camera))
            }
            FloatingActionButton(onClick = {
                viewModel.sendData("flash")
            }) {
                Icon(Icons.Filled.FlashOn, stringResource(R.string.description_toggle_flash))
            }
        }
    }) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
        ) {
            WebRTCVideoView { videoView ->
                viewModel.init(application, videoView)
            }
        }
    }

    BackHandler {
        viewModel.endCall()
        onNavigateBack()
    }
}
