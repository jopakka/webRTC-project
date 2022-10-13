package com.monitor.app.data.rtcclient

import android.util.Log
import org.webrtc.SdpObserver
import org.webrtc.SessionDescription

open class AppSdpObserver : SdpObserver {
    companion object {
        private const val TAG = "AppSdpObserver"
    }

    override fun onSetFailure(p0: String?) {
        Log.e(TAG, "onSetFailure: $p0")
    }

    override fun onSetSuccess() {
        Log.d(TAG, "onSetSuccess")
    }

    override fun onCreateSuccess(p0: SessionDescription?) {
        Log.d(TAG, "onCreateSuccess: $p0")
    }

    override fun onCreateFailure(p0: String?) {
        Log.e(TAG, "onCreateFailure: $p0")
    }
}