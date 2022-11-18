package com.monitor.app.ui.sensor.main

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.monitor.app.core.DataCommands
import com.monitor.app.core.SensorStatuses
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
        private const val TAG = "SensorMainViewModel"
    }

    private val mRtcClient = mutableStateOf<RTCClient?>(null)
    private val mSignalingClient = mutableStateOf<SignalingClient?>(null)
    private var isInitialized by mutableStateOf(false)
    private val sdpObserver = object : AppSdpObserver() {}
    private val firestore = Firebase.firestore
    private var batteryReceiver: BroadcastReceiver? = null

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
                    Log.d(TAG, "onCallEnded selfEndedCall=${Constants.selfEndedCall}")
//                    if (Constants.selfEndedCall) return
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

    fun saveBattery(context: Context) {
        var oldBattery: Int = -1
        val batteryStatus = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { iFilter ->
            batteryReceiver = object : BroadcastReceiver() {
                override fun onReceive(p0: Context?, p1: Intent?) {
                    val battery = p1?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
                    if (battery != -1 && oldBattery != battery) {
                        oldBattery = battery
                        saveBatteryToFirebase(battery)
                    }
                }
            }
            Log.d(TAG, "Register battery")
            context.registerReceiver(batteryReceiver, iFilter)
        }
        oldBattery =
            batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        if (oldBattery != -1) {
            saveBatteryToFirebase(oldBattery)
        }
    }

    fun unregisterBatteryReceiver(context: Context) {
        Log.d(TAG, "Unregister battery")
        try {
            context.unregisterReceiver(batteryReceiver)
        } catch (e: Exception) {
            Log.w(TAG, "${e.message}", e)
        }
    }

    private fun saveBatteryToFirebase(batteryLevel: Int) {
        try {
            val data = mapOf("battery" to batteryLevel)
            firestore.collection(userId).document(sensorId).update(data)
        } catch (e: Exception) {
            Log.e(TAG, "saveBatteryToFirebase", e)
        }
    }

    fun setStatus(status: SensorStatuses) {
        mRtcClient.value?.setStatus(userId, sensorId, status)
    }
}

class SensorMainViewModelFactory(private val userId: String, private val sensorId: String) :
    ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SensorMainViewModel(userId, sensorId) as T
    }
}
