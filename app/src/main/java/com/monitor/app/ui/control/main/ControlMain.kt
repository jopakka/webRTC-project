package com.monitor.app.ui.control.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.monitor.app.core.components.SensorList
import com.monitor.app.core.constants.Constants

@Composable
fun ControlMainScreen(navigator: NavHostController, viewModel: ControlMainViewModel = viewModel()) {
    val sensors = viewModel.sensors
    var name by remember { mutableStateOf("") }
    var info by remember { mutableStateOf("") }
    val openDialog = remember { mutableStateOf(false) }

    Constants.isIntiatedNow = true
    Constants.isCallEnded = true

    val fabOnClick = {
        openDialog.value = true
    }

    val clearFields = {
        name = ""
        info = ""
    }

    val closeDialog = {
        openDialog.value = false
        clearFields()
    }

    val addOnClick = {
        viewModel.addSensor(name, info, navigator)
        closeDialog()
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = fabOnClick) {
                Icon(Icons.Filled.Add, null)
            }
        },
        content = {
            Column(
                modifier = Modifier
                    .background(color = MaterialTheme.colors.background)
                    .padding(it)
            ) {
                Text(text = "Sensors", style = MaterialTheme.typography.h1)
                SensorList(sensors = sensors) { id ->
                    val user = "user-1"
                    navigator.navigate("sensorView/$user/$id")
                }
            }
        }
    )

    if (openDialog.value) {
        AlertDialog(
            onDismissRequest = closeDialog,
            title = { Text(text = "Title") },
            text = {
                Column(modifier = Modifier.padding(8.dp)) {
                    TextField(
                        value = name,
                        onValueChange = {
                            name = it
                        },
                        placeholder = { Text("Living room, yard, etc...") },
                        label = { Text("Name") },
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
                    )
                    TextField(
                        value = info,
                        onValueChange = {
                            info = it
                        },
                        placeholder = { Text("Behind garage door, etc...") },
                        label = { Text("Info") },
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = addOnClick) {
                    Text("ADD")
                }
            },
            dismissButton = {
                TextButton(onClick = closeDialog) {
                    Text(text = "CANCEL")
                }
            }
        )
    }
}
