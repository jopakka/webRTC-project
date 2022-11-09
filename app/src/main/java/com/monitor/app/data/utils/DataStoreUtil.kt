package com.monitor.app.data.utils

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.monitor.app.core.DeviceTypes
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

class DataStoreUtil(private val context: Context) {

    // to make sure there's only one instance
    companion object {
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("config")
        private val DEVICE_TYPE = stringPreferencesKey("device_type")
        private val SENSOR_ID = stringPreferencesKey("sensor_id")
    }

    //get the saved device type
    val getDeviceType: Flow<DeviceTypes> = context.dataStore.data
        .catch { exception ->
            // dataStore.data throws an IOException when an error is encountered when reading data
            if (exception is IOException) {
                emit(emptyPreferences())
                Log.e("DataStoreUtil", "Could not read data", exception)
            } else {
                throw exception
            }
        }
        .map {
            val json = it[DEVICE_TYPE]
            when (val deviceType = Gson().fromJson(json, DeviceTypes::class.java)) {
                null -> DeviceTypes.NONE
                else -> deviceType
            }
        }

    //get the saved sensor id
    val getSensorId: Flow<String?> = context.dataStore.data
        .catch { exception ->
            // dataStore.data throws an IOException when an error is encountered when reading data
            if (exception is IOException) {
                emit(emptyPreferences())
                Log.e("DataStoreUtil", "Could not read data", exception)
            } else {
                throw exception
            }
        }
        .map {
            when (val id = it[SENSOR_ID]) {
                null -> ""
                else -> id
            }
        }

    //save sensor id into datastore
    suspend fun saveDeviceType(deviceType: DeviceTypes) {
        try {
            context.dataStore.edit { preferences ->
                val json = Gson().toJson(deviceType)
                preferences[DEVICE_TYPE] = json
            }
        } catch (e: IOException) {
            Log.e("DataStoreUtil", "Could not write data", e)
        }
    }

    //save device type into datastore
    suspend fun saveDeviceId(id: String) {
        try {
            context.dataStore.edit { preferences ->
                preferences[SENSOR_ID] = id
            }
        } catch (e: IOException) {
            Log.e("DataStoreUtil", "Could not write data", e)
        }
    }
}