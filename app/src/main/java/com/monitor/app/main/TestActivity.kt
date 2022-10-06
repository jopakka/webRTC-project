package com.monitor.app.main

import android.app.AlertDialog
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.monitor.app.classes.*
import com.monitor.app.nav.AppNavHost
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.webrtc.*

@ExperimentalCoroutinesApi
class TestActivity : ComponentActivity() {
    companion object {
        private const val CAMERA_AUDIO_PERMISSION_REQUEST_CODE = 1
        private const val CAMERA_PERMISSION = android.Manifest.permission.CAMERA
        private const val AUDIO_PERMISSION = android.Manifest.permission.RECORD_AUDIO
        private const val TAG = "TestActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppNavHost()
        }
    }

//    private lateinit var rtcClient: RTCClient
//    private lateinit var signallingClient: SignalingClient
//    private val audioManager by lazy { RTCAudioManager.create(this) }
//
//    private val sdpObserver = object : AppSdpObserver() {
//        override fun onCreateSuccess(p0: SessionDescription?) {
//            super.onCreateSuccess(p0)
////            signallingClient.send(p0)
//        }
//    }
//
//    private var isJoin = false
//
//
//
//    private fun checkCameraAndAudioPermission() {
//        if ((ContextCompat.checkSelfPermission(this, CAMERA_PERMISSION)
//                    != PackageManager.PERMISSION_GRANTED) &&
//            (ContextCompat.checkSelfPermission(this, AUDIO_PERMISSION)
//                    != PackageManager.PERMISSION_GRANTED)
//        ) {
//            requestCameraAndAudioPermission()
//        } else {
//            onCameraAndAudioPermissionGranted()
//        }
//    }
//
//    private fun requestCameraAndAudioPermission(dialogShown: Boolean = false) {
//        if (ActivityCompat.shouldShowRequestPermissionRationale(
//                this,
//                CAMERA_PERMISSION
//            ) &&
//            ActivityCompat.shouldShowRequestPermissionRationale(this, AUDIO_PERMISSION) &&
//            !dialogShown
//        ) {
//            showPermissionRationaleDialog()
//        } else {
//            ActivityCompat.requestPermissions(
//                this, arrayOf(
//                    CAMERA_PERMISSION,
//                    AUDIO_PERMISSION
//                ), CAMERA_AUDIO_PERMISSION_REQUEST_CODE
//            )
//        }
//    }
//
//    private fun onCameraAndAudioPermissionGranted() {
//        rtcClient = RTCClient(
//            application,
//            object : PeerConnectionObserver() {
//                override fun onIceCandidate(p0: IceCandidate?) {
//                    super.onIceCandidate(p0)
//                    Log.d(TAG, "onIceCandidate: candidate=$p0")
//                    signallingClient.sendIceCandidate(p0, isJoin)
//                    rtcClient.addIceCandidate(p0)
//                }
//
//                override fun onAddStream(p0: MediaStream?) {
//                    super.onAddStream(p0)
//                    Log.d(TAG, "onAddStream: $p0")
////                    p0?.videoTracks?.get(0)?.addSink(remote_view)
//                }
//
//                override fun onIceConnectionChange(p0: PeerConnection.IceConnectionState?) {
//                    Log.d(TAG, "onIceConnectionChange: $p0")
//                }
//
//                override fun onIceConnectionReceivingChange(p0: Boolean) {
//                    Log.d(TAG, "onIceConnectionReceivingChange: $p0")
//                }
//
//                override fun onConnectionChange(newState: PeerConnection.PeerConnectionState?) {
//                    Log.d(TAG, "onConnectionChange: $newState")
//                }
//
//                override fun onDataChannel(p0: DataChannel?) {
//                    Log.d(TAG, "onDataChannel: $p0")
//                }
//
//                override fun onStandardizedIceConnectionChange(newState: PeerConnection.IceConnectionState?) {
//                    Log.d(TAG, "onStandardizedIceConnectionChange: $newState")
//                }
//
//                override fun onAddTrack(p0: RtpReceiver?, p1: Array<out MediaStream>?) {
//                    Log.d(TAG, "onAddTrack: $p0 \n $p1")
//                }
//
//                override fun onTrack(transceiver: RtpTransceiver?) {
//                    Log.d(TAG, "onTrack: $transceiver")
//                }
//            }
//        )
//
////        signallingClient = SignalingClient(meetingID, createSignallingClientListener())
////        if (!isJoin)
////            rtcClient.call(sdpObserver, meetingID)
//    }
//
//    private fun createSignallingClientListener() = object : SignalingClientListener {
//        override fun onConnectionEstablished() {
//            Log.d(TAG, "onConnectionEstablished")
//        }
//
//        override fun onOfferReceived(description: SessionDescription) {
//            Log.d(TAG, "onOfferReceived")
//        }
//
//        override fun onAnswerReceived(description: SessionDescription) {
//            Log.d(TAG, "onAnswerReceived")
//        }
//
//        override fun onIceCandidateReceived(iceCandidate: IceCandidate) {
//            Log.d(TAG, "onIceCandidateReceived")
//        }
//
//        override fun onCallEnded() {
//            Log.d(TAG, "onCallEnded")
//        }
//    }
//
//    private fun showPermissionRationaleDialog() {
//        AlertDialog.Builder(this)
//            .setTitle("Camera And Audio Permission Required")
//            .setMessage("This app need the camera and audio to function")
//            .setPositiveButton("Grant") { dialog, _ ->
//                dialog.dismiss()
//                requestCameraAndAudioPermission(true)
//            }
//            .setNegativeButton("Deny") { dialog, _ ->
//                dialog.dismiss()
//                onCameraPermissionDenied()
//            }
//            .show()
//    }
//
//    private fun onCameraPermissionDenied() {
//        Toast.makeText(this, "Camera and Audio Permission Denied", Toast.LENGTH_LONG).show()
//    }
}