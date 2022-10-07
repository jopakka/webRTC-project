package com.monitor.app.nav

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.monitor.app.main.ui.SensorsScreen
import com.monitor.app.sensorsend.ui.SensorSendScreen
import com.monitor.app.sensorview.ui.SensorViewScreen

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
            SensorsScreen(navController)
        }
        composable("sensorSend/{userID}/{sensorID}") {
            SensorSendScreen(
                it.arguments?.getString("userID")!!,
                it.arguments?.getString("sensorID")!!,
            )
        }
        composable("sensorView/{userID}/{sensorID}") {
            SensorViewScreen(
                it.arguments?.getString("userID")!!,
                it.arguments?.getString("sensorID")!!,
            )
        }
    }
}