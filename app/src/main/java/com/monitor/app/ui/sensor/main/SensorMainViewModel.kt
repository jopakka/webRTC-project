package com.monitor.app.ui.sensor.main

import android.app.Application
import android.content.Context
import android.os.BatteryManager
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.monitor.app.core.DataCommands
import com.monitor.app.core.constants.Constants
import com.monitor.app.data.rtcclient.AppSdpObserver
import com.monitor.app.data.rtcclient.DataChannelObserver
import com.monitor.app.data.rtcclient.PeerConnectionObserver
import com.monitor.app.data.rtcclient.RTCClient
import com.monitor.app.data.signalingclient.SignalingClient
import com.monitor.app.data.signalingclient.SignalingClientObserver
import io.ktor.util.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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
    private val firestore = Firebase.firestore

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

                    when (DataCommands.valueOf(String(byteBuffer))) {
                        DataCommands.CAMERA -> switchCamera()
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

    fun saveBattery(context: Context, delayTime: Long = 10_000) {
        var batteryLevel: Int = -1
        viewModelScope.launch {
            while (true) {
                val newBatteryLevel = getBatteryLevel(context)
                if (batteryLevel != newBatteryLevel) {
                    saveBatteryToFirebase(newBatteryLevel)
                    batteryLevel = newBatteryLevel
                }
                delay(delayTime)
            }
        }
    }

    private fun getBatteryLevel(context: Context): Int {
        val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        return batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
    }

    private fun saveBatteryToFirebase(batteryLevel: Int) {
        try {
            val data = mapOf("battery" to batteryLevel)
            firestore.collection(userId).document(sensorId).update(data)
        } catch (e: Exception) {
            Log.e(TAG, "saveBatteryToFirebase", e)
        }
    }
}

class SensorMainViewModelFactory(private val userId: String, private val sensorId: String) :
    ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SensorMainViewModel(userId, sensorId) as T
    }
}
