package com.abrarshakhi.envelope.auth.data.repository

import com.abrarshakhi.envelope.auth.data.local.AuthTokenLocalDataSource
import com.abrarshakhi.envelope.auth.domain.model.AuthState
import com.abrarshakhi.envelope.auth.domain.model.AuthTokens
import com.abrarshakhi.envelope.auth.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AuthRepositoryImpl(
    private val localDataSource: AuthTokenLocalDataSource,
    private val clock: () -> Long = System::currentTimeMillis,
) : AuthRepository {

    override fun observeAuthState(): Flow<AuthState> =
        localDataSource.observeTokens().map { tokens -> tokens.toAuthState() }

    override suspend fun getAuthState(): AuthState = localDataSource.getTokens().toAuthState()

    override suspend fun saveSession(tokens: AuthTokens) {
        localDataSource.saveTokens(tokens)
    }

    override suspend fun clearSession() {
        localDataSource.clearTokens()
    }

    private fun AuthTokens?.toAuthState(): AuthState {
        val tokens = this ?: return AuthState.Unauthenticated
        val now = clock()

        return when {
            now >= tokens.refreshTokenExpiryEpochMillis -> AuthState.Unauthenticated
            now >= tokens.accessTokenExpiryEpochMillis -> AuthState.RequiresRefresh(tokens)
            else -> AuthState.Authenticated(tokens)
        }
    }
}

