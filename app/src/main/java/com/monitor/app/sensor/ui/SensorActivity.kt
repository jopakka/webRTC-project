package com.monitor.app.sensor.ui

import android.Manifest
import android.app.Application
import android.util.Log
import android.view.LayoutInflater
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Adjust
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.monitor.app.Constants
import com.monitor.app.R
import com.monitor.app.classes.*
import com.monitor.app.sensor.SensorSendViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.webrtc.*

@Composable
fun SensorSendScreen(
    userId: String,
    sensorId: String,
    viewModel: SensorSendViewModel = viewModel()
) {
    Log.d("SensorSendScreen", "userId=$userId, sensorId=$sensorId")
    val permissions = arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
    val hasPermissions = viewModel.hasPermissions

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = {
            val granted = it.values.reduce { acc, next -> acc && next }
            viewModel.setHasPermission(granted)
        })

    viewModel.checkAndRequestPermissions(LocalContext.current, permissions, launcher)

    if (hasPermissions.value) {
        VideoView(LocalContext.current.applicationContext as Application, userId, sensorId)
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
@Composable
fun VideoView(application: Application, userId: String, sensorId: String) {
    val TAG = "SensorSendScreen VV"

    lateinit var rtcClient: RTCClient
    lateinit var signallingClient: SignalingClient

    KeepScreenOn()
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = {
                rtcClient.switchCamera()
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
                        rtcClient.onRemoteSessionReceived(description)
                        Constants.isIntiatedNow = false
                        rtcClient.answer(sdpObserver, userId, sensorId)
                    }

                    override fun onAnswerReceived(description: SessionDescription) {
                        Log.d(TAG, "onAnswerReceived")
                        rtcClient.onRemoteSessionReceived(description)
                        Constants.isIntiatedNow = false
                    }

                    override fun onIceCandidateReceived(iceCandidate: IceCandidate) {
                        Log.d(TAG, "onIceCandidateReceived")
                        rtcClient.addIceCandidate(iceCandidate)
                    }

                    override fun onCallEnded() {
                        Log.d(TAG, "onCallEnded")
                    }
                }

                fun onCameraAndAudioPermissionGranted(application: Application) {
                    rtcClient = RTCClient(
                        application,
                        object : PeerConnectionObserver() {
                            override fun onIceCandidate(p0: IceCandidate?) {
                                super.onIceCandidate(p0)
                                Log.d(TAG, "onIceCandidate: candidate=$p0")
                                signallingClient.sendIceCandidate(p0, true)
                                rtcClient.addIceCandidate(p0)
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

                    rtcClient.initSurfaceView(localView)
                    rtcClient.startLocalVideoCapture(localView)
                    signallingClient =
                        SignalingClient(userId, sensorId, createSignallingClientListener())
                    rtcClient.call(sdpObserver, userId, sensorId)
                }

                onCameraAndAudioPermissionGranted(application)
                view
            },
            modifier = Modifier.padding(it)
        )
    }
}

@Composable
fun KeepScreenOn() {
    val currentView = LocalView.current
    DisposableEffect(Unit) {
        currentView.keepScreenOn = true
        onDispose {
            currentView.keepScreenOn = false
        }
    }
}