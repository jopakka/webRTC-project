package com.monitor.app.ui.shared

import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.monitor.app.core.components.SelectableItem
import com.monitor.app.data.utils.DataStoreUtil
import kotlinx.coroutines.launch

@Composable
fun DeviceTypeView(navController: NavHostController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val dataStore = DataStoreUtil(context)

    val savedDeviceTypeIsMain by dataStore.getDeviceType.collectAsState(initial = null)
    var deviceTypeIsMain: Boolean? by remember { mutableStateOf(null) }

    /**
     * Inside let, navigate to corresponding view
     * based on value of savedDeviceType
     */
    LaunchedEffect(key1 = savedDeviceTypeIsMain) {
        scope.launch {
            savedDeviceTypeIsMain?.let {
                navController.navigate("main")
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceAround,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Choose your device type", style = MaterialTheme.typography.h4)
        Column(modifier = Modifier.padding(horizontal = 60.dp)) {
            SelectableItem(
                selected = deviceTypeIsMain ?: false,
                title = "Main Device",
                subtitle = "Choose this if this device is going to be used to monitor your security cameras"
            ) {
                deviceTypeIsMain = it
            }
            Spacer(modifier = Modifier.height(12.dp))
            SelectableItem(
                selected = deviceTypeIsMain?.let { !it } ?: false,
                title = "Sensor Device",
                subtitle = "Choose this if this device is going to be used as security camera"
            ) {
                deviceTypeIsMain = !it
            }
        }

        Button(onClick = {
            scope.launch {
                dataStore.saveDeviceType(deviceTypeIsMain ?: return@launch)
            }
        }, enabled = deviceTypeIsMain?.let { true } ?: false) {
            Text(text = "NEXT")
        }
    }
}