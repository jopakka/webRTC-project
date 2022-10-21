package com.monitor.app.ui.control.sensor

import android.app.Application
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
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

    WebRTCVideoView {
        viewModel.init(application, it)
    }

    BackHandler {
        viewModel.endCall()
        onNavigateBack()
    }
}
