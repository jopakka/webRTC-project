package com.monitor.app.core.components

import androidx.compose.foundation.layout.Row
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import com.monitor.app.R

@Composable
fun BatteryLevel(batteryLevel: Int?) {


    Row(verticalAlignment = Alignment.Bottom) {
        Icon(
            painter = painterResource(id = R.drawable.ic_baseline_battery_4_bar_24),
            contentDescription = "moro",
            tint = Color.White
        )
        Text(
            text = "${batteryLevel ?: 0} %",
            style = TextStyle(
                color = Color.White,
                fontWeight = FontWeight.Medium,
                fontSize = MaterialTheme.typography.subtitle1.fontSize
            )
        )
    }
}

