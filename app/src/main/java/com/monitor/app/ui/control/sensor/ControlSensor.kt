package com.monitor.app.ui.control.sensor

import android.app.Application
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.monitor.app.core.components.KeepScreenOn
import com.monitor.app.core.components.WebRTCVideoView

@Composable
fun ControlSensorScreen(
    navController: NavHostController,
    userId: String,
    sensorId: String,
    viewModel: ControlSensorViewModel = viewModel(
        factory = ControlSensorViewModelFactory(userId, sensorId)
    )
) {
    KeepScreenOn()
    val application = LocalContext.current.applicationContext as Application

    WebRTCVideoView {
        viewModel.init(application, it)
    }

    BackHandler {
        viewModel.endCall()
        navController.navigateUp()
    }
}
