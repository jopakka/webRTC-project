package com.monitor.app.ui

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.monitor.app.core.DeviceTypes
import com.monitor.app.core.Screens
import com.monitor.app.ui.control.main.ControlMainScreen
import com.monitor.app.ui.control.sensor.ControlSensorScreen
import com.monitor.app.ui.sensor.init.SensorInitScreen
import com.monitor.app.ui.sensor.main.SensorMainScreen
import com.monitor.app.ui.shared.DeviceTypeView
import com.monitor.app.ui.splashscreen.SplashScreen

@Composable
fun AppNavHost(
    activity: Activity,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    val TAG = "AppNavHost"
    val user = "user-1"
    NavHost(
        modifier = modifier, navController = navController, startDestination = Screens.SPLASH.name
    ) {
        composable(Screens.SPLASH.name) {
            SplashScreen { deviceType, sensorId ->
                val navDir = when (deviceType) {
                    DeviceTypes.NONE -> Screens.DEVICE_TYPE_VIEW.name
                    DeviceTypes.MAIN -> Screens.CONTROL_MAIN.name
                    DeviceTypes.SENSOR -> {
                        if (sensorId.isNotBlank()) {
                            "${Screens.SENSOR_SEND.name}/$sensorId"
                        } else {
                            Screens.DEVICE_TYPE_VIEW.name
                        }
                    }
                }
                navController.navigate(navDir) {
                    popUpTo(Screens.SPLASH.name) {
                        inclusive = true
                    }
                    launchSingleTop = true
                }
            }
        }
        composable(Screens.DEVICE_TYPE_VIEW.name) {
            DeviceTypeView { deviceType ->
                navController.navigate(
                    when (deviceType) {
                        DeviceTypes.SENSOR -> Screens.SENSOR_INIT.name
                        DeviceTypes.MAIN -> Screens.CONTROL_MAIN.name
                        else -> return@DeviceTypeView
                    }
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
                activity.finish()
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