package com.monitor.app.ui.control.sensor

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.monitor.app.core.DataCommands
import com.monitor.app.core.constants.Constants
import com.monitor.app.data.rtcclient.AppSdpObserver
import com.monitor.app.data.rtcclient.DataChannelObserver
import com.monitor.app.data.rtcclient.PeerConnectionObserver
import com.monitor.app.data.rtcclient.RTCClient
import com.monitor.app.data.signalingclient.SignalingClient
import com.monitor.app.data.signalingclient.SignalingClientObserver
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.webrtc.IceCandidate
import org.webrtc.MediaStream
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
    private val sdpObserver = object : AppSdpObserver() {}

    fun init(application: Application, videoView: SurfaceViewRenderer) {
        if (isInitialized) {
            return
        }
        isInitialized = true

        mRtcClient.value = RTCClient(
            application,
            object : PeerConnectionObserver(dataObserver = object : DataChannelObserver() {}) {
                override fun onIceCandidate(p0: IceCandidate?) {
                    super.onIceCandidate(p0)
                    mSignalingClient.value?.sendIceCandidate(p0, false)
                    mRtcClient.value?.addIceCandidate(p0)
                }

                override fun onAddStream(p0: MediaStream?) {
                    super.onAddStream(p0)
                    p0?.videoTracks?.get(0)?.addSink(videoView)
                }
            }
        )

        mRtcClient.value?.initSurfaceView(videoView)

        mSignalingClient.value = SignalingClient(
            userId,
            sensorId,
            object : SignalingClientObserver(mRtcClient.value, sdpObserver, userId, sensorId) {
                override fun onCallEnded() {
                    super.onCallEnded()
                    if (Constants.selfEndedCall) return
                    endCall()
                }
            })
    }

    fun endCall(recall: Boolean = false) {
        Constants.selfEndedCall = !recall
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
