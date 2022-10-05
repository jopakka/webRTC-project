package fi.joonasniemi.innovators.sensor.ui

import android.view.LayoutInflater
import androidx.compose.foundation.layout.padding
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Adjust
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.viewinterop.AndroidView
import fi.joonasniemi.innovators.R
import fi.joonasniemi.innovators.RTCClient
import org.webrtc.SurfaceViewRenderer

@Composable
fun SensorSendScreen(rtcClient: RTCClient) {
    VideoView(rtcClient)
}

@Composable
fun VideoView(rtcClient: RTCClient) {
    KeepScreenOn()
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = {
                rtcClient.switchCamera()
            }) {
                Icon(Icons.Filled.Adjust, null)
            }
        }
    ) {
        AndroidView(
            factory = { context ->
                val view =
                    LayoutInflater.from(context).inflate(R.layout.activity_sensor_send, null, false)
                val localView = view.findViewById<SurfaceViewRenderer>(R.id.local_view)
                rtcClient.initSurfaceView(localView)
                rtcClient.startLocalVideoCapture(localView)
                view
            },
            modifier = Modifier.padding(it)
        )
    }
}

@Composable
fun KeepScreenOn() {
    val currentView = LocalView.current
    DisposableEffect(Unit) {
        currentView.keepScreenOn = true
        onDispose {
            currentView.keepScreenOn = false
        }
    }
}