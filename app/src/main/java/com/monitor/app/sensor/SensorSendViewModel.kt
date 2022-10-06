package com.monitor.app.sensor

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.compose.runtime.*
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel

class SensorSendViewModel : ViewModel() {
    companion object {
        private const val CAMERA_PERMISSION = Manifest.permission.CAMERA
        private const val AUDIO_PERMISSION = Manifest.permission.RECORD_AUDIO
        private const val TAG = "SensorSendViewModel"
    }

    private val _hasPermissions: MutableState<Boolean> = mutableStateOf(false)
    val hasPermissions: State<Boolean>
        get() = _hasPermissions

    fun checkAndRequestPermissions(
        context: Context,
        permissions: Array<String>,
        launcher: ManagedActivityResultLauncher<Array<String>, Map<String, Boolean>>
    ) {
        if(permissions.all {
                ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
            }) {
            _hasPermissions.value = true
        } else {
            _hasPermissions.value = false
            launcher.launch(permissions)
        }
    }

    fun setHasPermission(value: Boolean) {
        _hasPermissions.value = value
    }
}