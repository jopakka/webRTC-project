package com.monitor.app.ui.control.main

import android.util.Log
import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.monitor.app.core.SensorStatuses
import com.monitor.app.data.rtcclient.model.SensorInfo
import java.util.*

class ControlMainViewModel(private val userId: String) : ViewModel() {
    companion object {
        private const val TAG = "ControlMainViewModel"
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

                if (querySnapshot == null || querySnapshot.isEmpty) return@addSnapshotListener

                querySnapshot.documentChanges.forEach {
                    val id = it.document.id
                    Log.d(TAG, "id: $id changed")
                    when (it.type) {
                        DocumentChange.Type.REMOVED -> {
                            _sensors.remove(id)
                        }
                        else -> {
                            val sensor = createSensorInfo(it.document) ?: return@forEach
                            if (!sensor.visible) {
                                _sensors.remove(id)
                                return@forEach
                            }
                            _sensors[id] = sensor
                        }
                    }
                }
            }
        } catch (e: Error) {
            Log.e(TAG, "${e.message}")
        }
    }

    private fun createSensorInfo(snapshot: QueryDocumentSnapshot): SensorInfo? {
        val data = snapshot.data
        val key = snapshot.id
        try {
            val visible = try {
                data["visible"] as Boolean
            } catch (e: Exception) {
                true
            }
            val status = when (data["status"].toString()) {
                SensorStatuses.OFFLINE.name -> SensorStatuses.OFFLINE
                SensorStatuses.ONLINE.name -> SensorStatuses.ONLINE
                else -> SensorStatuses.OFFLINE
            }
            val info = SensorInfo(
                data["name"].toString(),
                data["info"].toString(),
                snapshot.getTimestamp("createdAt")?.toDate(),
                key,
                data["battery"].toString().toIntOrNull(),
                status,
                status == SensorStatuses.ONLINE && data["type"].toString() == "OFFER",
                visible,
            )
            Log.d(TAG, "sensor=$info")
            return info
        } catch (e: Error) {
            Log.w(TAG, "${e.message}")
            return null
        }
    }

    fun setItemVisibility(itemId: String, state: Boolean) {
        try {
            val data = mapOf("visible" to state)
            firestore.collection(userId).document(itemId).update(data).addOnSuccessListener {
                Log.d(TAG, "Item '${itemId} updated successfully'")
            }
        } catch (e: Exception) {
            Log.w(TAG, "${e.message}")
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