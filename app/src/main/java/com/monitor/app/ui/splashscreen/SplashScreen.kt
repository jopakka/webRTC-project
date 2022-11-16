package com.monitor.app.ui.splashscreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
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
            .background(color = colorResource(R.color.ic_launcher_new_background))
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_launcher_new_foreground),
            contentDescription = null,
            modifier = Modifier.size(256.dp)
        )
        Text(
            stringResource(R.string.app_name),
            style = MaterialTheme.typography.h2,
            textAlign = TextAlign.Center
        )
        CircularProgressIndicator()
    }
}