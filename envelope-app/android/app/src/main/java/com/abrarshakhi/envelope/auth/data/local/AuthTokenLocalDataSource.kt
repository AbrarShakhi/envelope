package com.abrarshakhi.envelope.auth.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.abrarshakhi.envelope.auth.data.crypto.TokenEncryptionManager
import com.abrarshakhi.envelope.auth.domain.model.AuthTokens
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException

class AuthTokenLocalDataSource(
    private val dataStore: DataStore<Preferences>,
    private val encryptionManager: TokenEncryptionManager,
) {
    fun observeTokens(): Flow<AuthTokens?> = dataStore.data.catch { throwable ->
        if (throwable is IOException) emit(emptyPreferences())
        else throw throwable
    }.map { preferences -> preferences.toAuthTokensOrNull() }

    suspend fun getTokens(): AuthTokens? = observeTokens().first()

    suspend fun saveTokens(tokens: AuthTokens) {
        dataStore.edit { preferences ->
            preferences[Keys.ACCESS_TOKEN] = encryptionManager.encrypt(tokens.accessToken)
            preferences[Keys.REFRESH_TOKEN] = encryptionManager.encrypt(tokens.refreshToken)
            preferences[Keys.ACCESS_TOKEN_EXPIRY] = tokens.accessTokenExpiryEpochMillis
            preferences[Keys.REFRESH_TOKEN_EXPIRY] = tokens.refreshTokenExpiryEpochMillis
        }
    }

    suspend fun clearTokens() {
        dataStore.edit { preferences -> preferences.clear() }
    }

    private fun Preferences.toAuthTokensOrNull(): AuthTokens? {
        val encryptedAccessToken = this[Keys.ACCESS_TOKEN] ?: return null
        val encryptedRefreshToken = this[Keys.REFRESH_TOKEN] ?: return null
        val accessTokenExpiry = this[Keys.ACCESS_TOKEN_EXPIRY] ?: return null
        val refreshTokenExpiry = this[Keys.REFRESH_TOKEN_EXPIRY] ?: return null

        return runCatching {
            AuthTokens(
                accessToken = encryptionManager.decrypt(encryptedAccessToken),
                refreshToken = encryptionManager.decrypt(encryptedRefreshToken),
                accessTokenExpiryEpochMillis = accessTokenExpiry,
                refreshTokenExpiryEpochMillis = refreshTokenExpiry,
            )
        }.getOrNull()
    }

    private object Keys {
        val ACCESS_TOKEN = stringPreferencesKey("encrypted_access_token")
        val REFRESH_TOKEN = stringPreferencesKey("encrypted_refresh_token")
        val ACCESS_TOKEN_EXPIRY = longPreferencesKey("access_token_expiry_epoch_millis")
        val REFRESH_TOKEN_EXPIRY = longPreferencesKey("refresh_token_expiry_epoch_millis")
    }
}

