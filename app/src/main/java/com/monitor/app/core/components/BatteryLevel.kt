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
    val batteryLevelIcon = when (batteryLevel) {
        100 -> {
            R.drawable.ic_baseline_battery_full_24
        }
        in 85..99 -> {
            R.drawable.ic_baseline_battery_6_bar_24
        }
        in 68..84 -> {
            R.drawable.ic_baseline_battery_5_bar_24
        }
        in 52..68 -> {
            R.drawable.ic_baseline_battery_4_bar_24
        }
        in 34..51 -> {
            R.drawable.ic_baseline_battery_3_bar_24
        }
        in 17..33 -> {
            R.drawable.ic_baseline_battery_2_bar_24
        }
        in 1..16 -> {
            R.drawable.ic_baseline_battery_1_bar_24
        }
        else -> {
            R.drawable.ic_baseline_battery_0_bar_24
        }
    }
    Row(verticalAlignment = Alignment.Bottom) {
        Icon(
            painter = painterResource(id = batteryLevelIcon),
            contentDescription = "Battery level",
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

