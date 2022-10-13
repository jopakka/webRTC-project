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
import com.monitor.app.data.signalingclient.ISignalingClientListener
import com.monitor.app.data.signalingclient.SignalingClient
import com.monitor.app.data.signalingclient.SignalingClientObserver
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.webrtc.IceCandidate
import org.webrtc.SessionDescription
import org.webrtc.SurfaceViewRenderer

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
    private val sdpObserver = object : AppSdpObserver() {}

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

    fun init(application: Application, videoView: SurfaceViewRenderer) {
        if (isInitialized) {
            return
        }
        isInitialized = true

        mRtcClient.value = RTCClient(
            application,
            object : PeerConnectionObserver(mSignalingClient.value, mRtcClient.value, true) {}
        )

        mRtcClient.value?.initSurfaceView(videoView)
        mRtcClient.value?.startLocalVideoCapture(videoView)
        mSignalingClient.value = SignalingClient(
            userId,
            sensorId,
            object : SignalingClientObserver() {
                override fun onOfferReceived(description: SessionDescription) {
                    super.onOfferReceived(description)
                    mRtcClient.value?.onRemoteSessionReceived(description)
                    Constants.isIntiatedNow = false
                    mRtcClient.value?.answer(sdpObserver, userId, sensorId)
                }

                override fun onAnswerReceived(description: SessionDescription) {
                    super.onAnswerReceived(description)
                    mRtcClient.value?.onRemoteSessionReceived(description)
                    Constants.isIntiatedNow = false
                }

                override fun onIceCandidateReceived(iceCandidate: IceCandidate) {
                    super.onIceCandidateReceived(iceCandidate)
                    mRtcClient.value?.addIceCandidate(iceCandidate)
                }

                override fun onCallEnded() {
                    super.onCallEnded()
                    mRtcClient.value?.endCall(userId, sensorId, true)
                }
            })
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
