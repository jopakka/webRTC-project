package com.monitor.app.data.signalingclient

import org.webrtc.IceCandidate
import org.webrtc.SessionDescription

interface ISignalingClientListener {
    fun onConnectionEstablished()
    fun onOfferReceived(description: SessionDescription)
    fun onAnswerReceived(description: SessionDescription)
    fun onIceCandidateReceived(iceCandidate: IceCandidate)
    fun onCallEnded()
}