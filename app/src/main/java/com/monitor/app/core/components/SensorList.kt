package com.monitor.app.core.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.monitor.app.R
import com.monitor.app.data.rtcclient.model.ButtonData
import com.monitor.app.data.rtcclient.model.SampleData
import com.monitor.app.data.rtcclient.model.SensorInfo

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SensorList(
    sensors: List<SensorInfo>,
    changeItemVisibility: ((id: String, state: Boolean) -> Unit)? = null,
    itemOnClick: (id: String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(sensors) { sensor ->
            var visible by remember {
                mutableStateOf(sensor.visible)
            }
            var showDialog by remember {
                mutableStateOf(false)
            }
            val dismissState = rememberDismissState(
                confirmStateChange = {
                    if (it == DismissValue.DismissedToEnd) {
                        showDialog = true
                        visible = !visible
                    }
                    it != DismissValue.DismissedToEnd
                }
            )
            val closeDialog = {
                showDialog = false
            }
            if (showDialog) {
                AlertDialogMultipleButtons(
                    buttons = listOf(
                        ButtonData(stringResource(R.string.no), closeDialog),
                        ButtonData(stringResource(R.string.yes)) {
                            changeItemVisibility?.invoke(sensor.id!!, false)
                            closeDialog()
                        }
                    ),
                    title = "Confirm",
                    text = "Are you sure that you want to hide this camera?",
                    onClose = closeDialog,
                )
            }
            SwipeToDismiss(
                state = dismissState,
                directions = setOf(DismissDirection.StartToEnd),
                dismissThresholds = { FractionalThreshold(0.2f) },
                background = {
                    val direction = dismissState.dismissDirection ?: return@SwipeToDismiss
                    val color by animateColorAsState(
                        when (dismissState.targetValue) {
                            DismissValue.Default -> Color.LightGray
                            DismissValue.DismissedToEnd -> Color.Red
                            DismissValue.DismissedToStart -> Color.Green
                        }
                    )
                    val alignment = when (direction) {
                        DismissDirection.StartToEnd -> Alignment.CenterStart
                        DismissDirection.EndToStart -> Alignment.CenterEnd
                    }
                    val icon = when (direction) {
                        DismissDirection.StartToEnd -> Icons.Default.Delete
                        DismissDirection.EndToStart -> Icons.Default.Done
                    }
                    val scale by animateFloatAsState(
                        if (dismissState.targetValue == DismissValue.Default) 0.75f else 1f
                    )

                    Box(
                        Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(size = 10.dp))
                            .background(color)
                            .padding(horizontal = 20.dp),
                        contentAlignment = alignment
                    ) {
                        Icon(
                            icon,
                            contentDescription = "Localized description",
                            modifier = Modifier.scale(scale)
                        )
                    }
                }) {
                SensorListItem(sensor, itemOnClick)
            }
        }
    }
}

@Preview
@Composable
fun PreviewSensorList() {
    SensorList(SampleData.sensors) {}
}
