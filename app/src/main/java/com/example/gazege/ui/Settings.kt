package com.example.gazege.ui

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// At the top level of your kotlin file:
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

val INCLUIR_PRESUPUESTO_EN_SALDO_ACTUAL_FLOW = booleanPreferencesKey("incluir_presupuesto")
val INCLUIR_DEUDAS_EN_SALDO_ACTUAL_FLOW = booleanPreferencesKey("incluir_deudas")
val CATEGORY_ID_TO_EXPORT = intPreferencesKey("category_id_to_export")
val SHOW_ON_BOARDING = booleanPreferencesKey("show_on_boarding")

data class Settings(
    val context: Context
) {
    fun getIncluirPresupuestoEnSaldoActualFlow(): Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[INCLUIR_PRESUPUESTO_EN_SALDO_ACTUAL_FLOW] ?: false
        }

    fun getIncluirDeudasEnSaldoActualFlow(): Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[INCLUIR_DEUDAS_EN_SALDO_ACTUAL_FLOW] ?: false
        }

    fun getCategoryIdToExportFlow(): Flow<Int> = context.dataStore.data
        .map { preferences ->
            preferences[CATEGORY_ID_TO_EXPORT] ?: -1
        }

    fun getShowOnBoardingFlow(): Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[SHOW_ON_BOARDING] ?: true
        }

    suspend fun setIncluirPresupuestoEnSaldoActualFlow(valor: Boolean) {
        context.dataStore.edit { settings ->
            settings[INCLUIR_PRESUPUESTO_EN_SALDO_ACTUAL_FLOW] = valor
        }
    }

    suspend fun setIncluirDeudasEnSaldoActualFlow(valor: Boolean) {
        context.dataStore.edit { settings ->
            settings[INCLUIR_DEUDAS_EN_SALDO_ACTUAL_FLOW] = valor
        }
    }

    suspend fun setCategoryIdToExportFlow(valor: Int) {
        context.dataStore.edit { settings ->
            settings[CATEGORY_ID_TO_EXPORT] = valor
        }
    }

    suspend fun setShowOnBoarding(valor: Boolean) {
        context.dataStore.edit { settings ->
            settings[SHOW_ON_BOARDING] = valor
        }
    }
}

