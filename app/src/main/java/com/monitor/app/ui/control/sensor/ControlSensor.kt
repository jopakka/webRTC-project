package com.monitor.app.ui.control.sensor

import android.app.Application
import android.util.Log
import android.view.LayoutInflater
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.monitor.app.R
import com.monitor.app.core.components.KeepScreenOn
import com.monitor.app.core.components.WebRTCVideoView
import com.monitor.app.core.constants.Constants
import com.monitor.app.data.rtcclient.AppSdpObserver
import com.monitor.app.data.rtcclient.PeerConnectionObserver
import com.monitor.app.data.rtcclient.RTCClient
import com.monitor.app.data.signalingclient.SignalingClient
import com.monitor.app.data.signalingclient.SignalingClientListener
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.webrtc.*

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
