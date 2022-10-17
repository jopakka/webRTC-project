package com.monitor.app.ui.shared

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Checkbox
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.monitor.app.data.utils.DataStoreUtil
import kotlinx.coroutines.launch

@Composable
fun DeviceTypeView(navController: NavHostController) {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val dataStore = DataStoreUtil(context)

    val savedDeviceTypeIsMain by dataStore.getDeviceType.collectAsState(initial = null)

    LaunchedEffect(key1 = savedDeviceTypeIsMain, block = {
        scope.launch {
            savedDeviceTypeIsMain?.let {
                Log.d("devicetypeview", it.toString())
                navController.navigate("main")
            }
        }
    })

    var deviceTypeIsMain: Boolean? by remember { mutableStateOf(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colors.background),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceAround
    ) {
        Text(text = "Choose your device type", style = MaterialTheme.typography.h4)
        Column {
            Row(
                modifier = Modifier.padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val isChecked = remember { mutableStateOf(false) }
                Checkbox(checked = isChecked.value, onCheckedChange = {
                    isChecked.value = it
                    deviceTypeIsMain = !it
                })
                Text(text = "CAMERA DEVICE")
            }
            Row(
                modifier = Modifier.padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val isChecked = remember { mutableStateOf(false) }
                Checkbox(checked = isChecked.value, onCheckedChange = {
                    isChecked.value = it
                    deviceTypeIsMain = it
                })
                Text(text = "CONTROL DEVICE")
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

