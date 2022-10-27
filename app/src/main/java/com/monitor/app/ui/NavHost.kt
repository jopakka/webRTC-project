package com.monitor.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.monitor.app.core.Screens
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
) {
    val user = "user-1"
    NavHost(
        modifier = modifier, navController = navController, startDestination = Screens.SPLASH.name
    ) {
        composable(Screens.SPLASH.name) {
            SplashScreen { isMain, sensorId ->
                navController.navigate(
                    if (isMain)
                        Screens.CONTROL_MAIN.name
                    else if (sensorId != null) "${Screens.SENSOR_SEND.name}/$sensorId"
                    else Screens.DEVICE_TYPE_VIEW.name
                )
            }
        }
        composable(Screens.DEVICE_TYPE_VIEW.name) {
            DeviceTypeView { isMain ->
                navController.navigate(
                    if (isMain) Screens.CONTROL_MAIN.name
                    else Screens.SENSOR_INIT.name
                )
            }
        }
        composable(Screens.SENSOR_INIT.name) {
            SensorInitScreen(user) { id ->
                navController.navigate("${Screens.SENSOR_SEND.name}/${id}")
            }
        }
        composable(Screens.CONTROL_MAIN.name) {
            ControlMainScreen(user) { id ->
                navController.navigate("${Screens.SENSOR_VIEW.name}/$id")
            }
        }
        composable("${Screens.SENSOR_SEND.name}/{sensorID}") {
            val sensor = it.arguments?.getString("sensorID") ?: return@composable
            SensorMainScreen(user, sensor) {
                navController.navigateUp()
            }
        }
        composable("${Screens.SENSOR_VIEW.name}/{sensorID}") {
            val sensor = it.arguments?.getString("sensorID") ?: return@composable
            ControlSensorScreen(user, sensor) {
                navController.navigateUp()
            }
        }
    }
}