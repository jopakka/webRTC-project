package com.monitor.app.data.rtcclient

import android.util.Log
import org.webrtc.*

open class PeerConnectionObserver(
    private val dataObserver: DataChannelObserver,
) : PeerConnection.Observer {
    companion object {
        private const val TAG = "PeerConnectionObserver"
    }

    override fun onIceCandidate(p0: IceCandidate?) {
        Log.d(TAG, "onIceCandidate: $p0")
    }

    override fun onDataChannel(p0: DataChannel?) {
        Log.d(TAG, "onDataChannel: $p0")
        p0?.registerObserver(dataObserver)
    }

    override fun onIceConnectionReceivingChange(p0: Boolean) {
        Log.d(TAG, "onIceConnectionReceivingChange: $p0")
    }

    override fun onIceConnectionChange(p0: PeerConnection.IceConnectionState?) {
        Log.d(TAG, "onIceConnectionChange: $p0")
    }

    override fun onIceGatheringChange(p0: PeerConnection.IceGatheringState?) {
        Log.d(TAG, "onIceGatheringChange: $p0")
    }

    override fun onAddStream(p0: MediaStream?) {
        Log.d(TAG, "onAddStream: $p0")
    }

    override fun onSignalingChange(p0: PeerConnection.SignalingState?) {
        Log.d(TAG, "onSignalingChange: $p0")
    }

    override fun onIceCandidatesRemoved(p0: Array<out IceCandidate>?) {
        Log.d(TAG, "onIceCandidatesRemoved: $p0")
    }

    override fun onRemoveStream(p0: MediaStream?) {
        Log.d(TAG, "onRemoveStream: $p0")
    }

    override fun onRenegotiationNeeded() {
        Log.d(TAG, "onRenegotiationNeeded")
    }

    override fun onAddTrack(p0: RtpReceiver?, p1: Array<out MediaStream>?) {
        Log.d(TAG, "onAddTrack: receiver=$p0, stream=$p1")
    }

    override fun onStandardizedIceConnectionChange(newState: PeerConnection.IceConnectionState?) {
        super.onStandardizedIceConnectionChange(newState)
        Log.d(TAG, "onStandardizedIceConnectionChange: $newState")
    }

    override fun onConnectionChange(newState: PeerConnection.PeerConnectionState?) {
        super.onConnectionChange(newState)
        Log.d(TAG, "onConnectionChange: $newState")
    }

    override fun onSelectedCandidatePairChanged(p0: CandidatePairChangeEvent?) {
        super.onSelectedCandidatePairChanged(p0)
        Log.d(TAG, "onSelectedCandidatePairChanged: $p0")
    }

    override fun onTrack(transceiver: RtpTransceiver?) {
        super.onTrack(transceiver)
        Log.d(TAG, "onTrack: $transceiver")
    }
}