package com.monitor.app.ui.sensor.init

import android.widget.Toast
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.monitor.app.R
import com.monitor.app.core.components.LoadingIndicator
import com.monitor.app.core.components.TextInputField
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
                title = { Text(text = "House Monitor System") },
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
                    stringResource(R.string.give_sensor_info),
                    style = MaterialTheme.typography.h4,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Column(modifier = Modifier.padding(horizontal = 60.dp, vertical = 40.dp)) {
                    TextInputField(
                        stringResource(R.string.tf_name_title),
                        stringResource(R.string.tf_name_placeholder)
                    ) {
                        name = it
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    TextInputField(
                        stringResource(R.string.tf_info_title),
                        stringResource(R.string.tf_info_placeholder)
                    ) {
                        info = it
                    }
                }
                Button(
                    onClick, enabled = name.isNotBlank() && !loading,
                    shape = RoundedCornerShape(50.dp),
                    modifier = Modifier
                        .padding(horizontal = 60.dp)
                        .fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(R.string.ready),
                        color = Color.White,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        })
    LoadingIndicator(loading)
}