package com.monitor.app.sensorview.ui

import android.app.Application
import android.util.Log
import android.view.LayoutInflater
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.monitor.app.Constants
import com.monitor.app.R
import com.monitor.app.classes.*
import com.monitor.app.sensorsend.ui.KeepScreenOn
import com.monitor.app.sensorview.SensorViewViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.webrtc.*

@Composable
fun SensorViewScreen(
    userId: String,
    sensorId: String,
    viewModel: SensorViewViewModel = viewModel()
) {
    Log.d("SensorViewScreen", "userId=$userId, sensorId=$sensorId")

    VideoView(userId, sensorId)
}

@OptIn(ExperimentalCoroutinesApi::class)
@Composable
fun VideoView(
    userId: String,
    sensorId: String,
) {
    lateinit var rtcClient: RTCClient
    lateinit var signallingClient: SignalingClient

    KeepScreenOn()
    Scaffold {
        AndroidView(
            factory = { context ->
                val TAG = "SensorViewScreen"

                val view =
                    LayoutInflater.from(context).inflate(R.layout.webrtc_video_view, null, false)
                val videoView = view.findViewById<SurfaceViewRenderer>(R.id.video_view)

//    val audioManager by lazy { RTCAudioManager.create(LocalContext.current) }

                val sdpObserver = object : AppSdpObserver() {
                    override fun onCreateSuccess(p0: SessionDescription?) {
                        super.onCreateSuccess(p0)
                        Log.d(TAG, "sdpObserver onCreateSuccess")
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
                        if (!Constants.isCallEnded) {
                            Constants.isCallEnded = true
//                            rtcClient.endCall(meetingID)
                        }
                    }
                }

                fun onCameraAndAudioPermissionGranted(application: Application) {
                    rtcClient = RTCClient(
                        application,
                        object : PeerConnectionObserver() {
                            override fun onIceCandidate(p0: IceCandidate?) {
                                super.onIceCandidate(p0)
                                Log.d(TAG, "onIceCandidate: candidate=$p0")
                                signallingClient.sendIceCandidate(p0, false)
                                rtcClient.addIceCandidate(p0)
                            }

                            override fun onAddStream(p0: MediaStream?) {
                                super.onAddStream(p0)
                                Log.d(TAG, "onAddStream: $p0")
                                p0?.videoTracks?.get(0)?.addSink(videoView)
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

                    signallingClient =
                        SignalingClient(userId, sensorId, createSignallingClientListener())
                }

                onCameraAndAudioPermissionGranted(context.applicationContext as Application)

                rtcClient.initSurfaceView(videoView)
                view
            },
            modifier = Modifier.padding(it)
        )
    }
}
