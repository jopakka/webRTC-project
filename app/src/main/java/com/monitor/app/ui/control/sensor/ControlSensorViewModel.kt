package com.monitor.app.ui.control.sensor

import android.app.Application
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.monitor.app.core.constants.Constants
import com.monitor.app.data.rtcclient.AppSdpObserver
import com.monitor.app.data.rtcclient.PeerConnectionObserver
import com.monitor.app.data.rtcclient.RTCClient
import com.monitor.app.data.signalingclient.SignalingClient
import com.monitor.app.data.signalingclient.SignalingClientListener
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.webrtc.*

@OptIn(ExperimentalCoroutinesApi::class)
class ControlSensorViewModel(private val userId: String, private val sensorId: String) :
    ViewModel() {
    companion object {
        private const val TAG = "ControlSensorViewModel"
    }

    private val mRtcClient = mutableStateOf<RTCClient?>(null)
    private val mSignalingClient = mutableStateOf<SignalingClient?>(null)
    private var isInitialized by mutableStateOf(false)

    private val sdpObserver = object : AppSdpObserver() {
        override fun onCreateSuccess(p0: SessionDescription?) {
            super.onCreateSuccess(p0)
            Log.d(TAG, "sdpObserver onCreateSuccess")
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
            if (!Constants.isCallEnded) {
                Constants.isCallEnded = true
//                            rtcClient.endCall(meetingID)
            }
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

        mSignalingClient.value =
            SignalingClient(userId, sensorId, createSignallingClientListener())
    }

    fun endCall(recall: Boolean = false) {
        mRtcClient.value?.endCall(userId, sensorId, recall)
    }
}

class ControlSensorViewModelFactory(private val userId: String, private val sensorId: String) :
    ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ControlSensorViewModel(userId, sensorId) as T
    }
}
