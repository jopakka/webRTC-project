package com.monitor.app.ui.sensor.init

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.viewmodel.compose.viewModel
import com.monitor.app.R
import com.monitor.app.core.components.LoadingIndicator
import com.monitor.app.core.components.TextInputField

@Composable
fun SensorInitScreen(
    userId: String,
    viewModel: SensorInitViewModel = viewModel(
        factory = SensorInitViewModelFactory(userId)
    ),
    onSensorAdded: (id: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var info by remember { mutableStateOf("") }
    val context = LocalContext.current
    val errorToast =
        Toast.makeText(context, stringResource(R.string.error_create_sensor), Toast.LENGTH_SHORT)
    var loading by remember { mutableStateOf(false) }

    val onClick = {
        loading = true
        viewModel.addSensor(name, info) { id, error ->
            loading = false
            if (error != null || id == null) {
                errorToast.show()
                return@addSensor
            }
            onSensorAdded(id)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            stringResource(R.string.give_sensor_info),
            style = MaterialTheme.typography.h4,
            textAlign = TextAlign.Center
        )
        TextInputField(
            stringResource(R.string.tf_name_title),
            stringResource(R.string.tf_name_placeholder)
        ) {
            name = it
        }
        TextInputField(
            stringResource(R.string.tf_info_title),
            stringResource(R.string.tf_info_placeholder)
        ) {
            info = it
        }
        Button(onClick, enabled = name.isNotBlank() && !loading) {
            Text(stringResource(R.string.ready))
        }
    }

    LoadingIndicator(loading)
}