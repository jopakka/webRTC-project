package com.monitor.app.ui.sensor.main

import android.Manifest
import android.app.Application
import android.util.Log
import android.view.LayoutInflater
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import com.monitor.app.data.rtcclient.*
import com.monitor.app.data.signalingclient.SignalingClient
import com.monitor.app.data.signalingclient.SignalingClientListener
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.webrtc.*

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun SensorMainScreen(
    navController: NavHostController,
    userId: String,
    sensorId: String,
    viewModel: SensorMainViewModel = viewModel()
) {
    KeepScreenOn()
    Log.d("SensorSendScreen", "userId=$userId, sensorId=$sensorId")
    val permissions = listOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
    val permissionState = rememberMultiplePermissionsState(permissions = permissions)

    val navBack = {
        navController.navigateUp()
    }


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
                rtcClient?.switchCamera()
            }) {
                Icon(Icons.Filled.Adjust, null)
            }
        }
    ) {
        AndroidView(
            factory = { context ->
                val view =
                    LayoutInflater.from(context).inflate(R.layout.webrtc_video_view, null, false)
                val localView = view.findViewById<SurfaceViewRenderer>(R.id.video_view)

//    val audioManager by lazy { RTCAudioManager.create(LocalContext.current) }

                val sdpObserver = object : AppSdpObserver() {
                    override fun onCreateSuccess(p0: SessionDescription?) {
                        super.onCreateSuccess(p0)
                        Log.d(TAG, "onCreateSuccess send")
                    }
                }

                fun createSignallingClientListener() = object : SignalingClientListener {
                    override fun onConnectionEstablished() {
                        Log.d(TAG, "onConnectionEstablished")
                    }

                    override fun onOfferReceived(description: SessionDescription) {
                        Log.d(TAG, "onOfferReceived")
                        rtcClient?.onRemoteSessionReceived(description)
                        Constants.isIntiatedNow = false
                        rtcClient?.answer(sdpObserver, userId, sensorId)
                    }

                    override fun onAnswerReceived(description: SessionDescription) {
                        Log.d(TAG, "onAnswerReceived")
                        rtcClient?.onRemoteSessionReceived(description)
                        Constants.isIntiatedNow = false
                    }

                    override fun onIceCandidateReceived(iceCandidate: IceCandidate) {
                        Log.d(TAG, "onIceCandidateReceived")
                        rtcClient?.addIceCandidate(iceCandidate)
                    }

                    override fun onCallEnded() {
                        Log.d(TAG, "onCallEnded")
                        rtcClient?.endCall(userId, sensorId, true)
//                        if (!Constants.isCallEnded) {
//                            Constants.isCallEnded = true
//                            rtcClient?.endCall(userId, sensorId)
//                        }
                    }
                }

                fun onCameraAndAudioPermissionGranted(application: Application) {
                    rtcClient = RTCClient(
                        application,
                        object : PeerConnectionObserver() {
                            override fun onIceCandidate(p0: IceCandidate?) {
                                super.onIceCandidate(p0)
                                Log.d(TAG, "onIceCandidate: candidate=$p0")
                                signallingClient?.sendIceCandidate(p0, true)
                                rtcClient?.addIceCandidate(p0)
                            }

                            override fun onAddStream(p0: MediaStream?) {
                                super.onAddStream(p0)
                                Log.d(TAG, "onAddStream: $p0")
                            }

                            override fun onIceConnectionChange(p0: PeerConnection.IceConnectionState?) {
                                Log.d(TAG, "onIceConnectionChange: $p0")
                            }

                            override fun onIceConnectionReceivingChange(p0: Boolean) {
                                Log.d(TAG, "onIceConnectionReceivingChange: $p0")
                            }

                            override fun onConnectionChange(newState: PeerConnection.PeerConnectionState?) {
                                Log.d(TAG, "onConnectionChange: $newState")
                            }

                            override fun onDataChannel(p0: DataChannel?) {
                                Log.d(TAG, "onDataChannel: $p0")
                            }

                            override fun onStandardizedIceConnectionChange(newState: PeerConnection.IceConnectionState?) {
                                Log.d(TAG, "onStandardizedIceConnectionChange: $newState")
                            }

                            override fun onAddTrack(p0: RtpReceiver?, p1: Array<out MediaStream>?) {
                                Log.d(TAG, "onAddTrack: $p0 \n $p1")
                            }

                            override fun onTrack(transceiver: RtpTransceiver?) {
                                Log.d(TAG, "onTrack: $transceiver")
                            }
                        }
                    )

                    rtcClient?.initSurfaceView(localView)
                    rtcClient?.startLocalVideoCapture(localView)
                    signallingClient =
                        SignalingClient(userId, sensorId, createSignallingClientListener())
                    rtcClient?.call(sdpObserver, userId, sensorId)
                }

                onCameraAndAudioPermissionGranted(application)
                view
            },
            modifier = Modifier.padding(it)
        )
    }

    BackHandler {
        rtcClient?.endCall(userId, sensorId, false)
        navBack()
    }
}
