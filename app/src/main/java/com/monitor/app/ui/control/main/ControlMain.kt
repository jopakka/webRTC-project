package com.monitor.app.ui.control.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.monitor.app.R
import com.monitor.app.core.components.SensorList
import com.monitor.app.core.components.Title
import com.monitor.app.core.constants.Constants

@Composable
fun ControlMainScreen(
    userId: String,
    viewModel: ControlMainViewModel = viewModel(
        factory = ControlMainViewModelFactory(userId)
    ),
    onSensorSelected: (id: String) -> Unit,
    onChangeDeviceType: () -> Unit
) {
    val sensors = viewModel.sensors
    var displayMenu by remember { mutableStateOf(false) }

    Constants.isIntiatedNow = true
    Constants.isCallEnded = true

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.app_name)) },
                contentColor = Color.White,
                elevation = 10.dp,
                actions = {
                    IconButton(onClick = { displayMenu = !displayMenu }) {
                        Icon(Icons.Filled.MoreHoriz, contentDescription = "Options")
                    }

                    DropdownMenu(
                        expanded = displayMenu,
                        onDismissRequest = { displayMenu = false }) {
                        DropdownMenuItem(onClick = {
                            onChangeDeviceType()
                        }) {
                            Text(text = "Change device type")
                        }
                    }
                }
            )
        },
        content = {
            Column(
                modifier = Modifier
                    .background(color = MaterialTheme.colors.background)
                    .padding(it),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                Title(stringResource(R.string.camera_devices))
                SensorList(sensors = sensors) { id ->
                    onSensorSelected(id)
                }
            }
        }
    )
}
