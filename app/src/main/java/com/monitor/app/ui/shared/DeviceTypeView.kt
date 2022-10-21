package com.monitor.app.ui.shared

import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.monitor.app.R
import com.monitor.app.core.components.SelectableItem
import com.monitor.app.data.utils.DataStoreUtil
import kotlinx.coroutines.launch

@Composable
fun DeviceTypeView(onDeviceSelected: (isMain: Boolean) -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val dataStore = DataStoreUtil(context)

    var deviceTypeIsMain: Boolean? by remember { mutableStateOf(null) }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceAround,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.choose_device_type),
            style = MaterialTheme.typography.h4,
            textAlign = TextAlign.Center
        )
        Column(modifier = Modifier.padding(horizontal = 60.dp)) {
            SelectableItem(
                selected = deviceTypeIsMain ?: false,
                title = stringResource(R.string.device_main),
                subtitle = stringResource(R.string.device_main_info)
            ) {
                deviceTypeIsMain = it
            }
            Spacer(modifier = Modifier.height(12.dp))
            SelectableItem(
                selected = deviceTypeIsMain?.let { !it } ?: false,
                title = stringResource(R.string.device_sensor),
                subtitle = stringResource(R.string.device_sensor_info)
            ) {
                deviceTypeIsMain = !it
            }
        }

        Button(onClick = {
            scope.launch {
                val isMain = deviceTypeIsMain ?: return@launch
                dataStore.saveDeviceType(isMain)
                onDeviceSelected(isMain)
            }
        }, enabled = deviceTypeIsMain?.let { true } ?: false) {
            Text(text = stringResource(R.string.next))
        }
    }
}