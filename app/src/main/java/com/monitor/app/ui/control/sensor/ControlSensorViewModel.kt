package com.monitor.app.ui.control.sensor

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.monitor.app.core.DataCommands
import com.monitor.app.data.rtcclient.*
import com.monitor.app.data.signalingclient.SignalingClient
import com.monitor.app.data.signalingclient.SignalingClientObserver
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.webrtc.IceCandidate
import org.webrtc.MediaStream
import org.webrtc.SessionDescription
import org.webrtc.SurfaceViewRenderer

@OptIn(ExperimentalCoroutinesApi::class)
class ControlSensorViewModel(private val userId: String, private val sensorId: String) :
    ViewModel() {
    companion object {
        private const val TAG = "ControlSensorViewModel"
    }

    private val mRtcClient = mutableStateOf<RTCClient?>(null)
    private val mSignalingClient = mutableStateOf<SignalingClient?>(null)
    private var isInitialized by mutableStateOf(false)
    var isLoading by mutableStateOf(true)
        private set
    var callEnded by mutableStateOf(false)
        private set
    var microphoneState by mutableStateOf(false)
        private set
    private var audioManager: RTCAudioManager? = null

    private val sdpObserver = object : AppSdpObserver() {
        override fun onCreateSuccess(p0: SessionDescription?) {
            super.onCreateSuccess(p0)
            isLoading = false
            callEnded = false
        }
    }

    fun init(
        application: Application,
        remoteView: SurfaceViewRenderer,
        localView: SurfaceViewRenderer
    ) {
        if (isInitialized) {
            return
        }
        isInitialized = true

        audioManager = RTCAudioManager.create(application)

        mRtcClient.value = RTCClient(
            application,
            object : PeerConnectionObserver(object : DataChannelObserver() {}) {
                override fun onIceCandidate(p0: IceCandidate?) {
                    super.onIceCandidate(p0)
                    mSignalingClient.value?.sendIceCandidate(p0, false)
                    mRtcClient.value?.addIceCandidate(p0)
                }

                override fun onAddStream(p0: MediaStream?) {
                    super.onAddStream(p0)
                    p0?.videoTracks?.get(0)?.addSink(remoteView)
                }
            }
        )

        mRtcClient.value?.initSurfaceView(localView)
        mRtcClient.value?.initSurfaceView(remoteView)
        mRtcClient.value?.startLocalVideoCapture(localView)
        mRtcClient.value?.enableAudio(microphoneState)

        mSignalingClient.value = SignalingClient(
            userId,
            sensorId,
            object : SignalingClientObserver(mRtcClient.value, sdpObserver, userId, sensorId) {
                override fun onCallEnded() {
                    super.onCallEnded()
                    endCall()
                }
            })
    }

    fun toggleMicrophone() {
        val newState = !microphoneState
        microphoneState = newState
        mRtcClient.value?.enableAudio(newState)
    }

    fun endCall(recall: Boolean = false) {
        callEnded = true
        mRtcClient.value?.endCall(userId, sensorId, recall)
    }

    fun sendData(command: DataCommands) {
        mRtcClient.value?.sendData(command)
    }
}

class ControlSensorViewModelFactory(private val userId: String, private val sensorId: String) :
    ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ControlSensorViewModel(userId, sensorId) as T
    }
}
