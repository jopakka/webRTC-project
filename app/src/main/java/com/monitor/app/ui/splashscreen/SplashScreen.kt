package com.monitor.app.ui.splashscreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.monitor.app.R
import com.monitor.app.core.DeviceTypes
import com.monitor.app.core.components.GetDataStoreValues

@Composable
fun SplashScreen(onDataStore: (deviceType: DeviceTypes, sensorId: String) -> Unit) {
    GetDataStoreValues { deviceType, id ->
        onDataStore(deviceType, id)
    }

    Column(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            stringResource(R.string.app_name),
            style = MaterialTheme.typography.h2,
            textAlign = TextAlign.Center
        )
        CircularProgressIndicator()
    }
}