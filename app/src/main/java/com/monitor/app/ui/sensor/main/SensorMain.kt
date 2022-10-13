package com.monitor.app.ui.sensor.main

import android.Manifest
import android.app.Application
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
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

    val permissions = listOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
    val application = LocalContext.current.applicationContext as Application
    val permissionState = rememberMultiplePermissionsState(permissions = permissions)

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
        WebRTCVideoView {
            viewModel.init(application, it)
        }
    }

    BackHandler {
        viewModel.endCall()
        navController.navigateUp()
    }
}
