package com.monitor.app.main.ui

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.monitor.app.main.TestViewModel
import com.monitor.app.classes.SampleData
import com.monitor.app.classes.SensorInfo
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.navigation.NavHostController

@Composable
fun SensorsScreen(navigator: NavHostController, viewModel: TestViewModel = viewModel()) {
    val sensors = viewModel.sensors
    var name by remember { mutableStateOf("") }
    var info by remember { mutableStateOf("") }
    val openDialog = remember { mutableStateOf(false) }

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
                SensorList(sensors = sensors)
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
                        label = {Text("Name")},
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
                    )
                    TextField(
                        value = info,
                        onValueChange = {
                            info = it
                        },
                        placeholder = { Text("Behind garage door, etc...") },
                        label = {Text("Info")},
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

@Preview
@Composable
fun PreviewSensorList() {
    SensorList(SampleData.sensors)
}

@Composable
fun SensorList(sensors: List<SensorInfo>) {
    Surface(modifier = Modifier.fillMaxSize()) {
        LazyColumn {
            items(sensors) { sensor ->
                ListItem(sensor)
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ListItem(sensorInfo: SensorInfo) {
    Card(modifier = Modifier
        .padding(all = 8.dp)
        .fillMaxWidth(),
        onClick = { Log.d("ListItem", "HELLO ${sensorInfo.name}") }) {
        Column(modifier = Modifier.padding(all = 4.dp)) {
            Text(text = sensorInfo.name, style = MaterialTheme.typography.subtitle1)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = sensorInfo.info, style = MaterialTheme.typography.subtitle1)
        }
    }
}

@Preview
@Composable
fun PreviewListItem() {
    ListItem(SensorInfo("Testi", "Jee"))
}