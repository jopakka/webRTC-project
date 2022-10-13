package com.monitor.app.ui.sensor.main

import android.Manifest
import android.app.Application
import android.util.Log
import android.view.LayoutInflater
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
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.monitor.app.core.constants.Constants
import com.monitor.app.R
import com.monitor.app.core.components.KeepScreenOn
import com.monitor.app.core.components.WebRTCVideoView
import com.monitor.app.data.rtcclient.*
import com.monitor.app.data.signalingclient.SignalingClient
import com.monitor.app.data.signalingclient.SignalingClientListener
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.webrtc.*

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

    viewModel.checkAndRequestPermissions(LocalContext.current, permissions, launcher)

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
