package com.monitor.app.data.signalingclient

import android.util.Log
import com.monitor.app.core.constants.Constants
import com.monitor.app.data.rtcclient.RTCClient
import org.webrtc.IceCandidate
import org.webrtc.SdpObserver
import org.webrtc.SessionDescription

open class SignalingClientObserver(
    private val rtcClient: RTCClient?,
    private val sdpObserver: SdpObserver,
    private val userId: String,
    private val sensorId: String
) : ISignalingClientListener {
    companion object {
        private const val TAG = "SignalingClientObserver"
    }

    override fun onConnectionEstablished() {
        Log.d(TAG, "onConnectionEstablished")
    }

    override fun onOfferReceived(description: SessionDescription) {
        Log.d(TAG, "onOfferReceived: $description")
        rtcClient?.onRemoteSessionReceived(description)
        Constants.isIntiatedNow = false
        rtcClient?.answer(sdpObserver, userId, sensorId)
    }

    override fun onAnswerReceived(description: SessionDescription) {
        Log.d(TAG, "onAnswerReceived: $description")
        rtcClient?.onRemoteSessionReceived(description)
        Constants.isIntiatedNow = false
    }

    override fun onIceCandidateReceived(iceCandidate: IceCandidate) {
        Log.d(TAG, "onIceCandidateReceived: $iceCandidate")
        rtcClient?.addIceCandidate(iceCandidate)
    }

    override fun onCallEnded() {
        Log.d(TAG, "onCallEnded")
    }
}