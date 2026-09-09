package com.abrarshakhi.envelope.auth.domain.repository

import com.abrarshakhi.envelope.auth.domain.model.AuthState
import com.abrarshakhi.envelope.auth.domain.model.AuthTokens
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun observeAuthState(): Flow<AuthState>
    suspend fun getAuthState(): AuthState
    suspend fun saveSession(tokens: AuthTokens)
    suspend fun clearSession()
}
