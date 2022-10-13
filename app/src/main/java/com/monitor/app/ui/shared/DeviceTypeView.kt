package com.monitor.app.ui.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.monitor.app.core.components.DeviceTypeCheckBox

@Composable
fun DeviceTypeView(navController: NavHostController) {


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colors.background),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceAround
    ) {
        Text(text = "Choose your device type", style = MaterialTheme.typography.h4)
        DeviceTypeCheckBox()
        Button(onClick = { navController.navigate("main") }) {
            Text(text = "NEXT")
        }
    }

}

