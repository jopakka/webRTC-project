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
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.monitor.app.core.DataCommands
import com.monitor.app.core.SensorStatuses
import com.monitor.app.core.constants.Constants
import com.monitor.app.data.rtcclient.*
import com.monitor.app.data.signalingclient.SignalingClient
import com.monitor.app.data.signalingclient.SignalingClientObserver
import io.ktor.util.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.webrtc.DataChannel
import org.webrtc.IceCandidate
import org.webrtc.MediaStream
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
    private var audioManager: RTCAudioManager? = null

    private val _name = MutableStateFlow<String?>(null)
    val name: StateFlow<String?> = _name.asStateFlow()

    fun init(
        application: Application,
        localView: SurfaceViewRenderer,
        remoteView: SurfaceViewRenderer
    ) {
        if (isInitialized) {
            return
        }
        isInitialized = true

        val document = firestore.collection(userId).document(sensorId)
        val collection = document.collection("candidates")
        val answer = collection.document("answerCandidate")
        val offer = collection.document("offerCandidate")

        firestore.runBatch {
            answer.delete()
            offer.delete()
            document.update(
                hashMapOf<String, Any>(
                    "type" to FieldValue.delete(),
                    "sdp" to FieldValue.delete(),
                )
            )
        }.addOnSuccessListener {
            audioManager = RTCAudioManager.create(application)

            mRtcClient.value = RTCClient(
                application,
                object : PeerConnectionObserver(object :
                    DataChannelObserver() {
                    override fun onMessage(p0: DataChannel.Buffer?) {
                        super.onMessage(p0)
                        val byteBuffer = p0?.data?.moveToByteArray() ?: ByteArray(0)
                        val message = String(byteBuffer)
                        Log.d(TAG, "message: $message")

                        when (DataCommands.valueOf(message)) {
                            DataCommands.CAMERA -> switchCamera()
                        }
                    }
                }) {
                    override fun onIceCandidate(p0: IceCandidate?) {
                        super.onIceCandidate(p0)
                        mSignalingClient.value?.sendIceCandidate(p0, true)
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
            mSignalingClient.value = SignalingClient(
                userId,
                sensorId,
                object : SignalingClientObserver(mRtcClient.value, sdpObserver, userId, sensorId) {
                    override fun onCallEnded() {
                        super.onCallEnded()
                        Log.d(TAG, "onCallEnded selfEndedCall=${Constants.selfEndedCall}")
                        endCall(true)
                    }
                })
            mRtcClient.value?.call(sdpObserver, userId, sensorId)
        }
        getSensorName(document)
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

    private fun getSensorName(document: DocumentReference) {
        try {
            document
                .addSnapshotListener { querySnapshot, e ->
                    if (e != null) {
                        Log.w(TAG, "listen:error", e)
                        return@addSnapshotListener
                    }

                    _name.value = querySnapshot?.get("name") as String?
                }
        } catch (e: Error) {
            Log.e(TAG, "${e.message}")
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
