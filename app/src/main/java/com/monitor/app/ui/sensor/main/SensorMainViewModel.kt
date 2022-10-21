package com.monitor.app.ui.sensor.main

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.monitor.app.core.constants.Constants
import com.monitor.app.data.rtcclient.AppSdpObserver
import com.monitor.app.data.rtcclient.DataChannelObserver
import com.monitor.app.data.rtcclient.PeerConnectionObserver
import com.monitor.app.data.rtcclient.RTCClient
import com.monitor.app.data.signalingclient.SignalingClient
import com.monitor.app.data.signalingclient.SignalingClientObserver
import io.ktor.util.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.webrtc.DataChannel
import org.webrtc.SurfaceViewRenderer

@OptIn(ExperimentalCoroutinesApi::class)
class SensorMainViewModel(private val userId: String, private val sensorId: String) : ViewModel() {
    companion object {
        private const val TAG = "SensorSendViewModel"
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
            object : PeerConnectionObserver(mSignalingClient.value, mRtcClient.value, true, object :
                DataChannelObserver() {
                override fun onMessage(p0: DataChannel.Buffer?) {
                    super.onMessage(p0)
                    val byteBuffer = p0?.data?.moveToByteArray() ?: ByteArray(0)

                    when (String(byteBuffer)) {
                        "flash" -> Log.d(TAG, "this should toggle flash")
                        "camera" -> Log.d(TAG, "this switch cameras")
                        else -> Log.d(TAG, "Unknown command")
                    }
                }
            }) {}
        )

        mRtcClient.value?.initSurfaceView(videoView)
        mRtcClient.value?.startLocalVideoCapture(videoView)
        mSignalingClient.value = SignalingClient(
            userId,
            sensorId,
            object : SignalingClientObserver(mRtcClient.value, sdpObserver, userId, sensorId) {
                override fun onCallEnded() {
                    super.onCallEnded()
                    if (Constants.selfEndedCall) return
                    endCall(true)
                }
            })
        mRtcClient.value?.call(sdpObserver, userId, sensorId)
    }

    fun switchCamera() {
        mRtcClient.value?.switchCamera()
    }

    fun endCall(recall: Boolean = false) {
        Constants.selfEndedCall = !recall
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
