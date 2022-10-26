package com.monitor.app.ui.control.main

import android.util.Log
import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.monitor.app.data.model.SensorInfo
import java.util.*

class ControlMainViewModel(private val userId: String) : ViewModel() {
    companion object {
        private const val TAG = "TestViewModel"
    }

    private val firestore = Firebase.firestore
    private val _sensors = mutableStateMapOf<String, SensorInfo>()
    val sensors: List<SensorInfo>
        get() = _sensors.values.toList()

    init {
        getFirebaseSensors()
    }

    private fun getFirebaseSensors() {
        try {
            firestore.collection(userId).addSnapshotListener { querySnapshot, e ->
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
                            snapshot.getTimestamp("createdAt")?.toDate(),
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
}

class ControlMainViewModelFactory(private val userId: String) :
    ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ControlMainViewModel(userId) as T
    }
}