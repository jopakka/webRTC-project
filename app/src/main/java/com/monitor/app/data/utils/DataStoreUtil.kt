package com.monitor.app.data.utils

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

class DataStoreUtil(private val context: Context) {

    // to make sure there's only one instance
    companion object {
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("config")
        private val IS_MAIN_DEVICE = booleanPreferencesKey("is_main_device")
        private val SENSOR_ID = stringPreferencesKey("sensor_id")
    }

    //get the saved device type
    val getDeviceType: Flow<Boolean?> = context.dataStore.data
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
            it[IS_MAIN_DEVICE]
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
            it[SENSOR_ID]
        }

    //save sensor id into datastore
    suspend fun saveDeviceType(isMainDevice: Boolean) {
        try {
            context.dataStore.edit { preferences ->
                preferences[IS_MAIN_DEVICE] = isMainDevice
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