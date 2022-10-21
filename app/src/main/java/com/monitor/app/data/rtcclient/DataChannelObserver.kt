package com.monitor.app.data.rtcclient

import android.util.Log
import org.webrtc.DataChannel

open class DataChannelObserver : DataChannel.Observer {
    companion object {
        private const val TAG = "DataChannelObserver"
    }

    override fun onBufferedAmountChange(p0: Long) {
        Log.d(TAG, "onBufferedAmountChange: $p0")
    }

    override fun onStateChange() {
        Log.d(TAG, "onStateChange")
    }

    override fun onMessage(p0: DataChannel.Buffer?) {
        Log.d(TAG, "onMessage: $p0")
    }
}