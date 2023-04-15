package com.example.gazege.ui

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// At the top level of your kotlin file:
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

val INCLUIRPRESUPUESTOENSALDOACTUALFLOW = booleanPreferencesKey("example_counter")

data class Settings(
    val context: Context
) {
    fun getIncluirPresupuestoEnSaldoActualFlow(): Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[INCLUIRPRESUPUESTOENSALDOACTUALFLOW] ?: false
        }

    suspend fun setIncluirPresupuestoEnSaldoActualFlow(valor: Boolean) {
        context.dataStore.edit { settings ->
            settings[INCLUIRPRESUPUESTOENSALDOACTUALFLOW] = valor
        }
    }
}

