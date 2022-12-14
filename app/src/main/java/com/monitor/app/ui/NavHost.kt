package com.monitor.app.ui

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.firebase.auth.FirebaseAuth
import com.monitor.app.core.DeviceTypes
import com.monitor.app.core.Screens
import com.monitor.app.ui.autentication.LoginScreen
import com.monitor.app.ui.control.main.ControlMainScreen
import com.monitor.app.ui.control.sensor.ControlSensorScreen
import com.monitor.app.ui.sensor.init.SensorInitScreen
import com.monitor.app.ui.sensor.main.SensorMainScreen
import com.monitor.app.ui.shared.DeviceTypeView
import com.monitor.app.ui.splashscreen.SplashScreen

@Composable
fun AppNavHost(
    activity: Activity,
    auth: FirebaseAuth,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
//    var userId by remember {
//        mutableStateOf(auth.currentUser?.uid)
//    }
    var userId = "user-2"
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
//                    else -> return@SplashScreen
                }
                navController.navigate(navDir) {
                    popUpTo(Screens.SPLASH.name) {
                        inclusive = true
                    }
                    launchSingleTop = true
                }
            }
        }
        composable(Screens.AUTH.name) {
            LoginScreen(auth) { id ->
                userId = id
                navController.navigate(Screens.SPLASH.name) {
                    popUpTo(Screens.AUTH.name) {
                        inclusive = true
                    }
                    launchSingleTop = true
                }
            }
        }
        composable(
            "${Screens.DEVICE_TYPE_VIEW.name}?sensorID={sensorID}",
            arguments = listOf(navArgument("sensorID") {
                nullable = true
                defaultValue = null
                type = NavType.StringType
            })
        ) {
            val sensorId = it.arguments?.getString("sensorID", null)
            DeviceTypeView { deviceType ->
                navController.navigate(
                    when (deviceType) {
                        DeviceTypes.SENSOR -> sensorId?.let { "${Screens.SENSOR_INIT.name}?sensorID=$sensorId" }
                            ?: Screens.SENSOR_INIT.name
                        DeviceTypes.MAIN -> Screens.CONTROL_MAIN.name
                        else -> return@DeviceTypeView
                    }
                )
            }
        }
        composable(
            "${Screens.SENSOR_INIT.name}?sensorID={sensorID}",
            arguments = listOf(navArgument("sensorID") {
                nullable = true
                defaultValue = null
                type = NavType.StringType
            })
        ) {
            val sensorId = it.arguments?.getString("sensorID", null)
            SensorInitScreen(userId!!, sensorId) { id ->
                navController.navigate("${Screens.SENSOR_SEND.name}/$id")
            }
        }
        composable(Screens.CONTROL_MAIN.name) {
            ControlMainScreen(userId = userId!!, onSensorSelected = { id ->
                navController.navigate("${Screens.SENSOR_VIEW.name}/$id")
            }, onChangeDeviceType = {
                navController.navigate(Screens.DEVICE_TYPE_VIEW.name)
            })
        }
        composable("${Screens.SENSOR_SEND.name}/{sensorID}") {
            val sensor = it.arguments?.getString("sensorID") ?: return@composable
            SensorMainScreen(
                userId!!,
                sensor,
                onNavigateBack = { activity.finish() },
                onChangeDeviceType = { id ->
                    navController.navigate("${Screens.DEVICE_TYPE_VIEW.name}?sensorID=$id")
                },
                onChangeInformation = { id ->
                    navController.navigate("${Screens.SENSOR_INIT.name}?sensorID=$id")
                })
        }
        composable("${Screens.SENSOR_VIEW.name}/{sensorID}") {
            val sensor = it.arguments?.getString("sensorID") ?: return@composable
            ControlSensorScreen(userId!!, sensor) {
                navController.navigateUp()
            }
        }
    }
}