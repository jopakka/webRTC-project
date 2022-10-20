package com.monitor.app.ui.sensor.init

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.monitor.app.data.model.SensorInfo

class SensorInitViewModel : ViewModel() {
    companion object {
        private const val TAG = "SensorInitViewModel"
    }

    private val firestore = Firebase.firestore

    fun addSensor(name: String, info: String, onComplete: (id: String?, error: Error?) -> Unit) {
        try {
            val sensor = SensorInfo(name, info)
            val user = "user-1"
            firestore.collection(user).add(sensor).addOnSuccessListener {
                Log.d(TAG, "Sensor added successfully: ${it.id}")
                onComplete(it.id, null)
            }
        } catch (e: Error) {
            Log.e(TAG, "${e.message}")
            onComplete(null, e)
        }
    }
}