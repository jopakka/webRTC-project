package com.monitor.app.ui.sensor.main

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.compose.runtime.*
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.monitor.app.core.constants.Constants
import com.monitor.app.data.rtcclient.AppSdpObserver
import com.monitor.app.data.rtcclient.PeerConnectionObserver
import com.monitor.app.data.rtcclient.RTCClient
import com.monitor.app.data.signalingclient.SignalingClient
import com.monitor.app.data.signalingclient.SignalingClientListener
import com.monitor.app.ui.control.sensor.ControlSensorViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.webrtc.*

@OptIn(ExperimentalCoroutinesApi::class)
class SensorMainViewModel(private val userId: String, private val sensorId: String) : ViewModel() {
    companion object {
        private const val CAMERA_PERMISSION = Manifest.permission.CAMERA
        private const val AUDIO_PERMISSION = Manifest.permission.RECORD_AUDIO
        private const val TAG = "SensorSendViewModel"
    }

    private val mRtcClient = mutableStateOf<RTCClient?>(null)
    private val mSignalingClient = mutableStateOf<SignalingClient?>(null)
    private var isInitialized by mutableStateOf(false)

    private val _hasPermissions: MutableState<Boolean> = mutableStateOf(false)
    val hasPermissions: State<Boolean>
        get() = _hasPermissions

    fun checkAndRequestPermissions(
        context: Context,
        permissions: Array<String>,
        launcher: ManagedActivityResultLauncher<Array<String>, Map<String, Boolean>>
    ) {
        if (permissions.all {
                ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
            }) {
            _hasPermissions.value = true
        } else {
            _hasPermissions.value = false
            launcher.launch(permissions)
        }
    }

    fun setHasPermission(value: Boolean) {
        _hasPermissions.value = value
    }

    val sdpObserver = object : AppSdpObserver() {
        override fun onCreateSuccess(p0: SessionDescription?) {
            super.onCreateSuccess(p0)
            Log.d(TAG, "onCreateSuccess send")
        }
    }

    private fun createSignallingClientListener() = object : SignalingClientListener {
        override fun onConnectionEstablished() {
            Log.d(TAG, "onConnectionEstablished")
        }

        override fun onOfferReceived(description: SessionDescription) {
            Log.d(TAG, "onOfferReceived")
            mRtcClient.value?.onRemoteSessionReceived(description)
            Constants.isIntiatedNow = false
            mRtcClient.value?.answer(sdpObserver, userId, sensorId)
        }

        override fun onAnswerReceived(description: SessionDescription) {
            Log.d(TAG, "onAnswerReceived")
            mRtcClient.value?.onRemoteSessionReceived(description)
            Constants.isIntiatedNow = false
        }

        override fun onIceCandidateReceived(iceCandidate: IceCandidate) {
            Log.d(TAG, "onIceCandidateReceived")
            mRtcClient.value?.addIceCandidate(iceCandidate)
        }

        override fun onCallEnded() {
            Log.d(TAG, "onCallEnded")
            mRtcClient.value?.endCall(userId, sensorId, true)
        }
    }

    fun init(application: Application, videoView: SurfaceViewRenderer) {
        if(isInitialized) {
            return
        }
        isInitialized = true

        mRtcClient.value = RTCClient(
            application,
            object : PeerConnectionObserver() {
                override fun onIceCandidate(p0: IceCandidate?) {
                    super.onIceCandidate(p0)
                    Log.d(TAG, "onIceCandidate: candidate=$p0")
                    mSignalingClient.value?.sendIceCandidate(p0, true)
                    mRtcClient.value?.addIceCandidate(p0)
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

        mRtcClient.value?.initSurfaceView(videoView)
        mRtcClient.value?.startLocalVideoCapture(videoView)
        mSignalingClient.value =
            SignalingClient(userId, sensorId, createSignallingClientListener())
        mRtcClient.value?.call(sdpObserver, userId, sensorId)
    }

    fun switchCamera() {
        mRtcClient.value?.switchCamera()
    }

    fun endCall(recall: Boolean = false) {
        mRtcClient.value?.endCall(userId, sensorId, recall)
    }
}

class SensorMainViewModelFactory(private val userId: String, private val sensorId: String) :
    ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SensorMainViewModel(userId, sensorId) as T
    }
}
