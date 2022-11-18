package com.monitor.app.ui.sensor.init

import android.widget.Toast
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.monitor.app.R
import com.monitor.app.core.components.*
import com.monitor.app.data.utils.DataStoreUtil
import kotlinx.coroutines.launch

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
    val scope = rememberCoroutineScope()
    val dataStore = DataStoreUtil(context)

    val onClick = {
        loading = true
        viewModel.addSensor(name, info) { id, error ->
            loading = false
            if (error != null || id == null) {
                errorToast.show()
                return@addSensor
            }
            scope.launch {
                dataStore.saveDeviceId(id)
                onSensorAdded(id)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(id = R.string.app_name)) },
                contentColor = Color.White,
                elevation = 10.dp
            )
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Title(text = stringResource(R.string.give_sensor_info))
                MCard {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        TextInputField(
                            value = name,
                            label = stringResource(R.string.tf_name_title),
                            placeholder = stringResource(R.string.tf_name_placeholder),
                            onChange = {
                                name = it
                            }
                        )
                        TextInputField(
                            value = info,
                            label = stringResource(R.string.tf_info_title),
                            placeholder = stringResource(R.string.tf_info_placeholder),
                            onChange = {
                                info = it
                            }
                        )
                        MButton(
                            text = stringResource(R.string.ready),
                            enabled = name.isNotBlank() && !loading,
                            onClick = onClick,
                        )
                    }
                }
            }
        })
    LoadingIndicator(loading)
}