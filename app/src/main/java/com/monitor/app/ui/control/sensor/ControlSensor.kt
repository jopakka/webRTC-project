package com.monitor.app.ui.control.sensor

import android.Manifest
import android.app.Application
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.monitor.app.R
import com.monitor.app.core.DataCommands
import com.monitor.app.core.components.AlertDialogOk
import com.monitor.app.core.components.KeepScreenOn
import com.monitor.app.core.components.WebRTCVideoView
import org.webrtc.SurfaceViewRenderer

@OptIn(ExperimentalPermissionsApi::class)
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

    var remoteView by remember { mutableStateOf<SurfaceViewRenderer?>(null) }
    var localView by remember { mutableStateOf<SurfaceViewRenderer?>(null) }
    val permissions = listOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
    val permissionState = rememberMultiplePermissionsState(permissions = permissions)

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(key1 = lifecycleOwner, effect = {
        val observer = LifecycleEventObserver { _, event ->
            Log.d("SensorMain", event.name)
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    if (!permissionState.allPermissionsGranted) {
                        permissionState.launchMultiplePermissionRequest()
                    }
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    })

    if (remoteView != null && localView != null) {
        viewModel.init(application, remoteView!!, localView!!)
    }

    Log.d(
        "ControlSensor",
        "permissionState.allPermissionsGranted: ${permissionState.allPermissionsGranted}"
    )

    if (permissionState.allPermissionsGranted) {
        Scaffold(floatingActionButton = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.alpha(if (viewModel.isLoading) 0f else 1f)
            ) {
                FloatingActionButton(onClick = {
                    viewModel.toggleMicrophone()
                }) {
                    Icon(
                        if (viewModel.microphoneState) Icons.Filled.Mic else Icons.Filled.MicOff,
                        stringResource(R.string.description_toggle_mic)
                    )
                }
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
                    remoteView = videoView
                }
                Column(modifier = Modifier.height(0.dp)) {
                    WebRTCVideoView { videoView ->
                        localView = videoView
                    }
                }
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
