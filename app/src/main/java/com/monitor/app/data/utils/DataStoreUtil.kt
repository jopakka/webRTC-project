package com.monitor.app.data.utils

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

class DataStoreUtil(private val context: Context) {

    // to make sure there's only one instance
    companion object {
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("deviceType")
        val IS_MAIN_DEVICE = booleanPreferencesKey("is_main_device")
    }

    //get the saved email
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

    //save email into datastore
    suspend fun saveDeviceType(isMainDevice: Boolean) {
        try {
            context.dataStore.edit { preferences ->
                preferences[IS_MAIN_DEVICE] = isMainDevice
            }
        } catch (e: IOException) {
            Log.e("DataStoreUtil", "Could not write data", e)
        }
    }
}