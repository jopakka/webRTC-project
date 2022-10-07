package com.monitor.app.main

import android.util.Log
import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.ViewModel
import androidx.navigation.NavHostController
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.monitor.app.classes.SensorInfo

class MainViewModel : ViewModel() {
    private val TAG = "TestViewModel"
    private val firestore = Firebase.firestore
    private val _sensors = mutableStateMapOf<String, SensorInfo>()
    val sensors: List<SensorInfo>
        get() = _sensors.values.toList()

    init {
        getFirebaseSensors()
    }

    private fun getFirebaseSensors() {
        try {
            firestore.collection("user-1").addSnapshotListener { querySnapshot, e ->
                if (e != null) {
                    Log.w(TAG, "listen:error", e)
                    return@addSnapshotListener
                }

                _sensors.clear()

                if (querySnapshot == null || querySnapshot.isEmpty) return@addSnapshotListener

                for (snapshot in querySnapshot) {
                    val data = snapshot.data
                    val key = snapshot.id
                    try {
                        val info = SensorInfo(
                            data["name"].toString(),
                            data["info"].toString(),
                            data["createdAt"]?.toString()?.toLong() ?: 0,
                            key
                        )
                        _sensors[key] = info
                    } catch (e: Error) {
                        Log.w(TAG, "${e.message}")
                    }
                }
            }
        } catch (e: Error) {
            Log.e(TAG, "${e.message}")
        }
    }

    fun addSensor(name: String, info: String, navigator: NavHostController) {
        try {
            val sensor = SensorInfo(name, info)
            val user = "user-1"
            firestore.collection(user).add(sensor).addOnSuccessListener {
                Log.d(TAG, "Sensor added successfully: ${it.id}")
                navigator.navigate("sensorSend/$user/${it.id}")
            }
        } catch (e: Error) {
            Log.e(TAG, "${e.message}")
        }
    }
}