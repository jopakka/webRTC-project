package com.monitor.app.ui.sensor.main

import android.Manifest
import android.app.Application
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.monitor.app.R
import com.monitor.app.core.SensorStatuses
import com.monitor.app.core.components.KeepScreenOn
import com.monitor.app.core.components.WebRTCVideoView
import org.webrtc.SurfaceViewRenderer

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun SensorMainScreen(
    userId: String,
    sensorId: String,
    viewModel: SensorMainViewModel = viewModel(
        factory = SensorMainViewModelFactory(userId, sensorId)
    ),
    onNavigateBack: () -> Unit
) {
    KeepScreenOn()

    val context = LocalContext.current

    var remoteView by remember { mutableStateOf<SurfaceViewRenderer?>(null) }
    var localView by remember { mutableStateOf<SurfaceViewRenderer?>(null) }
    val permissions = listOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
    val application = context.applicationContext as Application
    val permissionState = rememberMultiplePermissionsState(permissions = permissions)

    if (remoteView != null && localView != null) {
        viewModel.init(application, remoteView!!, localView!!)
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(key1 = lifecycleOwner, effect = {
        val observer = LifecycleEventObserver { _, event ->
            Log.d("SensorMain", event.name)
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    if (!permissionState.allPermissionsGranted) {
                        permissionState.launchMultiplePermissionRequest()
                    } else {
                        viewModel.setStatus(SensorStatuses.ONLINE)
                        viewModel.saveBattery(context)
                    }
                }
                Lifecycle.Event.ON_PAUSE -> {
                    viewModel.setStatus(SensorStatuses.OFFLINE)
                    viewModel.unregisterBatteryReceiver(context)
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    })

    if (permissionState.allPermissionsGranted) {
        Scaffold(floatingActionButton = {
            FloatingActionButton(onClick = {
                viewModel.switchCamera()
            }) {
                Icon(Icons.Filled.Camera, stringResource(R.string.description_change_camera))
            }
        }) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(it)
            ) {
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

    BackHandler {
        viewModel.endCall()
        onNavigateBack()
    }
}
