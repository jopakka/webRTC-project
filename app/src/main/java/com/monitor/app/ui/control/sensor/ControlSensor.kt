package com.monitor.app.ui.control.sensor

import android.app.Application
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.monitor.app.R
import com.monitor.app.core.DataCommands
import com.monitor.app.core.components.AlertDialogOk
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
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.alpha(if (viewModel.isLoading) 0f else 1f)
        ) {
            FloatingActionButton(onClick = {
                viewModel.sendData(DataCommands.CAMERA)
            }) {
                Icon(Icons.Filled.Camera, stringResource(R.string.description_change_camera))
            }
        }
    }) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize(if (viewModel.isLoading) 1f else 0f)
                    .background(colorResource(R.color.black)),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
            }
            WebRTCVideoView { videoView ->
                viewModel.init(application, videoView)
            }
        }
    }

    if (viewModel.callEnded) {
        Log.d("ControlSensor", "viewModel.callEnded=${viewModel.callEnded}")
        AlertDialogOk(
            title = stringResource(R.string.call_ended),
        ) {
            viewModel.endCall()
            onNavigateBack()
        }
    }

    BackHandler {
        viewModel.endCall()
        onNavigateBack()
    }
}
