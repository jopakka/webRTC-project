package com.monitor.app.data.signalingclient

import android.util.Log
import org.webrtc.IceCandidate
import org.webrtc.SessionDescription

open class SignalingClientObserver : ISignalingClientListener {
    companion object {
        private const val TAG = "SignalingClientObserver"
    }

    override fun onConnectionEstablished() {
        Log.d(TAG, "onConnectionEstablished")
    }

    override fun onOfferReceived(description: SessionDescription) {
        Log.d(TAG, "onOfferReceived: $description")
    }

    override fun onAnswerReceived(description: SessionDescription) {
        Log.d(TAG, "onAnswerReceived: $description")
    }

    override fun onIceCandidateReceived(iceCandidate: IceCandidate) {
        Log.d(TAG, "onIceCandidateReceived: $iceCandidate")
    }

    override fun onCallEnded() {
        Log.d(TAG, "onCallEnded")
    }
}