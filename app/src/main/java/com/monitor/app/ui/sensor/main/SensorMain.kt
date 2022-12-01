package com.monitor.app.ui.sensor.main

import android.Manifest
import android.app.Application
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import com.monitor.app.core.components.BatteryLevel
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
    var displayMenu by remember { mutableStateOf(false) }
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
                Icon(Icons.Filled.Cameraswitch, stringResource(R.string.description_change_camera))
            }
        }) {
            Box(
                modifier = Modifier
                    .fillMaxSize(1f)
                    .padding(it)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
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
                Row(
                    modifier = Modifier
                        .fillMaxWidth(1f)
                        .height(80.dp)
                        .background(
                            brush = Brush.verticalGradient(
                                0.0f to Color.Black,
                                0.25f to Color(0xE6000000),
                                0.50f to Color(0xBF000000),
                                0.75f to Color(0x80000000),
                                0.99f to Color(0x40000000),
                                1.0f to Color.Transparent,
                                startY = 0f,
                                endY = Float.POSITIVE_INFINITY
                            )
                        )
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BatteryLevel(batteryLevel = 20)
                    Text(
                        text = "LIVING ROOM",
                        style = MaterialTheme.typography.h6,
                        color = Color.White
                    )
                    Box(contentAlignment = Alignment.Center) {
                        IconButton(onClick = { displayMenu = !displayMenu }) {
                            Icon(
                                Icons.Filled.MoreHoriz,
                                contentDescription = "Options",
                                tint = Color.White
                            )
                        }
                        DropdownMenu(
                            expanded = displayMenu,
                            onDismissRequest = { displayMenu = false }) {
                            DropdownMenuItem(onClick = {
                            }) {
                                Text(text = "Change device type")
                            }
                            DropdownMenuItem(onClick = {}) {
                                Text(text = "Change information")
                            }
                        }
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
