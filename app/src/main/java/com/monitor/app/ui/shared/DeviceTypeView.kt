package com.monitor.app.ui.shared

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.monitor.app.R
import com.monitor.app.core.DeviceTypes
import com.monitor.app.core.components.MButton
import com.monitor.app.core.components.SelectableItem
import com.monitor.app.core.components.Title
import com.monitor.app.data.utils.DataStoreUtil
import kotlinx.coroutines.launch

@Composable
fun DeviceTypeView(onDeviceSelected: (isMain: DeviceTypes) -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val dataStore = DataStoreUtil(context)

    var deviceType: DeviceTypes? by remember { mutableStateOf(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(id = R.string.app_name)) },
                elevation = 10.dp
            )
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(30.dp),
            ) {
                Title(text = stringResource(R.string.choose_device_type))
                Column(
                    modifier = Modifier.padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SelectableItem(
                        selected = deviceType == DeviceTypes.MAIN,
                        title = stringResource(R.string.device_main),
                        subtitle = stringResource(R.string.device_main_info)
                    ) {
                        deviceType = DeviceTypes.MAIN
                    }
                    SelectableItem(
                        selected = deviceType == DeviceTypes.SENSOR,
                        title = stringResource(R.string.device_sensor),
                        subtitle = stringResource(R.string.device_sensor_info)
                    ) {
                        deviceType = DeviceTypes.SENSOR
                    }
                    MButton(
                        text = stringResource(R.string.next),
                        enabled = deviceType?.let { true } ?: false) {
                        scope.launch {
                            deviceType ?: return@launch
                            dataStore.saveDeviceType(deviceType!!)
                            onDeviceSelected(deviceType!!)
                        }
                    }
                }
            }
        })
}