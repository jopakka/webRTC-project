package com.monitor.app.core.components

import android.view.LayoutInflater
import androidx.compose.runtime.Composable
import androidx.compose.ui.viewinterop.AndroidView
import com.monitor.app.R
import org.webrtc.SurfaceViewRenderer

@Composable
fun WebRTCVideoView(
    videoViewReady: (videoView: SurfaceViewRenderer) -> Unit
) {
    AndroidView(
        factory = {
            val view = LayoutInflater.from(it).inflate(R.layout.webrtc_video_view, null, false)
            val videoView = view.findViewById<SurfaceViewRenderer>(R.id.video_view)
            videoViewReady(videoView)
            view
        },
    )
}