package com.monitor.app.core.components

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import com.monitor.app.data.utils.DataStoreUtil

@Composable
fun GetDataStoreValues(onDataStoreFound: (isMain: Boolean, id: String?) -> Unit) {
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
        onDataStoreFound(savedDeviceTypeIsMain == true, savedSensorId)
    }
}