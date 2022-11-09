package com.monitor.app.core.components

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import com.monitor.app.core.DeviceTypes
import com.monitor.app.data.utils.DataStoreUtil

@Composable
fun GetDataStoreValues(onDataStoreFound: (deviceType: DeviceTypes, id: String) -> Unit) {
    val context = LocalContext.current
    val dataStore = DataStoreUtil(context)
    val savedDeviceTypeIsMain by dataStore.getDeviceType.collectAsState(initial = null)
    val savedSensorId by dataStore.getSensorId.collectAsState(initial = null)

    /**
     * Inside let, navigate to corresponding view
     * based on value of savedDeviceType
     */
    LaunchedEffect(savedDeviceTypeIsMain, savedSensorId) {
        Log.d(
            "GetDataStoreValues",
            "savedDeviceTypeIsMain=$savedDeviceTypeIsMain; savedSensorId=$savedSensorId"
        )
        if (savedDeviceTypeIsMain != null && savedSensorId != null) {
            onDataStoreFound(savedDeviceTypeIsMain!!, savedSensorId!!)
        }
    }
}