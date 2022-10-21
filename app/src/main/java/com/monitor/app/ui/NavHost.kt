package com.monitor.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.monitor.app.ui.control.main.ControlMainScreen
import com.monitor.app.ui.control.sensor.ControlSensorScreen
import com.monitor.app.ui.sensor.init.SensorInitScreen
import com.monitor.app.ui.sensor.main.SensorMainScreen
import com.monitor.app.ui.shared.DeviceTypeView
import com.monitor.app.ui.splashscreen.SplashScreen

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = "splashScreen"
) {
    val user = "user-1"
    NavHost(
        modifier = modifier, navController = navController, startDestination = startDestination
    ) {
        composable("splashScreen") {
            SplashScreen { isMain ->
                navController.navigate(
                    if (isMain) "controlMain"
                    else "deviceTypeView"
                )
            }
        }
        composable("deviceTypeView") {
            DeviceTypeView { isMain ->
                navController.navigate(if (isMain) "controlMain" else "sensorInit")
            }
        }
        composable("sensorInit") {
            SensorInitScreen(user) { id ->
                navController.navigate("sensorSend/${id}")
            }
        }
        composable("controlMain") {
            ControlMainScreen(user) { id ->
                navController.navigate("sensorView/$id")
            }
        }
        composable("sensorSend/{sensorID}") {
            val sensor = it.arguments?.getString("sensorID") ?: return@composable
            SensorMainScreen(user, sensor) {
                navController.navigateUp()
            }
        }
        composable("sensorView/{sensorID}") {
            val sensor = it.arguments?.getString("sensorID") ?: return@composable
            ControlSensorScreen(user, sensor) {
                navController.navigateUp()
            }
        }
    }
}