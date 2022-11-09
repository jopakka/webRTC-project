package com.monitor.app.ui.sensor.init

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.monitor.app.data.rtcclient.model.SensorInfo

class SensorInitViewModel(private val userId: String) : ViewModel() {
    companion object {
        private const val TAG = "SensorInitViewModel"
    }

    private val firestore = Firebase.firestore

    fun addSensor(name: String, info: String, onComplete: (id: String?, error: Error?) -> Unit) {
        try {
            val sensor = SensorInfo(name, info)
            firestore.collection(userId).add(sensor).addOnSuccessListener {
                val id = it.id
                Log.d(TAG, "Sensor added successfully: $id")
                firestore.collection(userId).document(it.id).update(mapOf("id" to it.id))
                    .addOnSuccessListener {
                        onComplete(id, null)
                    }
            }
        } catch (e: Error) {
            Log.e(TAG, "${e.message}")
            onComplete(null, e)
        }
    }
}

class SensorInitViewModelFactory(private val userId: String) :
    ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SensorInitViewModel(userId) as T
    }
}