package com.monitor.app.ui.control.sensor

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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import com.monitor.app.core.components.BatteryLevel
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

    val backAction = {
        viewModel.endCall()
        onNavigateBack()
    }

    if (viewModel.shouldNavigateBack) {
        backAction()
    }
    val application = LocalContext.current.applicationContext as Application

    var remoteView by remember { mutableStateOf<SurfaceViewRenderer?>(null) }
    var localView by remember { mutableStateOf<SurfaceViewRenderer?>(null) }
    val batteryLevel by viewModel.batteryLevel.collectAsState()
    val name by viewModel.name.collectAsState()
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
                    Icon(
                        Icons.Filled.Cameraswitch,
                        stringResource(R.string.description_change_camera)
                    )
                }
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
                    IconButton(onClick = {
                        viewModel.endCall()
                        onNavigateBack()
                    }) {
                        Icon(Icons.Filled.ArrowBack, "Navigate Back", tint = Color.White)
                    }
                    Text(
                        text = name?.uppercase() ?: "",
                        style = MaterialTheme.typography.h6,
                        color = Color.White
                    )
                    BatteryLevel(batteryLevel = batteryLevel?.toInt())
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

    BackHandler(onBack = backAction)
}
