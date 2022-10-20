package com.monitor.app.ui.splashscreen

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation.NavHostController
import com.monitor.app.R
import com.monitor.app.data.screens.Screen
import com.monitor.app.data.utils.DataStoreUtil

@Composable
fun SplashScreen(navController: NavHostController) {
    val context = LocalContext.current
    val dataStore = DataStoreUtil(context)
    val savedDeviceTypeIsMain by dataStore.getDeviceType.collectAsState(initial = null)

    /**
     * Inside let, navigate to corresponding view
     * based on value of savedDeviceType
     */
    LaunchedEffect(key1 = savedDeviceTypeIsMain) {
        Log.d("SplashScreen", "savedDeviceTypeIsMain=$savedDeviceTypeIsMain")
        navController.navigate(
            if (savedDeviceTypeIsMain == true) Screen.ControlMain.route
            else Screen.TypeSelect.route
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            stringResource(R.string.app_name),
            style = MaterialTheme.typography.h2,
            textAlign = TextAlign.Center
        )
        CircularProgressIndicator()
    }
}