package com.monitor.app.ui.shared

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.monitor.app.R
import com.monitor.app.core.DeviceTypes
import com.monitor.app.core.components.SelectableItem
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
                title = { Text(text = "House Monitor System") },
                backgroundColor = Color(0xFFE39E37),
                contentColor = Color.White,
                elevation = 10.dp
            )
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.choose_device_type),
                    style = MaterialTheme.typography.h4,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Column(modifier = Modifier.padding(horizontal = 60.dp, vertical = 40.dp)) {
                    SelectableItem(
                        selected = deviceType == DeviceTypes.MAIN,
                        title = stringResource(R.string.device_main),
                        subtitle = stringResource(R.string.device_main_info)
                    ) {
                        deviceType = DeviceTypes.MAIN
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    SelectableItem(
                        selected = deviceType == DeviceTypes.SENSOR,
                        title = stringResource(R.string.device_sensor),
                        subtitle = stringResource(R.string.device_sensor_info)
                    ) {
                        deviceType = DeviceTypes.SENSOR
                    }
                }

                Button(onClick = {
                    scope.launch {
                        deviceType ?: return@launch
                        dataStore.saveDeviceType(deviceType!!)
                        onDeviceSelected(deviceType!!)
                    }
                },
                    enabled = deviceType?.let { true } ?: false,
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFE39E37)),
                    shape = RoundedCornerShape(50.dp),
                    modifier = Modifier
                        .padding(horizontal = 60.dp)
                        .fillMaxWidth()
                )
                {
                    Text(
                        text = stringResource(R.string.next),
                        color = Color.White,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        })

}