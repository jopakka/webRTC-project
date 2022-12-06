package com.monitor.app.ui.control.sensor

import android.app.Application
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.monitor.app.core.DataCommands
import com.monitor.app.data.rtcclient.*
import com.monitor.app.data.signalingclient.SignalingClient
import com.monitor.app.data.signalingclient.SignalingClientObserver
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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

    private val firestore = Firebase.firestore

    private val _name = MutableStateFlow<String?>(null)
    val name: StateFlow<String?> = _name.asStateFlow()

    private val _batteryLevel = MutableStateFlow<Long?>(null)
    val batteryLevel: StateFlow<Long?> = _batteryLevel.asStateFlow()


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
    var shouldNavigateBack by mutableStateOf(false)
        private set

    init {
        Handler(Looper.getMainLooper()).postDelayed({
            if (isLoading) {
                shouldNavigateBack = true
            }
        }, 10_000)
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
        getSensorData()
    }

    private fun getSensorData() {
        try {
            firestore.collection(userId).document(sensorId)
                .addSnapshotListener { querySnapshot, e ->
                    if (e != null) {
                        Log.w(TAG, "listen:error", e)
                        return@addSnapshotListener
                    }

                    _batteryLevel.value = querySnapshot?.get("battery") as Long?
                    _name.value = querySnapshot?.get("name") as String?
                }
        } catch (e: Error) {
            Log.e(TAG, "${e.message}")
        }
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
