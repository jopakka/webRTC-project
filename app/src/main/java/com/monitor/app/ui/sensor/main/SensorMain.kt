package com.monitor.app.ui.sensor.main

import android.Manifest
import android.app.Application
import android.util.Log
import android.view.LayoutInflater
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Adjust
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.monitor.app.core.constants.Constants
import com.monitor.app.R
import com.monitor.app.core.components.KeepScreenOn
import com.monitor.app.core.components.WebRTCVideoView

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun SensorMainScreen(
    navController: NavHostController,
    userId: String,
    sensorId: String,
    viewModel: SensorMainViewModel = viewModel(
        factory = SensorMainViewModelFactory(userId, sensorId)
    )
) {
    KeepScreenOn()

    val permissions = arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
    val hasPermissions = viewModel.hasPermissions
    val application = LocalContext.current.applicationContext as Application

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = {
            val granted = it.values.reduce { acc, next -> acc && next }
            viewModel.setHasPermission(granted)
        })
    Log.d("SensorSendScreen", "userId=$userId, sensorId=$sensorId")
    val permissions = listOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
    val permissionState = rememberMultiplePermissionsState(permissions = permissions)

    viewModel.checkAndRequestPermissions(LocalContext.current, permissions, launcher)

    if (!permissionState.allPermissionsGranted) {
        val lifecycleOwner = LocalLifecycleOwner.current
        DisposableEffect(key1 = lifecycleOwner, effect = {
            val observer = LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) {
                    permissionState.launchMultiplePermissionRequest()
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
            }
        })
    } else {
        VideoView(LocalContext.current.applicationContext as Application, userId, sensorId, navBack)
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
@Composable
fun VideoView(application: Application, userId: String, sensorId: String, navBack: () -> Boolean) {
    val TAG = "SensorSendScreen"

    var rtcClient by remember { mutableStateOf<RTCClient?>(null) }
    var signallingClient by remember { mutableStateOf<SignalingClient?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = {
                viewModel.switchCamera()
            }) {
                Icon(Icons.Filled.Adjust, null)
            }
        }
    ) {
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(it)) {
            WebRTCVideoView { videoView ->
                if(hasPermissions.value) {
                    viewModel.init(application, videoView)
                }
            }
        }
    }

    BackHandler {
        viewModel.endCall()
        navController.navigateUp()
    }
}
