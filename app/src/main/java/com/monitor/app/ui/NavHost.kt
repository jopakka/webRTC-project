package com.monitor.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.monitor.app.ui.control.main.ControlMainScreen
import com.monitor.app.ui.sensor.main.SensorMainScreen
import com.monitor.app.ui.control.sensor.ControlSensorScreen

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = "main"
) {
    NavHost(
        modifier = modifier, navController = navController, startDestination = startDestination
    ) {
        composable("main") {
            ControlMainScreen(navController)
        }
        composable("sensorSend/{userID}/{sensorID}") {
            SensorMainScreen(
                navController,
                it.arguments?.getString("userID")!!,
                it.arguments?.getString("sensorID")!!,
            )
        }
        composable("sensorView/{userID}/{sensorID}") {
            ControlSensorScreen(
                navController,
                it.arguments?.getString("userID")!!,
                it.arguments?.getString("sensorID")!!,
            )
        }
    }
}