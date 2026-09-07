package com.abrarshakhi.envelope.auth.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AuthLocalDataSource(
    private val dataStore: DataStore<Preferences>
) {
    private object PreferenceKeys {
        val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
    }

    val isLoggedIn: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[PreferenceKeys.IS_LOGGED_IN] ?: false
    }

    suspend fun setLoggedIn(isLoggedIn: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferenceKeys.IS_LOGGED_IN] = isLoggedIn
        }
    }
}