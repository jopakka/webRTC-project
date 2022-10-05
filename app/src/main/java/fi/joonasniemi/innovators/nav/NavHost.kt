package fi.joonasniemi.innovators.nav

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import fi.joonasniemi.innovators.RTCClient
import fi.joonasniemi.innovators.main.ui.SensorsScreen
import fi.joonasniemi.innovators.sensor.ui.SensorSendScreen

@Composable
fun AppNavHost(
    rtcClient: RTCClient,
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
            SensorSendScreen(rtcClient)
        }
    }
}